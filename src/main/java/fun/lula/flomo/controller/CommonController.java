package fun.lula.flomo.controller;

import fun.lula.flomo.model.dto.response.ImageVerifyCodeDto;
import fun.lula.flomo.model.dto.request.SmsVerifyCodeDto;
import fun.lula.flomo.service.SendSmsService;
import fun.lula.flomo.service.UserService;
import fun.lula.flomo.util.result.ApiResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/commons")
public class CommonController {
    @Resource
    SendSmsService sendSmsService;

    @Resource
    UserService userService;


    @PostMapping("/verify_code")
    public ApiResult sendSmsVerifyCode(@RequestBody SmsVerifyCodeDto smsVerifyCodeDto) {
        sendSmsService.sendVerifyCode(smsVerifyCodeDto);
        return ApiResult.SUCCESS();
    }

    @PostMapping("/verify_code_image")
    public ApiResult generateVerifyCodeImage() {
        ImageVerifyCodeDto imageVerifyCodeDto = userService.generateImageAndTokenId();
        return ApiResult.SUCCESS(imageVerifyCodeDto);
    }

}
