package fun.lula.flomo.util.api;

import fun.lula.flomo.config.SmsConfigProperty;
import fun.lula.flomo.model.dto.response.SendSmsDto;
import fun.lula.flomo.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Slf4j
@Service
public class SmsApi {
    @Resource
    private SmsConfigProperty smsConfigProperty;

    public void send(SendSmsDto sendSmsDto) {
        String username = smsConfigProperty.getUsername();
        String password = smsConfigProperty.getPassword();
        String encodeMessage = encodeUtf(sendSmsDto.getMessage());
        String url =
                "https://api.smsbao.com/sms?u=" + username + "&p=" + password + "&m=" + sendSmsDto.getPhone() + "&c=" + encodeMessage;
        String responseBody = HttpUtil.sendGetRequest(url);
        log.info("开放平台响应返回体：" + responseBody);
    }

    private String encodeUtf(String content) {
        try {
            return URLEncoder.encode(content, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


}
