package prosistemas.support.chat.websocket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import prosistemas.support.chat.security.TokenService;
import prosistemas.support.chat.security.User;
import prosistemas.support.chat.service.SupportConversationService;

@Component 
public class InterceptorAuthentication implements ChannelInterceptor {
    
    private static final Pattern SUPPORT_CONVERSATION_DESTINY = Pattern.compile("^/(topic|app)/supportConversations/(\\d+)(/\\w+)?$");

    private final TokenService tokenService;
    private final SupportConversationService supportConversationService;

    public InterceptorAuthentication(
        TokenService tokenService,
        SupportConversationService supportConversationService
    ) {
        this.tokenService = tokenService;
        this.supportConversationService = supportConversationService;
    }
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            accessor.setUser(tokenService.validate(accessor.getFirstNativeHeader("Authorization")));
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) || StompCommand.SEND.equals(accessor.getCommand())) {
            verifyDestiny((User) accessor.getUser(), accessor.getDestination());
        }
        return message;
    }

    private void verifyDestiny(User user, String destiny) {
        if (user != null && destiny != null && destiny.startsWith("/user/")) {
            return;
        }

        Matcher matcher = SUPPORT_CONVERSATION_DESTINY.matcher(destiny != null ? destiny : "");
        if (!matcher.matches()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Destino não permitido");
        }
        supportConversationService.search(Long.valueOf(matcher.group(2)), user);
    }
}
