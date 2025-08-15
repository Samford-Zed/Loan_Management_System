package com.Loanmanagement.Loan.LMS.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanRequest {
    private Long bankAccountId;
    private BigDecimal principalAmount;
    private int loanTermMonths;
    private String loanPurpose;
    private BigDecimal annualIncome;
}