package com.example.loanengine.controller;

import com.example.loanengine.dto.LoanCalculationRequest;
import com.example.loanengine.dto.LoanCalculationResponse;
import com.example.loanengine.exception.LoanCalculationException;
import com.example.loanengine.service.LoanCalculationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
public class LoanCalculationController {

    private final LoanCalculationService loanCalculationService;

    public LoanCalculationController(LoanCalculationService loanCalculationService) {
        this.loanCalculationService = loanCalculationService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<LoanCalculationResponse> calculateLoan(@RequestBody LoanCalculationRequest request) {
        try {
            LoanCalculationResponse response = loanCalculationService.calculateLoan(request);
            return ResponseEntity.ok(response);
        } catch (LoanCalculationException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
