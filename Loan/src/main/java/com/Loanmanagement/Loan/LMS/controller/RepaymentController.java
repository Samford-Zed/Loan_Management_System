package com.Loanmanagement.Loan.LMS.controller;

import com.Loanmanagement.Loan.LMS.dto.RepaymentRequest;
import com.Loanmanagement.Loan.LMS.model.Repayment;
import com.Loanmanagement.Loan.LMS.service.RepaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repayments")
public class RepaymentController {

    @Autowired
    private RepaymentService repaymentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Repayment> getAllRepayments() {
        return repaymentService.getAllRepayments();
    }

    @PostMapping(value = "/pay", consumes = "application/json")
    @PreAuthorize("hasRole('USER')")
    public Repayment makePayment(@Valid @RequestBody RepaymentRequest repaymentRequest) {
        return repaymentService.makeRepayment(repaymentRequest);
    }
}