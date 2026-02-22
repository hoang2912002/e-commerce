# Fashion Shop Backend (Spring Boot)

## Overview
🚀 Architected and developed a high-performance Java Spring Boot microservices platform powered by PostgreSQL and Kafka (KRaft mode, Zookeeper-free).
Implemented secure API Gateway with JWT authentication and service-to-service communication via OpenFeign. Enhanced system throughput by leveraging distributed caching with Redisson-based locking to prevent cache stampede and protect the database under high concurrency. Optimized inter-service API aggregation using parallel execution with CompletableFuture and Java Virtual Threads, significantly reducing response latency and improving scalability.

## 📌 Getting Started
### 🐳 Start Docker Containers
- Run the following command to start all required infrastructure services (PostgreSQL, MySQL, Redis, Kafka, etc.):
```
docker-compose -f environment/docker-compose-dev.yml up -d
```
- To stop the containers:
```
docker-compose -f environment/docker-compose-dev.yml down
```
### 🗄️ Database Configuration

**PostgreSQL (pgAdmin)**
- Access to pgAdmin: 
    - Open your browser and navigate to: 
    http://localhost:5050
    - Login with the following credentials:
        - **Email:** admin@admin.com  
        - **Password:** root  
- Create a New Server:
    - Right-click on **Servers**
    - Select **Register → Server**
    - Configure as follows:
        - **Tab General:** - *Name:* local-postgres (or any preferred name). Example: postgre
        - **Tab Connection:**
            - *Host name/address*: **postgres-db** (Docker container name)
            - *Post:* **5432**
            - *Username:* **postgres**
            - *Password:* **123456**
    - Click **Save** to complete the setup.

**MySQL**
- Install MySQL Workbench (Windows): https://dev.mysql.com/downloads/file/?id=549397
- Connection Configuration:
    - *Host:* **localhost**  
    - *Port:* **3306**  
    - *Username:* **root**  
    - *Password:* **123456**   
- Click **Test Connection** (if using GUI tools), then **Connect**.

## Project Structure
```
microservice-clothing-shop/
├── api-gateway/            # Spring Cloud Gateway (routing, auth)
│   ├── pom.xml
│   └── src/main/java/
├── identity-service/       # User management
│   ├── pom.xml
│   └── src/main/java/
├── inventory-service/      # Inventory & stock management
│   ├── pom.xml
│   └── src/main/java/
├── order-service/          # Order management
│   ├── pom.xml
│   └── src/main/java/
├── notification-service/   # Notification management (email)
│   ├── pom.xml
│   └── src/main/java/
├── product-service/        # Product & shop management
│   ├── pom.xml
│   └── src/main/java/
├── resource-service/       # File & resource management (images)
│   ├── pom.xml
│   └── src/main/java/
├── payment-service/        # Payment processing & transaction handling
│   ├── pom.xml
│   └── src/main/java/
└── shipping-service/       # Shipping & delivery management
    ├── pom.xml
    └── src/main/java/
```
## 🚀 Technical Highlights & Key Features
### Backend Architecture
- Layered Architecture: Implemented a clean (Controller – Service – Repository) pattern for scalability and maintainability.
- Core Entities Management: Full CRUD operations for Users, Products, Categories, Orders, Inventories, Promotions, and Shippings...
- Security: JWT-based authentication/authorization including Login, Logout, Refresh Token mechanisms and Role-Based Access Control (RBAC).

### Distributed Transactions & Consistency (Saga Pattern)
- Saga Orchestration (Kafka-based): Orchestrated complex order workflows across multiple services: Order, Promotion, Coupon, Inventory, Shipping, and Payment.
- Saga Choreography: Decentralized product-inventory synchronization.
- Atomic Operations with Lua Scripts: Utilized Lua scripting in Redis to ensure Atomic transactions for Inventory, Promotion, and Coupon balance updates, preventing race conditions.

