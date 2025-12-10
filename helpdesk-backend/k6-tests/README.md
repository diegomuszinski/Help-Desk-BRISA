# K6 Load Testing for HelpDesk API

## Overview

This directory contains K6 load testing scripts for the HelpDesk API to validate performance, scalability, and reliability under various load conditions.

## Test Types

### 1. Load Test (`load-test.js`)
**Purpose**: Validate system performance under expected load

**Configuration**:
- Ramp up: 30s to 10 users
- Sustain: 1m at 10 users
- Ramp up: 30s to 50 users
- Sustain: 2m at 50 users
- Ramp up: 30s to 100 users
- Sustain: 2m at 100 users
- Ramp down: 30s to 0 users

**Thresholds**:
- 95% requests < 500ms
- 99% requests < 1s
- Error rate < 1%

### 2. Spike Test (`spike-test.js`)
**Purpose**: Test system behavior under sudden traffic spikes

**Configuration**:
- Normal: 10s at 10 users
- Spike: 5s ramp to 500 users
- Sustain: 30s at 500 users
- Recovery: 10s to 10 users
- Normal: 30s at 10 users

**Thresholds**:
- 95% requests < 2s (more lenient)
- Error rate < 5%

### 3. Stress Test (`stress-test.js`)
**Purpose**: Find the breaking point of the system

**Configuration**:
- Gradually increase load from 50 to 500 users over 14 minutes
- Identify at what point system degrades

**Thresholds**:
- 95% requests < 5s
- Error rate < 10%

### 4. Soak Test (`soak-test.js`)
**Purpose**: Test system stability over extended period

**Configuration**:
- Ramp up: 2m to 50 users
- Sustain: 30m at 50 users (soak period)
- Ramp down: 2m to 0 users

**Thresholds**:
- 95% requests < 500ms
- Error rate < 1%

## Installation

### Install K6

**Windows (Chocolatey)**:
```powershell
choco install k6
```

**Windows (Scoop)**:
```powershell
scoop install k6
```

**Windows (Manual)**:
Download from https://k6.io/docs/getting-started/installation/

**Linux**:
```bash
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6
```

**macOS**:
```bash
brew install k6
```

## Usage

### PowerShell Runner (Recommended)

```powershell
# Run load test
.\run-load-tests.ps1 -TestType load

# Run spike test
.\run-load-tests.ps1 -TestType spike

# Run stress test
.\run-load-tests.ps1 -TestType stress

# Run soak test (30 minutes)
.\run-load-tests.ps1 -TestType soak

# Run all tests sequentially
.\run-load-tests.ps1 -TestType all

# Custom base URL
.\run-load-tests.ps1 -TestType load -BaseUrl "https://api.helpdesk.com"

# Custom output directory
.\run-load-tests.ps1 -TestType load -OutputDir "D:\load-test-results"
```

### Direct K6 Commands

```powershell
# Load test
k6 run load-test.js

# With custom base URL
k6 run -e BASE_URL=https://api.helpdesk.com load-test.js

# With HTML report
k6 run --out json=results.json load-test.js

# With Cloud output (requires k6 Cloud account)
k6 run --out cloud load-test.js
```

## Prerequisites

Before running tests, ensure:

1. **HelpDesk API is running**:
   ```powershell
   cd ..\helpdesk-api
   .\gradlew bootRun
   ```

2. **Test user exists**:
   - Email: `loadtest@helpdesk.com`
   - Password: `LoadTest123!`
   - Create via API or database

3. **Database has seed data**:
   - At least 1 category
   - At least 1 priority

## Test Scenarios

### What Each Test Validates

**Load Test**:
- ✅ API endpoints respond within SLA
- ✅ Cache is working (categories, priorities < 50ms)
- ✅ Rate limiting doesn't affect normal users
- ✅ Database queries are optimized

**Spike Test**:
- ✅ System handles sudden traffic increases
- ✅ Rate limiting protects API (429 responses expected)
- ✅ System recovers after spike
- ✅ No crashes or memory leaks

**Stress Test**:
- ✅ Identifies system breaking point
- ✅ Validates connection pool sizing
- ✅ Tests database under load
- ✅ Validates thread pool configuration

