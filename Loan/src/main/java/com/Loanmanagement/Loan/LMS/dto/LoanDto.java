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
public class LoanDto {
    private Long id;
    private BigDecimal amount;
    private BigDecimal remainingAmount;
    private BigDecimal interestRate;
    private Integer termInMonths;
    private LocalDate startDate;
    private LocalDate dueDate;
    private String status;
}