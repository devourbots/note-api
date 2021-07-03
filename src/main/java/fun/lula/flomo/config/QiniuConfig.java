package fun.lula.flomo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "qiniu")
public class QiniuConfig {
    private String AK;
    private String SK;
    private String DOMAIN;
    private String BUCKET;
}
