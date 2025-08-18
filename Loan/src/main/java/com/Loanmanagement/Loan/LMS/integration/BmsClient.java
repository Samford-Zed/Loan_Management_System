package com.Loanmanagement.Loan.LMS.integration;

import java.math.BigDecimal;

public interface BmsClient {
    boolean startBankAccountVerification(String accountHolderName, String accountNumber, String bankName, String ifscCode);
    boolean disburseLoan(String userEmail, BigDecimal amount, String description);
    boolean processPayment(String userEmail, BigDecimal amount, String description);
}
