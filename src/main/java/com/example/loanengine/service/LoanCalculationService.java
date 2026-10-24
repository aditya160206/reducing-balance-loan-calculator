package com.example.loanengine.service;

import com.example.loanengine.dto.LoanCalculationRequest;
import com.example.loanengine.dto.LoanCalculationResponse;
import com.example.loanengine.exception.LoanCalculationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class LoanCalculationService {

    private static final int MONEY_SCALE = 2;
    private static final MathContext MATH_CONTEXT = new MathContext(28, RoundingMode.HALF_UP);
    private static final BigDecimal MONTHS_PER_YEAR = BigDecimal.valueOf(12);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public LoanCalculationResponse calculateLoan(LoanCalculationRequest request) {
        if (request == null) {
            throw new LoanCalculationException("Loan request cannot be null");
        }

        BigDecimal principal = requirePositive(request.getPrincipal(), "Principal must be positive");
        BigDecimal annualRate = requireNonNegative(request.getAnnualInterestRate(), "Interest rate cannot be negative");
        int tenureYears = request.getTenureInYears();
        if (tenureYears <= 0) {
            throw new LoanCalculationException("Tenure must be positive");
        }

        String calculationMode = normalizeMode(request.getCalculationMode());
        if (calculationMode == null || "FIXED_RATE".equals(calculationMode)) {
            return calculateFixedRateLoan(principal, annualRate, tenureYears, request);
        }
        if ("FLOATING_RATE".equals(calculationMode)) {
            return calculateFloatingRateLoan(request, principal, annualRate, tenureYears);
        }

        return calculateFixedRateLoan(principal, annualRate, tenureYears, request);
    }

    public LoanCalculationResponse calculateLoanEMI(LoanCalculationRequest request) {
        return calculateLoan(request);
    }

    public LoanCalculationResponse calculateLoan(BigDecimal principal, BigDecimal annualRate, int tenureYears) {
        principal = requirePositive(principal, "Principal must be positive");
        annualRate = requireNonNegative(annualRate, "Interest rate cannot be negative");
        if (tenureYears <= 0) {
            throw new LoanCalculationException("Tenure must be positive");
        }

        int numberOfPayments = tenureYears * 12;
        BigDecimal monthlyRate = monthlyRate(annualRate);
        BigDecimal emi = calculateEMI(principal, monthlyRate, numberOfPayments);
        List<LoanCalculationResponse.AmortizationSchedule> schedule = generateAmortizationSchedule(principal, emi, monthlyRate, numberOfPayments, annualRate);
        BigDecimal totalAmountPaid = monthlyRate.compareTo(BigDecimal.ZERO) == 0
                ? principal.setScale(MONEY_SCALE, RoundingMode.HALF_UP)
                : emi.multiply(BigDecimal.valueOf(numberOfPayments), MATH_CONTEXT).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal totalInterestPaid = totalAmountPaid.subtract(principal).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        LoanCalculationResponse response = new LoanCalculationResponse(emi, totalAmountPaid, totalInterestPaid, schedule);
        response.setTotalPrincipalPaid(principal.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        response.setRemainingBalance(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        return response;
    }

    private LoanCalculationResponse calculateFixedRateLoan(BigDecimal principal, BigDecimal annualRate, int tenureYears, LoanCalculationRequest request) {
        int numberOfPayments = tenureYears * 12;
        BigDecimal monthlyRate = monthlyRate(annualRate);
        BigDecimal emi = calculateEMI(principal, monthlyRate, numberOfPayments);
        List<LoanCalculationResponse.AmortizationSchedule> schedule = generateAmortizationSchedule(
                principal,
                emi,
                monthlyRate,
                numberOfPayments,
                annualRate,
                request
        );

        BigDecimal totalAmountPaid = schedule.stream()
                .map(row -> row.getEmi().add(row.getAdditionalPrincipalPayment() == null ? BigDecimal.ZERO : row.getAdditionalPrincipalPayment()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal totalInterestPaid = schedule.stream()
                .map(LoanCalculationResponse.AmortizationSchedule::getInterest)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal remainingBalance = schedule.isEmpty() ? principal : schedule.get(schedule.size() - 1).getRemainingBalance();

        LoanCalculationResponse response = new LoanCalculationResponse(emi, totalAmountPaid, totalInterestPaid, schedule);
        response.setTotalPrincipalPaid(principal.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        response.setRemainingBalance(remainingBalance.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        return response;
    }

    private LoanCalculationResponse calculateFloatingRateLoan(LoanCalculationRequest request, BigDecimal principal, BigDecimal annualRate, int tenureYears) {
        int totalMonths = tenureYears * 12;
        BigDecimal emi = calculateEMI(principal, monthlyRate(annualRate), totalMonths);
        String rateChangeMode = request.getRateChangeMode() == null ? "EMI_UNCHANGED" : request.getRateChangeMode();
        List<LoanCalculationRequest.RateChange> rateChanges = request.getRateChanges() == null ? new ArrayList<>() : request.getRateChanges();
        rateChanges.sort(Comparator.comparingInt(LoanCalculationRequest.RateChange::getEffectiveMonth));

        BigDecimal remainingBalance = principal;
        BigDecimal totalInterestPaid = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalAdditionalPrincipal = BigDecimal.ZERO;
        List<LoanCalculationResponse.AmortizationSchedule> schedule = new ArrayList<>();

        for (int month = 1; month <= totalMonths; month++) {
            if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal effectiveRate = effectiveAnnualRateForMonth(annualRate, rateChanges, month);
            BigDecimal monthlyRate = monthlyRate(effectiveRate);
            BigDecimal interest = remainingBalance.multiply(monthlyRate).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            BigDecimal currentEmi = "TENURE_UNCHANGED".equalsIgnoreCase(rateChangeMode)
                    ? calculateEMI(remainingBalance, monthlyRate, totalMonths - month + 1)
                    : emi;

            if (month > 1 && "TENURE_UNCHANGED".equalsIgnoreCase(rateChangeMode)) {
                emi = currentEmi;
            }

            BigDecimal principalComponent = currentEmi.subtract(interest).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            if (principalComponent.compareTo(remainingBalance) > 0) {
                principalComponent = remainingBalance;
            }

            BigDecimal additionalPrincipal = getAdditionalPrincipalForMonth(request, month);
            if (month == totalMonths) {
                BigDecimal requiredPrincipal = remainingBalance.subtract(additionalPrincipal);
                if (requiredPrincipal.compareTo(BigDecimal.ZERO) < 0) {
                    requiredPrincipal = BigDecimal.ZERO;
                }
                principalComponent = requiredPrincipal;
            }

            BigDecimal totalPrincipalPaidThisMonth = principalComponent.add(additionalPrincipal).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            if (totalPrincipalPaidThisMonth.compareTo(remainingBalance) > 0) {
                totalPrincipalPaidThisMonth = remainingBalance;
            }

            remainingBalance = remainingBalance.subtract(totalPrincipalPaidThisMonth).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            totalInterestPaid = totalInterestPaid.add(interest).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            totalPaid = totalPaid.add(currentEmi).add(additionalPrincipal).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            totalAdditionalPrincipal = totalAdditionalPrincipal.add(additionalPrincipal).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            schedule.add(new LoanCalculationResponse.AmortizationSchedule(
                    month,
                    month == 1 ? principal : schedule.get(schedule.size() - 1).getRemainingBalance(),
                    effectiveRate,
                    interest,
                    currentEmi,
                    principalComponent,
                    additionalPrincipal,
                    remainingBalance
            ));
        }

        LoanCalculationResponse response = new LoanCalculationResponse(
                emi,
                totalPaid.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                totalInterestPaid.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                schedule
        );
        response.setTotalPrincipalPaid(principal.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        response.setRemainingBalance(remainingBalance.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        return response;
    }

    private BigDecimal calculateEMI(BigDecimal principal, BigDecimal monthlyRate, int numberOfPayments) {
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(numberOfPayments), MONEY_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal power = onePlusRate.pow(numberOfPayments);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(power);
        BigDecimal denominator = power.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private List<LoanCalculationResponse.AmortizationSchedule> generateAmortizationSchedule(BigDecimal principal, BigDecimal emi, BigDecimal monthlyRate, int numberOfPayments, BigDecimal annualRate) {
        return generateAmortizationSchedule(principal, emi, monthlyRate, numberOfPayments, annualRate, null);
    }

    private List<LoanCalculationResponse.AmortizationSchedule> generateAmortizationSchedule(BigDecimal principal, BigDecimal emi, BigDecimal monthlyRate, int numberOfPayments,
                                                                                           BigDecimal annualRate, LoanCalculationRequest request) {
        List<LoanCalculationResponse.AmortizationSchedule> schedule = new ArrayList<>();
        BigDecimal remainingBalance = principal;

        for (int month = 1; month <= numberOfPayments; month++) {
            BigDecimal openingBalance = remainingBalance;
            BigDecimal interestPayment = remainingBalance.multiply(monthlyRate).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            BigDecimal principalPayment = emi.subtract(interestPayment).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            BigDecimal additionalPrincipal = request == null ? BigDecimal.ZERO : getAdditionalPrincipalForMonth(request, month);

            if (month == numberOfPayments) {
                principalPayment = remainingBalance.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
                if (principalPayment.add(additionalPrincipal).compareTo(remainingBalance) > 0) {
                    additionalPrincipal = remainingBalance.subtract(principalPayment).max(BigDecimal.ZERO);
                }
            } else if (principalPayment.compareTo(remainingBalance) > 0) {
                principalPayment = remainingBalance;
            }

            BigDecimal totalPrincipalRepayment = principalPayment.add(additionalPrincipal).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            if (totalPrincipalRepayment.compareTo(remainingBalance) > 0) {
                totalPrincipalRepayment = remainingBalance;
            }

            remainingBalance = remainingBalance.subtract(totalPrincipalRepayment).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            schedule.add(new LoanCalculationResponse.AmortizationSchedule(
                    month,
                    openingBalance,
                    annualRate,
                    interestPayment,
                    emi,
                    principalPayment,
                    additionalPrincipal,
                    remainingBalance
            ));

            if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
        }

        return schedule;
    }

    private BigDecimal monthlyRate(BigDecimal annualInterestRate) {
        if (annualInterestRate == null) {
            return BigDecimal.ZERO;
        }
        if (annualInterestRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return annualInterestRate.divide(HUNDRED, MATH_CONTEXT)
                .divide(MONTHS_PER_YEAR, MATH_CONTEXT);
    }

    private BigDecimal effectiveAnnualRateForMonth(BigDecimal defaultRate, List<LoanCalculationRequest.RateChange> changes, int month) {
        BigDecimal applicableRate = defaultRate;
        for (LoanCalculationRequest.RateChange change : changes) {
            if (change != null && change.getEffectiveMonth() != null && change.getEffectiveMonth() <= month) {
                if (change.getAnnualInterestRate() != null) {
                    applicableRate = change.getAnnualInterestRate();
                }
            }
        }
        return applicableRate;
    }

    private BigDecimal getAdditionalPrincipalForMonth(LoanCalculationRequest request, int month) {
        if (request == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal fixedRecurring = request.getAdditionalPrincipalPayment();
        BigDecimal recurringAmount = fixedRecurring == null ? BigDecimal.ZERO : fixedRecurring;
        if (recurringAmount.compareTo(BigDecimal.ZERO) > 0) {
            Integer everyMonths = request.getAdditionalPrincipalEveryMonths() == null ? 1 : request.getAdditionalPrincipalEveryMonths();
            if (everyMonths <= 0) {
                everyMonths = 1;
            }
            if (month % everyMonths == 0) {
                return recurringAmount;
            }
        }

        for (LoanCalculationRequest.AdditionalPrincipalPayment payment : request.getAdditionalPrincipalPayments()) {
            if (payment == null || payment.getPaymentMonth() == null || payment.getAmount() == null) {
                continue;
            }
            if (payment.getPaymentMonth().equals(month)) {
                return payment.getAmount();
            }
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal requirePositive(BigDecimal value, String message) {
        if (value == null) {
            throw new LoanCalculationException(message);
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new LoanCalculationException(message);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal requireNonNegative(BigDecimal value, String message) {
        if (value == null) {
            throw new LoanCalculationException(message);
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new LoanCalculationException(message);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeMode(String requestedMode) {
        if (requestedMode == null || requestedMode.isBlank()) {
            return "FIXED_RATE";
        }
        String trimmed = requestedMode.trim();
        if (trimmed.equalsIgnoreCase("FIXED") || trimmed.equalsIgnoreCase("FIXED_RATE") || trimmed.equalsIgnoreCase("MONTHLY_REDUCING_BALANCE")) {
            return "FIXED_RATE";
        }
        if (trimmed.equalsIgnoreCase("FLOATING") || trimmed.equalsIgnoreCase("FLOATING_RATE")) {
            return "FLOATING_RATE";
        }
        return trimmed.toUpperCase();
    }
}