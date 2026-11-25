package me.ihqqq.trohub.repository;

import me.ihqqq.trohub.entity.Invoice;
import me.ihqqq.trohub.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    @Query("""
            select i from Invoice i
            join fetch i.room r
            where i.property.id = :propertyId and i.status in :statuses and i.deleted = false
            order by i.period desc, r.code asc
            """)
    List<Invoice> findAllByProperty(@Param("propertyId") UUID propertyId,
                                    @Param("statuses") Collection<InvoiceStatus> statuses);

    @Query("""
            select i from Invoice i
            join fetch i.room r
            where i.property.id = :propertyId and i.period = :period
              and i.status in :statuses and i.deleted = false
            order by r.code asc
            """)
    List<Invoice> findAllByPropertyAndPeriod(@Param("propertyId") UUID propertyId,
                                             @Param("period") LocalDate period,
                                             @Param("statuses") Collection<InvoiceStatus> statuses);

    @EntityGraph(attributePaths = {"room", "items"})
    Optional<Invoice> findByIdAndPropertyIdAndDeletedFalse(UUID id, UUID propertyId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Invoice i where i.id = :id and i.property.id = :propertyId and i.deleted = false")
    Optional<Invoice> findForUpdate(@Param("id") UUID id, @Param("propertyId") UUID propertyId);

    boolean existsByIdAndPropertyIdAndDeletedFalse(UUID id, UUID propertyId);

    interface InvoiceTotals {
        BigDecimal getTotal();   // null nếu kỳ chưa có hóa đơn nào
        BigDecimal getPaid();
    }

    @Query("""
            select sum(i.totalAmount) as total, sum(i.paidAmount) as paid
            from Invoice i
            where i.property.id = :propertyId and i.period = :period
              and i.status in :statuses and i.deleted = false
            """)
    InvoiceTotals sumTotalsByPeriod(@Param("propertyId") UUID propertyId,
                                    @Param("period") LocalDate period,
                                    @Param("statuses") Collection<InvoiceStatus> statuses);


    @Query("""
            select i from Invoice i
            join fetch i.room r
            where i.contract.tenant.id in :tenantIds and i.status in :statuses and i.deleted = false
            order by i.period desc, r.code asc
            """)
    List<Invoice> findAllForTenants(@Param("tenantIds") Collection<UUID> tenantIds,
                                    @Param("statuses") Collection<InvoiceStatus> statuses);

    @Query("""
            select i from Invoice i
            join fetch i.room r
            where i.contract.tenant.id in :tenantIds and i.period = :period
              and i.status in :statuses and i.deleted = false
            order by r.code asc
            """)
    List<Invoice> findAllForTenantsAndPeriod(@Param("tenantIds") Collection<UUID> tenantIds,
                                             @Param("period") LocalDate period,
                                             @Param("statuses") Collection<InvoiceStatus> statuses);

    @EntityGraph(attributePaths = {"room", "items"})
    @Query("""
            select i from Invoice i
            where i.id = :id and i.contract.tenant.id in :tenantIds
              and i.status in :statuses and i.deleted = false
            """)
    Optional<Invoice> findDetailForTenants(@Param("id") UUID id,
                                           @Param("tenantIds") Collection<UUID> tenantIds,
                                           @Param("statuses") Collection<InvoiceStatus> statuses);

    @Query("""
            select i.property.id from Invoice i
            where i.id = :id and i.contract.tenant.id in :tenantIds
              and i.status in :statuses and i.deleted = false
            """)
    Optional<UUID> findPropertyIdForTenants(@Param("id") UUID id,
                                            @Param("tenantIds") Collection<UUID> tenantIds,
                                            @Param("statuses") Collection<InvoiceStatus> statuses);

    @Query("""
            select i.room.id from Invoice i
            where i.property.id = :propertyId and i.period = :period
              and i.deleted = false and i.status <> me.ihqqq.trohub.enums.InvoiceStatus.CANCELLED
            """)
    List<UUID> findInvoicedRoomIds(@Param("propertyId") UUID propertyId, @Param("period") LocalDate period);
}