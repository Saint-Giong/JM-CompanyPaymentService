package rmit.saintgiong.paymentservice.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwe")
@Data
public class JweConfig {
    @Value("${jwe.issuer}")
    private String issuer;

    @Value("${jwe.temp-token-ttl-seconds}")  // Default: 5 minutes
    private int tempTokenTtlSeconds;

    @Value("${jwe.access-token-ttl-seconds}")  // Default: 15 minutes
    private int accessTokenTtlSeconds;

    @Value("${jwe.refresh-token-ttl-seconds}")  // Default: 7 days
    private int refreshTokenTtlSeconds;
}