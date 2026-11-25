package me.ihqqq.trohub.repository;


import me.ihqqq.trohub.entity.Contract;
import me.ihqqq.trohub.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {
    List<Contract> findAllByPropertyIdAndDeletedFalseOrderByStartDateDesc(UUID propertyId);
    List<Contract> findAllByPropertyIdAndStatusAndDeletedFalseOrderByStartDateDesc(UUID propertyId, ContractStatus status);
    Optional<Contract> findByIdAndPropertyIdAndDeletedFalse(UUID id, UUID propertyId);
    @Query("""
            select c from Contract c
            join fetch c.room
            where c.property.id = :propertyId and c.status = :status and c.deleted = false
            """)
    List<Contract> findAllWithRoomByPropertyAndStatus(@Param("propertyId") UUID propertyId,
                                                      @Param("status") ContractStatus status);
    @Query("""
            select c from Contract c
            join fetch c.room
            join fetch c.property
            where c.tenant.id in :tenantIds and c.deleted = false
            order by c.startDate desc
            """)
    List<Contract> findAllForTenants(@Param("tenantIds") Collection<UUID> tenantIds);

    boolean existsByRoomIdAndStatusAndDeletedFalse(UUID roomId, ContractStatus status);
    boolean existsByTenantIdAndStatusAndDeletedFalse(UUID tenantId, ContractStatus status);
}