package fun.lula.flomo.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import fun.lula.flomo.exception.ServiceException;
import fun.lula.flomo.mapper.UserMapper;
import fun.lula.flomo.model.dto.request.*;
import fun.lula.flomo.model.dto.response.ImageVerifyCodeDto;
import fun.lula.flomo.model.dto.response.UserInfo;
import fun.lula.flomo.model.entity.User;
import fun.lula.flomo.service.UserService;
import fun.lula.flomo.util.DateTimeUtil;
import fun.lula.flomo.util.SysConst;
import fun.lula.flomo.util.ValidateCodeUtil;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    UserMapper userMapper;

    @Resource
    RedissonClient redissonClient;


    @Override
    public void register(RegisterDto registerDto) {
        RBucket<String> verifyCode =
                redissonClient.getBucket(SysConst.RedisPrefix.SMS_VERIFY_CODE + registerDto.getPhone());

        if (!StringUtil.equals(verifyCode.get(), registerDto.getVerifyCode())) {
            throw new ServiceException("验证码错误，请确认后重试");
        } else {
            verifyCode.delete();
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("phone", registerDto.getPhone()));
        if (user != null) {
            throw new ServiceException("该手机号已经注册！");
        }

        User user1 = new User();
        user1.setCreateTime(DateTimeUtil.getNowString());
        user1.setPhone(registerDto.getPhone());
        user1.setPassword(DigestUtils.md5Hex(SysConst.USER_PASSWORD_SALT + registerDto.getPassword()));
        user1.setStatus(User.STATUS_NORMAL);
        user1.setNickName(registerDto.getPhone());

        userMapper.insert(user1);
    }

    @Override
    public User login(LoginDto loginDto) {
        RBucket<Integer> loginFailedTimes =
                redissonClient.getBucket(SysConst.RedisPrefix.LOGIN_FAILED_TIMES + loginDto.getPhone());
        RBucket<Object> loginFailedLock =
                redissonClient.getBucket(SysConst.RedisPrefix.LOGIN_FAILED_LOCK + loginDto.getPhone());
        if (loginFailedLock.isExists()) {
            throw new ServiceException("登录失败次数超过3次，账户锁定20分钟，剩余锁定时间" + loginFailedLock.remainTimeToLive() / 1000 + "秒");
        }
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("phone", loginDto.getPhone()));
        if (user == null || (!StringUtil.equals(DigestUtils.md5Hex(SysConst.USER_PASSWORD_SALT + loginDto.getPassword()),
                user.getPassword()))) {
            if (!loginFailedTimes.isExists()) {
                loginFailedTimes.set(1);
            } else {
                loginFailedTimes.set(loginFailedTimes.get() + 1);
            }
            if (loginFailedTimes.get() >= 3) {
                loginFailedLock.set("", 20, TimeUnit.MINUTES);
            }
            throw new ServiceException("账号密码不匹配");
        }
        // 登录成功，清空失败尝试次数
        if (loginFailedTimes.isExists()) {
            loginFailedTimes.delete();
        }
        return user;
    }

    @Override
    public ImageVerifyCodeDto generateImageAndTokenId() {
        String token = UUID.randomUUID().toString().replace("-", "");
        ValidateCodeUtil.Validate imageCodeObj = ValidateCodeUtil.getRandomCode();
        String validateText = imageCodeObj.getValue().toLowerCase(Locale.ROOT);
        log.info("图片验证码：{}", validateText);
        String base64Text = "data:image/jpg;base64," + imageCodeObj.getBase64Str();
        RBucket<String> imageCode = redissonClient.getBucket(SysConst.RedisPrefix.IMAGE_VERIFY_CODE + token);
        imageCode.set(validateText, 10, TimeUnit.MINUTES);
        return new ImageVerifyCodeDto(token, base64Text);
    }

    @Override
    public String imageValidateUser(UserValidateImageCodeDto userValidateImageCodeDto) {
        RBucket<String> bucket =
                redissonClient.getBucket(SysConst.RedisPrefix.IMAGE_VERIFY_CODE + userValidateImageCodeDto.getTokenId());
        if (!bucket.isExists()) {
            throw new ServiceException("验证码已失效，请刷新页面后重试！");
        }

        if (!StringUtil.equals(bucket.get(), userValidateImageCodeDto.getVerifyCode().toLowerCase(Locale.ROOT))) {
            throw new ServiceException("验证码错误，请刷新页面后重试！");
        } else {
            bucket.delete();
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("phone", userValidateImageCodeDto.getPhone()));
        if (user == null) {
            throw new ServiceException("账号不存在");
        }

        // 用户校验通过，设置第二步token，和手机唯一绑定
        String token = UUID.randomUUID().toString().replace("-", "");
        RBucket<Object> bindTokenPhone =
                redissonClient.getBucket(SysConst.RedisPrefix.FORGET_PASSWORD_BIND_PHONE_TOKEN + token);
        bindTokenPhone.set(userValidateImageCodeDto.getPhone(), 10, TimeUnit.MINUTES);
        return token;
    }

    @Override
    public void smsValidateUser(UserValidateSMSCodeDto userValidateSMSCodeDto) {
        RBucket<String> bindPhoneToken =
                redissonClient.getBucket(SysConst.RedisPrefix.SMS_VERIFY_CODE + userValidateSMSCodeDto.getToken());
        if (!bindPhoneToken.isExists()) {
            throw new ServiceException("验证码失效，请返回重试！");
        }

        // 通过手机号获取验证码
        RBucket<String> verifyCode =
                redissonClient.getBucket(SysConst.RedisPrefix.SMS_VERIFY_CODE + userValidateSMSCodeDto.getToken());
        if (!verifyCode.isExists()) {
            throw new ServiceException("验证码已失效，请重新发送");
        }

        if (!StringUtil.equals(verifyCode.get(), userValidateSMSCodeDto.getVerifyCode())) {
            throw new ServiceException("验证码错误，请重新发送验证码后重试！");
        } else {
            verifyCode.delete();
        }

        // 用户通过验证，设置重置密码的 Redis key
        RBucket<Object> resetPassword =
                redissonClient.getBucket(SysConst.RedisPrefix.FORGET_PASSWORD_RESET_PASSWORD + userValidateSMSCodeDto.getToken());

        resetPassword.set("", 10, TimeUnit.MINUTES);
    }

    @Override
    public void forgetPasswordSetNewPassword(UserSetNewPassword userSetNewPassword) {
        RBucket<Object> bucket =
                redissonClient.getBucket(SysConst.RedisPrefix.FORGET_PASSWORD_RESET_PASSWORD + userSetNewPassword.getToken());
        RBucket<String> bind =
                redissonClient.getBucket(SysConst.RedisPrefix.FORGET_PASSWORD_BIND_PHONE_TOKEN + userSetNewPassword.getToken());
        if (!bucket.isExists()) {
            throw new ServiceException("未通过短信验证码验证，请返回重试");
        }

        String phone = bind.get();
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("phone", phone));
        user.setPassword(DigestUtils.md5Hex(SysConst.USER_PASSWORD_SALT + userSetNewPassword.getPassword()));
        userMapper.updateById(user);
    }

    @Override
    public void updateUserInfo(UpdateUserInfoDto dto) {
        String userID = StpUtil.getLoginIdAsString();

        User user = userMapper.selectById(userID);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        if (user.getStatus() == 1) {
            throw new ServiceException("用户被封禁，无法更改用户信息");
        }

        if (StringUtils.isNotEmpty(dto.getNickName())) {
            user.setNickName(dto.getNickName());
        }

        if (StringUtils.isNoneEmpty(dto.getNewPassword(), dto.getOldPassword())) {
            if (!StringUtil.equals(DigestUtils.md5Hex(SysConst.USER_PASSWORD_SALT + dto.getOldPassword()),
                    user.getPassword())) {
                throw new ServiceException("原密码不正确！");
            }
            user.setPassword(DigestUtils.md5Hex(SysConst.USER_PASSWORD_SALT + dto.getNewPassword()));
        }
        userMapper.updateById(user);
    }

    @Override
    public UserInfo userInfo() {
        String userId = StpUtil.getLoginIdAsString();
        UserInfo userInfo = new UserInfo();
        User user = userMapper.selectById(userId);
        userInfo.setUserId(userId);
        userInfo.setUserStatus(user.getStatus());
        userInfo.setNickName(user.getNickName());
        userInfo.setJoinDate(user.getCreateTime());
        userInfo.setJoinDays(DateTimeUtil.getDaysUntilNow(user.getCreateTime()));
        return userInfo;
    }
}
