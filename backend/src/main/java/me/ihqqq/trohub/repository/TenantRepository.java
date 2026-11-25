package me.ihqqq.trohub.repository;


import me.ihqqq.trohub.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    List<Tenant> findAllByPropertyIdAndDeletedFalseOrderByFullNameAsc(UUID propertyId);
    Optional<Tenant> findByIdAndPropertyIdAndDeletedFalse(UUID id, UUID propertyId);

    @Query("select t.id from Tenant t where t.phoneNormalized = :phone and t.deleted = false")
    List<UUID> findIdsByPhoneNormalized(@Param("phone") String phone);
}