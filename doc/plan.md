# Assignment 3 Team Plan

## Architecture Overview

```
      ┌──────────────────────────────────────────────────┐
      │              CLIENT / LOAD TESTER                │
      │        (200,000 checkout-only requests)          │
      └─────────────────────────┬────────────────────────┘
                                │ HTTPS
                                ▼
      ┌──────────────────────────────────────────────────┐
      │         AWS Application Load Balancer (ALB)      │
      │                                                  │
      │   /products/* ──────────── Target Group A        │
      │   /product    ──────────── Target Group A        │
      │   /shopping-* ──────────── Target Group B        │
      │   /credit-*   ──────────── Target Group C        │
      └────────────┬───────────────────────────┬─────────┘
                   │                           │
      /products    │                           │   /shopping-*
                   ▼                           ▼
  ┌────────────────────────┐       ┌────────────────────────┐
  │    Product Service     │       │  Shopping Cart Service  │
  │    (Target Group A)    │       │    (Target Group B)     │
  │                        │       │                         │
  │  GET /products/        │       │  POST /shopping-cart    │
  │       {productId}      │       │  POST /shopping-carts/  │
  │  POST /product         │       │       {id}/addItem      │
  │                        │       │  POST /shopping-carts/  │
  │  ┌──────────────────┐  │       │       {id}/checkout     │
  │  │  Good Instance 1 │  │       └────────────┬────────────┘
  │  └──────────────────┘  │                    │
  │  ┌──────────────────┐  │                    │ 1. call CCA (sync)
  │  │  Good Instance 2 │  │                    ▼
  │  └──────────────────┘  │       ┌────────────────────────┐
  │  ┌──────────────────┐  │       │  Credit Card Authorizer │
  │  │  Bad Instance    │  │       │    (Target Group C)     │
  │  │  (~50% 503s)     │  │       │                         │
  │  └──────────────────┘  │       │  POST /credit-card-     │
  │                        │       │       authorizer/       │
  │  ATW shifts traffic    │       │       authorize         │
  │  away from bad one     │       │                         │
  └────────────────────────┘       │  90% ─► 200 authorized  │
                                   │  10% ─► 402 declined    │
                                   └────────────┬────────────┘
                                                │
                                                │ 2. if authorized,
                                                │    Shopping Cart publishes
                                                ▼
                                   ┌────────────────────────┐
                                   │     RabbitMQ Broker     │
                                   │                         │
                                   │  durable queue          │
                                   │  publisher confirms     │
                                   └────────────┬────────────┘
                                                │ consume
                                                ▼
                                   ┌────────────────────────┐
                                   │   Warehouse Consumer    │
                                   │   (Java, NOT in ALB)    │
                                   │                         │
                                   │  multithreaded          │
                                   │  manual ACK             │
                                   │  thread-safe counters   │
                                   │  prints total on exit   │
                                   └────────────────────────┘
```

**Checkout flow (happy path):** Client → ALB → Shopping Cart → CCA (authorize) → RabbitMQ (publish w/ confirms) → Warehouse (consume & manual ACK) → return `order_id`

**Checkout flow (declined):** CCA returns 402 → Shopping Cart does NOT publish → returns error to client

**ATW demo:** 2 good Product instances + 1 bad instance all in Target Group A. Bad instance returns 503 ~50% of the time but health check always returns 200, so ALB keeps it alive. ATW shifts traffic away from it over time.

| Service | Behind ALB | Language | Key behavior |
|---|---|---|---|
| Product Service (good ×2) | Yes — Target Group A | Any | Normal |
| Product Service (bad ×1) | Yes — Target Group A | Any | 50% 503, health check always 200 |
| Shopping Cart | Yes — Target Group B | Any | Orchestrates checkout, channel pool for RabbitMQ |
| Credit Card Authorizer | Yes — Target Group C | Any | 90% approve / 10% decline |
| RabbitMQ | No | — | Message broker |
| Warehouse Consumer | No | Java | Multithreaded, manual ACK |

---

## Goal

Finish CS6650 Assignment 3 with a team of 4:

| Student | Owns |
|---|---|
| **Student 1** | Product Service (good ×2 + bad ×1) + Credit Card Authorizer + Docker Compose |
| **Student 2** | Shopping Cart Service (full checkout flow + RabbitMQ channel pool) |
| **Student 3** | RabbitMQ broker + Warehouse Java consumer |
| **Student 4** | AWS / ALB / Terraform + load tester + report |

