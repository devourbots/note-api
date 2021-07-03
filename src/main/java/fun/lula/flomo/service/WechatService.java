package fun.lula.flomo.service;

import fun.lula.flomo.config.WechatConfig;

import javax.annotation.Resource;

public interface WechatService {
    public String authWechatServer(String signature, String timestamp, String nonce);

    public String getAccessToken();

    public String getQRCodeURL(String userId);

    String callbackEvent(String message, String nonce, String timestamp, String msgSignature);

    String callbackEvent(String message);
}
