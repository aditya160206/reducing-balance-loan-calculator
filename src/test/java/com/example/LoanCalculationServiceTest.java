import com.example.loanengine.dto.LoanCalculationResponse;
import com.example.loanengine.exception.LoanCalculationException;
import com.example.loanengine.service.LoanCalculationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoanCalculationServiceTest {

    private final LoanCalculationService loanCalculationService = new LoanCalculationService();

    @Test
    void testCalculateLoanWithZeroInterest() {
        BigDecimal principal = BigDecimal.valueOf(10000);
        BigDecimal interestRate = BigDecimal.ZERO;
        int tenureYears = 5;

        LoanCalculationResponse response = loanCalculationService.calculateLoan(principal, interestRate, tenureYears);

        assertEquals(principal.divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP), response.getMonthlyEMI());
        assertEquals(new BigDecimal("10000.00"), response.getTotalAmountPaid().setScale(2));
        assertEquals(BigDecimal.ZERO.setScale(2), response.getTotalInterestPaid().setScale(2));
        assertEquals(60, response.getAmortizationSchedule().size());
    }

    @Test
    void testCalculateLoanWithNormalInterest() {
        BigDecimal principal = BigDecimal.valueOf(10000);
        BigDecimal interestRate = BigDecimal.valueOf(5);
        int tenureYears = 5;

        LoanCalculationResponse response = loanCalculationService.calculateLoan(principal, interestRate, tenureYears);

        assertNotNull(response);
        assertTrue(response.getMonthlyEMI().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(response.getTotalAmountPaid().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(response.getTotalInterestPaid().compareTo(BigDecimal.ZERO) >= 0);
        assertEquals(60, response.getAmortizationSchedule().size());
    }

    @Test
    void testCalculateLoanWithInvalidInput() {
        BigDecimal principal = BigDecimal.valueOf(-10000);
        BigDecimal interestRate = BigDecimal.valueOf(5);
        int tenureYears = 5;

        LoanCalculationException exception = assertThrows(LoanCalculationException.class, () -> {
            loanCalculationService.calculateLoan(principal, interestRate, tenureYears);
        });

        assertEquals("Principal must be positive", exception.getMessage());
    }

    @Test
    void testFinalOutstandingBalance() {
        BigDecimal principal = BigDecimal.valueOf(10000);
        BigDecimal interestRate = BigDecimal.valueOf(5);
        int tenureYears = 5;

        LoanCalculationResponse response = loanCalculationService.calculateLoan(principal, interestRate, tenureYears);
        List<LoanCalculationResponse.AmortizationSchedule> schedule = response.getAmortizationSchedule();

        BigDecimal totalPaid = schedule.stream()
                .map(LoanCalculationResponse.AmortizationSchedule::getPrincipalRepaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(principal.setScale(2), totalPaid.setScale(2));
    }
}