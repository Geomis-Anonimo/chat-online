package prosistemas.support.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import prosistemas.support.chat.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySupportConversationIdOrderBySentDate(Long atendimentoId);
}