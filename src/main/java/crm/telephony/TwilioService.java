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
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwilioService {

    private final TwilioConfig twilioConfig;

    public String generateAccessToken(String identity) {
        long nowSeconds = System.currentTimeMillis() / 1000;
        long expirySeconds = nowSeconds + 3600;

        // Voice grant — строго по спецификации Twilio
        Map<String, Object> outgoing = new HashMap<>();
        outgoing.put("application_sid", twilioConfig.getTwimlAppSid());

        Map<String, Object> incoming = new HashMap<>();
        incoming.put("allow", true);

        Map<String, Object> voiceGrant = new LinkedHashMap<>();
        voiceGrant.put("outgoing", outgoing);
        voiceGrant.put("incoming", incoming);

        Map<String, Object> grants = new LinkedHashMap<>();
        grants.put("identity", identity);
        grants.put("voice", voiceGrant);

        Algorithm algorithm = Algorithm.HMAC256(twilioConfig.getApiKeySecret());

        // Twilio требует тип токена "JWT" в заголовке
        Map<String, Object> headerClaims = new HashMap<>();
        headerClaims.put("typ", "JWT");
        headerClaims.put("alg", "HS256");

        String token = JWT.create()
                .withHeader(headerClaims)
                .withJWTId(twilioConfig.getApiKeySid() + "-" + nowSeconds)
                .withIssuer(twilioConfig.getApiKeySid())
                .withSubject(twilioConfig.getAccountSid())
                .withIssuedAt(new Date(nowSeconds * 1000))
                .withExpiresAt(new Date(expirySeconds * 1000))
                .withClaim("grants", grants)
                .sign(algorithm);

        log.info("Generated Twilio token for identity={}, iss={}, sub={}",
                identity,
                twilioConfig.getApiKeySid().substring(0, 6) + "...",
                twilioConfig.getAccountSid().substring(0, 6) + "...");
        return token;
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