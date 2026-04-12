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

    // Используем оригинальный Twilio SDK — теперь конфликта нет (оба используют jjwt 0.9.1)
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