**Why this split:** CCA is small (regex check + random 90/10) so it pairs naturally with Product. Docker Compose also goes to Student 1 since they finish earliest — setting up local dev infrastructure keeps them productive while others are still building their services. This frees Student 4 to focus entirely on Terraform and AWS, which is a full task on its own.

Starting design artifacts:
- `improved_api.yaml` — main API contract, treat as source of truth
- `Use_Cases.md` — user flow for add-to-cart and checkout
- `idl/*.py` — rough interface notes only, contain syntax problems, do not use as code
- `sample code/Send.java` and `sample code/RecvMT.java` — RabbitMQ reference examples
- `Assignment-3.pdf` — assignment specification

---

## Shared Decisions — Agree Before Coding

- [ ] Programming language and framework for each microservice
- [ ] Exact health check endpoint path for each service (e.g. `GET /health`)
- [ ] RabbitMQ message schema: what fields does checkout publish?
- [ ] Queue name, exchange type, routing key, durability settings
- [ ] How Shopping Cart stores carts and line items (in-memory is fine)
- [ ] Channel pool strategy for SCS → RabbitMQ (Apache Commons pool recommended)
- [ ] Common error response format (matches `Error` schema in `improved_api.yaml`)
- [ ] Docker image naming and port conventions
- [ ] Local Docker Compose setup — Student 1 owns, others plug in

---

## Student 1: Product Service + Credit Card Authorizer

**Owns:** Product microservice (good ×2 + bad ×1) and the CCA microservice

### Recommended Approach

**Stack:** Spring Boot (reuse from A2) for Product, any lightweight framework for CCA (Spring Boot or plain servlet).

**Product storage:** in-memory `ConcurrentHashMap<Integer, Product>` with `AtomicInteger` for ID generation. No database needed.

**Bad instance trick:** the good and bad versions share 100% of the same code. The only difference is a single environment variable or flag. Add this at the top of each product endpoint handler (not the health check):

```java
if (Math.random() < 0.5) {
    return ResponseEntity.status(503).body("Simulated failure");
}
```

Keep `GET /health` completely separate — it must always return 200 regardless of the flag.

**CCA is stateless and tiny:**
```java
// Validate format
if (!cardNumber.matches("^[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}$")) {
    return ResponseEntity.status(400)...;
}
// Random approve/decline
if (ThreadLocalRandom.current().nextDouble() < 0.9) {
    return ResponseEntity.ok()...;       // 200 authorized
} else {
    return ResponseEntity.status(402)...; // declined
}
```

### Checklist

#### Product Service
- [ ] Port or reuse from Assignment 2
- [ ] `GET /products/{productId}` — return 404 if not found
- [ ] `POST /product` — validate all fields, store in memory, return generated `product_id`
- [ ] `GET /health` — always returns 200 (used by ALB health checks)
- [ ] Consistent error responses matching `Error` schema in `improved_api.yaml`

#### Bad Product Instance
- [ ] Copy the good service, add random 503 on product endpoints only
- [ ] `GET /health` on bad instance **always returns 200** — never conditional
- [ ] Deploy **2 good + 1 bad** into Target Group A on AWS

#### Credit Card Authorizer
- [ ] `POST /credit-card-authorizer/authorize`
- [ ] Regex validate: `^[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}$` → 400 if invalid
- [ ] `ThreadLocalRandom` → 90% return 200, 10% return 402
- [ ] `GET /health` always returns 200

#### Packaging
- [ ] `Dockerfile` for good Product service (e.g. port 8080)
- [ ] `Dockerfile` for bad Product service (same port, different image tag)
- [ ] `Dockerfile` for CCA (e.g. port 8082)
- [ ] Confirm image names, ports, and health check path with Student 4

#### Docker Compose (local dev — all students depend on this)
- [ ] Create `docker-compose.yml` with all services: product-good, product-bad, shopping-cart, cca, rabbitmq, warehouse
- [ ] Use service names as hostnames (e.g. `rabbitmq`, `cca`) so other students can hardcode them in env vars
- [ ] Expose management ports: RabbitMQ console on 15672, each service on its agreed port
- [ ] Add health check entries so services wait for dependencies before starting
- [ ] Publish `docker-compose.yml` on Day 2 so Students 2, 3, 4 can plug their containers in immediately
- [ ] Document how to start the full stack: `docker compose up --build`

