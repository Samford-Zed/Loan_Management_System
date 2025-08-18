package com.Loanmanagement.Loan.LMS.service;
import com.Loanmanagement.Loan.LMS.config.JwtService;
import com.Loanmanagement.Loan.LMS.dto.AuthRequest;
import com.Loanmanagement.Loan.LMS.dto.AuthResponse;
import com.Loanmanagement.Loan.LMS.dto.RegisterRequest;
import com.Loanmanagement.Loan.LMS.exception.UserAlreadyExistsException;
import com.Loanmanagement.Loan.LMS.model.Role;
import com.Loanmanagement.Loan.LMS.model.User;
import com.Loanmanagement.Loan.LMS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        // Build and save the new user
        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.USER) // Default role for new registrations
                .enabled(true)
                .build();

        userRepository.save(user);

        // For registration, we will not include the token in the response,
        // as requested. The client can prompt for login separately.
        return AuthResponse.builder()
                // .token(token) // Token is intentionally excluded here for registration success
                .message("Registration successful")
                .user(AuthResponse.UserInfo.builder()
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .fullName(user.getFirstname() + " " + user.getLastname())
                        .phone(user.getPhone())
                        .build())
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        // Authenticate the user with provided credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Retrieve the user after successful authentication
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found")); // Should not happen if authentication passed

        // Generate JWT token for the authenticated user
        String jwtToken = jwtService.generateToken(user, user.getRole().name());

        // Return AuthResponse with token and user info for successful login
        return AuthResponse.builder()
                .token(jwtToken) // Token is included for successful authentication (login)
                .message("Login successful")
                .user(AuthResponse.UserInfo.builder()
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .fullName(user.getFirstname() + " " + user.getLastname())
                        .phone(user.getPhone())
                        .build())
                .build();
    }

    public void validateToken(String token) {
        jwtService.validateToken(token);
    }
}
