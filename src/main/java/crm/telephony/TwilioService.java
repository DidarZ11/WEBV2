package crm.telephony;

import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VoiceGrant;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Dial;
import com.twilio.twiml.voice.Client;
import com.twilio.twiml.voice.Say;
import crm.config.TwilioConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TwilioService {

    private final TwilioConfig twilioConfig;

    public String generateAccessToken(String identity) {
        VoiceGrant grant = new VoiceGrant();
        grant.setOutgoingApplicationSid(twilioConfig.getTwimlAppSid());
        grant.setIncomingAllow(true);

        AccessToken token = new AccessToken.Builder(
                twilioConfig.getAccountSid(),
                twilioConfig.getApiKeySid(),      // SK...
                twilioConfig.getApiKeySecret()    // Secret
        )
                .identity(identity)
                .grant(grant)
                .build();

        return token.toJwt();
    }

    public String handleIncomingCall() {
        Say say = new Say.Builder("Добро пожаловать. Соединяем с оператором.")
                .language(Say.Language.RU_RU)
                .build();
        VoiceResponse response = new VoiceResponse.Builder().say(say).build();
        return response.toXml();
    }

    public String connectToOperator(String operatorIdentity) {
        Client client = new Client.Builder(operatorIdentity).build();
        Dial dial = new Dial.Builder().client(client).build();
        VoiceResponse response = new VoiceResponse.Builder().dial(dial).build();
        return response.toXml();
    }
}