#### Testing
- [ ] `GET /products/{id}` returns product or 404
- [ ] `POST /product` with missing/invalid fields returns 400
- [ ] Bad instance: run 100 requests, verify roughly 50 are 503, health check always 200
- [ ] CCA: valid card returns 200 or 402; malformed card always returns 400

#### Definition of Done
- [ ] All three containers start and respond locally
- [ ] Bad instance never fails health check
- [ ] CCA rejects bad formats and randomly approves/declines valid ones
- [ ] Student 4 can register all three Product instances in Target Group A

---

## Student 2: Shopping Cart Service

**Owns:** Shopping Cart — the most technically complex service in the assignment

### Recommended Approach

**Stack:** Spring Boot. In-memory cart storage with `ConcurrentHashMap`.

**Cart storage:**
```java
// cartId → Cart object
ConcurrentHashMap<Integer, Cart> carts = new ConcurrentHashMap<>();
AtomicInteger cartIdCounter = new AtomicInteger(1);

class Cart {
    int customerId;
    List<CartItem> items = new ArrayList<>();  // or CopyOnWriteArrayList for thread safety
}
```

**Calling CCA** — use `RestTemplate` or `HttpClient`. The call is synchronous: block until CCA responds before doing anything else.

**RabbitMQ channel pool** — this is the key performance decision. Do NOT open/close a channel per request (too slow). Use Apache Commons pool:

```java
// In @PostConstruct (runs once at startup):
Connection connection = factory.newConnection();  // slow, do once
GenericObjectPool<Channel> channelPool = new GenericObjectPool<>(
    new ChannelFactory(connection)
);

// In checkout():
Channel channel = channelPool.borrowObject();
try {
    channel.confirmSelect();
    channel.basicPublish(exchange, routingKey, props, messageBytes);
    channel.waitForConfirmsOrDie(5000); // blocks until broker confirms
} finally {
    channelPool.returnObject(channel);  // always return, even on exception
}
```

**Publisher confirms** — `channel.waitForConfirmsOrDie(5000)` throws if the broker nacks or times out. Catch that and return a 500 to the client — do not return success if the message wasn't confirmed.

**Checkout flow in order:**
1. Validate cart exists and has items
2. Call CCA → if not 200, return error immediately, stop
3. Borrow channel from pool
4. Publish order message
5. Wait for publisher confirm
6. Return channel to pool
7. Return `order_id` to client

### Checklist

#### Shopping Cart Endpoints
- [ ] `POST /shopping-cart` — create cart, return new `shopping_cart_id`
- [ ] `POST /shopping-carts/{id}/addItem` — validate product_id and quantity (1–10,000), add to cart
- [ ] `POST /shopping-carts/{id}/checkout` — full checkout flow (see above)
- [ ] `GET /health` — always returns 200
- [ ] In-memory storage with `ConcurrentHashMap` (thread-safe)

#### Checkout Logic
- [ ] Call CCA first via HTTP — do nothing else until response received
- [ ] CCA 402 → return failure to client, no RabbitMQ publish
- [ ] CCA 400 → return 400 to client, no publish
- [ ] CCA 200 → publish to RabbitMQ with publisher confirms
- [ ] RabbitMQ nack or timeout → return 500, do not claim success
- [ ] Return `order_id` only after confirm received

#### RabbitMQ Channel Pool
- [ ] Initialize one `Connection` at startup (`@PostConstruct`)
- [ ] Create `GenericObjectPool<Channel>` with `ChannelFactory`
- [ ] Each checkout: `borrowObject()` → publish → `waitForConfirmsOrDie()` → `returnObject()`
- [ ] Always return channel in `finally` block
- [ ] Set pool max size (start with 20, tune during load test)

#### Packaging & Integration
- [ ] `Dockerfile` for Shopping Cart (e.g. port 8081)
- [ ] Needs env vars: `CCA_URL`, `RABBITMQ_HOST`, `RABBITMQ_PORT`
- [ ] Agree message schema with Student 3 before implementing publish

#### Testing
- [ ] Create cart → add item → checkout (CCA mocked at 200) → confirm order_id returned
- [ ] Checkout with declined card: 402 returned, no RabbitMQ message
- [ ] Checkout with invalid card format: 400 returned, no publish
- [ ] Kill RabbitMQ mid-test: checkout must fail, not return success
- [ ] Concurrent checkouts: channel pool returns correct channels under load

