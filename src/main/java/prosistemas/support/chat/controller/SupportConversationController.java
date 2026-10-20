package prosistemas.support.chat.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import prosistemas.support.chat.dto.MessageResponse;
import prosistemas.support.chat.dto.NewSupportConversationResponse;
import prosistemas.support.chat.dto.SupportConversationRequest;
import prosistemas.support.chat.dto.SupportConversationResponse;
import prosistemas.support.chat.model.Sender;
import prosistemas.support.chat.model.StatusSupportConversation;
import prosistemas.support.chat.model.SupportConversation;
import prosistemas.support.chat.security.TokenService;
import prosistemas.support.chat.service.MessageService;
import prosistemas.support.chat.service.SupportConversationService;

@RestController 
@RequestMapping("/api/supportConversations")
public class SupportConversationController {
    private final SupportConversationService supportConversationService;
    private final MessageService messageService;
    private final TokenService tokenService;

    public SupportConversationController(
        SupportConversationService supportConversationService,
        MessageService messageService,
        TokenService tokenService
    ) {
        this.supportConversationService = supportConversationService;
        this.messageService = messageService;
        this.tokenService = tokenService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NewSupportConversationResponse open(@RequestBody @Valid SupportConversationRequest request) {
        SupportConversation supportConversation = supportConversationService.open(request);
        String token = tokenService.generate(Sender.CLIENT, supportConversation.getId());
        return new NewSupportConversationResponse(SupportConversationResponse.from(supportConversation), token);
    }

    @GetMapping("/{id}")
    public SupportConversationResponse search(@PathVariable Long id,
                                      @RequestHeader(value = "Authorization", required = false) String authorization) {
        return SupportConversationResponse.from(supportConversationService.search(id, tokenService.validate(authorization)));
    }

    @PostMapping("/{id}/finish")
    public SupportConversationResponse finish(@PathVariable Long id,
                                        @RequestHeader(value = "Authorization", required = false) String authorization) {
        return SupportConversationResponse.from(supportConversationService.finish(id, tokenService.validateAdmin(authorization)));
    }

    @GetMapping("/{id}/messages")
    public List<MessageResponse> listMessages(@PathVariable Long id,
                                                  @RequestHeader(value = "Authorization", required = false) String authorization) {
        supportConversationService.search(id, tokenService.validate(authorization));
        return messageService.list(id).stream()
                .map(MessageResponse::from)
                .toList();
    }
}