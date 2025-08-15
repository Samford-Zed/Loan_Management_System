package com.Loanmanagement.Loan.LMS.dto;

import java.math.BigDecimal;

public class BankAccountVerificationRequest {

    private String accountNumber;
    private BigDecimal deposit1;

    public BankAccountVerificationRequest() {
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getDeposit1() {
        return deposit1;
    }

    public void setDeposit1(BigDecimal deposit1) {
        this.deposit1 = deposit1;
    }

}