package crm.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Slf4j
public class TwilioConfig {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String phoneNumber;

    @Value("${twilio.twiml-app-sid}")
    private String twimlAppSid;

    @Value("${twilio.api-key-sid}")
    private String apiKeySid;

    @Value("${twilio.api-key-secret}")
    private String apiKeySecret;

    @PostConstruct
    public void init() {
        log.info("=== TWILIO CONFIG CHECK ===");
        log.info("accountSid   : [{}] length={}", maskValue(accountSid), accountSid != null ? accountSid.length() : 0);
        log.info("authToken    : [{}] length={}", maskValue(authToken), authToken != null ? authToken.length() : 0);
        log.info("phoneNumber  : [{}]", phoneNumber);
        log.info("twimlAppSid  : [{}] length={}", maskValue(twimlAppSid), twimlAppSid != null ? twimlAppSid.length() : 0);
        log.info("apiKeySid    : [{}] length={}", maskValue(apiKeySid), apiKeySid != null ? apiKeySid.length() : 0);
        log.info("apiKeySecret : [{}] length={}", maskValue(apiKeySecret), apiKeySecret != null ? apiKeySecret.length() : 0);

        try {
            Twilio.init(accountSid, authToken);
            log.info("=== TWILIO INIT SUCCESS ===");
        } catch (Exception e) {
            log.error("=== TWILIO INIT FAILED: {} ===", e.getMessage(), e);
            throw e; // пробрасываем чтобы Spring не стартовал с битой конфигурацией
        }
    }

    private String maskValue(String value) {
        if (value == null) return "NULL";
        if (value.isBlank()) return "EMPTY";
        if (value.length() <= 4) return value;
        return value.substring(0, 4) + "..." + value.substring(value.length() - 2);
    }
}