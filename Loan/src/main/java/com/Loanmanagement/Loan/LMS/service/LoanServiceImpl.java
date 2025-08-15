package com.Loanmanagement.Loan.LMS.service;

import com.Loanmanagement.Loan.LMS.client.BmsClient;
import com.Loanmanagement.Loan.LMS.dto.DisbursementResultDto;
import com.Loanmanagement.Loan.LMS.dto.LoanRequest;
import com.Loanmanagement.Loan.LMS.model.BankAccount;
import com.Loanmanagement.Loan.LMS.model.Loan;
import com.Loanmanagement.Loan.LMS.model.User;
import com.Loanmanagement.Loan.LMS.repository.BankAccountRepository;
import com.Loanmanagement.Loan.LMS.repository.LoanRepository;
import com.Loanmanagement.Loan.LMS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LoanServiceImpl extends LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanServiceImpl.class);

    private static final String UPLOAD_DIR = "uploads/";

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private BmsClient bmsClient;

    // Other injected beans...

    @Override
    public Loan approveLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus(Loan.LoanStatus.APPROVED);
        loanRepository.save(loan);

        try {
            // Call BMS to disburse funds
            bmsClient.disburseLoan(loan);

            // Record disbursement time on success
            loan.setDisbursedAt(LocalDateTime.now());
            loanRepository.save(loan);

        } catch (Exception e) {
            // Rollback to pending or handle failure
            loan.setStatus(Loan.LoanStatus.PENDING);
            loanRepository.save(loan);

            throw new RuntimeException("Failed to disburse loan funds: " + e.getMessage(), e);
        }

        return loan;
    }

    @Override
    public Loan applyForLoan(LoanRequest loanRequest, MultipartFile licenseFile, MultipartFile homePhotoFile, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount bankAccount = bankAccountRepository.findById(loanRequest.getBankAccountId())
                .orElseThrow(() -> new RuntimeException("Bank account not found"));

        if (!bankAccount.isVerified()) {
            throw new RuntimeException("Bank account is not verified");
        }

        // Save uploaded files and store file names
        String licenseFilename = saveFile(licenseFile);
        String homePhotoFilename = saveFile(homePhotoFile);

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setBankAccount(bankAccount);
        loan.setPrincipalAmount(loanRequest.getPrincipalAmount());
        loan.setLoanTermMonths(loanRequest.getLoanTermMonths());
        loan.setLoanPurpose(loanRequest.getLoanPurpose());
        loan.setAnnualIncome(loanRequest.getAnnualIncome());
        loan.setStatus(Loan.LoanStatus.PENDING);
        loan.setOutstandingBalance(loanRequest.getPrincipalAmount());
        loan.setLicenseDocumentFilename(licenseFilename);
        loan.setHomePhotoFilename(homePhotoFilename);

        return loanRepository.save(loan);
    }

    private String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            String originalFilename = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" + originalFilename;
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.write(filePath, file.getBytes());
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public Loan rejectLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        loan.setStatus(Loan.LoanStatus.REJECTED);
        return loanRepository.save(loan);
    }

    @Override
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    @Override
    public Optional<Loan> getLoanById(Long id) {
        return loanRepository.findById(id);
    }

    @Override
    public void processDisbursementResult(DisbursementResultDto result) {
        Long loanId = result.getLoanId();
        Optional<Loan> optionalLoan = loanRepository.findById(loanId);
        if (optionalLoan.isEmpty()) {
            // Log or handle case where loan is not found
            // For example:
            logger.warn("Disbursement callback received for unknown loan id: {}", loanId);
            return;
        }

        Loan loan = optionalLoan.get();

        // Update loan based on disbursement result
        if ("SUCCESS".equalsIgnoreCase(result.getStatus())) {
            loan.setStatus(Loan.LoanStatus.DISBURSED);
            loan.setDisbursedAt(result.getTimestamp() != null ? result.getTimestamp() : LocalDateTime.now());
            // Optionally clear failure reason if any
            loan.setDisbursementFailureReason(null);
        } else if ("FAILURE".equalsIgnoreCase(result.getStatus())) {
            loan.setStatus(Loan.LoanStatus.DISBURSEMENT_FAILED);
            loan.setDisbursementFailureReason(result.getFailureReason());
            loan.setDisbursedAt(null);
        } else {
            // Optionally handle unexpected status values; for now just log
            logger.warn("Received unknown disbursement status '{}' for loan id {}", result.getStatus(), loanId);
            return;
        }

        loanRepository.save(loan);
        logger.info("Updated loan id {} with disbursement result status '{}'", loanId, result.getStatus());
    }

}