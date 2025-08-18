package com.Loanmanagement.Loan.LMS.repository;
import com.Loanmanagement.Loan.LMS.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Integer> {
    Optional<BankAccount> findByUserEmail(String email);
}