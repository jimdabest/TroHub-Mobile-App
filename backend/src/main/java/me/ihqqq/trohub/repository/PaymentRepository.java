package me.ihqqq.trohub.repository;

import me.ihqqq.trohub.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findAllByInvoiceIdAndDeletedFalseOrderByPaidAtDesc(UUID invoiceId);

    Optional<Payment> findByIdAndInvoiceIdAndDeletedFalse(UUID id, UUID invoiceId);

    @Query("""
            select p from Payment p
            join fetch p.invoice i
            where i.property.id = :propertyId and p.updatedAt > :since
            """)
    List<Payment> findAllChangedSince(@Param("propertyId") UUID propertyId, @Param("since") Instant since);
}