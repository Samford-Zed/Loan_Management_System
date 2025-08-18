package com.Loanmanagement.Loan.LMS.service;
import com.Loanmanagement.Loan.LMS.dto.LoanApplicationDto;
import com.Loanmanagement.Loan.LMS.dto.LoanDto;
import com.Loanmanagement.Loan.LMS.dto.PaymentDto;
import com.Loanmanagement.Loan.LMS.exception.AccountNotVerifiedException;
import com.Loanmanagement.Loan.LMS.exception.InsufficientFundsException;
import com.Loanmanagement.Loan.LMS.exception.LoanNotFoundException;
import com.Loanmanagement.Loan.LMS.exception.UserNotFoundException;
import com.Loanmanagement.Loan.LMS.integration.BmsClient;
import com.Loanmanagement.Loan.LMS.model.*;
import com.Loanmanagement.Loan.LMS.repository.LoanRepository;
import com.Loanmanagement.Loan.LMS.repository.PaymentRepository;
import com.Loanmanagement.Loan.LMS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final BankAccountService bankAccountService;
    private final BmsClient bmsClient;
    private final PaymentRepository paymentRepository;

    public LoanDto applyForLoan(String userEmail, LoanApplicationDto loanApplicationDto) {
        if (!bankAccountService.isBankAccountVerified(userEmail)) {
            throw new AccountNotVerifiedException("Bank account not verified");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // In a real system, we would check credit score, income, etc.
        // For now, we'll just create a loan application

        Loan loan = Loan.builder()
                .amount(loanApplicationDto.getAmount())
                .remainingAmount(loanApplicationDto.getAmount())
                .interestRate(BigDecimal.valueOf(0.1)) // 10% interest rate
                .termInMonths(loanApplicationDto.getTermInMonths())
                .startDate(LocalDate.now())
                .dueDate(LocalDate.now().plusMonths(loanApplicationDto.getTermInMonths()))
                .status("PENDING")
                .user(user)
                .build();

        loan = loanRepository.save(loan);

        return mapToLoanDto(loan);
    }

    public List<LoanDto> getUserLoans(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return loanRepository.findByUser(user).stream()
                .map(this::mapToLoanDto)
                .collect(Collectors.toList());
    }

    public List<LoanDto> getPendingLoans() {
        return loanRepository.findByStatus("PENDING").stream()
                .map(this::mapToLoanDto)
                .collect(Collectors.toList());
    }

    public LoanDto approveLoan(Integer loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found"));

        loan.setStatus("APPROVED");
        loan = loanRepository.save(loan);

        // Disburse the loan amount to the user's bank account
        boolean disbursementSuccess = bmsClient.disburseLoan(
                loan.getUser().getEmail(),
                loan.getAmount(),
                "Loan disbursement for loan ID: " + loan.getId()
        );

        if (!disbursementSuccess) {
            throw new RuntimeException("Failed to disburse loan amount");
        }

        return mapToLoanDto(loan);
    }

    public LoanDto rejectLoan(Integer loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found"));

        loan.setStatus("REJECTED");
        loan = loanRepository.save(loan);

        return mapToLoanDto(loan);
    }

    public Payment makePayment(String userEmail, PaymentDto paymentDto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Loan loan = loanRepository.findById(paymentDto.getLoanId().intValue())
                .orElseThrow(() -> new LoanNotFoundException("Loan not found"));

        if (!loan.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("This loan does not belong to the user");
        }

        if (paymentDto.getAmount().compareTo(loan.getRemainingAmount()) > 0) {
            throw new InsufficientFundsException("Payment amount exceeds remaining loan amount");
        }

        // Process payment through BMS
        boolean paymentSuccess = bmsClient.processPayment(
                user.getEmail(),
                paymentDto.getAmount(),
                "Loan payment for loan ID: " + loan.getId()
        );

        Payment payment = Payment.builder()
                .amount(paymentDto.getAmount())
                .paymentDate(LocalDate.now().atStartOfDay())
                .status(paymentSuccess ? "SUCCESS" : "FAILED")
                .loan(loan)
                .build();

        payment = paymentRepository.save(payment);

        if (paymentSuccess) {
            loan.setRemainingAmount(loan.getRemainingAmount().subtract(paymentDto.getAmount()));

            if (loan.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
                loan.setStatus("PAID");
            }

            loanRepository.save(loan);
        }

        return payment;
    }

    private LoanDto mapToLoanDto(Loan loan) {
        return LoanDto.builder()
                .id((long) loan.getId())
                .amount(loan.getAmount())
                .remainingAmount(loan.getRemainingAmount())
                .interestRate(loan.getInterestRate())
                .termInMonths(loan.getTermInMonths())
                .startDate(loan.getStartDate())
                .dueDate(loan.getDueDate())
                .status(loan.getStatus())
                .build();
    }
    public BigDecimal calculateEMI(Loan loan) {
        BigDecimal monthlyInterestRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);

        return calculateEMI(loan.getAmount(), monthlyInterestRate, loan.getTermInMonths());
    }

    private BigDecimal calculateEMI(BigDecimal principal, BigDecimal monthlyInterestRate, int termInMonths) {
        BigDecimal factor = BigDecimal.ONE.add(monthlyInterestRate).pow(termInMonths);
        return principal.multiply(monthlyInterestRate)
                .multiply(factor)
                .divide(factor.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
    }
}