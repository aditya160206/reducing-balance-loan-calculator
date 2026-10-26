# Loan Mathematics Engine

A loan EMI calculator built with **Java 17, Spring Boot, and plain HTML/CSS/JavaScript**. Enter a principal, annual interest rate, and tenure to calculate the monthly EMI, total repayment, total interest, and a month-by-month amortization schedule.

**Live app:** https://reducing-balance-loan-calculator.onrender.com/  
**Source code:** https://github.com/aditya160206/reducing-balance-loan-calculator

## What it does

- Calculates monthly EMI for a loan using the monthly reducing-balance method.
- Shows total amount paid and total interest over the loan tenure.
- Displays an amortization schedule with opening balance, annual interest rate, interest charged, EMI, principal component, additional principal, and closing balance.
- Shows the EMI formula with the entered values substituted into the calculation.
- Handles a zero-interest loan separately.
- Uses Java `BigDecimal` for monetary calculations and rounds displayed monetary values to two decimal places.

The web form currently asks for principal, annual interest rate, and tenure in years. The backend also contains calculation logic for fixed-rate and floating-rate requests and optional additional-principal payments; those options are not exposed as controls in the current web form.

## How the calculation works

Let:

- `P` = original loan principal
- `R` = annual interest rate, expressed as a percentage
- `r` = monthly interest rate as a decimal
- `n` = number of monthly instalments
- `EMI` = fixed monthly instalment

The monthly rate and number of instalments are:

```text
r = R / 1200
n = tenure in years * 12
```

For a positive interest rate, the EMI formula is:

```text
EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)
```

At 0% interest:

```text
EMI = P / n
```

Each month, the calculator computes interest from the opening balance:

```text
Monthly interest = opening balance * R / 1200
Principal component = EMI - monthly interest
Closing balance = opening balance - principal component - additional principal payment
```

The next month's opening balance is the previous month's closing balance. The final instalment is adjusted in the schedule to clear the remaining principal, subject to the implementation's rounding rules.

## Technology

- **Java 17**
- **Spring Boot 3.3.5**
- **Maven**
- **HTML, CSS, and JavaScript** for the browser interface
- **JUnit 5 / Spring Boot Test** for automated tests
- **Docker** for containerized deployment

## Project structure

```text
src/main/java/com/example/loanengine/
  controller/   REST endpoint
  dto/          Request and response models
  exception/    Loan calculation exception handling
  service/      EMI and amortization calculations
src/main/resources/static/
  index.html    Browser interface
  css/          Styles
src/test/       Automated tests
Dockerfile      Container build and runtime
pom.xml         Maven dependencies and build configuration
```

## Run locally

Prerequisites: JDK 17 and Maven.

1. Clone the repository:

   ```bash
   git clone https://github.com/aditya160206/reducing-balance-loan-calculator.git
   cd reducing-balance-loan-calculator
   ```

2. Start the application:

   ```bash
   mvn spring-boot:run
   ```

3. Open http://localhost:8080 in your browser.

To run the automated tests:

```bash
mvn test
```

## API

The calculator endpoint is:

```http
POST /api/loans/calculate
Content-Type: application/json
```

Example request:

```json
{
  "principal": 6000000,
  "annualInterestRate": 5.17,
  "tenureInYears": 20
}
```

The response includes the monthly EMI, total amount paid, total interest paid, and the amortization schedule. The backend validates that the principal is positive, the annual interest rate is non-negative, and the tenure is positive.

## Deployment

The application is configured for deployment as a **Docker web service on Render**. Spring Boot serves both the static frontend and the `/api/loans/calculate` endpoint from the same application, so the main deployment does not require a separate frontend host.

For Render, create a Web Service from this repository, select Docker, and deploy using the repository-root `Dockerfile`. The service reads the `PORT` environment variable and defaults to port 8080 for local use. The live link above points to the deployed app.

## Scope and assumptions

This is an educational calculator, not financial advice or a lender's official quotation. Results depend on the stated rate, tenure, payment assumptions, and rounding. The basic web form models a constant-rate loan with monthly instalments; actual loan contracts may use different conventions, fees, rate-reset rules, or payment dates.
