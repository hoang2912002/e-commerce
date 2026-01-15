# Fashion Shop Backend (Spring Boot)

## Overview
🚀 Java Spring Boot microservices with a complete ecosystem: PostgreSQL, Kafka (KRaft mode, no Zookeeper), JWT authentication, Spring Cloud Gateway, OpenFeign for inter-service communication.

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
## Key Features
- Developed RESTful APIs following layered architecture (Controller – Service – Repository)
- Full CRUD operations for core entities such as users, products, categories, orders, inventories, promotions, and shippings
- JWT-based authentication and authorization:
  - Login, Logout, Refresh Token
  - Role-Based Access Control (RBAC)
- Business workflows:
  - Product publishing approval
  - Inventory update approval
- Event-driven processing using Kafka for order, payment, and notification flows
- Integration with third-party shipping APIs to calculate delivery time and shipping cost
- Centralized exception handling and request validation
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
│              ┌────────────────────────────┐                 │
│              │        Kafka Cluster       │                 │        
│              │         (Port 9092)        │                 │
│              │                            │                 │
│              │ Topics:                    │                 │ 
│              │ • permission-register      │                 │
│              │ • user-created             │                 │
│              │ • order-created            │                 │  
│              │ • product-created          │                 │    
│              │ • shop-management-created  │                 │
│              │ • shipping-delivery-success│                 │
│              └────────────────────────────┘                 │
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
| **API Gateway** | Single entry point, routing, JWT auth | Spring Cloud Gateway, DevTools, Lombok|
| **Identity service** | Authentication, user & role management | Spring Data JPA, Spring Boot Starter, Kafka, Spring security, Spring Data JPA, Feign Client, MySQL, Lombok |
| **Notification Service** | Asynchronous notifications (email) | Kafka, MongoDB, ThymeLeaf, Spring Boot Starter, Lombok |
| **Product Service** | Manages product master data, variants, shop configuration, and approval workflows. | Spring Boot Starter, Spring Data JPA, Kafka, Feign Client, PostgreSQL, Lombok |
| **Resource Service** | Manages file. | Spring Boot Starter, Kafka, MongoDB |

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
| **MySQL** | 3306 |
| **MongoDB** | 27017 |
| **PostgreSQL** | 5432 |
| **Kafka** | 9094 |
| **PgAdmin** | 5050 |
