package com.example;

import com.example.loanengine.controller.LoanCalculationController;
import com.example.loanengine.dto.LoanCalculationRequest;
import com.example.loanengine.dto.LoanCalculationResponse;
import com.example.loanengine.exception.LoanCalculationException;
import com.example.loanengine.service.LoanCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoanCalculationControllerTest {

    private MockMvc mockMvc;
    private LoanCalculationService loanCalculationService;

    @BeforeEach
    void setUp() {
        loanCalculationService = Mockito.mock(LoanCalculationService.class);
        LoanCalculationController controller = new LoanCalculationController(loanCalculationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testCalculateLoanEMI_Success() throws Exception {
        LoanCalculationResponse response = new LoanCalculationResponse();
        response.setEmi("1000.00");
        response.setTotalAmountPaid("120000.00");
        response.setTotalInterestPaid("20000.00");
        response.setAmortizationSchedule(List.of(new LoanCalculationResponse.AmortizationSchedule()));

        when(loanCalculationService.calculateLoan(any(LoanCalculationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/loans/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"principal\":100000,\"annualInterestRate\":5,\"tenureInYears\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyEMI").value(1000.00))
                .andExpect(jsonPath("$.totalAmountPaid").value(120000.00))
                .andExpect(jsonPath("$.totalInterestPaid").value(20000.00));
    }

    @Test
    void testCalculateLoanEMI_InvalidInput() throws Exception {
        when(loanCalculationService.calculateLoan(any(LoanCalculationRequest.class)))
                .thenThrow(new LoanCalculationException("Principal must be positive"));

        mockMvc.perform(post("/api/loans/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"principal\":-100000,\"annualInterestRate\":5,\"tenureInYears\":10}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCalculateLoanEMI_ZeroInterest() throws Exception {
        LoanCalculationResponse response = new LoanCalculationResponse();
        response.setEmi("5000.00");
        response.setTotalAmountPaid("60000.00");
        response.setTotalInterestPaid("0.00");
        response.setAmortizationSchedule(List.of(new LoanCalculationResponse.AmortizationSchedule()));

        when(loanCalculationService.calculateLoan(any(LoanCalculationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/loans/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"principal\":60000,\"annualInterestRate\":0,\"tenureInYears\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyEMI").value(5000.00))
                .andExpect(jsonPath("$.totalAmountPaid").value(60000.00))
                .andExpect(jsonPath("$.totalInterestPaid").value(0.00));
    }
}