package com.Loanmanagement.Loan.LMS.controller;

import com.Loanmanagement.Loan.LMS.dto.LoanApplicationDto;
import com.Loanmanagement.Loan.LMS.dto.LoanDto;
import com.Loanmanagement.Loan.LMS.dto.PaymentDto;
import com.Loanmanagement.Loan.LMS.model.Payment;
import com.Loanmanagement.Loan.LMS.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/apply")
    public ResponseEntity<LoanDto> applyForLoan(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody LoanApplicationDto loanApplicationDto
    ) {
        return ResponseEntity.ok(loanService.applyForLoan(userDetails.getUsername(), loanApplicationDto));
    }

    @GetMapping
    public ResponseEntity<List<LoanDto>> getUserLoans(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(loanService.getUserLoans(userDetails.getUsername()));
    }

    @PostMapping("/pay")
    public ResponseEntity<Payment> makePayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PaymentDto paymentDto
    ) {
        return ResponseEntity.ok(loanService.makePayment(userDetails.getUsername(), paymentDto));
    }
}

@RestController
@RequestMapping("/api/admin/loans")
@RequiredArgsConstructor
class AdminLoanController {

    private final LoanService loanService;

    @GetMapping("/pending")
    public ResponseEntity<List<LoanDto>> getPendingLoans() {
        return ResponseEntity.ok(loanService.getPendingLoans());
    }

    @PostMapping("/{loanId}/approve")
    public ResponseEntity<LoanDto> approveLoan(@PathVariable Integer loanId) {
        return ResponseEntity.ok(loanService.approveLoan(loanId));
    }

    @PostMapping("/{loanId}/reject")
    public ResponseEntity<LoanDto> rejectLoan(@PathVariable Integer loanId) {
        return ResponseEntity.ok(loanService.rejectLoan(loanId));
    }
}