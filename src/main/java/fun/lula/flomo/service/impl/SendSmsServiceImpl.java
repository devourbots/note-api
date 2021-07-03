package fun.lula.flomo.service.impl;

import fun.lula.flomo.exception.ServiceException;
import fun.lula.flomo.model.dto.response.SendSmsDto;
import fun.lula.flomo.model.dto.request.SmsVerifyCodeDto;
import fun.lula.flomo.service.SendSmsService;
import fun.lula.flomo.util.SysConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SendSmsServiceImpl implements SendSmsService {


    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public void sendVerifyCode(SmsVerifyCodeDto smsVerifyCodeDto) {
        RBucket<String> sendVerifyCodeLock =
                redissonClient.getBucket(SysConst.RedisPrefix.SMS_SEND_LOCK + smsVerifyCodeDto.getPhone());
        RBucket<String> verifyCode =
                redissonClient.getBucket(SysConst.RedisPrefix.SMS_VERIFY_CODE + smsVerifyCodeDto.getPhone());
        if (sendVerifyCodeLock.isExists()) {
            log.info("{} 频繁发送验证，已被拒绝", smsVerifyCodeDto.getPhone());
            throw new ServiceException("60s内最多发送一次验证码，请稍后重试！");
        }
        String randomStr = RandomStringUtils.randomNumeric(6);

        // 判断短信发送场景
        if (smsVerifyCodeDto.getSceneCode().equals(SmsVerifyCodeDto.SCENE_CODE_REGISTER)) {
            sendRegisterCode(randomStr, smsVerifyCodeDto.getPhone());
        } else if (smsVerifyCodeDto.getSceneCode().equals(SmsVerifyCodeDto.SCENE_CODE_FORGET_PASSWORD)) {
            sendForgetPasswordCode(randomStr, smsVerifyCodeDto.getPhone());
        }

        // 60s内无法重复发送验证码
        sendVerifyCodeLock.set(randomStr, 60, TimeUnit.SECONDS);
        // 验证码十分钟内有效
        verifyCode.set(randomStr, 10, TimeUnit.MINUTES);
    }

    private void sendForgetPasswordCode(String verifyCode, String token) {
        RBucket<String> bindTokenPhone =
                redissonClient.getBucket(SysConst.RedisPrefix.FORGET_PASSWORD_BIND_PHONE_TOKEN + token);
        if (!bindTokenPhone.isExists()) {
            throw new ServiceException("回话失效，请返回上一步重试！");
        }

        String phone = bindTokenPhone.get();
        String message = "【噜啦】您的找回密码验证码是：" + verifyCode + "，10分钟内有效。";
        log.info("向{}发送找回密码验证码：{}", phone, verifyCode);
        rabbitTemplate.convertAndSend("sms_queue", new SendSmsDto(phone, message));
    }


    private void sendRegisterCode(String verifyCode, String phone) {
        String message = "【噜啦】您的注册验证码是：" + verifyCode + "，10分钟内有效。";
        log.info("向{}发送注册验证码：{}", phone, verifyCode);
        rabbitTemplate.convertAndSend("sms_queue", new SendSmsDto(phone, message));
    }

}
