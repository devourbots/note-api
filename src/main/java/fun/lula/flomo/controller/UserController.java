package fun.lula.flomo.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qiniu.storage.Api;
import fun.lula.flomo.model.dto.request.*;
import fun.lula.flomo.model.dto.response.UserInfo;
import fun.lula.flomo.model.entity.User;
import fun.lula.flomo.service.UserService;
import fun.lula.flomo.service.WechatService;
import fun.lula.flomo.util.result.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Slf4j
public class UserController {

    @Resource
    UserService userService;

    @Resource
    WechatService wechatService;

    @PostMapping("/register")
    public ApiResult userRegister(@RequestBody RegisterDto registerDto) {
        userService.register(registerDto);
        return ApiResult.SUCCESS();
    }

    @PostMapping("/login")
    public ApiResult userLogin(@RequestBody LoginDto dto) {
        User user = userService.login(dto);
        StpUtil.setLoginId(user.getId(), dto.getDevice());
        return ApiResult.SUCCESS(StpUtil.getTokenInfo());
    }

    @PostMapping("/logout")
    public ApiResult userLogout() {
        log.info("{} 用户退出登录，Device：{}", StpUtil.getLoginId(), StpUtil.getLoginDevice());
        StpUtil.logout();
        return ApiResult.SUCCESS();
    }

    @PostMapping("/forget/validate_user")
    public ApiResult imageCodeValidateUser(@RequestBody UserValidateImageCodeDto dto) {
        String token = userService.imageValidateUser(dto);
        return ApiResult.SUCCESS(token);
    }

    @PostMapping("/forget/validate_verify_code")
    public ApiResult validateUserSMSCode(@RequestBody UserValidateSMSCodeDto dto) {
        userService.smsValidateUser(dto);
        return ApiResult.SUCCESS();
    }

    @PostMapping("/forget/new_password")
    public ApiResult setNewPassword(@RequestBody UserSetNewPassword dto) {
        userService.forgetPasswordSetNewPassword(dto);
        return ApiResult.SUCCESS();
    }

    @PostMapping("/user/setting")
    public ApiResult setUserInfo(@RequestBody UpdateUserInfoDto dto) {
        userService.updateUserInfo(dto);
        return ApiResult.SUCCESS();
    }

    @PostMapping("/user/info")
    public ApiResult getUserInfo() {
        UserInfo userInfo = userService.userInfo();
        return ApiResult.SUCCESS(userInfo);
    }

    @PostMapping("/user/wx/qrcode")
    public ApiResult getWXQRCode() {
        String userId = StpUtil.getLoginIdAsString();
        String qrCodeURL = wechatService.getQRCodeURL(userId);
        return ApiResult.SUCCESS(qrCodeURL);
    }

}
