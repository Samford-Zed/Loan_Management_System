package com.Loanmanagement.Loan.LMS.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanApplicationDto {
    private BigDecimal amount;
    private Integer termInMonths;
}