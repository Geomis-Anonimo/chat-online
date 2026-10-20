package prosistemas.support.chat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import prosistemas.support.chat.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);
}
