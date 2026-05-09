package crm.telephony;

import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VoiceGrant;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Client;
import com.twilio.twiml.voice.Dial;
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
                .ttl(3600)
                .build();

        log.info("Generated token: identity={}, appSid={}",
                identity, twilioConfig.getTwimlAppSid());
        return token.toJwt();
    }

    public String connectToOperator(String operatorIdentity) {
        log.info("connectToOperator: identity={}", operatorIdentity);

        Client client = new Client.Builder(operatorIdentity).build();
        Dial dial = new Dial.Builder()
                .callerId(twilioConfig.getPhoneNumber())
                .client(client)
                .build();

        return new VoiceResponse.Builder().dial(dial).build().toXml();
    }
}