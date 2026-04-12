package crm.telephony;

import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VoiceGrant;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Dial;
import com.twilio.twiml.voice.Client;
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

    // Генерируем токен для входа оператора в сеть
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

        return token.toJwt();
    }

    // Основной метод для входящего звонка от Twilio
    public String handleIncomingCall(String operatorIdentity) {
        // 1. Приветствие
        Say say = new Say.Builder("Добро пожаловать в CRM. Соединяем с оператором.")
                .language(Say.Language.RU_RU)
                .build();

        // 2. Соединение с браузером (Client)
        // ВАЖНО: identity должен быть тот же, что и в токене фронтенда
        Client client = new Client.Builder(operatorIdentity).build();
        Dial dial = new Dial.Builder().client(client).build();

        // Собираем ответ
        VoiceResponse response = new VoiceResponse.Builder()
                .say(say)
                .dial(dial)
                .build();

        return response.toXml();
    }
}