#### Definition of Done
- [ ] Checkout never publishes when CCA is non-200
- [ ] Checkout fails if publisher confirm is not received
- [ ] Channel pool works under concurrent load (no channel leaks)
- [ ] Service connects to Student 3's RabbitMQ container via Docker Compose

---

## Student 3: RabbitMQ + Warehouse

**Owns:** RabbitMQ broker configuration and Warehouse Java consumer

### Recommended Approach

**RabbitMQ broker:** use the official `rabbitmq:3-management` Docker image — the management plugin gives you the web console for queue monitoring screenshots.

**Recommended queue config:**
- Exchange: default (direct, `""`) or a named direct exchange
- Queue: durable, named e.g. `order_queue`
- Routing key: same as queue name
- Message format (agree with Student 2):
```json
{ "shoppingCartId": 123, "productId": 456, "quantity": 2 }
```

**Warehouse consumer structure:**
```java
// One shared connection, N threads each with their own channel
Connection connection = factory.newConnection();
ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);

for (int i = 0; i < NUM_THREADS; i++) {
    pool.submit(() -> {
        Channel channel = connection.createChannel();
        channel.basicQos(PREFETCH_COUNT);  // limit in-flight messages per consumer
        channel.basicConsume(QUEUE_NAME, false, (tag, delivery) -> {
            // parse message, update counters
            orderCount.incrementAndGet();
            // update productQuantities map
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }, tag -> {});
    });
}
```

**Thread-safe counters:**
```java
AtomicLong orderCount = new AtomicLong(0);
ConcurrentHashMap<Integer, AtomicLong> productQuantities = new ConcurrentHashMap<>();
```

**Shutdown hook** to print total:
```java
Runtime.getRuntime().addShutdownHook(new Thread(() ->
    System.out.println("Total orders processed: " + orderCount.get())
));
```

**Tuning goal:** set `NUM_THREADS` and `PREFETCH_COUNT` so consumption rate ≈ production rate. Start with 10 threads / prefetch 10, then adjust based on RabbitMQ console graphs during load test.

### Checklist

#### RabbitMQ Broker
- [ ] Use `rabbitmq:3-management` image (port 5672 for AMQP, 15672 for console)
- [ ] Declare durable queue in config or let Warehouse declare it on startup
- [ ] Document: queue name, exchange, routing key, credentials → share with Student 2
- [ ] Containerize with `docker-compose` entry

#### Warehouse Consumer
- [ ] One `Connection` created at startup
- [ ] `ExecutorService` with fixed thread pool, each thread gets its own `Channel`
- [ ] `channel.basicQos(prefetchCount)` per channel before consuming
- [ ] `basicConsume` with `autoAck = false`
- [ ] ACK (`basicAck`) only after message is recorded in counters
- [ ] `AtomicLong` for total order count
- [ ] `ConcurrentHashMap<Integer, AtomicLong>` for quantity per product ID
- [ ] `Runtime.getRuntime().addShutdownHook(...)` prints total orders on exit
- [ ] `Dockerfile` for Warehouse consumer

#### Tuning
- [ ] Start with 10 consumer threads, prefetch 10
- [ ] Monitor queue depth in RabbitMQ console during Student 4's load test
- [ ] Increase threads / prefetch until queue stays flat under 1,000 messages
- [ ] Target profile: ramp up → flat plateau → drain (not spike-and-crash)

#### Testing
- [ ] Manually publish 10 messages, verify Warehouse consumes and prints correct count
- [ ] Kill Warehouse mid-consume, restart — verify unACKed messages requeue and re-process
- [ ] Run 10,000 messages concurrently — verify final count matches sent count exactly

#### Definition of Done
- [ ] Warehouse consumes all messages and ACKs correctly
- [ ] Killing consumer mid-run does not lose messages
- [ ] Order count is correct under concurrent load
- [ ] Queue depth stays manageable under load
- [ ] Shutdown hook prints correct total

---

## Student 4: AWS, ALB, Load Test, and Report

**Owns:** AWS deployment, ALB configuration, load testing, and final report
**Does NOT own:** Docker Compose (moved to Student 1)

### Recommended Approach

**Terraform resources needed:**
```
aws_instance (×6: Product×3, SCS, CCA, RabbitMQ+Warehouse)
aws_lb (Application Load Balancer)
aws_lb_target_group (×3: product, cart, cca)
aws_lb_listener (port 80/443)
aws_lb_listener_rule (×3: path-based routing)
aws_lb_target_group_attachment (×5: register instances)
aws_security_group (allow 80/443 from internet, allow internal ports between services)
```