**Soak Test**:
- ✅ No memory leaks over time
- ✅ Consistent performance for 30+ minutes
- ✅ Connection pool doesn't exhaust
- ✅ Cache doesn't cause issues

## Analyzing Results

### K6 Output

K6 provides detailed metrics:

```
checks.........................: 100.00% ✓ 2000 ✗ 0
data_received..................: 1.2 MB  20 kB/s
data_sent......................: 240 kB  4.0 kB/s
http_req_duration..............: avg=150ms min=50ms med=120ms max=500ms p(95)=350ms p(99)=450ms
http_req_failed................: 0.00%   ✓ 0    ✗ 2000
http_reqs......................: 2000    33/s
```

### Key Metrics

| Metric | Target | Critical |
|--------|--------|----------|
| http_req_duration (p95) | < 500ms | < 1s |
| http_req_duration (p99) | < 1s | < 2s |
| http_req_failed | < 1% | < 5% |
| errors | < 5% | < 10% |

### Results Files

Results are saved to `./results/`:
- `*-results.json` - Detailed metrics
- `*-summary.json` - Summary statistics

### Visualizing Results

**k6-reporter** (HTML reports):
```bash
npm install -g k6-to-junit
k6-to-junit results/load-test-results.json -o results/report.xml
```

**Grafana + InfluxDB**:
```bash
# Run with InfluxDB output
k6 run --out influxdb=http://localhost:8086/k6 load-test.js
```

## Troubleshooting

### High Error Rate

**Possible causes**:
1. API not running
2. Test user doesn't exist
3. Database connection issues
4. Rate limiting too aggressive

**Solutions**:
```powershell
# Check API health
curl http://localhost:8080/actuator/health

# Verify test user
curl -X POST http://localhost:8080/v1/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"loadtest@helpdesk.com","password":"LoadTest123!"}'
```

### Slow Response Times

**Check**:
1. Database indexes (see `CREATE_INDEXES.sql`)
2. Connection pool size
3. JVM heap size
4. Cache configuration

### Rate Limiting Issues

If getting too many 429 responses:

1. **Adjust rate limits** in `RateLimit` annotations
2. **Increase limits** in `RateLimitConfig`
3. **Test with more time between requests**

### Out of Memory

For stress/soak tests:
```powershell
# Increase JVM heap
$env:JAVA_OPTS = "-Xmx2g -Xms1g"
.\gradlew bootRun
```

## Best Practices

1. **Run tests in isolation**: Don't run multiple test types simultaneously
2. **Use dedicated test environment**: Avoid production
3. **Monitor system resources**: CPU, memory, disk I/O during tests
4. **Baseline before changes**: Run tests before and after code changes
5. **Document results**: Keep history of test results for comparison

## CI/CD Integration

### GitHub Actions

```yaml
name: Load Tests

on:
  schedule:
    - cron: '0 2 * * *'  # Daily at 2 AM

jobs:
  load-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup K6
        run: |
          sudo gpg -k
          sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
          echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
          sudo apt-get update
          sudo apt-get install k6
      
      - name: Run Load Test
        run: |
          cd helpdesk-backend/k6-tests
          k6 run load-test.js
```

## Performance Goals

### Target Performance

| Load Level | Users | Req/s | p95 Latency | Error Rate |
|------------|-------|-------|-------------|------------|
| Low | 10 | 10 | < 200ms | < 0.1% |
| Medium | 50 | 50 | < 500ms | < 1% |
| High | 100 | 100 | < 1s | < 2% |
| Peak | 500 | 500 | < 2s | < 5% |

### Capacity Planning

Based on test results:
- **Current capacity**: ~100 concurrent users
- **Recommended max**: 80 concurrent users (80% capacity)
- **Scale trigger**: 60 concurrent users (60% capacity)

## Next Steps

After load testing:

1. **Optimize bottlenecks** identified in tests
2. **Tune database** queries and indexes
3. **Adjust cache** TTLs based on hit rates
4. **Configure auto-scaling** based on load thresholds
5. **Set up monitoring** alerts for performance degradation

## References

- [K6 Documentation](https://k6.io/docs/)
- [K6 Cloud](https://k6.io/cloud/)
- [Load Testing Best Practices](https://k6.io/docs/testing-guides/test-types/)
- [Performance Testing Guide](https://www.nginx.com/blog/load-testing-101/)
