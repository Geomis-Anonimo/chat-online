package prosistemas.support.chat.service;

import java.io.ObjectInputFilter.Status;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import prosistemas.support.chat.dto.SupportConversationRequest;
import prosistemas.support.chat.dto.SupportConversationResponse;
import prosistemas.support.chat.model.Admin;
import prosistemas.support.chat.model.Client;
import prosistemas.support.chat.model.Sender;
import prosistemas.support.chat.model.StatusSupportConversation;
import prosistemas.support.chat.model.SupportConversation;
import prosistemas.support.chat.repository.AdminRepository;
import prosistemas.support.chat.repository.SupportConversationRepository;
import prosistemas.support.chat.security.User;
import prosistemas.support.chat.websocket.Notificator;
import prosistemas.support.chat.websocket.OnlineAdmins;

@Service 
public class SupportConversationService {

    private final SupportConversationRepository supportConversationRepository;
    private final AdminRepository adminRepository;
    private final OnlineAdmins onlineAdmins;
    private final Notificator notificator;

    public SupportConversationService(
        SupportConversationRepository supportConversationRepository,
        AdminRepository adminRepository,
        OnlineAdmins onlineAdmins,
        Notificator notificator
    ) {
        this.supportConversationRepository = supportConversationRepository;
        this.adminRepository = adminRepository;
        this.onlineAdmins = onlineAdmins;
        this.notificator = notificator;
    }
    
    public List<SupportConversation> listByAdminId(Long atendenteId) {
        return supportConversationRepository.findByAdminIdAndStatus(
            atendenteId, StatusSupportConversation.IN_PROGRESS
        );
    }

    public SupportConversation search(Long id) {
        return supportConversationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support conversation not found"));
    }

    public synchronized SupportConversation open(SupportConversationRequest request) {
        Client cliente = new Client();
        cliente.setName(request.name());
        cliente.setEmail(request.email());
        cliente.setPhone(request.phone());

        SupportConversation atendimento = new SupportConversation();
        atendimento.setClient(cliente);
        atendimento.setTitle(request.title());
        atendimento.setStatus(StatusSupportConversation.WAITING);
        atendimento.setCreatedAt(Instant.now());
        atendimento = supportConversationRepository.save(atendimento);

        distribute(atendimento);
        return atendimento;
    }

    private void distribute(SupportConversation supportConversation) {
        Optional<Admin> admin = choseFreeAdminByOciosityOrder();
        if (admin.isEmpty()) {
            return;
        }

        supportConversation.setAdmin(admin.get());
        supportConversation.setStatus(StatusSupportConversation.IN_PROGRESS);
        supportConversation.setAssignedAt(Instant.now());
        supportConversationRepository.save(supportConversation);

        SupportConversationResponse response = SupportConversationResponse.from(supportConversation);
        notificator.newSupportConversation(admin.get().getId(), response);
        notificator.updateTalkStatus(response);
    }

    private Optional<Admin> choseFreeAdminByOciosityOrder() {
        Set<Long> onlineIds = onlineAdmins.onlineIds();
        if (onlineIds.isEmpty()) {
            return Optional.empty();
        }
        return adminRepository.findNextAvailableAdmin(onlineIds);
    }

    private boolean canAccess(SupportConversation supportConversation, User user) {
        if (user == null) {
            return false;
        }
        if (user.type() == Sender.CLIENT) {
            return supportConversation.getId().equals(user.id());
        }
        return supportConversation.getAdmin() != null && supportConversation.getAdmin().getId().equals(user.id());
    }

    public SupportConversation finish(Long id, User usuario) {
        SupportConversation supportConversation = search(id, usuario);

        if (!supportConversation.getStatus().equals(StatusSupportConversation.IN_PROGRESS)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O Atendimento não está em andamento para ser finalizado");
        }
        supportConversation.setStatus(StatusSupportConversation.CLOSED);
        supportConversation.setFinishedAt(Instant.now());
        supportConversation = supportConversationRepository.save(supportConversation);

        notificator.updateTalkStatus(SupportConversationResponse.from(supportConversation));
        return supportConversation;
    }

    public SupportConversation search(Long id, User usuario) {
        SupportConversation supportConveersation = search(id);
        if (!canAccess(supportConveersation, usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Denied access");
        }
        return supportConveersation;
    }

    public synchronized void queueDistribute() {
        supportConversationRepository.findByStatusOrderByCreatedAt(StatusSupportConversation.WAITING)
                .forEach(this::distribute);
    }
}