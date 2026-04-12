package crm.telephony;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Client;
import com.twilio.twiml.voice.Dial;
import com.twilio.twiml.voice.Number;
import com.twilio.twiml.voice.Say;
import crm.config.TwilioConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwilioService {

    private final TwilioConfig twilioConfig;

    /**
     * Генерируем Twilio Access Token вручную через Auth0 java-jwt.
     * Это обходит конфликт между Twilio SDK (старый jjwt API) и нашим jjwt 0.12.3.
     *
     * Структура токена соответствует спецификации Twilio Access Token:
     * https://www.twilio.com/docs/iam/access-tokens
     */
    public String generateAccessToken(String identity) {
        long now = System.currentTimeMillis();
        long expiry = now + (3600 * 1000); // 1 час

        // Grant для Voice SDK
        Map<String, Object> voiceGrant = new HashMap<>();
        voiceGrant.put("outgoing", Map.of("application_sid", twilioConfig.getTwimlAppSid()));
        voiceGrant.put("incoming", Map.of("allow", true));

        Map<String, Object> grants = new HashMap<>();
        grants.put("identity", identity);
        grants.put("voice", voiceGrant);

        Algorithm algorithm = Algorithm.HMAC256(twilioConfig.getApiKeySecret());

        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withIssuer(twilioConfig.getApiKeySid())
                .withSubject(twilioConfig.getAccountSid())
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(expiry))
                .withClaim("grants", grants)
                .sign(algorithm);
    }

    public String handleIncomingCall(String operatorIdentity) {
        Say say = new Say.Builder("Входящий звонок. Соединяю с оператором.")
                .language(Say.Language.RU_RU)
                .build();

        Client client = new Client.Builder(operatorIdentity).build();
        Dial dial = new Dial.Builder().client(client).build();

        return new VoiceResponse.Builder().say(say).dial(dial).build().toXml();
    }

    public String handleOutgoingCall(String clientPhoneNumber) {
        Number number = new Number.Builder(clientPhoneNumber).build();
        Dial dial = new Dial.Builder().number(number).build();

        return new VoiceResponse.Builder().dial(dial).build().toXml();
    }
}