package com.Loanmanagement.Loan.LMS.client;

import com.Loanmanagement.Loan.LMS.model.BankAccount;
import com.Loanmanagement.Loan.LMS.model.Loan;
import com.Loanmanagement.Loan.LMS.model.Repayment;

public interface BmsClient {
    // Method to ask the BMS to start verifying a new bank account
    void startBankAccountVerification(BankAccount bankAccount);

    // Method to ask the BMS to disburse funds for an approved loan
    void disburseLoan(Loan loan);

    // Method to ask the BMS to process a repayment
    boolean processRepayment(Repayment repayment);
}