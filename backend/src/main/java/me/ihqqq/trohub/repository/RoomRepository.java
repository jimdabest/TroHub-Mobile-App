package me.ihqqq.trohub.repository;


import me.ihqqq.trohub.entity.Room;
import me.ihqqq.trohub.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {
    List<Room> findAllByPropertyIdAndDeletedFalseOrderByCodeAsc(UUID propertyId);
    List<Room> findAllByPropertyIdAndStatusAndDeletedFalseOrderByCodeAsc(UUID propertyId, RoomStatus status);
    Optional<Room> findByIdAndPropertyIdAndDeletedFalse(UUID id, UUID propertyId);
    boolean existsByPropertyIdAndCodeAndDeletedFalse(UUID propertyId, String code);
    boolean existsByPropertyIdAndCodeAndDeletedFalseAndIdNot(UUID propertyId, String code, UUID id);
}