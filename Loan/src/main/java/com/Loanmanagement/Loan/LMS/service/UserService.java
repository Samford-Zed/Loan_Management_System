package com.Loanmanagement.Loan.LMS.service;

import com.Loanmanagement.Loan.LMS.model.User;
import com.Loanmanagement.Loan.LMS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User registerUser(User user) {
        // Encode the raw password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER"); // Default single role as String
        return userRepository.save(user);
    }
}