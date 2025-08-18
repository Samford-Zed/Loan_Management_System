package com.Loanmanagement.Loan.LMS.service;

import com.Loanmanagement.Loan.LMS.dto.BankAccountDto;
import com.Loanmanagement.Loan.LMS.exception.UserNotFoundException;
import com.Loanmanagement.Loan.LMS.integration.MockBankService;
import com.Loanmanagement.Loan.LMS.model.BankAccount;
import com.Loanmanagement.Loan.LMS.model.User;
import com.Loanmanagement.Loan.LMS.repository.BankAccountRepository;
import com.Loanmanagement.Loan.LMS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final MockBankService mockBankService;

    public void linkBankAccount(String userEmail, BankAccountDto dto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Verify with mock bank system
        boolean isVerified = mockBankService.verifyBankAccount(
                dto.getAccountNumber(),
                dto.getIfscCode(),
                dto.getAccountHolderName()
        );

        if (!isVerified) {
            throw new RuntimeException("Bank account verification failed");
        }

        BankAccount bankAccount = BankAccount.builder()
                .accountNumber(dto.getAccountNumber())
                .accountHolderName(dto.getAccountHolderName())
                .ifscCode(dto.getIfscCode())
                .verified(true)
                .user(user)
                .build();

        bankAccountRepository.save(bankAccount);
    }

    public boolean isBankAccountVerified(String userEmail) {
        return bankAccountRepository.findByUserEmail(userEmail)
                .map(BankAccount::isVerified)
                .orElse(false);
    }
}