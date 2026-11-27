package me.ihqqq.trohub.repository;

import me.ihqqq.trohub.entity.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

@Repository
public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {

    @Query("""
            select m from MeterReading m
            join fetch m.room r
            join fetch m.servicePrice s
            where m.property.id = :propertyId and m.period = :period and m.deleted = false
            order by r.code asc, s.name asc
            """)
    List<MeterReading> findAllByPropertyAndPeriod(@Param("propertyId") UUID propertyId,
                                                  @Param("period") LocalDate period);

    boolean existsByRoomIdAndServicePriceIdAndPeriodAndDeletedFalse(UUID roomId, UUID servicePriceId, LocalDate period);

    Optional<MeterReading> findFirstByRoomIdAndServicePriceIdAndDeletedFalseOrderByPeriodDesc(UUID roomId, UUID servicePriceId);

    Optional<MeterReading> findFirstByRoomIdAndServicePriceIdAndPeriodLessThanAndDeletedFalseOrderByPeriodDesc(
            UUID roomId, UUID servicePriceId, LocalDate period);

    @Query("""
            select m from MeterReading m
            join fetch m.room
            join fetch m.servicePrice
            where m.property.id = :propertyId and m.updatedAt > :since
            """)
    List<MeterReading> findAllChangedSince(@Param("propertyId") UUID propertyId, @Param("since") Instant since);
}