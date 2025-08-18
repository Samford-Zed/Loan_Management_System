package com.Loanmanagement.Loan.LMS.exception;
public class BankAccountNotFoundException extends RuntimeException {
    public BankAccountNotFoundException(String message) {
        super(message);
    }
}