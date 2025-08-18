package com.Loanmanagement.Loan.LMS.controller;
import com.Loanmanagement.Loan.LMS.dto.BankAccountDto;
import com.Loanmanagement.Loan.LMS.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/bank-account")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping("/link")
    public ResponseEntity<Void> linkBankAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody BankAccountDto bankAccountDto
    ) {
        bankAccountService.linkBankAccount(userDetails.getUsername(), bankAccountDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/is-verified")
    public ResponseEntity<Boolean> isBankAccountVerified(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bankAccountService.isBankAccountVerified(userDetails.getUsername()));
    }
}