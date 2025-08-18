package com.Loanmanagement.Loan.LMS.integration;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class MockBankService {

    // Simulate bank account database
    private final Map<String, BankAccount> mockBankDatabase = new HashMap<>();
    private final Random random = new Random();

    public boolean verifyBankAccount(String accountNumber, String ifscCode, String accountHolderName) {
        // Simulate verification (80% success rate)
        boolean isVerified = random.nextDouble() < 0.8;

        if (isVerified) {
            mockBankDatabase.put(accountNumber,
                    new BankAccount(accountNumber, ifscCode, accountHolderName, true));
        }

        return isVerified;
    }

    public boolean isAccountVerified(String accountNumber) {
        BankAccount account = mockBankDatabase.get(accountNumber);
        return account != null && account.isVerified();
    }

    // Mock bank account class
    private static class BankAccount {
        private final String accountNumber;
        private final String ifscCode;
        private final String accountHolderName;
        private final boolean verified;

        public BankAccount(String accountNumber, String ifscCode,
                           String accountHolderName, boolean verified) {
            this.accountNumber = accountNumber;
            this.ifscCode = ifscCode;
            this.accountHolderName = accountHolderName;
            this.verified = verified;
        }

        public boolean isVerified() {
            return verified;
        }
    }
}