**ATW in Terraform:**
```hcl
resource "aws_lb_target_group" "product" {
  load_balancing_algorithm_type = "weighted_random"
  load_balancing_anomaly_mitigation = "on"   # this is ATW
  ...
}
```

**Unhealthy threshold** — set high so bad Product instance stays in the target group:
```hcl
health_check {
  path                = "/health"
  healthy_threshold   = 2
  unhealthy_threshold = 10   # allow many failures before removing
  interval            = 30
}
```

**Load tester structure:**
```java
int TOTAL_REQUESTS = 200_000;
int THREAD_COUNT = 100; // tune this
AtomicInteger successCount = new AtomicInteger(0);
AtomicInteger failCount = new AtomicInteger(0);
CountDownLatch latch = new CountDownLatch(TOTAL_REQUESTS);

long startTime = System.currentTimeMillis();
// submit TOTAL_REQUESTS tasks to thread pool
// each task: POST /shopping-carts/{id}/checkout
// on 200: successCount++; else: failCount++; latch.countDown()
latch.await();
long wallTime = System.currentTimeMillis() - startTime;

System.out.println("Successful: " + successCount.get());
System.out.println("Failed: " + failCount.get());
System.out.println("Wall time: " + wallTime / 1000.0 + "s");
System.out.println("Throughput: " + TOTAL_REQUESTS / (wallTime / 1000.0) + " req/s");
```

**Note:** the spec says checkout-only — no cart creation in the load test. Pre-create a few carts manually before the test and reuse their IDs.

### Checklist

#### AWS Infrastructure
- [ ] Terraform for all resources (EC2, ALB, target groups, security groups)
- [ ] EC2 instances: Product ×3, Shopping Cart ×1, CCA ×1, RabbitMQ+Warehouse ×1
- [ ] Security groups: ALB open to internet; internal services only reachable from each other
- [ ] Warehouse instance NOT registered in any target group

#### ALB Configuration
- [ ] Path rules: `/products/*` and `/product` → TG-A, `/shopping-*` → TG-B, `/credit-card-*` → TG-C
- [ ] Register 2 good + 1 bad Product instances in TG-A
- [ ] `load_balancing_anomaly_mitigation = "on"` on TG-A (ATW)
- [ ] `unhealthy_threshold = 10` so bad instance is never removed
- [ ] Verify routing works: curl each path through ALB before load test

#### Load Tester
- [ ] Java, runs on its own EC2 instance in AWS
- [ ] Sends 200,000 checkout-only POST requests (no cart creation)
- [ ] Pre-create test carts before starting the timed section
- [ ] Configurable thread count (start 100, tune up/down)
- [ ] Prints: success count, failure count, wall time, throughput

#### Screenshots
- [ ] ALB console → Target Group A → Targets tab: shows traffic split (good vs bad proportion)
- [ ] RabbitMQ console → queue graph: shows flat plateau, not a spike
- [ ] Warehouse terminal output on shutdown: total orders number

#### Report
- [ ] All team member names + repo URL
- [ ] ALB/ATW screenshot: label which instance is bad, describe how proportion changes
- [ ] RabbitMQ screenshot: describe queue shape and what it means
- [ ] Warehouse shutdown screenshot
- [ ] Performance table: success, failure, wall time, throughput
- [ ] 1–2 sentences of observations per screenshot

#### Definition of Done
- [ ] All path routing works correctly through ALB
- [ ] ATW is demonstrably shifting traffic away from bad Product instance
- [ ] Load test completes 200,000 requests from AWS with metrics printed
- [ ] Report PDF is complete with all required screenshots and names

---

## Work Phases (9 Days)

| Phase | Days | Owner(s) |
|---|---|---|
| 1. Contract Freeze | Day 1 | All |
| 2. Local Service Build | Days 2–5 | All (parallel) |
| 3. Local Integration | Day 6 | All |
| 4. AWS Deployment | Days 7–8 | S4 + all verify |
| 5. Load Test + Report | Day 9 | S4 leads |

### Phase 1: Contract Freeze (Day 1)
- [ ] Review `improved_api.yaml` together and resolve any ambiguities
- [ ] Agree on RabbitMQ message schema (fields, types)
- [ ] Agree on queue name, exchange type, durability settings
- [ ] Agree on health check endpoint path for each service
- [ ] Agree on internal service URLs for container networking
- [ ] Fill in all items in the **Shared Decisions** section above

