package com.example.loanengine.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LoanCalculationRequest {
    private BigDecimal principal;
    @JsonAlias({"annualRate"})
    private BigDecimal annualInterestRate;
    @JsonAlias({"tenure"})
    private int tenureInYears;

    private String calculationMode;
    private String rateChangeMode;
    private List<RateChange> rateChanges = new ArrayList<>();
    private BigDecimal additionalPrincipalPayment;
    private String additionalPrincipalFrequency = "MONTHLY";
    private Integer additionalPrincipalEveryMonths = 1;
    private List<AdditionalPrincipalPayment> additionalPrincipalPayments = new ArrayList<>();

    public LoanCalculationRequest() {
    }

    public LoanCalculationRequest(BigDecimal principal, BigDecimal annualInterestRate, int tenureInYears) {
        this.principal = principal;
        this.annualInterestRate = annualInterestRate;
        this.tenureInYears = tenureInYears;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public void setPrincipal(BigDecimal principal) {
        this.principal = principal;
    }

    public BigDecimal getAnnualInterestRate() {
        return annualInterestRate;
    }

    public BigDecimal getAnnualRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(BigDecimal annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
    }

    public void setAnnualRate(BigDecimal annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
    }

    public int getTenureInYears() {
        return tenureInYears;
    }

    public int getTenure() {
        return tenureInYears;
    }

    public void setTenureInYears(int tenureInYears) {
        this.tenureInYears = tenureInYears;
    }

    public void setTenure(int tenureInYears) {
        this.tenureInYears = tenureInYears;
    }

    public String getCalculationMode() {
        return calculationMode;
    }

    public void setCalculationMode(String calculationMode) {
        this.calculationMode = calculationMode;
    }

    public String getRateChangeMode() {
        return rateChangeMode;
    }

    public void setRateChangeMode(String rateChangeMode) {
        this.rateChangeMode = rateChangeMode;
    }

    public List<RateChange> getRateChanges() {
        return rateChanges;
    }

    public void setRateChanges(List<RateChange> rateChanges) {
        this.rateChanges = rateChanges == null ? new ArrayList<>() : rateChanges;
    }

    public BigDecimal getAdditionalPrincipalPayment() {
        return additionalPrincipalPayment;
    }

    public void setAdditionalPrincipalPayment(BigDecimal additionalPrincipalPayment) {
        this.additionalPrincipalPayment = additionalPrincipalPayment;
    }

    public String getAdditionalPrincipalFrequency() {
        return additionalPrincipalFrequency;
    }

    public void setAdditionalPrincipalFrequency(String additionalPrincipalFrequency) {
        this.additionalPrincipalFrequency = additionalPrincipalFrequency;
    }

    public Integer getAdditionalPrincipalEveryMonths() {
        return additionalPrincipalEveryMonths;
    }

    public void setAdditionalPrincipalEveryMonths(Integer additionalPrincipalEveryMonths) {
        this.additionalPrincipalEveryMonths = additionalPrincipalEveryMonths;
    }

    public List<AdditionalPrincipalPayment> getAdditionalPrincipalPayments() {
        return additionalPrincipalPayments;
    }

    public void setAdditionalPrincipalPayments(List<AdditionalPrincipalPayment> additionalPrincipalPayments) {
        this.additionalPrincipalPayments = additionalPrincipalPayments == null ? new ArrayList<>() : additionalPrincipalPayments;
    }

    public static class RateChange {
        private Integer effectiveMonth;
        private String effectiveDate;
        @JsonAlias({"rate", "annualRate", "interestRate"})
        private BigDecimal annualInterestRate;

        public RateChange() {
        }

        public Integer getEffectiveMonth() {
            return effectiveMonth;
        }

        public void setEffectiveMonth(Integer effectiveMonth) {
            this.effectiveMonth = effectiveMonth;
        }

        public String getEffectiveDate() {
            return effectiveDate;
        }

        public void setEffectiveDate(String effectiveDate) {
            this.effectiveDate = effectiveDate;
        }

        public BigDecimal getAnnualInterestRate() {
            return annualInterestRate;
        }

        public BigDecimal getRate() {
            return annualInterestRate;
        }

        public void setAnnualInterestRate(BigDecimal annualInterestRate) {
            this.annualInterestRate = annualInterestRate;
        }

        public void setRate(BigDecimal annualInterestRate) {
            this.annualInterestRate = annualInterestRate;
        }
    }

    public static class AdditionalPrincipalPayment {
        private Integer paymentMonth;
        private BigDecimal amount;
        private String type;
        private Integer everyMonths;

        public Integer getPaymentMonth() {
            return paymentMonth;
        }

        public void setPaymentMonth(Integer paymentMonth) {
            this.paymentMonth = paymentMonth;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getEveryMonths() {
            return everyMonths;
        }

        public void setEveryMonths(Integer everyMonths) {
            this.everyMonths = everyMonths;
        }
    }
}