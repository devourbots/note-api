package fun.lula.flomo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "wechat")
public class WechatConfig {
    private String appName;
    private String appId;
    private String appSecret;
    private String token;
}
