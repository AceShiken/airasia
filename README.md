# Currency Conversion API

A robust and scalable RESTful API for currency conversion using the Open Exchange Rates API. Built with Spring Boot 3.2.0 and Java 17.

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup Instructions](#setup-instructions)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)
- [Docker Support](#docker-support)
- [Performance Optimizations](#performance-optimizations)
- [Error Handling](#error-handling)
- [AI Usage Documentation](#ai-usage-documentation)
- [Configuration](#configuration)

## ✨ Features

- ✅ Currency conversion between any supported currencies
- ✅ Fetch all latest exchange rates
- ✅ Caching mechanism to minimize API calls and avoid rate limits
- ✅ H2 in-memory database for temporary storage and fallback
- ✅ Comprehensive error handling with meaningful HTTP status codes
- ✅ RESTful API design following best practices
- ✅ Docker containerization support
- ✅ Unit and integration tests
- ✅ Health check endpoints
- ✅ Workaround for free plan limitations (no /convert endpoint)

## 🛠 Tech Stack

- **Java 21 LTS** - Programming language
- **Spring Boot 3.2.0** - Application framework
- **Spring Web** - REST API development
- **Spring Data JPA** - Data persistence
- **H2 Database** - In-memory database
- **Guava Cache** - High-performance caching
- **Lombok** - Boilerplate code reduction
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **Maven** - Build tool
- **Docker** - Containerization

## 📁 Project Structure

```
currency-conversion-api/
├── src/
│   ├── main/
│   │   ├── java/com/airasia/currencyconversion/
│   │   │   ├── config/
│   │   │   │   └── AppConfig.java
│   │   │   ├── controller/
│   │   │   │   └── CurrencyConversionController.java
│   │   │   ├── dto/
│   │   │   │   ├── ConversionResponse.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   └── ExchangeRatesResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── CurrencyNotFoundException.java
│   │   │   │   ├── ExternalApiException.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── InvalidConversionRequestException.java
│   │   │   ├── model/
│   │   │   │   └── ExchangeRate.java
│   │   │   ├── repository/
│   │   │   │   └── ExchangeRateRepository.java
│   │   │   ├── service/
│   │   │   │   └── ExchangeRateService.java
│   │   │   └── CurrencyConversionApiApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/com/airasia/currencyconversion/
│           ├── controller/
│           │   └── CurrencyConversionControllerTest.java
│           ├── service/
│           │   └── ExchangeRateServiceTest.java
│           └── CurrencyConversionApiApplicationTests.java
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── pom.xml
└── README.md
```

## 📦 Prerequisites

- Java 21 LTS (recommended) or higher
- Maven 3.6 or higher
- Docker (optional, for containerized deployment)
- Open Exchange Rates API key (free plan available at https://openexchangerates.org/)

**Note:** If Maven is using a different Java version by default, you can specify Java 21 by setting `JAVA_HOME`:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

## 🚀 Setup Instructions

### 1. Clone the Repository

```bash
cd airasia
```

### 2. Get API Key

1. Sign up for a free account at [Open Exchange Rates](https://openexchangerates.org/signup/free)
2. Copy your API key from the dashboard

### 3. Configure API Key

Open `src/main/resources/application.yml` and replace `YOUR_API_KEY_HERE` with your actual API key:

```yaml
openexchangerates:
  api:
    key: YOUR_ACTUAL_API_KEY_HERE
```

Alternatively, you can set it as an environment variable:

```bash
export OPENEXCHANGERATES_API_KEY=your_actual_api_key
```

## 🏃 Running the Application

### Option 1: Using Maven

```bash
# Ensure Java 21 is being used
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### Option 2: Using Java

```bash
# Build the JAR
mvn clean package

# Run the JAR
java -jar target/currency-conversion-api-1.0.0.jar
```

### Option 3: Using Docker

See [Docker Support](#docker-support) section below.

## 📡 API Endpoints

### 1. Convert Currency

Convert an amount from one currency to another.

**Endpoint:** `GET /convert`

**Parameters:**
- `from` (required): Source currency code (e.g., USD)
- `to` (required): Target currency code (e.g., EUR)
- `amount` (required): Amount to convert (must be > 0)

**Example Request (cURL):**

```bash
curl -X GET "http://localhost:8080/convert?from=USD&to=EUR&amount=100"
```

**Example Response:**

```json
{
  "from": "USD",
  "to": "EUR",
  "amount": 100.0,
  "convertedAmount": 85.0,
  "exchangeRate": 0.85,
  "timestamp": "2025-12-12T10:30:00"
}
```

**Status Codes:**
- `200 OK` - Successful conversion
- `400 Bad Request` - Invalid parameters
- `404 Not Found` - Currency not found
- `503 Service Unavailable` - External API error

### 2. Get All Latest Rates (Bonus)

Retrieve all available exchange rates.

**Endpoint:** `GET /rates`

**Example Request (cURL):**

```bash
curl -X GET "http://localhost:8080/rates"
```

**Example Response:**

```json
{
  "USD": 1.0,
  "EUR": 0.85,
  "GBP": 0.73,
  "JPY": 110.0,
  "AUD": 1.35,
  ...
}
```

### 3. Health Check

Check if the API is running.

**Endpoint:** `GET /`

**Example Request (cURL):**

```bash
curl -X GET "http://localhost:8080/"
```

**Example Response:**

```json
{
  "status": "UP",
  "service": "Currency Conversion API",
  "version": "1.0.0"
}
```

### 4. Actuator Health Endpoint

**Endpoint:** `GET /actuator/health`

```bash
curl -X GET "http://localhost:8080/actuator/health"
```

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Tests with Coverage

```bash
mvn test jacoco:report
```

### Test Results

The project includes comprehensive tests:
- **Unit Tests**: Testing service and controller logic in isolation
- **Integration Tests**: Testing the full application context
- **Test Coverage**: Covers main business logic, error handling, and edge cases

**Example Test Output:**

```
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Manual Testing with Postman

Import the following examples into Postman:

**Convert USD to EUR:**
```
GET http://localhost:8080/convert?from=USD&to=EUR&amount=100
```

**Convert EUR to GBP:**
```
GET http://localhost:8080/convert?from=EUR&to=GBP&amount=50
```

**Get All Rates:**
```
GET http://localhost:8080/rates
```

**Error Case - Invalid Amount:**
```
GET http://localhost:8080/convert?from=USD&to=EUR&amount=-100
```

## 🐳 Docker Support

### Build and Run with Docker

1. **Build the Docker image:**

```bash
docker build -t currency-conversion-api .
```

2. **Run the container:**

```bash
docker run -p 8080:8080 \
  -e OPENEXCHANGERATES_API_KEY=your_api_key_here \
  currency-conversion-api
```

### Using Docker Compose

1. **Create `.env` file:**

```bash
cp .env.example .env
# Edit .env and add your API key
```

2. **Start the application:**

```bash
docker-compose up -d
```

3. **View logs:**

```bash
docker-compose logs -f
```

4. **Stop the application:**

```bash
docker-compose down
```

## ⚡ Performance Optimizations

### 1. Caching Strategy

The application implements **Guava Cache** with the following configuration:

- **Cache Duration**: 60 minutes (configurable)
- **Maximum Size**: 500 entries
- **Strategy**: Cache the entire rates map to minimize API calls
- **Statistics**: Records cache hit/miss statistics for monitoring

**Benefits:**
- Reduces external API calls by ~99% for repeated requests
- Stays within free plan rate limits (1,000 requests/month)
- Improves response time significantly

### 2. H2 Database Fallback

- Exchange rates are stored in H2 in-memory database
- If the external API fails, the application falls back to database
- Provides resilience and reliability

### 3. Efficient Conversion Algorithm

Since the free plan doesn't support the `/convert` endpoint, we:
1. Fetch all rates once (cached for 1 hour)
2. Perform cross-currency conversion using base rates
3. Formula: `amount * (toRate / fromRate)`

**Example:**
- Convert 100 EUR to GBP
- EUR rate: 0.85, GBP rate: 0.73
- Result: 100 * (0.73 / 0.85) = 85.88 GBP

## 🛡 Error Handling

The API provides consistent error responses with appropriate HTTP status codes:

### Error Response Format

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Amount must be greater than 0",
  "timestamp": "2025-12-12T10:30:00",
  "path": "/convert"
}
```

### Common Error Scenarios

| Error | Status Code | Description |
|-------|-------------|-------------|
| Invalid amount (negative or zero) | 400 | Bad Request |
| Missing required parameters | 400 | Bad Request |
| Invalid currency code format | 400 | Bad Request |
| Currency not found | 404 | Not Found |
| External API failure | 503 | Service Unavailable |
| Internal server error | 500 | Internal Server Error |

## 🤖 AI Usage Documentation

This project was developed with assistance from AI tools (GitHub Copilot) to accelerate development and ensure best practices.

### AI Prompts Used

#### 1. Project Setup
```
Create a Spring Boot 3.2.0 Maven project with Java 17 for a currency conversion API.
Include dependencies for web, cache, H2 database, JPA, validation, and testing.
```

#### 2. Service Layer with Caching
```
Implement a service class that:
- Fetches exchange rates from Open Exchange Rates API using RestTemplate
- Implements Caffeine caching to minimize API calls
- Stores rates in H2 database for fallback
- Handles currency conversion without using the /convert endpoint (free plan limitation)
- Includes comprehensive error handling
```

#### 3. Controller Implementation
```
Create a REST controller with:
- GET /convert endpoint with parameters: from, to, amount
- GET /rates endpoint to return all exchange rates
- Proper error handling using @ControllerAdvice
- Validation for all request parameters
```

#### 4. Testing Strategy
```
Generate comprehensive unit tests using JUnit 5 and Mockito for:
- Service layer with mocked RestTemplate and repository
- Controller layer with MockMvc
- Test cases for success scenarios, error cases, and edge cases
```

#### 5. Docker Configuration
```
Create a multi-stage Dockerfile for:
- Building the Maven project
- Running with minimal JRE Alpine image
- Including health checks
- Running as non-root user for security
Also create docker-compose.yml with environment variable support
```

#### 6. Documentation
```
Generate a comprehensive README.md that includes:
- Setup instructions
- API documentation with cURL examples
- Testing guide
- Docker deployment instructions
- Performance optimization explanations
- AI usage documentation
```

### AI-Assisted Components

The following components were primarily generated or enhanced with AI:
- Boilerplate code (DTOs, entities, exceptions)
- Test scaffolding and test cases
- Dockerfile and docker-compose configuration
- README structure and examples
- Error handling patterns
- Caching configuration

### Manual Refinements

The following were manually refined after AI generation:
- Business logic for cross-currency conversion
- Caching strategy optimization
- Database fallback mechanism
- API response formatting
- Specific validation rules

## ⚙️ Configuration

### Application Properties

Key configuration options in `application.yml`:

```yaml
# Open Exchange Rates API
openexchangerates:
  api:
    key: YOUR_API_KEY_HERE
    base-url: https://openexchangerates.org/api
    cache-duration-minutes: 60

# Server
server:
  port: 8080

# Cache
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=500,expireAfterWrite=3600s

# H2 Database
spring:
  datasource:
    url: jdbc:h2:mem:currencydb
  h2:
    console:
      enabled: true
      path: /h2-console
```

### Environment Variables

You can override configuration using environment variables:

```bash
export OPENEXCHANGERATES_API_KEY=your_key
export SERVER_PORT=8080
export SPRING_CACHE_CAFFEINE_SPEC=maximumSize=1000,expireAfterWrite=7200s
```

## 📊 H2 Console Access

Access the H2 database console at: `http://localhost:8080/h2-console`

**Connection Details:**
- JDBC URL: `jdbc:h2:mem:currencydb`
- Username: `sa`
- Password: (leave empty)

## 🚀 Deployment to Cloud

### Google Cloud Run

```bash
# Build and push to Google Container Registry
gcloud builds submit --tag gcr.io/YOUR_PROJECT_ID/currency-api

# Deploy to Cloud Run
gcloud run deploy currency-api \
  --image gcr.io/YOUR_PROJECT_ID/currency-api \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars OPENEXCHANGERATES_API_KEY=your_key
```

### AWS ECS/Fargate

```bash
# Build and push to ECR
docker build -t currency-api .
docker tag currency-api:latest YOUR_AWS_ACCOUNT.dkr.ecr.region.amazonaws.com/currency-api:latest
docker push YOUR_AWS_ACCOUNT.dkr.ecr.region.amazonaws.com/currency-api:latest

# Deploy using ECS task definition
```

## 📝 License

This project is created for evaluation purposes.

## 👥 Author

AirAsia Candidate

## 🙏 Acknowledgments

- [Open Exchange Rates](https://openexchangerates.org/) for providing the exchange rates API
- Spring Boot team for the excellent framework
- GitHub Copilot for AI assistance in development

---

**Note:** Remember to replace `YOUR_API_KEY_HERE` with your actual Open Exchange Rates API key before running the application.
