package prosistemas.support.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import prosistemas.support.chat.model.StatusSupportConversation;
import prosistemas.support.chat.model.SupportConversation;

public interface SupportConversationRepository extends JpaRepository<SupportConversation, Long> {
    List<SupportConversation> findByStatusOrderByCreatedAt(StatusSupportConversation status);
    List<SupportConversation> findByAdminIdAndStatus(Long adminId, StatusSupportConversation status);
    long countByAdminIdAndStatus(Long adminId, StatusSupportConversation status);
}
