package prosistemas.support.chat.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import prosistemas.support.chat.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);

    @Query(value = """
        select a.*
        from admin a
        cross join lateral (
            select count(*) as load
            from support_conversation sc
            where sc.admin_id = a.id and sc.status = 'IN_PROGRESS'
        ) l
        cross join lateral (
            select max(sc.assigned_at) as last_assigned_at
            from support_conversation sc
            where sc.admin_id = a.id
        ) la
        where a.id in (:onlineIds)
        order by l.load, la.last_assigned_at nulls first, a.id
        limit 1
        """, nativeQuery = true)
    Optional<Admin> findNextAvailableAdmin(@Param("onlineIds") Collection<Long> onlineIds);
}
