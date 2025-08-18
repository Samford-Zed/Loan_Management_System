package com.Loanmanagement.Loan.LMS.controller;

import com.Loanmanagement.Loan.LMS.dto.PaymentDto;
import com.Loanmanagement.Loan.LMS.dto.RepaymentScheduleDto;
import com.Loanmanagement.Loan.LMS.model.Payment;
import com.Loanmanagement.Loan.LMS.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Payment> makePayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PaymentDto paymentDto
    ) {
        return ResponseEntity.ok(paymentService.processPayment(userDetails.getUsername(), paymentDto));
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<Payment>> getPaymentHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long loanId
    ) {
        return ResponseEntity.ok(paymentService.getPaymentHistory(userDetails.getUsername(), loanId));
    }

    @GetMapping("/loan/{loanId}/schedule")
    public ResponseEntity<List<RepaymentScheduleDto>> getRepaymentSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long loanId
    ) {
        return ResponseEntity.ok(paymentService.getRepaymentSchedule(userDetails.getUsername(), loanId));
    }
}