> **Hard stop:** no one writes service code until this phase is done.

### Phase 2: Local Service Build (Days 2–5)
All four students build in parallel. Student 1 publishes `docker-compose.yml` on Day 2 so others can plug in immediately.

- [ ] Student 1: build Product (good + bad) + CCA + publish `docker-compose.yml`
- [ ] Student 2: build and test Shopping Cart with channel pool
- [ ] Student 3: build and test RabbitMQ + Warehouse consumer
- [ ] Student 4: write Terraform, provision AWS instances, configure ALB
- [ ] Each student: service runs in Docker and passes local tests before Day 6

### Phase 3: Local Integration (Day 6)
Prove the complete end-to-end flow locally before touching AWS:
- [ ] Checkout success: SCS → CCA (200) → RabbitMQ publish confirmed → Warehouse ACK
- [ ] Checkout declined: SCS → CCA (402) → error returned, no RabbitMQ publish
- [ ] Checkout invalid card: 400 returned, no publish
- [ ] Bad Product instance returns mixed results without failing health checks
- [ ] All services reachable via Docker Compose

> **Do not move to AWS until local integration is fully working.**

### Phase 4: AWS Deployment (Days 7–8)
- [ ] Deploy all containers to AWS
- [ ] Configure ALB with path-based routing and target groups
- [ ] Register 2 good + 1 bad Product instances in Target Group A
- [ ] Enable ATW, verify bad instance stays in service
- [ ] Verify all service-to-service calls work in AWS
- [ ] Run Pre-Load-Test checklist before Day 9

### Phase 5: Load Test + Report (Day 9)
- [ ] Run 200,000 checkout-only requests from AWS client
- [ ] Tune thread counts and prefetch if needed
- [ ] Capture all required screenshots
- [ ] Fill in performance results table
- [ ] Write observations and assemble final PDF
- [ ] Submit PDF + repo link

---

## Pre-Load-Test Integration Checklist

- [ ] Product service responds correctly behind the ALB
- [ ] Shopping Cart can reach CCA and RabbitMQ
- [ ] Checkout only publishes after CCA returns 200
- [ ] Publisher confirms are working (checkout fails if nack received)
- [ ] Warehouse uses manual ACKs (verify: kill consumer mid-run, messages requeue)
- [ ] Warehouse order counts correct under concurrent load
- [ ] Bad Product instance is healthy but returns 503 ~50% of the time
- [ ] ATW is shifting traffic away from bad instance (visible in ALB metrics)
- [ ] Queue length stays stable under load (not continuously growing)

---

## Final Submission Checklist

### Code Repo
- [ ] Product service (good) — 2 instances
- [ ] Product service (bad) — 1 instance
- [ ] Shopping Cart service
- [ ] Credit Card Authorizer
- [ ] RabbitMQ container config
- [ ] Warehouse consumer (Java)
- [ ] Dockerfiles for all services
- [ ] Terraform / AWS config files
- [ ] Java load tester

### Report PDF
- [ ] All team member names
- [ ] Repo URL
- [ ] ALB/ATW screenshot (what proportion to good vs bad? does it change over time?)
- [ ] RabbitMQ queue depth screenshot (flat plateau, ideally under 1,000)
- [ ] Warehouse shutdown screenshot (total order count)
- [ ] Performance results table
- [ ] Written observations

---

## Practical Notes

1. **Start local, not AWS.** Get everything working in Docker Compose before touching AWS.
2. **Freeze the message schema on Day 1.** Changing it after Student 2 and Student 3 have started breaks both simultaneously.
3. **Bad instance health check must always return 200.** If AWS marks it unhealthy and removes it, the ATW demo is ruined.
4. **Channel pool is required for performance.** Opening/closing a RabbitMQ channel per request will be too slow for 200,000 requests. Student 2 should use Apache Commons Generic Object Pool.
5. **Load test is checkout-only.** The spec says no need to simulate cart creation or addItem — just hammer the checkout endpoint directly.
6. **Queue shape matters for grading.** A flat plateau (even at 1,000 messages) scores better than a spike-and-drain `/\` profile.
7. **Test failure paths.** Graders will test declined cards and malformed card numbers.

---

## Team Sync Routine

- **Daily 15-min standup:** what finished, what is blocked, what interface changed
- **One integration owner per day:** pull latest, run full stack, report breakages immediately
- **Interface freeze:** no payload or schema changes after Phase 3 without team agreement
