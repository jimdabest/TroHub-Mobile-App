package me.ihqqq.trohub.repository;


import me.ihqqq.trohub.entity.PropertyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyMemberRepository extends JpaRepository<PropertyMember, UUID> {
    Optional<PropertyMember> findByPropertyIdAndUserId(UUID propertyId, UUID userId);

    boolean existsByPropertyIdAndUserId(UUID propertyId, UUID userId);

    @Query("select m from PropertyMember m join fetch m.user where m.property.id = :propertyId order by m.createdAt")
    List<PropertyMember> findAllWithUser(@Param("propertyId") UUID propertyId);

    @Query("select m from PropertyMember m join fetch m.property p " +
            "where m.user.id = :userId and p.deleted = false order by p.name")
    List<PropertyMember> findAllActiveByUserId(@Param("userId") UUID userId);
}