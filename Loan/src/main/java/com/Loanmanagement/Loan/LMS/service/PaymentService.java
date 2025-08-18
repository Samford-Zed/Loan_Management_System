package com.Loanmanagement.Loan.LMS.service;
import com.Loanmanagement.Loan.LMS.dto.PaymentDto;
import com.Loanmanagement.Loan.LMS.dto.RepaymentScheduleDto;
import com.Loanmanagement.Loan.LMS.exception.*;
import com.Loanmanagement.Loan.LMS.integration.BmsClient;
import com.Loanmanagement.Loan.LMS.model.*;
import com.Loanmanagement.Loan.LMS.repository.BankAccountRepository;
import com.Loanmanagement.Loan.LMS.repository.LoanRepository;
import com.Loanmanagement.Loan.LMS.repository.PaymentRepository;
import com.Loanmanagement.Loan.LMS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final BigDecimal LATE_PAYMENT_PENALTY_RATE = BigDecimal.valueOf(0.02); // 2% penalty per month
    private static final int GRACE_PERIOD_DAYS = 5; // 5-day grace period before penalties apply
    private static final BigDecimal MIN_PENALTY = BigDecimal.valueOf(100); // Minimum penalty amount
    private static final BigDecimal MAX_PENALTY = BigDecimal.valueOf(5000); // Maximum penalty amount

    private final PaymentRepository paymentRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BmsClient bmsClient;

    public Payment processPayment(String userEmail, PaymentDto paymentDto) {
        // Validate user and loan ownership
        User user = validateUserAndLoanOwnership(userEmail, paymentDto.getLoanId());

        // Get and validate bank account
        BankAccount bankAccount = validateBankAccount(paymentDto.getBankAccountId());

        // Get the loan
        Loan loan = loanRepository.findById(paymentDto.getLoanId().intValue())
                .orElseThrow(() -> new LoanNotFoundException("Loan not found"));

        // Calculate payment components including penalty if late
        PaymentCalculation calculation = calculatePaymentComponents(loan, paymentDto.getAmount());

        // Process payment through BMS
        boolean paymentSuccess = processPaymentThroughBMS(user, calculation.getTotalAmount(), loan.getId());

        // Create and save payment record
        Payment payment = createPaymentRecord(paymentDto, calculation, loan, bankAccount, user, paymentSuccess);

        // Update loan status if payment was successful
        if (paymentSuccess) {
            updateLoanStatus(loan, calculation.getPrincipalAmount());
        }

        return payment;
    }

    public List<Payment> getPaymentHistory(String userEmail, Long loanId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Loan loan = loanRepository.findById(loanId.intValue())
                .orElseThrow(() -> new LoanNotFoundException("Loan not found"));

        validateLoanOwnership(user, loan);

        return paymentRepository.findByLoan(loan);
    }

    public List<RepaymentScheduleDto> getRepaymentSchedule(String userEmail, Long loanId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Loan loan = loanRepository.findById(loanId.intValue())
                .orElseThrow(() -> new LoanNotFoundException("Loan not found"));

        validateLoanOwnership(user, loan);

        return generateRepaymentSchedule(loan);
    }

    // ========== PRIVATE HELPER METHODS ==========

    private User validateUserAndLoanOwnership(String userEmail, Long loanId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Loan loan = loanRepository.findById(loanId.intValue())
                .orElseThrow(() -> new LoanNotFoundException("Loan not found"));

        validateLoanOwnership(user, loan);

        return user;
    }

    private void validateLoanOwnership(User user, Loan loan) {
        if (!loan.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("This loan does not belong to the user");
        }
    }

    private BankAccount validateBankAccount(Long bankAccountId) {
        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId.intValue())
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found"));

        if (!bankAccount.isVerified()) {
            throw new AccountNotVerifiedException("Bank account is not verified");
        }

        return bankAccount;
    }

    private PaymentCalculation calculatePaymentComponents(Loan loan, BigDecimal paymentAmount) {
        BigDecimal monthlyInterestRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);

        // Calculate interest and principal components
        BigDecimal interestAmount = loan.getRemainingAmount().multiply(monthlyInterestRate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal principalAmount = paymentAmount.min(loan.getRemainingAmount().add(interestAmount))
                .subtract(interestAmount)
                .max(BigDecimal.ZERO);

        // Calculate penalty if payment is late
        BigDecimal penaltyAmount = calculateLatePenalty(loan);

        // Ensure total payment covers at least the interest and penalty
        BigDecimal totalAmount = principalAmount.add(interestAmount).add(penaltyAmount);

        return new PaymentCalculation(
                principalAmount,
                interestAmount,
                penaltyAmount,
                totalAmount,
                penaltyAmount.compareTo(BigDecimal.ZERO) > 0
        );
    }

    private BigDecimal calculateLatePenalty(Loan loan) {
        if (LocalDate.now().isAfter(loan.getDueDate().plusDays(GRACE_PERIOD_DAYS))) {
            long daysLate = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());

            // Calculate penalty (2% per month prorated by days late)
            BigDecimal penalty = loan.getRemainingAmount()
                    .multiply(LATE_PAYMENT_PENALTY_RATE)
                    .multiply(BigDecimal.valueOf(daysLate))
                    .divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);

            // Apply minimum and maximum penalty bounds
            penalty = penalty.max(MIN_PENALTY).min(MAX_PENALTY);

            return penalty;
        }
        return BigDecimal.ZERO;
    }

    private boolean processPaymentThroughBMS(User user, BigDecimal amount, Integer loanId) {
        return bmsClient.processPayment(
                user.getEmail(),
                amount,
                "Loan payment for loan ID: " + loanId
        );
    }

    private Payment createPaymentRecord(PaymentDto paymentDto, PaymentCalculation calculation,
                                        Loan loan, BankAccount bankAccount, User user, boolean success) {
        Payment payment = Payment.builder()
                .amount(paymentDto.getAmount())
                .principalAmount(calculation.getPrincipalAmount())
                .interestAmount(calculation.getInterestAmount())
                .penaltyAmount(calculation.getPenaltyAmount())
                .paymentDate(LocalDateTime.now())
                .status(success ? "SUCCESS" : "FAILED")
                .isLatePayment(calculation.isLate())
                .loan(loan)
                .paymentAccount(bankAccount)
                .user(user)
                .build();

        return paymentRepository.save(payment);
    }

    private void updateLoanStatus(Loan loan, BigDecimal principalPaid) {
        loan.setRemainingAmount(loan.getRemainingAmount().subtract(principalPaid));

        if (loan.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus("PAID");
        } else if (LocalDate.now().isAfter(loan.getDueDate())) {
            loan.setStatus("DEFAULTED");
        }

        loanRepository.save(loan);
    }

    private BigDecimal calculateEMI(BigDecimal principal, BigDecimal monthlyInterestRate, int termInMonths) {
        BigDecimal factor = BigDecimal.ONE.add(monthlyInterestRate).pow(termInMonths);
        return principal.multiply(monthlyInterestRate)
                .multiply(factor)
                .divide(factor.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
    }

    private List<RepaymentScheduleDto> generateRepaymentSchedule(Loan loan) {
        List<RepaymentScheduleDto> schedule = new ArrayList<>();
        BigDecimal monthlyInterestRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);

        BigDecimal emi = calculateEMI(loan.getAmount(), monthlyInterestRate, loan.getTermInMonths());
        BigDecimal remainingPrincipal = loan.getAmount();

        for (int i = 1; i <= loan.getTermInMonths(); i++) {
            LocalDate dueDate = loan.getStartDate().plusMonths(i);
            boolean isOverdue = LocalDate.now().isAfter(dueDate);

            BigDecimal interest = remainingPrincipal.multiply(monthlyInterestRate)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal principal = emi.subtract(interest);
            BigDecimal penalty = isOverdue ? calculatePenaltyForSchedule(loan, dueDate) : BigDecimal.ZERO;

            if (i == loan.getTermInMonths()) {
                principal = remainingPrincipal;
                emi = principal.add(interest);
            }

            remainingPrincipal = remainingPrincipal.subtract(principal);
            remainingPrincipal = remainingPrincipal.max(BigDecimal.ZERO);

            schedule.add(RepaymentScheduleDto.builder()
                    .installmentNumber(i)
                    .dueDate(dueDate)
                    .emiAmount(emi)
                    .principalAmount(principal)
                    .interestAmount(interest)
                    .penaltyAmount(penalty)
                    .remainingPrincipal(remainingPrincipal)
                    .status(getPaymentStatus(loan, dueDate))
                    .isLate(isOverdue)
                    .build());
        }

        return schedule;
    }

    private BigDecimal calculatePenaltyForSchedule(Loan loan, LocalDate dueDate) {
        long daysLate = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        if (daysLate > GRACE_PERIOD_DAYS) {
            BigDecimal penalty = loan.getAmount()
                    .multiply(LATE_PAYMENT_PENALTY_RATE)
                    .multiply(BigDecimal.valueOf(daysLate))
                    .divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
            return penalty.max(MIN_PENALTY).min(MAX_PENALTY);
        }
        return BigDecimal.ZERO;
    }

    private String getPaymentStatus(Loan loan, LocalDate dueDate) {
        // Check if payment exists for this due date
        List<Payment> payments = paymentRepository.findByLoan(loan);
        for (Payment payment : payments) {
            if (payment.getPaymentDate().toLocalDate().isEqual(dueDate) ||
                    payment.getPaymentDate().toLocalDate().isAfter(dueDate)) {
                return payment.getStatus();
            }
        }

        if (LocalDate.now().isAfter(dueDate)) {
            return "OVERDUE";
        }
        return "PENDING";
    }

    // Helper class for payment calculation results
    private static class PaymentCalculation {
        private final BigDecimal principalAmount;
        private final BigDecimal interestAmount;
        private final BigDecimal penaltyAmount;
        private final BigDecimal totalAmount;
        private final boolean isLate;

        public PaymentCalculation(BigDecimal principalAmount, BigDecimal interestAmount,
                                  BigDecimal penaltyAmount, BigDecimal totalAmount, boolean isLate) {
            this.principalAmount = principalAmount;
            this.interestAmount = interestAmount;
            this.penaltyAmount = penaltyAmount;
            this.totalAmount = totalAmount;
            this.isLate = isLate;
        }

        public BigDecimal getPrincipalAmount() { return principalAmount; }
        public BigDecimal getInterestAmount() { return interestAmount; }
        public BigDecimal getPenaltyAmount() { return penaltyAmount; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public boolean isLate() { return isLate; }
    }
}