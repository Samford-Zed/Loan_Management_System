package com.Loanmanagement.Loan.LMS.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Loan {

    @Id
    @GeneratedValue
    private Integer id;

    private BigDecimal amount;
    private BigDecimal remainingAmount;
    private BigDecimal emiAmount;  // Added EMI amount field
    private BigDecimal interestRate;
    private Integer termInMonths;
    private LocalDate startDate;
    private LocalDate dueDate;
    private String status; // PENDING, APPROVED, REJECTED, PAID, DEFAULTED

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "loan")
    private List<Payment> payments;

    // Custom builder to calculate EMI
    public static class LoanBuilder {
        public Loan build() {
            Loan loan = new Loan();
            loan.id = this.id;
            loan.amount = this.amount;
            loan.remainingAmount = this.amount; // Initialize remaining amount
            loan.interestRate = this.interestRate;
            loan.termInMonths = this.termInMonths;
            loan.startDate = this.startDate;
            loan.dueDate = this.dueDate;
            loan.status = this.status;
            loan.user = this.user;
            loan.payments = this.payments;

            // Calculate EMI if all required fields are present
            if (loan.amount != null && loan.interestRate != null && loan.termInMonths != null && loan.termInMonths > 0) {
                BigDecimal monthlyInterestRate = loan.interestRate
                        .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                        .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
                loan.emiAmount = calculateEMI(loan.amount, monthlyInterestRate, loan.termInMonths);
            }

            return loan;
        }

        private BigDecimal calculateEMI(BigDecimal principal, BigDecimal monthlyInterestRate, int termInMonths) {
            BigDecimal factor = BigDecimal.ONE.add(monthlyInterestRate).pow(termInMonths);
            return principal.multiply(monthlyInterestRate)
                    .multiply(factor)
                    .divide(factor.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
        }
    }
}