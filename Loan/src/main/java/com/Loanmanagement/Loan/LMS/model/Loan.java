package com.Loanmanagement.Loan.LMS.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Loan {

private String licenseDocumentFilename;
private String homePhotoFilename;
private String disbursementFailureReason;

public void setLicenseDocumentFilename(String licenseFilename) {
    this.licenseDocumentFilename = licenseFilename;
}

public void setHomePhotoFilename(String homePhotoFilename) {
    this.homePhotoFilename = homePhotoFilename;
}

public void setDisbursementFailureReason(String failureReason) {
    this.disbursementFailureReason = failureReason;
}

public enum LoanStatus {
    PENDING,
    APPROVED,
    REJECTED,
    DISBURSED,
    DISBURSEMENT_FAILED,
    PAID_OFF;
}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id") // foreign key column name
    private User user;

    @ManyToOne
    @JoinColumn(name = "bank_account_id") // foreign key column name
    private BankAccount bankAccount;

    private BigDecimal principalAmount;

    private int loanTermMonths;

    private String loanPurpose;

    private BigDecimal annualIncome;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    private BigDecimal outstandingBalance;

    private LocalDateTime disbursedAt;

    // Getters and setters ...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(BigDecimal principalAmount) {
        this.principalAmount = principalAmount;
    }

    public int getLoanTermMonths() {
        return loanTermMonths;
    }

    public void setLoanTermMonths(int loanTermMonths) {
        this.loanTermMonths = loanTermMonths;
    }

    public String getLoanPurpose() {
        return loanPurpose;
    }

    public void setLoanPurpose(String loanPurpose) {
        this.loanPurpose = loanPurpose;
    }

    public BigDecimal getAnnualIncome() {
        return annualIncome;
    }

    public void setAnnualIncome(BigDecimal annualIncome) {
        this.annualIncome = annualIncome;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public LocalDateTime getDisbursedAt() {
        return disbursedAt;
    }

    public void setDisbursedAt(LocalDateTime disbursedAt) {
        this.disbursedAt = disbursedAt;
    }
}