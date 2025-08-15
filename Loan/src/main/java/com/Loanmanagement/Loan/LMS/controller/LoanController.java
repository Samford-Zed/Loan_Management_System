package com.Loanmanagement.Loan.LMS.controller;

import com.Loanmanagement.Loan.LMS.dto.LoanRequest;
import com.Loanmanagement.Loan.LMS.model.Loan;
import com.Loanmanagement.Loan.LMS.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Loan> getAllLoans() {
        return loanService.getAllLoans();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLoanById(@PathVariable Long id) {
        Optional<Loan> loanOpt = loanService.getLoanById(id);
        if (loanOpt.isPresent()) {
            return ResponseEntity.ok(loanOpt.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Loan not found with id " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping(value = "/apply", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> applyForLoan(@RequestBody @Valid LoanRequest loanRequest) {
        try {
            String userEmail = SecurityContextHolder.getContext()
                    .getAuthentication().getName();
            Loan loan = loanService.applyForLoan(loanRequest);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Loan application failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveLoan(@PathVariable Long id) {
        try {
            Loan loan = loanService.approveLoan(id);
            return ResponseEntity.ok(loan);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Loan approval failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectLoan(@PathVariable Long id) {
        try {
            Loan loan = loanService.rejectLoan(id);
            if (loan == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Loan not found with id " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            return ResponseEntity.ok(loan);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Loan rejection failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}