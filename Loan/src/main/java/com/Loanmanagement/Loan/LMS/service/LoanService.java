package com.Loanmanagement.Loan.LMS.service;

import com.Loanmanagement.Loan.LMS.client.BmsClient;
import com.Loanmanagement.Loan.LMS.dto.DisbursementResultDto;
import com.Loanmanagement.Loan.LMS.dto.LoanRequest;
import com.Loanmanagement.Loan.LMS.model.Loan;
import com.Loanmanagement.Loan.LMS.repository.BankAccountRepository;
import com.Loanmanagement.Loan.LMS.repository.LoanRepository;
import jakarta.validation.Valid;
//import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public abstract class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private BmsClient bmsClient; // Inject the client

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public Optional<Loan> getLoanById(Long id) {
        return loanRepository.findById(id);
    }

    public Loan applyForLoan(LoanRequest loanRequest) {
        var bankAccount = bankAccountRepository.findById(loanRequest.getBankAccountId())
                .orElseThrow(() -> new RuntimeException("Bank account not found"));

        if (!bankAccount.isVerified()) {
            throw new IllegalStateException("Bank account is not verified. Please verify your account before applying for a loan.");
        }

        Loan newLoan = new Loan();
        newLoan.setBankAccount(bankAccount);
        newLoan.setPrincipalAmount(loanRequest.getPrincipalAmount());
        newLoan.setLoanTermMonths(loanRequest.getLoanTermMonths());
        newLoan.setLoanPurpose(loanRequest.getLoanPurpose());
        newLoan.setAnnualIncome(loanRequest.getAnnualIncome());
        newLoan.setStatus(Loan.LoanStatus.PENDING);
        newLoan.setOutstandingBalance(loanRequest.getPrincipalAmount());

        return loanRepository.save(newLoan);
    }

    public abstract Loan applyForLoan(LoanRequest loanRequest,
                                      MultipartFile licenseFile,
                                      MultipartFile homePhotoFile,
                                      String userEmail);

    public Loan approveLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        loan.setStatus(Loan.LoanStatus.APPROVED);
        loan.setDisbursedAt(LocalDateTime.now());

        // Call the BMS to disburse the funds
        bmsClient.disburseLoan(loan);

        return loanRepository.save(loan);
    }

    public Loan rejectLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        loan.setStatus(Loan.LoanStatus.REJECTED);
        return loanRepository.save(loan);
    }

    /*
    // Temporarily commented - file upload method for future use
    public Loan applyForLoan(@Valid LoanRequest loanRequest, MultipartFile licenseFile,
    MultipartFile homePhotoFile, String userEmail) {
        // Implementation deferred until file uploading is enabled
        return null;
    }
     */

    public abstract void processDisbursementResult(DisbursementResultDto result);
}