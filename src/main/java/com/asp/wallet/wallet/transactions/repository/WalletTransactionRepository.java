package com.asp.wallet.wallet.transactions.repository;

import com.asp.wallet.wallet.transactions.model.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    @Query("""
            SELECT wt
            FROM WalletTransaction wt
            WHERE wt.fromWallet.id = :walletId
               OR wt.toWallet.id = :walletId
            """)
    Page<WalletTransaction> findAllByWalletId(
            @Param("walletId") Long walletId,
            Pageable pageable
    );

    @Query("""
            SELECT wt
            FROM WalletTransaction wt
            WHERE wt.id = :transactionId
              AND (
                  wt.fromWallet.id = :walletId
                  OR wt.toWallet.id = :walletId
              )
            """)
    Optional<WalletTransaction> findByIdAndWalletId(
            @Param("transactionId") Long transactionId,
            @Param("walletId") Long walletId
    );

    long countByFromWallet_IdOrToWallet_Id(Long fromWalletId, Long toWalletId);

    void deleteAllByFromWallet_IdOrToWallet_Id(Long fromWalletId, Long toWalletId);
}
