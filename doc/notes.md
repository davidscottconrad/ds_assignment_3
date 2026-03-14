# Assignment 3 — Team Notes

## Team Split

| Student | Owns | Difficulty |
|---|---|---|
| Student 1 | Product Service (good ×2 + bad ×1) + CCA + Docker Compose | ⭐⭐⭐ |
| Student 2 | Shopping Cart Service (checkout + channel pool) | ⭐⭐⭐⭐⭐ |
| Student 3 | RabbitMQ broker + Warehouse Java consumer | ⭐⭐⭐⭐ |
| Student 4 | AWS / Terraform / ALB / Load Test / Report | ⭐⭐⭐⭐ |

---

## Architecture at a Glance

```
Client (200k requests)
  → ALB
    /products/*   → Product Service   (2 good + 1 bad EC2 instances)
    /shopping-*   → Shopping Cart
    /credit-card-* → CCA

Shopping Cart checkout flow:
  1. HTTP call → CCA  (sync, separate service)
     - 402/400 → stop, return error, do NOT publish
     - 200 → continue
  2. Publish to RabbitMQ (with publisher confirms)
  3. Return order_id to client

RabbitMQ → Warehouse Consumer (Java, NOT behind ALB)
  - Manual ACK
  - Multithreaded (one channel per thread)
  - Prints total orders on shutdown
```

---

## Key Facts from the Assignment PDF

- **3 HTTP microservices** behind ALB: Product, Shopping Cart, CCA
- **Product target group**: 2 good instances + 1 bad instance (bad = ~50% 503)
- **Bad instance health check** must always return 200 — or AWS removes it and ATW demo breaks
- **Load test**: checkout-only, 200,000 requests, client runs on AWS (not local)
- **No cart creation needed** in load test — pre-create carts manually and reuse IDs
- **Warehouse** is Java only, NOT behind ALB, consumes via RabbitMQ
- **Publisher confirms**: SCS waits for broker ACK before returning success to client
- **Manual ACK**: Warehouse ACKs only after order is recorded in counters

---

## Ordered Job List

```
PHASE 1 — Agree Before Writing Any Code (Day 1)
 1. Review improved_api.yaml together
 2. Agree on RabbitMQ message schema (fields + types)
 3. Agree on queue name, exchange, durability settings
 4. Agree on health check path for every service
 5. Agree on internal hostnames for Docker networking
 6. Agree on Docker image names and ports

PHASE 2 — Build Locally (Days 2–5, parallel)
 7.  [S1] Port Product service from Assignment 2
 8.  [S1] GET /products/{productId} and POST /product
 9.  [S1] Bad Product image (random 503, health always 200)
10.  [S1] CCA (regex validation + 90/10 random)
11.  [S1] docker-compose.yml for full local stack — publish Day 2
12.  [S2] Shopping Cart (createCart, addItem, checkout)
13.  [S2] Checkout calls CCA via HTTP
14.  [S2] RabbitMQ channel pool (Apache Commons GenericObjectPool)
15.  [S2] Publisher confirms in checkout
16.  [S3] RabbitMQ broker container (rabbitmq:3-management)
17.  [S3] Warehouse Java consumer (multithreaded, one channel per thread)
18.  [S3] Manual ACK in Warehouse
19.  [S3] Thread-safe counters (AtomicLong + ConcurrentHashMap)
20.  [S3] Shutdown hook prints total orders
21.  [S4] Terraform: EC2 instances + security groups
22.  [S4] Terraform: ALB + 3 target groups + path routing rules
23.  [S4] ATW on Product target group

PHASE 3 — Local Integration (Day 6)
24. docker compose up — verify full stack starts
25. Happy path: checkout → CCA 200 → RabbitMQ confirmed → Warehouse ACK
26. Declined card: CCA 402 → no publish, error to client
27. Invalid card: 400 returned, no publish
28. Bad Product: ~50% 503, health check always 200
29. Warehouse count matches successful checkouts

PHASE 4 — AWS Deployment (Days 7–8)
30. Deploy all containers via Terraform
31. Register 2 good + 1 bad in Target Group A
32. Verify path routing through ALB (curl each path)
33. Verify SCS → CCA and SCS → RabbitMQ work on AWS
34. Verify ATW active, bad instance stays in service

PHASE 5 — Load Test + Report (Day 9)
35. [S4] Java load tester: 200k checkout requests from AWS EC2
36. [S4] Collect: success, failures, wall time, throughput
37. [S3] Tune consumer threads + prefetch (target: queue < 1,000 flat)
38. [S4] Screenshot: ALB traffic split (ATW working)
39. [S4] Screenshot: RabbitMQ queue graph (flat plateau)
40. [S3] Screenshot: Warehouse shutdown total
41. [S4] Assemble PDF and submit
```

