package prosistemas.support.chat.websocket;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import prosistemas.support.chat.model.Sender;
import prosistemas.support.chat.security.User;
import prosistemas.support.chat.service.SupportConversationService;

@Component 
public class WebSocketEventListener {
    private final OnlineAdmins onlineAdmins;
    private final SupportConversationService supportConversationService;

    public WebSocketEventListener(
        OnlineAdmins onlineAdmins,
        SupportConversationService supportConversationService
    ) {
        this.onlineAdmins = onlineAdmins;
        this.supportConversationService = supportConversationService;
    }

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        if (event.getUser() instanceof User user && user.type() == Sender.ADMIN) {
            String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
            onlineAdmins.add(sessionId, user.id());
            supportConversationService.queueDistribute();
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        onlineAdmins.delete(event.getSessionId());
    }
}
