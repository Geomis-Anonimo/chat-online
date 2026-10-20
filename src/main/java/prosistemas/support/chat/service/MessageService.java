package prosistemas.support.chat.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import prosistemas.support.chat.dto.MessageRequest;
import prosistemas.support.chat.model.Message;
import prosistemas.support.chat.model.Sender;
import prosistemas.support.chat.model.StatusSupportConversation;
import prosistemas.support.chat.model.SupportConversation;
import prosistemas.support.chat.repository.MessageRepository;

@Service 
public class MessageService {
    private final SupportConversationService supportConversationService;
    private final MessageRepository messageRepository;

    public MessageService(
        SupportConversationService supportConversationService,
        MessageRepository messageRepository
    ) {
        this.supportConversationService = supportConversationService;
        this.messageRepository = messageRepository;
    }

    public Message save(
        Long supportConversationId,
        Sender sender,
        MessageRequest request
    ) {
        SupportConversation supportConversation = supportConversationService.search(supportConversationId);

        if (supportConversation.getStatus() == StatusSupportConversation.CLOSED) {
            throw new IllegalStateException("Support conversation finished");
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("Empty message");
        }

        Message message = new Message();
        message.setSupportConversation(supportConversation);
        message.setSender(sender);
        message.setContent(request.content().trim());
        message.setSentDate(Instant.now());
        return messageRepository.save(message);
    }

    public List<Message> list(Long atendimentoId) {
        return messageRepository.findBySupportConversationIdOrderBySentDate(atendimentoId);
    }
}
