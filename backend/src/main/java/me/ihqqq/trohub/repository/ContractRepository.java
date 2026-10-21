package me.ihqqq.trohub.repository;


import me.ihqqq.trohub.entity.Contract;
import me.ihqqq.trohub.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {
    List<Contract> findAllByPropertyIdAndDeletedFalseOrderByStartDateDesc(UUID propertyId);
    List<Contract> findAllByPropertyIdAndStatusAndDeletedFalseOrderByStartDateDesc(UUID propertyId, ContractStatus status);
    Optional<Contract> findByIdAndPropertyIdAndDeletedFalse(UUID id, UUID propertyId);
    boolean existsByRoomIdAndStatusAndDeletedFalse(UUID roomId, ContractStatus status);
    boolean existsByTenantIdAndStatusAndDeletedFalse(UUID tenantId, ContractStatus status);
}