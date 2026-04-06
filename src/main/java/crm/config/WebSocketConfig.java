package crm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Включаем простой брокер сообщений. Все подписчики на "/topic" будут получать уведомления
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Эндпоинт, к которому будет подключаться наш React-фронтенд
        registry.addEndpoint("/ws-telephony")
                .setAllowedOriginPatterns("*") // Разрешаем CORS, как и в SecurityConfig
                .withSockJS(); // Поддержка фоллбэка, если чистые вебсокеты недоступны
    }
}