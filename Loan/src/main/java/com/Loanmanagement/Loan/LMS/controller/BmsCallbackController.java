package com.Loanmanagement.Loan.LMS.controller;

import com.Loanmanagement.Loan.LMS.dto.DisbursementResultDto;
import com.Loanmanagement.Loan.LMS.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bms/callback")
public class BmsCallbackController {

    @Autowired
    private LoanService loanService;

    @PostMapping("/disbursementResult")
    public ResponseEntity<Void> handleDisbursementResult(@RequestBody DisbursementResultDto result) {
        loanService.processDisbursementResult(result);
        return ResponseEntity.ok().build();
    }
}