package me.ihqqq.trohub.repository;


import me.ihqqq.trohub.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Page<Tenant> findAllByPropertyIdAndDeletedFalse(UUID propertyId, Pageable pageable);
    Optional<Tenant> findByIdAndPropertyIdAndDeletedFalse(UUID id, UUID propertyId);

    @Query("select t.id from Tenant t where t.userId = :userId and t.deleted = false")
    List<UUID> findIdsByUserId(@Param("userId") UUID userId);

    /** Khóa dòng để hai tài khoản dùng cùng một mã cùng lúc thì chỉ một bên thành công. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Tenant t where t.inviteCodeHash = :hash and t.deleted = false")
    Optional<Tenant> findForLink(@Param("hash") String hash);

    /** Dùng cho đồng bộ: gồm cả dòng đã xóa mềm (để báo cho client xóa). */
    List<Tenant> findAllByPropertyIdAndUpdatedAtAfter(UUID propertyId, Instant since);
}