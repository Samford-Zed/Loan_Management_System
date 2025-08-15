package com.Loanmanagement.Loan.LMS.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;
    private String bankName;
    private String branchName;
    private String accountHolderName;
    private boolean verified;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Micro-deposit amounts sent during verification
    private BigDecimal microDeposit1;

    public BankAccount() {
    }
}