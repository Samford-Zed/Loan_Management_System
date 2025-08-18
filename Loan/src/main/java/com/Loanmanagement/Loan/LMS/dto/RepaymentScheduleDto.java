package com.Loanmanagement.Loan.LMS.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepaymentScheduleDto {
    private int installmentNumber;
    private LocalDate dueDate;
    private BigDecimal emiAmount;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal remainingPrincipal;
    private String status; // PENDING, PAID, OVERDUE
    private boolean isLate;
}