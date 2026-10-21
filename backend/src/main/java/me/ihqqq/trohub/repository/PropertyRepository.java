package me.ihqqq.trohub.repository;


import me.ihqqq.trohub.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {
    Optional<Property> findByIdAndDeletedFalse(UUID id);
}