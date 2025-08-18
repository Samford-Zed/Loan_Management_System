package com.Loanmanagement.Loan.LMS.integration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class BmsClientMock implements BmsClient {

    @Override
    public boolean startBankAccountVerification(String accountHolderName, String accountNumber, String bankName, String ifscCode) {
        log.info("Mock BMS: Starting bank account verification for account {} at {}", accountNumber, bankName);
        // In a real implementation, this would call the actual BMS API
        // For now, we'll just simulate a successful verification
        return true;
    }

    @Override
    public boolean disburseLoan(String userEmail, BigDecimal amount, String description) {
        log.info("Mock BMS: Disbursing loan amount {} to user {} for {}", amount, userEmail, description);
        // In a real implementation, this would call the actual BMS API to transfer funds
        // For now, we'll just simulate a successful disbursement
        return true;
    }

    @Override
    public boolean processPayment(String userEmail, BigDecimal amount, String description) {
        log.info("Mock BMS: Processing payment of {} from user {} for {}", amount, userEmail, description);
        // In a real implementation, this would call the actual BMS API to process payment
        // For now, we'll just simulate a successful payment
        return true;
    }
}
