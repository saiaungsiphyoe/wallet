package com.asp.wallet.wallet.repository;

import com.asp.wallet.wallet.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    List<Wallet> findByAppUserId(Long appUserId);

    Optional<Wallet> findByIdAndAppUser_Id(Long id, Long appUserId);
}
