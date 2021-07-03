package fun.lula.flomo.service;

import fun.lula.flomo.model.dto.request.*;
import fun.lula.flomo.model.dto.response.ImageVerifyCodeDto;
import fun.lula.flomo.model.dto.response.UserInfo;
import fun.lula.flomo.model.entity.User;

public interface UserService {

    void register(RegisterDto registerDto);

    User login(LoginDto loginDto);

    ImageVerifyCodeDto generateImageAndTokenId();

    String imageValidateUser(UserValidateImageCodeDto userValidateImageCodeDto);

    void smsValidateUser(UserValidateSMSCodeDto userValidateSMSCodeDto);

    void forgetPasswordSetNewPassword(UserSetNewPassword userSetNewPassword);

    void updateUserInfo(UpdateUserInfoDto dto);

    UserInfo userInfo();

}
