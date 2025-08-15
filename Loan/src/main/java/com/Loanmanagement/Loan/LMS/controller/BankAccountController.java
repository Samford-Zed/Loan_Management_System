package com.Loanmanagement.Loan.LMS.controller;

import com.Loanmanagement.Loan.LMS.client.BmsClient;
import com.Loanmanagement.Loan.LMS.dto.BankAccountVerificationRequest;
import com.Loanmanagement.Loan.LMS.model.BankAccount;
import com.Loanmanagement.Loan.LMS.model.User;
import com.Loanmanagement.Loan.LMS.repository.BankAccountRepository;
import com.Loanmanagement.Loan.LMS.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@RestController
@RequestMapping("/api/bank-accounts")
public class BankAccountController {

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BmsClient bmsClient;

    @Autowired
    public BankAccountController(UserRepository userRepository,
                                 BankAccountRepository bankAccountRepository,
                                 BmsClient bmsClient) {
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.bmsClient = bmsClient;
    }

    @PostMapping("/link")
    public ResponseEntity<?> linkOrCreateBankAccount(@Valid @RequestBody BankAccount bankAccountRequest) {

        // Step 1: Extract authenticated user's email from the security context
        String authenticatedEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // Step 2: Find the user by authenticated email
        Optional<User> userOpt = userRepository.findByEmail(authenticatedEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authenticated user not found");
        }
        User user = userOpt.get();

        // Step 3: Find existing bank account by account number
        Optional<BankAccount> existingAccountOpt = bankAccountRepository.findByAccountNumber(bankAccountRequest.getAccountNumber());

        if (existingAccountOpt.isPresent()) {
            BankAccount bankAccount = existingAccountOpt.get();

            // Check the ownership, optionally verify it belongs to this user
            if (!bankAccount.getUser().getEmail().equals(authenticatedEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Bank account does not belong to the authenticated user");
            }

            // Update bank account with request details
            bankAccount.setBankName(bankAccountRequest.getBankName());
            bankAccount.setBranchName(bankAccountRequest.getBranchName());
            bankAccount.setAccountHolderName(bankAccountRequest.getAccountHolderName());

            // Mark as unverified for fresh verification
            bankAccount.setVerified(false);

            // Save changes and trigger verification
            BankAccount savedBankAccount = bankAccountRepository.save(bankAccount);
            bmsClient.startBankAccountVerification(savedBankAccount);
            bankAccountRepository.save(savedBankAccount); // save updated after micro deposits etc.

            return ResponseEntity.ok(savedBankAccount);
        } else {
            // If no existing account found, reject or handle as you want for "only try exist" use case
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Bank account with this account number does not exist");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyBankAccount(@RequestBody BankAccountVerificationRequest request) {
        Optional<BankAccount> bankAccountOpt = bankAccountRepository.findByAccountNumber(request.getAccountNumber());
        if (bankAccountOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bank account not found");
        }
        BankAccount bankAccount = bankAccountOpt.get();

        if (bankAccount.isVerified()) {
            return ResponseEntity.ok("Bank account is already verified");
        }
        if (bankAccount.getMicroDeposit1().compareTo(request.getDeposit1()) == 0) {
            bankAccount.setVerified(true);
            bankAccountRepository.save(bankAccount);
            return ResponseEntity.ok("Bank account verified successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Micro-deposit amounts do not match");
        }
    }
}