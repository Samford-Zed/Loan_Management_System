package com.Loanmanagement.Loan.LMS.client;

import com.Loanmanagement.Loan.LMS.dto.DisbursementResultDto;
import com.Loanmanagement.Loan.LMS.model.BankAccount;
import com.Loanmanagement.Loan.LMS.model.Loan;
import com.Loanmanagement.Loan.LMS.model.Repayment;
import com.Loanmanagement.Loan.LMS.service.LoanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class BmsClientMockImpl implements BmsClient {

    private static final Logger logger = LoggerFactory.getLogger(BmsClientMockImpl.class);

    private boolean simulateTransferFailure = false;

    @Autowired
    @Lazy
    private LoanService loanService;  // Inject LoanService to simulate callback

    public void setSimulateTransferFailure(boolean simulate) {
        this.simulateTransferFailure = simulate;
    }

    @Override
    public void disburseLoan(Loan loan) {
        logger.info("MOCK BMS CALL: Disbursing loan with ID: {}", loan.getId());

        if (simulateTransferFailure) {
            // Simulate failure by throwing exception
            logger.error("MOCK BMS CALL: Simulated disbursement failure for loan ID: {}", loan.getId());

            // Also simulate callback notifying failure status
            DisbursementResultDto failureResult = new DisbursementResultDto();
            failureResult.setLoanId(loan.getId());
            failureResult.setStatus("FAILURE");
            failureResult.setTimestamp(LocalDateTime.now());
            failureResult.setFailureReason("Simulated BMS transfer failure");

            loanService.processDisbursementResult(failureResult);

            throw new RuntimeException("Simulated BMS transfer failure");
        }

        // Simulate successful disbursement
        logger.info("MOCK BMS CALL: Simulated successful disbursement for loan ID: {}", loan.getId());

        // Simulate asynchronous callback to LMS
        DisbursementResultDto successResult = new DisbursementResultDto();
        successResult.setLoanId(loan.getId());
        successResult.setStatus("SUCCESS");
        successResult.setTimestamp(LocalDateTime.now());

        // Call the LMS service callback method to update loan status, simulating BMS callback
        loanService.processDisbursementResult(successResult);
    }

    @Override
    public void startBankAccountVerification(BankAccount bankAccount) {
        logger.info("MOCK BMS CALL: Initiating verification for account {}", bankAccount.getAccountNumber());

        // Generate a random micro-deposit between 0.01 and 0.99
        double randomAmount = 0.01 + (Math.random() * 0.98);
        BigDecimal deposit1 = BigDecimal.valueOf(randomAmount).setScale(2, BigDecimal.ROUND_HALF_UP);

        bankAccount.setMicroDeposit1(deposit1);
        bankAccount.setVerified(false);

        logger.info("MOCK BMS CALL: Micro deposit sent: {}", deposit1);
    }

    @Override
    public boolean processRepayment(Repayment repayment) {
        logger.info("MOCK BMS CALL: Processing repayment {} of amount {} from account {}",
                repayment.getId(), repayment.getAmount(), repayment.getPaymentAccount().getAccountNumber());
        return true;
    }
}