---

## Technical Decisions

### Student 1 — Product + CCA
- **Bad instance**: identical code to good, one extra check per endpoint:
  ```java
  if (Math.random() < 0.5) return ResponseEntity.status(503).build();
  ```
- **Health check**: completely separate endpoint, never has the 503 check
- **CCA**: stateless, no storage needed
  ```java
  if (!card.matches("^[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}$")) return 400;
  return ThreadLocalRandom.current().nextDouble() < 0.9 ? 200 : 402;
  ```
- **Docker Compose**: publish on Day 2, use service names as hostnames

### Student 2 — Shopping Cart
- **Channel pool**: one `Connection` at startup, pool of `Channel` objects
  - Borrow channel → publish → `waitForConfirmsOrDie(5000)` → return channel
  - Always return in `finally` block
- **Checkout order**: validate → call CCA → borrow channel → publish → confirm → return order_id
- **CCA call**: use `RestTemplate`, env var `CCA_URL`

### Student 3 — RabbitMQ + Warehouse
- **Broker**: `rabbitmq:3-management` (port 5672 AMQP, 15672 console)
- **Consumer**: `ExecutorService` fixed thread pool, each thread owns one channel
  ```java
  channel.basicQos(prefetchCount);
  channel.basicConsume(queue, false, deliverCallback, cancelCallback);
  // in deliverCallback: update counters, then basicAck
  ```
- **Tuning start**: 10 threads, prefetch 10 — adjust based on queue graph

### Student 4 — AWS + Load Test
- **ATW Terraform**:
  ```hcl
  load_balancing_algorithm_type     = "weighted_random"
  load_balancing_anomaly_mitigation = "on"
  ```
- **Health check**: `unhealthy_threshold = 10` so bad instance is never removed
- **Load tester**: `CountDownLatch` for timing, `AtomicInteger` for counts
  - Pre-create carts before timed section starts
  - Configurable thread count (start 100, tune up/down)

---

## Shared Decisions to Fill In

| Decision | Value |
|---|---|
| Health check path | |
| RabbitMQ queue name | |
| RabbitMQ exchange | |
| Message schema | |
| Product service port | |
| Shopping Cart port | |
| CCA port | |
| RabbitMQ AMQP port | 5672 |
| RabbitMQ console port | 15672 |

---

## Submission Checklist

### Code Repo
- [ ] Product service (good image)
- [ ] Product service (bad image)
- [ ] Shopping Cart service
- [ ] Credit Card Authorizer
- [ ] RabbitMQ config
- [ ] Warehouse consumer (Java)
- [ ] Dockerfiles for all services
- [ ] docker-compose.yml
- [ ] Terraform files
- [ ] Java load tester

### Report PDF
- [ ] All team member names
- [ ] Repo URL
- [ ] ALB/ATW screenshot — traffic split good vs bad, does it change over time?
- [ ] RabbitMQ queue screenshot — flat plateau under 1,000
- [ ] Warehouse shutdown screenshot — total order count
- [ ] Performance table — success, failures, wall time, throughput
- [ ] Written observations
