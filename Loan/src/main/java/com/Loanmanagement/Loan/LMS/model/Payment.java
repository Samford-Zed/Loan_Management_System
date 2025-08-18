package com.Loanmanagement.Loan.LMS.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private BigDecimal amount;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal penaltyAmount;
    private LocalDateTime paymentDate;  // Changed from repaymentDate to match builder
    private String status; // SUCCESS, FAILED, PENDING
    private boolean isLatePayment;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @ManyToOne
    @JoinColumn(name = "bank_account_id")
    private BankAccount paymentAccount;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Custom builder to handle all fields
    public static PaymentBuilder builder() {
        return new PaymentBuilder();
    }

    public static class PaymentBuilder {
        private Integer id;
        private BigDecimal amount;
        private BigDecimal principalAmount;
        private BigDecimal interestAmount;
        private BigDecimal penaltyAmount;
        private LocalDateTime paymentDate;
        private String status;
        private boolean isLatePayment;
        private Loan loan;
        private BankAccount paymentAccount;
        private User user;

        public PaymentBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        public PaymentBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public PaymentBuilder principalAmount(BigDecimal principalAmount) {
            this.principalAmount = principalAmount;
            return this;
        }

        public PaymentBuilder interestAmount(BigDecimal interestAmount) {
            this.interestAmount = interestAmount;
            return this;
        }

        public PaymentBuilder penaltyAmount(BigDecimal penaltyAmount) {
            this.penaltyAmount = penaltyAmount;
            return this;
        }

        public PaymentBuilder paymentDate(LocalDateTime paymentDate) {
            this.paymentDate = paymentDate;
            return this;
        }

        public PaymentBuilder status(String status) {
            this.status = status;
            return this;
        }

        public PaymentBuilder isLatePayment(boolean isLatePayment) {
            this.isLatePayment = isLatePayment;
            return this;
        }

        public PaymentBuilder loan(Loan loan) {
            this.loan = loan;
            return this;
        }

        public PaymentBuilder paymentAccount(BankAccount paymentAccount) {
            this.paymentAccount = paymentAccount;
            return this;
        }

        public PaymentBuilder user(User user) {
            this.user = user;
            return this;
        }

        public Payment build() {
            Payment payment = new Payment();
            payment.setId(id);
            payment.setAmount(amount);
            payment.setPrincipalAmount(principalAmount);
            payment.setInterestAmount(interestAmount);
            payment.setPenaltyAmount(penaltyAmount);
            payment.setPaymentDate(paymentDate);
            payment.setStatus(status);
            payment.setLatePayment(isLatePayment);
            payment.setLoan(loan);
            payment.setPaymentAccount(paymentAccount);
            payment.setUser(user);
            return payment;
        }
    }
}