package prosistemas.support.chat.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import prosistemas.support.chat.dto.MessageRequest;
import prosistemas.support.chat.dto.MessageResponse;
import prosistemas.support.chat.security.User;
import prosistemas.support.chat.service.MessageService;

@Controller 
public class MessageController {
    
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @MessageMapping("/support-conversations/{id}/messages")
    @SendTo("/topic/support-conversations/{id}")
    public MessageResponse enviar(@DestinationVariable Long id, MessageRequest request, User usuario) {
        return MessageResponse.from(messageService.save(id, usuario.type(), request));
    }
}
