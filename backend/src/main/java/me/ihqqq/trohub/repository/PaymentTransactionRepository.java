package me.ihqqq.trohub.repository;

import jakarta.persistence.LockModeType;
import me.ihqqq.trohub.entity.PaymentTransaction;
import me.ihqqq.trohub.enums.PaymentProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from PaymentTransaction t where t.provider = :provider and t.txnRef = :txnRef")
    Optional<PaymentTransaction> findForUpdate(@Param("provider") PaymentProvider provider,
                                               @Param("txnRef") String txnRef);
}