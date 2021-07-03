package fun.lula.flomo.controller;

import com.qiniu.storage.Api;
import fun.lula.flomo.service.WechatService;
import fun.lula.flomo.util.result.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/wechat")
public class WechatController {

    @Resource
    WechatService wechatService;


    @RequestMapping("/callback")
    public String callback(HttpServletRequest request){
        String signature = request.getParameter("signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");

        String authResult = wechatService.authWechatServer(signature, timestamp, nonce);
        if (StringUtils.isNotEmpty(authResult)) {
            return "error";
        }
        String echostr = request.getParameter("echostr");
        if (StringUtils.isNotEmpty(echostr)) {
            return echostr;
        }

        String encryptType = request.getParameter("encrypt_type");
        String msgSignature = request.getParameter("msg_signature");
        log.info("encryptType: " + encryptType + ", msgSignature: " + msgSignature);

        String message = null;
        try {
            message = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "success";
        }
        log.info(message);

        String result;
        if (StringUtils.equals("aes", encryptType)) {
            log.info("aes加密");
            result = wechatService.callbackEvent(message, nonce, timestamp, msgSignature);
        } else {
            log.info("未加密");
            result = wechatService.callbackEvent(message);
        }
        return result;
    }

}
