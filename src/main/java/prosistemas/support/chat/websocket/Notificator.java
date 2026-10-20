package prosistemas.support.chat.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import prosistemas.support.chat.dto.SupportConversationResponse;
import prosistemas.support.chat.model.Sender;
import prosistemas.support.chat.security.User;

@Component 
public class Notificator {
    private final SimpMessagingTemplate messagingTemplate;

    public Notificator(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void newSupportConversation(Long adminId, SupportConversationResponse atendimento) {
        String usuario = new User(Sender.ADMIN, adminId).getName();
        messagingTemplate.convertAndSendToUser(usuario, "/queue/support_conversations", atendimento);
    }

    public void updatedStatus(SupportConversationResponse supportConversation) {
        messagingTemplate.convertAndSend("/topic/support_conversations/" + supportConversation.id() + "/status", supportConversation);
    }
}
