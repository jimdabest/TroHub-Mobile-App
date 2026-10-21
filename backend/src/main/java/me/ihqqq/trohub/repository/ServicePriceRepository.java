package me.ihqqq.trohub.repository;

import me.ihqqq.trohub.entity.ServicePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServicePriceRepository extends JpaRepository<ServicePrice, UUID> {
    List<ServicePrice> findAllByPropertyIdAndDeletedFalseOrderByNameAsc(UUID propertyId);
    Optional<ServicePrice> findByIdAndPropertyIdAndDeletedFalse(UUID id, UUID propertyId);
}