package com.Loanmanagement.Loan.LMS.controller;
import com.Loanmanagement.Loan.LMS.dto.AuthRequest;
import com.Loanmanagement.Loan.LMS.dto.AuthResponse;
import com.Loanmanagement.Loan.LMS.dto.RegisterRequest;
import com.Loanmanagement.Loan.LMS.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestParam String token) {
        authService.validateToken(token);
        return ResponseEntity.ok("Token is valid");
    }
}