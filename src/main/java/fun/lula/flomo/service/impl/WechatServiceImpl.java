package fun.lula.flomo.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import fun.lula.flomo.config.WechatConfig;
import fun.lula.flomo.exception.ServiceException;
import fun.lula.flomo.mapper.MemoMapper;
import fun.lula.flomo.mapper.UserMapper;
import fun.lula.flomo.model.entity.Memo;
import fun.lula.flomo.model.entity.User;
import fun.lula.flomo.service.WechatService;
import fun.lula.flomo.util.DateTimeUtil;
import fun.lula.flomo.util.HttpUtil;
import fun.lula.flomo.util.SysConst;
import fun.lula.flomo.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class WechatServiceImpl implements WechatService {
    @Resource
    WechatConfig wechatConfig;

    @Resource
    RedissonClient redissonClient;

    @Resource
    UserMapper userMapper;

    @Resource
    MemoMapper memoMapper;


    @Override
    public String authWechatServer(String signature, String timestamp, String nonce) {
        log.info("signature: {}, timestamp: {}, nonce: {}", signature, timestamp, nonce);

        String token = wechatConfig.getToken();

        if (StringUtils.isAnyEmpty(signature, timestamp, nonce)) {
            log.error("参数不足");
            return "error";
        }

        List<String> list = Arrays.asList(token, timestamp, nonce);
        Collections.sort(list);

        StringBuilder builder = new StringBuilder();
        for (String s : list) {
            builder.append(s);
        }

        String signature1 =
                DigestUtils.sha1Hex(builder.toString().getBytes(StandardCharsets.UTF_8));

        if (!StringUtils.equals(signature1, signature)) {
            log.error("签名错误，认证错误！");
            return "error";
        } else {
            log.info("微信服务器签名验证成功");
            return "";
        }
    }

    @Override
    public String getAccessToken() {
        RBucket<String> bucket =
                redissonClient.getBucket(SysConst.RedisPrefix.WECHAT_ACCESS_TOKEN);

        if (!bucket.isExists()) {
            String URL = "https://api.weixin.qq" +
                    ".com/cgi-bin/token?grant_type=client_credential&appid=" +
                    wechatConfig.getAppId() + "&secret=" + wechatConfig.getAppSecret();
            String s = HttpUtil.sendGetRequest(URL);
            try {
//                log.info("actoken 接口返回数据" + s);
                HashMap<String, Object> returnMap = JSON.parseObject(s, HashMap.class);
                if (returnMap.get("errcode") != null) {
                    throw new ServiceException("access token 获取失败");
                } else {
                    if (returnMap.containsKey("access_token")) {
                        String token = returnMap.get("access_token").toString();
                        bucket.set(token, Long.parseLong(returnMap.get("expires_in").toString()),
                                TimeUnit.SECONDS);
                        log.info("获取到 actoken" + token);
                        return token;
                    } else {
                        throw new ServiceException("access token 获取失败");
                    }
                }
            } catch (Exception exception) {
                throw new ServiceException(exception.getMessage());
            }
        } else {
//            log.info("获取到 actoken" + bucket.get());
            return bucket.get();
        }
    }

    @Override
    public String getQRCodeURL(String userId) {
        String URL =
                "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + getAccessToken();

        Map<String, Object> map = new HashMap<>();
        map.put("expire_seconds", "600");
        map.put("action_name", "QR_STR_SCENE");
        Map<String, Object> action_info = new HashMap<>();
        Map<String, Object> scene = new HashMap<>();
        scene.put("scene_str", userId);
        action_info.put("scene", scene);
        map.put("action_info", action_info);
        String requestBody = JSON.toJSONString(map);

        String s = HttpUtil.sendPostRequestWithJson(URL, requestBody);
//        log.info("ticket 接口返回数据" + s);
        try {
            Map<String, Object> resultMap = JSON.parseObject(s, HashMap.class);
            if (resultMap.containsKey("errcode")) {
                log.error("ticket 获取失败");
                log.info(resultMap.toString());
                throw new ServiceException("微信场景二维码异常" + resultMap.get("errmsg"));
            } else {
                String ticket = resultMap.get("ticket").toString();
                return "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + URLEncoder.encode(ticket, "UTF-8");
            }
        } catch (Exception e) {
            throw new ServiceException("生成 json 异常");
        }
    }

    @Override
    public String callbackEvent(String message, String nonce, String timestamp, String msgSignature) {
        return null;
    }

    @Override
    public String callbackEvent(String message) {
        Map<String, String> messageMap = XmlUtil.parseXmlToMap(message);

        if (StringUtils.equalsIgnoreCase(messageMap.get("MsgType"), "event") && StringUtils.equalsAnyIgnoreCase(messageMap.get("Event"), "subscribe", "SCAN")) {
            // 为扫描二维码事件
            wxBindUser(messageMap.get("FromUserName"), messageMap.get("EventKey"));
            return sendTextMessageToUser(messageMap.get("FromUserName"), "绑定成功");
        } else if (StringUtils.equalsIgnoreCase(messageMap.get("MsgType"), "text") && StringUtils.isNotEmpty(messageMap.get("Content"))) {
            // 用户创建 memo
            wxCreateMemo(messageMap.get("FromUserName"), messageMap.get("Content"));
            return sendTextMessageToUser(messageMap.get("FromUserName"), "笔记添加成功");
        } else {
            return "";
        }
    }

    private void wxCreateMemo(String fromUserName, String content) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("wx_open_id", fromUserName));
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        Memo memo = new Memo();
        memo.setDevice("微信客户端");
        memo.setContent(content);
        memo.setUserId(user.getId());
        memo.setCreateTime(DateTimeUtil.getNowString());
        memoMapper.insert(memo);
    }

    private String sendTextMessageToUser(String s, String message) {
        return "<xml>\n" +
                "  <ToUserName><![CDATA[" + s + "]]></ToUserName>\n" +
                "  <FromUserName><![CDATA[" + wechatConfig.getAppName() + "]]></FromUserName>\n" +
                "  <CreateTime>" + new Date().getTime() + "</CreateTime>\n" +
                "  <MsgType><![CDATA[text]]></MsgType>\n" +
                "  <Content><![CDATA[" + message + "]]></Content>\n" +
                "</xml>";
    }

    private void wxBindUser(String fromUserName, String eventKey) {
        if (StringUtils.startsWith(eventKey, "qrscene_")) {
            String userId = StringUtils.substringAfter(eventKey, "qrscene_");
            User user = userMapper.selectById(userId);

            if (user == null) {
                throw new ServiceException("用户不存在，微信绑定失败：" + userId);
            }

            user.setWxOpenId(fromUserName);
            userMapper.updateById(user);
            log.info("微信绑定成功，openId: " + fromUserName + " , userId: " + userId);
        }
    }
}
