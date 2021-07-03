package fun.lula.flomo.util;

public interface SysConst {
    String USER_PASSWORD_SALT = "adf{}>!?$!$saj";

    interface RedisPrefix {
        // 注册时，手机号和验证码的绑定
        String SMS_VERIFY_CODE = "sms:verify:code:";

        // 发送验证码，频繁锁定
        String SMS_SEND_LOCK = "sms:verify:send:lock:";

        // 登录失败次数
        String LOGIN_FAILED_TIMES = "login:failed:times:";

        // 登录失败锁定
        String LOGIN_FAILED_LOCK = "login:failed:lock:";

        // 图片验证码
        String IMAGE_VERIFY_CODE = "image:verify:code:";

        // 忘记密码 接收手机验证码
        String FORGET_PASSWORD_BIND_PHONE_TOKEN = "sms:forget:bind:phone:token:";

        // 忘记密码 短信验证码 验证通过 重新设置密码
        String FORGET_PASSWORD_RESET_PASSWORD = "forget:reset:password:";

        // wechat access token
        String WECHAT_ACCESS_TOKEN = "wechat:access:token";

    }


}
