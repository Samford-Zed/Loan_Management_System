package com.Loanmanagement.Loan.LMS.service;

import com.Loanmanagement.Loan.LMS.client.BmsClient;
import com.Loanmanagement.Loan.LMS.model.BankAccount;
import com.Loanmanagement.Loan.LMS.repository.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BankAccountService {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private BmsClient bmsClient; // Inject the client

    public List<BankAccount> getAllBankAccounts() {
        return bankAccountRepository.findAll();
    }

    public Optional<BankAccount> getBankAccountById(Long id) {
        return bankAccountRepository.findById(id);
    }

    public BankAccount linkBankAccount(BankAccount bankAccount) {
        // Save the bank account first with isVerified = false
        bankAccount.setVerified(false);
        BankAccount savedAccount = bankAccountRepository.save(bankAccount);

        // Now, call the BMS to start the verification process
        bmsClient.startBankAccountVerification(savedAccount);

        return savedAccount;
    }
}

