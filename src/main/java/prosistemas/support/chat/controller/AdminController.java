package prosistemas.support.chat.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import prosistemas.support.chat.dto.AdminResponse;
import prosistemas.support.chat.dto.LoginRequest;
import prosistemas.support.chat.dto.SupportConversationResponse;
import prosistemas.support.chat.model.Admin;
import prosistemas.support.chat.model.Sender;
import prosistemas.support.chat.security.TokenService;
import prosistemas.support.chat.security.User;
import prosistemas.support.chat.service.AdminService;
import prosistemas.support.chat.service.SupportConversationService;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService adminService;
    private final SupportConversationService supportConversationService;
    private final TokenService tokenService;

    public AdminController(
        AdminService adminService,
        SupportConversationService supportConversationService,
        TokenService tokenService
    ) {
        this.adminService = adminService;
        this.tokenService = tokenService;
        this.supportConversationService = supportConversationService;
    }

    @PostMapping("/login")
    public AdminResponse login(@RequestBody @Valid LoginRequest request) {
        Admin admin = adminService.login(
            request.email(),
            request.password()
        );

        return AdminResponse.from(admin, tokenService.generate(
            Sender.ADMIN, admin.getId()
        ));
    }

    @GetMapping("/me/support_conversation")
    public List<SupportConversationResponse> listarAtendimentos(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User usuario = tokenService.validateAdmin(authorization);
        return supportConversationService.listByAdminId(usuario.id()).stream()
                .map(SupportConversationResponse::from)
                .toList();
    }
}
