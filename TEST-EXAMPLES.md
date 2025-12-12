# Test Examples for Currency Conversion API

## cURL Examples

### 1. Convert USD to EUR
```bash
curl -X GET "http://localhost:8080/convert?from=USD&to=EUR&amount=100" | jq
```

### 2. Convert EUR to GBP
```bash
curl -X GET "http://localhost:8080/convert?from=EUR&to=GBP&amount=50" | jq
```

### 3. Convert JPY to USD
```bash
curl -X GET "http://localhost:8080/convert?from=JPY&to=USD&amount=10000" | jq
```

### 4. Get All Exchange Rates
```bash
curl -X GET "http://localhost:8080/rates" | jq
```

### 5. Health Check
```bash
curl -X GET "http://localhost:8080/" | jq
```

### 6. Actuator Health
```bash
curl -X GET "http://localhost:8080/actuator/health" | jq
```

## Error Test Cases

### 1. Invalid Amount (Negative)
```bash
curl -X GET "http://localhost:8080/convert?from=USD&to=EUR&amount=-100" | jq
```
Expected: 400 Bad Request

### 2. Invalid Amount (Zero)
```bash
curl -X GET "http://localhost:8080/convert?from=USD&to=EUR&amount=0" | jq
```
Expected: 400 Bad Request

### 3. Missing Parameter
```bash
curl -X GET "http://localhost:8080/convert?from=USD&to=EUR" | jq
```
Expected: 400 Bad Request

### 4. Currency Not Found
```bash
curl -X GET "http://localhost:8080/convert?from=USD&to=XYZ&amount=100" | jq
```
Expected: 404 Not Found

### 5. Invalid Currency Code (Too Short)
```bash
curl -X GET "http://localhost:8080/convert?from=US&to=EUR&amount=100" | jq
```
Expected: 400 Bad Request

## PowerShell Examples (Windows)

### Convert Currency
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/convert?from=USD&to=EUR&amount=100" -Method Get
```

### Get All Rates
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/rates" -Method Get
```

## HTTPie Examples

### Convert Currency
```bash
http GET "localhost:8080/convert" from==USD to==EUR amount==100
```

### Get All Rates
```bash
http GET "localhost:8080/rates"
```

## Wget Examples

### Convert Currency
```bash
wget -qO- "http://localhost:8080/convert?from=USD&to=EUR&amount=100"
```

### Get All Rates
```bash
wget -qO- "http://localhost:8080/rates"
```

## Performance Testing with Apache Bench

### Test /convert endpoint
```bash
ab -n 1000 -c 10 "http://localhost:8080/convert?from=USD&to=EUR&amount=100"
```

### Test /rates endpoint
```bash
ab -n 1000 -c 10 "http://localhost:8080/rates"
```

## Testing with Python

```python
import requests

# Convert currency
response = requests.get('http://localhost:8080/convert', params={
    'from': 'USD',
    'to': 'EUR',
    'amount': 100
})
print(response.json())

# Get all rates
response = requests.get('http://localhost:8080/rates')
print(response.json())
```

## Testing with JavaScript/Node.js

```javascript
const axios = require('axios');

// Convert currency
axios.get('http://localhost:8080/convert', {
  params: {
    from: 'USD',
    to: 'EUR',
    amount: 100
  }
}).then(response => {
  console.log(response.data);
});

// Get all rates
axios.get('http://localhost:8080/rates')
  .then(response => {
    console.log(response.data);
  });
```

## Expected Response Times

With caching enabled:
- First request: ~200-500ms (API call)
- Subsequent requests: <50ms (cached)

Without cache:
- Every request: ~200-500ms (API call)
