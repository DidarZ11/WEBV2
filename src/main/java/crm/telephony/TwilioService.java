package crm.telephony;

import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VoiceGrant;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Client;
import com.twilio.twiml.voice.Dial;
import com.twilio.twiml.voice.Number;
import com.twilio.twiml.voice.Say;
import crm.config.TwilioConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwilioService {

    private final TwilioConfig twilioConfig;

    // Генерация токена для браузера
    public String generateAccessToken(String identity) {
        VoiceGrant grant = new VoiceGrant();
        grant.setOutgoingApplicationSid(twilioConfig.getTwimlAppSid());
        grant.setIncomingAllow(true);

        AccessToken token = new AccessToken.Builder(
                twilioConfig.getAccountSid(),
                twilioConfig.getApiKeySid(),
                twilioConfig.getApiKeySecret()
        )
                .identity(identity)
                .grant(grant)
                .build();

        String jwt = token.toJwt();
        log.info("Generated Twilio token for identity={}", identity);
        return jwt;
    }

    // Соединение клиента с оператором (Входящий)
    public String handleIncomingCall(String operatorIdentity) {
        Say say = new Say.Builder("Входящий звонок. Соединяю с оператором.")
                .language(Say.Language.RU_RU)
                .build();

        Client client = new Client.Builder(operatorIdentity).build();
        Dial dial = new Dial.Builder()
                .callerId(twilioConfig.getPhoneNumber()) // +16414018641
                .client(client)
                .build();

        return new VoiceResponse.Builder().say(say).dial(dial).build().toXml();
    }

    // Соединение оператора с клиентом (Исходящий)
    public String handleOutgoingCall(String clientPhoneNumber) {
        log.info("handleOutgoingCall: to={}, callerId={}", clientPhoneNumber, twilioConfig.getPhoneNumber());

        Number number = new Number.Builder(clientPhoneNumber).build();

        // callerId ОБЯЗАТЕЛЕН на триальном аккаунте Twilio — используем купленный номер
        Dial dial = new Dial.Builder()
                .callerId(twilioConfig.getPhoneNumber()) // +16414018641
                .number(number)
                .build();

        return new VoiceResponse.Builder().dial(dial).build().toXml();
    }
}