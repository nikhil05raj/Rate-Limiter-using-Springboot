# 🚦 Spring Cloud Gateway + Redis Rate Limiter  
**Token Bucket Implementation in Spring Boot**



[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-green.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud%20Gateway-2023.0.3-blue.svg)](https://spring.io/projects/spring-cloud-gateway)
[![Redis](https://img.shields.io/badge/Redis-7.4-red.svg)](https://redis.io)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://www.java.com)

---

## 🎯 What is a Rate Limiter?

A rate limiter is a mechanism that **controls how many requests a client can make within a specific time window**. It acts as a gatekeeper for your backend APIs.  

---

**Need of Rate Limiting** — For production APIs rate limiting isn't optional, It's essential infrastructure for protection against abuse, cost control, fair resource allocation & better traffic management.

---

## 🪣 Algorithm Used: Token Bucket

### Key Characteristics & Advantages

- **Enforces an average request rate**  
  Tokens are added to the bucket at a constant, fixed rate (refill rate).  

- **Allows bounded bursts**  
  Unlike simpler algorithms (e.g., fixed window), Token Bucket naturally permits short-term bursts of traffic up to the bucket’s full capacity.  

- **Bucket size = maximum burst limit**  
  The bucket capacity (C) directly defines **how many requests can be handled instantly** when the bucket is full.  
  Example:  
  - Capacity = 50 tokens → allows up to 50 requests in a very short time  
  - Then throttles back to the refill rate until the bucket refills

- **Fixed-rate token replenishment**  
  Tokens are continuously added at a steady rate (R tokens per second).  
  Example configurations:  
  - 10 tokens/second → average limit of 10 req/s

- **Excess tokens are discarded (when bucket is full)**  
  This prevents the system from accumulating unlimited credit during long idle periods — a desirable property for most API use cases.

- **Request admission is simple and strict**  
  - Each request consumes **1 token** (or K tokens if configured differently)  
  - If at least 1 token is available → request is allowed  
  - If no tokens remain → request is rejected (typically with HTTP 429 Too Many Requests)


**Visual Flow**:

Request Arrives
->
Check Bucket
->
Consume Token (if available) → Proceed
->
Reject (HTTP 429) if empty
->
Tokens continuously refill at fixed rate


---


## 🏗️ Architecture: Spring Cloud Gateway + Redis (via Jedis)
All Incoming Traffic
->
Spring Cloud Gateway (Unified Entry Point)
->
Redis-backed Token Bucket Rate Limiter using Jedis
->
Backend Microservices


**Key Highlights**:
- **Jedis** used as the Redis client → chosen for its simplicity, synchronous performance, and lightweight nature in this context
- Token bucket state stored in **Redis** → fully **distributed** and consistent across multiple gateway instances
- **Client identification** via IP, User ID, or API Key (mapped to unique Redis key)
- **Shared state** across all Gateway pods

---

## 🛠️ Technologies & Stack

- **Spring Boot 3.3+** + **Java 21**
- **Spring Cloud Gateway** (reactive routing & filters)
- **Jedis** (synchronous Redis client – chosen for simplicity & good performance in the rate-limiting critical path)
- **Redis** (distributed token bucket state storage)
- **Gradle** (build tool & dependency management)
- **Lombok** (boilerplate reduction)
- - Python 3 (simple mock backend server)  
- Bash (automated rate-limit testing script)

---

## 🔧 How the Implementation Works

1. **Client Identification**  
   Each client (IP / User ID / API Key) → unique Redis key

2. **Shared State Storage**  
   Redis holds the **current token count** (accessible by every Gateway instance)

3. **Token Refill Logic**  
   Tokens regenerate at a **fixed configurable rate**

4. **Request Processing**  
   Every incoming request consumes **1 token** if available

5. **Rate Limit Response**  
   No tokens left? → Return **HTTP 429 Too Many Requests**

---


## 🚀 Quick Start

```bash
# 1. Clone
git clone https://github.com/NuancedNickel/Rate-Limiter-using-Springboot.git
cd RateLimiting

# 2. Start Redis (on WSL/Linux)
sudo service redis-server start

# 3. Start the Python mock backend (in one terminal)
python mock_server.py
# → runs on http://localhost:8081

# 4. Start the Spring Boot Gateway (in another terminal)
./gradlew bootRun
# → runs on http://localhost:8080

# 5. Test manually
curl http://localhost:8080/api/test

# 6. Or run the automated test
bash test-rate-limit.sh

