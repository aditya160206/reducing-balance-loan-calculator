package com.example.loanengine.dto;

import java.math.BigDecimal;
import java.util.List;

public class LoanCalculationResponse {
    private BigDecimal monthlyEMI;
    private BigDecimal totalAmountPaid;
    private BigDecimal totalInterestPaid;
    private BigDecimal totalPrincipalPaid;
    private BigDecimal remainingBalance;
    private List<AmortizationSchedule> amortizationSchedule;

    public LoanCalculationResponse() {
    }

    public LoanCalculationResponse(BigDecimal monthlyEMI, BigDecimal totalAmountPaid, BigDecimal totalInterestPaid, List<AmortizationSchedule> amortizationSchedule) {
        this.monthlyEMI = monthlyEMI;
        this.totalAmountPaid = totalAmountPaid;
        this.totalInterestPaid = totalInterestPaid;
        this.amortizationSchedule = amortizationSchedule;
    }

    public BigDecimal getMonthlyEMI() {
        return monthlyEMI;
    }

    public BigDecimal getEmi() {
        return monthlyEMI;
    }

    public void setMonthlyEMI(BigDecimal monthlyEMI) {
        this.monthlyEMI = monthlyEMI;
    }

    public void setEmi(String emi) {
        this.monthlyEMI = new BigDecimal(emi);
    }

    public void setEmi(BigDecimal emi) {
        this.monthlyEMI = emi;
    }

    public BigDecimal getTotalAmountPaid() {
        return totalAmountPaid;
    }

    public void setTotalAmountPaid(BigDecimal totalAmountPaid) {
        this.totalAmountPaid = totalAmountPaid;
    }

    public void setTotalAmountPaid(String totalAmountPaid) {
        this.totalAmountPaid = new BigDecimal(totalAmountPaid);
    }

    public BigDecimal getTotalInterestPaid() {
        return totalInterestPaid;
    }

    public void setTotalInterestPaid(BigDecimal totalInterestPaid) {
        this.totalInterestPaid = totalInterestPaid;
    }

    public void setTotalInterestPaid(String totalInterestPaid) {
        this.totalInterestPaid = new BigDecimal(totalInterestPaid);
    }

    public BigDecimal getTotalPrincipalPaid() {
        return totalPrincipalPaid;
    }

    public void setTotalPrincipalPaid(BigDecimal totalPrincipalPaid) {
        this.totalPrincipalPaid = totalPrincipalPaid;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public List<AmortizationSchedule> getAmortizationSchedule() {
        return amortizationSchedule;
    }

    public void setAmortizationSchedule(List<AmortizationSchedule> amortizationSchedule) {
        this.amortizationSchedule = amortizationSchedule;
    }

    public static class AmortizationSchedule {
        private int month;
        private BigDecimal openingBalance;
        private BigDecimal annualInterestRate;
        private BigDecimal emi;
        private BigDecimal interest;
        private BigDecimal principalComponent;
        private BigDecimal additionalPrincipalPayment;
        private BigDecimal closingBalance;

        private BigDecimal principalRepaid;
        private BigDecimal remainingBalance;

        public AmortizationSchedule() {
        }

        public AmortizationSchedule(int month, BigDecimal emi, BigDecimal interest, BigDecimal principalRepaid, BigDecimal remainingBalance) {
            this.month = month;
            this.emi = emi;
            this.interest = interest;
            this.principalRepaid = principalRepaid;
            this.remainingBalance = remainingBalance;
            this.closingBalance = remainingBalance;
            this.principalComponent = principalRepaid;
        }

        public AmortizationSchedule(int month, BigDecimal openingBalance, BigDecimal annualInterestRate, BigDecimal interest,
                                   BigDecimal emi, BigDecimal principalComponent, BigDecimal additionalPrincipalPayment,
                                   BigDecimal closingBalance) {
            this.month = month;
            this.openingBalance = openingBalance;
            this.annualInterestRate = annualInterestRate;
            this.interest = interest;
            this.emi = emi;
            this.principalComponent = principalComponent;
            this.additionalPrincipalPayment = additionalPrincipalPayment;
            this.closingBalance = closingBalance;
            this.principalRepaid = principalComponent;
            this.remainingBalance = closingBalance;
        }

        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public BigDecimal getOpeningBalance() {
            return openingBalance;
        }

        public void setOpeningBalance(BigDecimal openingBalance) {
            this.openingBalance = openingBalance;
        }

        public BigDecimal getAnnualInterestRate() {
            return annualInterestRate;
        }

        public void setAnnualInterestRate(BigDecimal annualInterestRate) {
            this.annualInterestRate = annualInterestRate;
        }

        public BigDecimal getEmi() {
            return emi;
        }

        public void setEmi(BigDecimal emi) {
            this.emi = emi;
        }

        public BigDecimal getInterest() {
            return interest;
        }

        public BigDecimal getInterestPayment() {
            return interest;
        }

        public void setInterest(BigDecimal interest) {
            this.interest = interest;
        }

        public void setInterestPayment(BigDecimal interest) {
            this.interest = interest;
        }

        public BigDecimal getPrincipalComponent() {
            return principalComponent;
        }

        public void setPrincipalComponent(BigDecimal principalComponent) {
            this.principalComponent = principalComponent;
            this.principalRepaid = principalComponent;
        }

        public BigDecimal getPrincipalRepaid() {
            return principalRepaid == null ? principalComponent : principalRepaid;
        }

        public BigDecimal getPrincipalPayment() {
            return getPrincipalRepaid();
        }

        public void setPrincipalRepaid(BigDecimal principalRepaid) {
            this.principalRepaid = principalRepaid;
            this.principalComponent = principalRepaid;
        }

        public void setPrincipalPayment(BigDecimal principalRepaid) {
            this.principalRepaid = principalRepaid;
            this.principalComponent = principalRepaid;
        }

        public BigDecimal getAdditionalPrincipalPayment() {
            return additionalPrincipalPayment;
        }

        public void setAdditionalPrincipalPayment(BigDecimal additionalPrincipalPayment) {
            this.additionalPrincipalPayment = additionalPrincipalPayment;
        }

        public BigDecimal getClosingBalance() {
            return closingBalance;
        }

        public void setClosingBalance(BigDecimal closingBalance) {
            this.closingBalance = closingBalance;
            this.remainingBalance = closingBalance;
        }

        public BigDecimal getRemainingBalance() {
            return remainingBalance == null ? closingBalance : remainingBalance;
        }

        public void setRemainingBalance(BigDecimal remainingBalance) {
            this.remainingBalance = remainingBalance;
            this.closingBalance = remainingBalance;
        }
    }
}