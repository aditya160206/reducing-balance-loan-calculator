# Loan Mathematics Engine

## Overview
The Loan Mathematics Engine is a simple educational project built using Java 17 and Spring Boot 3.3.x. It provides a REST API for calculating loan parameters such as monthly EMI, total payments, total interest, and an amortization schedule based on user inputs.

## Core Functionality
The application accepts the following inputs:
- **Principal (P)**: The initial amount of the loan.
- **Annual Nominal Interest Rate (R)**: The interest rate expressed as a percentage.
- **Loan Tenure (T)**: The duration of the loan in years.

### Calculations
The application calculates and displays:
- Monthly interest rate: \( r = \frac{R}{1200} \)
- Number of payments: \( n = 12 \times T \)
- Monthly EMI
- Total amount paid
- Total interest paid
- Month-by-month amortization schedule, showing EMI, interest, principal repaid, and remaining balance.

### Mathematical Derivation
The derivation of the EMI formula is based on the present value of future payments. The formula is derived from the geometric series, which allows us to express the present value of repayments in terms of the fixed monthly payment (A):

\[
P = A \left[ \frac{1}{1+r} + \frac{1}{(1+r)^2} + \cdots + \frac{1}{(1+r)^n} \right]
\]

This series can be simplified using the finite geometric series identity, leading to the final EMI formula:

\[
\boxed{A = P \frac{r(1+r)^n}{(1+r)^n - 1}}
\]

In cases where \( r = 0 \), the formula simplifies to \( A = \frac{P}{n} \).

## Architecture
The project is structured as follows:
- **Controller**: Handles HTTP requests and responses.
- **Service**: Contains the business logic for loan calculations.
- **DTOs**: Data Transfer Objects for request and response handling.
- **Exception Handling**: Custom exceptions for managing errors.

## Running the Application
1. Clone the repository:
   ```
   git clone <repository-url>
   cd loan-mathematics-engine
   ```

2. Build the project using Maven:
   ```
   mvn clean install
   ```

3. Run the application:
   ```
   mvn spring-boot:run
   ```

4. Access the API at `http://localhost:8080/api/loans/calculate` using a tool like Postman or curl.

## Deploying the website

The project uses a Java/Spring Boot backend and a static HTML frontend. Deploy the backend as a Docker web service and the static frontend on Vercel; a Vercel Function at `/api/loans/calculate` forwards calculator requests to the backend.

### 1. Push the project to GitHub

Create a GitHub repository and push the project source, including `Dockerfile`, `.dockerignore`, `vercel.json`, and `api/loans/calculate.js`. Do not commit credentials or private environment files.

### 2. Deploy the Spring Boot backend on Render

1. In Render, create a new **Web Service** from the GitHub repository.
2. Select **Docker** as the runtime and use the repository-root `Dockerfile`.
3. Deploy and wait for the service to become live.
4. Copy the service URL, for example `https://your-loan-api.onrender.com`.
5. Test `POST https://your-loan-api.onrender.com/api/loans/calculate` with JSON containing `principal`, `annualInterestRate`, and `tenureInYears`.

The app reads Render's `PORT` environment variable and defaults to port 8080 for local use.

### 3. Deploy the frontend on Vercel

1. Import the same GitHub repository into Vercel.
2. Keep the project root as the Root Directory. The checked-in `vercel.json` sets the static output directory and disables a build command.
3. In Vercel project settings, add the environment variable `BACKEND_URL` with the Render service URL from step 2 (no path suffix is needed).
4. Deploy. Vercel will provide a public website URL.
5. Open the Vercel URL, calculate a sample loan, and confirm the browser's request to `/api/loans/calculate` succeeds.

The Vercel endpoint is a small proxy; it keeps the browser on the Vercel origin and forwards the JSON request to the Spring Boot service. On Render's free tier, the backend may take a short time to wake after inactivity.

## Testing
JUnit tests are included to validate the functionality of the loan calculations and API endpoints. To run the tests, use:
```
mvn test
```

## Conclusion
This project serves as a practical demonstration of loan calculations and the underlying mathematics. It is designed for educational purposes, emphasizing clarity and transparency in the calculations.