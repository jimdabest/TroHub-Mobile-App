package me.ihqqq.trohub.repository;

import jakarta.persistence.LockModeType;
import me.ihqqq.trohub.entity.PaymentTransaction;
import me.ihqqq.trohub.enums.PaymentProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import me.ihqqq.trohub.enums.PaymentTransactionStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from PaymentTransaction t where t.provider = :provider and t.txnRef = :txnRef")
    Optional<PaymentTransaction> findForUpdate(@Param("provider") PaymentProvider provider,
                                               @Param("txnRef") String txnRef);

    boolean existsByInvoiceIdAndStatusAndCreatedAtAfter(UUID invoiceId, PaymentTransactionStatus status, Instant after);

    @Query(value = """
            select t from PaymentTransaction t
            join fetch t.invoice i
            join fetch i.room
            where i.property.id = :propertyId and t.status in :statuses
            order by t.createdAt desc, t.id
            """,
            countQuery = """
            select count(t) from PaymentTransaction t
            where t.invoice.property.id = :propertyId and t.status in :statuses
            """)
    Page<PaymentTransaction> findPageByProperty(@Param("propertyId") UUID propertyId,
                                                @Param("statuses") Collection<PaymentTransactionStatus> statuses,
                                                Pageable pageable);

    @Query("""
            select t from PaymentTransaction t
            join fetch t.invoice i
            join fetch i.room
            where t.id = :id and i.property.id = :propertyId
            """)
    Optional<PaymentTransaction> findByIdAndProperty(@Param("id") UUID id, @Param("propertyId") UUID propertyId);

    @Query("""
            select t from PaymentTransaction t
            join fetch t.invoice i
            join fetch i.room
            where t.id = :id and i.contract.tenant.id in :tenantIds
            """)
    Optional<PaymentTransaction> findByIdForTenants(@Param("id") UUID id,
                                                    @Param("tenantIds") Collection<UUID> tenantIds);

    /** Cập nhật hàng loạt nên @PreUpdate không chạy: phải set updatedAt thủ công. */
    @Modifying
    @Query("""
            update PaymentTransaction t set t.status = :expired, t.updatedAt = :now
            where t.status = :pending and t.createdAt < :before
            """)
    int expirePending(@Param("pending") PaymentTransactionStatus pending,
                      @Param("expired") PaymentTransactionStatus expired,
                      @Param("before") Instant before, @Param("now") Instant now);
}