### Performance & Caching Strategy
- Multi-level Caching: Combined Local Cache (Guava Cache) for ultra-fast access and Distributed Redis Cache with Optimistic Key (Version) for shared data consistency.
- Distributed Locking: Applied Redisson distributed locks to prevent cache stampede, reduce database pressure, and protect the system from Redis failure–induced DB overload.
- Data Warm-up & Cronjobs: Automated background tasks to synchronize data from atomic updates and pre-load (warm-up) hot data into Redis for peak performance.

### System Resilience & Stability
- Resilience4j Integration: Enhanced system reliability using Circuit Breaker, Retry, and Rate Limiter.

### Database Optimization
- Table Partitioning & Indexing: Optimized high-volume tables (Order, Shipping, Payment) by Monthly Partitioning (Couple key: Id + Created_at) and strategic indexing to ensure query performance as data scales.

### Third-party Integrations
- Shipping Services: Integrated with Giao Hang Nhanh (GHN) API for real-time delivery time estimation and shipping cost calculation.
- Payment Simulation: Mocked payment gateways for Momo, VnPay and COD to simulate end-to-end transaction flows.

## 🏗️ System Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                     Client Layer                            │
│  Web Browser, Postman, curl, etc.                           │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTP/REST
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway                              │
│                    (Port 8000)                              │
│  Validate JWT, Route request to internal business service   │            
└─────────────────────┬───────────────────────────────────────┘  
                      │ HTTP/REST - internal service
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                  Service Layer                              │
│                                                             │
│  ┌──────────────────┐         ┌────────────────────┐        │
│  │ Identity Service │────────▶|  Product Service   │        │
│  │   (Port 8080)    │  Feign  │   (Port 8081)      │        │
│  │                  │  Client │                    │        │
│  │• Authentication  │         │• Product Management│        │
│  │• User Management │         │• Shop Management   │        │
│  │• Role Management │         │• Approval workflows│        │
│  └─────────┬────────┘         └────────────────────┘        │
│            │  Feign Client                                  │
│  ┌─────────▼─────────┐         ┌────────────────────────┐   │
│  │ Order Service     │◄────────┤ Inventory Service      │   │
│  │   (Port 8082)     │  Feign  │   (Port 8083)          │   │
│  │                   │  Client │                        │   │
│  │• Order Management │         │• Inventory Management  │   │
│  │• Coupon Management│         │• Transaction management│   │
│  └───────────────────┘         └────────────────────────┘   │
│                                                             │
│  ┌────────────────────┐         ┌─────────────────────┐     │
│  │ Payment Service    │         │ Shipping Service    │     │
│  │   (Port 8084)      │         │   (Port 8085)       │     │
│  │                    │         │                     │     │
│  │• Payment method    │         │• Shipping Management│     │
│  │• Payment Management│         │• Api third-party    │     │
│  └────────────────────┘         └─────────────────────┘     │
│                                                             │  
│  ┌─────────────────────┐         ┌─────────────────┐        │
│  │ Notification Service│         │ Resource Service│        │
│  │   (Port 8086)       │         │   (Port 8087)   │        │
│  │                     │         │                 │        │
│  │ • Send mail         │         │• File management│        │
│  └─────────────────────┘         └─────────────────┘        │
└────────────────────────┬────────────────────────────────────┘
                         │ Kafka Events
                         ▼               
