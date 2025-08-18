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
public class PaymentDto {
    private Long loanId;
    private BigDecimal amount;
    private Long paymentAccountId;
    private Long userId;
    private Long bankAccountId;
    private BigDecimal principalAmount;
    private int loanTermMonths;
}
