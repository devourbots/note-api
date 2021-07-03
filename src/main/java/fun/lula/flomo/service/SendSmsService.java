package fun.lula.flomo.service;

import fun.lula.flomo.model.dto.request.SmsVerifyCodeDto;

public interface SendSmsService {
    public void sendVerifyCode(SmsVerifyCodeDto smsVerifyCodeDto);
}