┌─────────────────────────────────────────────────────────────┐
│                  Message Layer                              │
│                                                             │
│       ┌───────────────────────────────────────┐             │
│       │        Kafka Cluster                  │             │        
│       │         (Port 9092)                   │             │
│       │                                       │             │
│       │ Topics:                               │             │ 
│       │ • permission-register                 │             │
│       │ • user-created                        │             │
│       │ • order-created (Saga Orchestration)  │             │  
│       │ • product-created (Saga Choreography) │             │    
│       │ • shop-management-created             │             │
│       │ • shipping-delivery-success           │             │
│       └───────────────────────────────────────┘             │
└─────────────────────────────────────────────────────────────┘                                                   
┌─────────────────────────────────────────────────────────────┐
│                  Data Layer                                 │
│                                                             │
│ ┌───────────────┐  ┌───────────────┐   ┌───────────────┐    │
│ │  Identity DB  │  │  Product DB   │   │  Order DB     │    │
│ │     MySQL     │  │   PostgreSQL  │   │  PostgreSQL   │    │
│ │  (Port 3306)  │  │  (Port 5432)  │   │  (Port 5432)  │    │
│ │               │  │               │   │               │    │
│ └───────────────┘  └───────────────┘   └───────────────┘    │
│                                                             │
│ ┌───────────────┐  ┌───────────────┐   ┌───────────────┐    │  
│ │  Inventory DB │  │  Payment DB   │   │  Shipping DB  │    │
│ │   PostgreSQL  │  │   PostgreSQL  │   │   PostgreSQL  │    │
│ │  (Port 5432)  │  │  (Port 5432)  │   │  (Port 5432)  │    │
│ │               │  │               │   │               │    │
│ └───────────────┘  └───────────────┘   └───────────────┘    │
│                                                             │
│ ┌───────────────┐                                           │
│ │  Resource DB  │                                           │
│ │   MongoDB     │                                           │
│ │  (Port 27017) │                                           │
│ │               │                                           │ 
│ └───────────────┘                                           │
└─────────────────────────────────────────────────────────────┘
```
## 📊 Module & Service Overview

### Maven Modules
| Module | Purpose | Dependencies |
|--------|---------|-------------|
| **API Gateway** | Entry point, centralized routing, and global JWT authentication. | Spring Cloud Gateway, DevTools, Lombok|
| **Identity Service** | AAA (Authentication, Authorization, Accounting) & RBAC management. | Spring Data JPA, Spring Boot Starter, DevTools, Kafka, Mapstruct, Spring security, Feign Client, MySQL, Lombok, Redis, Redisson, Guava, Resilience 4j |
| **Inventory Service** | Inventory management, Stock Reservation, and Warehouse synchronization. | Spring Data JPA, Spring Boot Starter, Kafka, Spring security, Validation, Feign Client, DevTools, PostgreSQL, Mapstruct, Lombok, Redis, Redisson, Guava, Resilience 4j |
| **Notification Service** | Asynchronous notifications (email). | Kafka, Mapstruct, MongoDB, ThymeLeaf, Spring Boot Starter, Lombok |
| **Order Service** | Saga Orchestrator, order lifecycle, and coupon management. | Spring Data JPA, Spring Boot Starter, Kafka, Spring security, Validation, Feign Client, DevTools, PostgreSQL, Mapstruct, Lombok, Redis, Redisson, Guava, Resilience 4j |
| **Payment Service** | Payment processing, transaction recording, and Third-party Mocks. | Spring Data JPA, Spring Boot Starter, Kafka, Spring security, Validation, Feign Client, DevTools, PostgreSQL, Mapstruct, Lombok, Redis, Redisson, Guava, Resilience 4j |
| **Product Service** | Master data (Product, Variant), Approval Workflows, and Shop config. | Spring Boot Starter, DevTools, Spring Data JPA, Kafka, Feign Client, Spring security, PostgreSQL, Lombok, Redis, Redisson, Guava, Resilience 4j |
| **Resource Service** | Centralized file storage and meta-data management. | Spring Boot Starter, DevTools, Spring security, Kafka, MongoDB |
| **Shipping Service** | Shipping transaction logic & GHN Integration. | Spring Data JPA, Spring Boot Starter, DevTools, Kafka, Mapstruct, Spring security, Feign Client, PostgreSQL, Lombok, Redis, Redisson, Guava, Resilience 4j |

### Runtime Services
| Service | Port |
|---------|------|
| **Api gateway** | 8000 |
| **Identity service** | 8080 |
| **Product Service** | 8081 |
| **Order Service** | 8082 |
| **Inventory Service** | 8083 |
| **Payment Service** | 8084 |
| **Shipping Service** | 8085|
| **Notification Service** | 8086 |
| **Resource Service** | 8087|
| **Redis** | 6319 |
| **MySQL** | 3306 |
| **MongoDB** | 27017 |
| **PostgreSQL** | 5432 |
| **Kafka** | 9094 |
| **Kafka UI** | 8888 |
| **PgAdmin** | 5050 |