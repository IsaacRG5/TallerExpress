# TallerExpress 

A desktop application for an auto repair shop developed using **Java SE 17+**, **JOptionPane**, **JDBC + PostgreSQL**, and a layered architecture.

The system centralizes the management of parts, customers, vehicles, users, and service orders, with business validations, custom 

## 1. Tecnologías

- Java SE 17+
- Maven
- PostgreSQL 16
- JDBC for PostgreSQL
- Swing / JOptionPane
- Object-Oriented Programming
- Docker Compose 

## 2. Project Structure

```text
TallerExpress/
├── pom.xml
├── .env.example
├── .gitignore
├── README.md
└── src/main/
    ├── java/com/tallerexpress/
    │   ├── TallerExpressApp.java
    │   ├── config/
    │   ├── controller/
    │   ├── exception/
    │   ├── model/
    │   ├── repository/
    │   │   └── impl/
    │   ├── service/
    │   │   └── impl/
    │   └── view/
    └── dockers/db/
    └── dockers/db/
        └──docker-compose.yml
        ├── db
             └── schema.sql
```

## 3. Prerequisites

- JDK 17 or later.
- Maven 3.8+ or Maven integrated into IntelliJ IDEA.
- PostgreSQL 16+ or Docker Desktop.
- DBeaver, pgAdmin, or `psql` for managing the database.

## 4. Configuring PostgreSQL with Docker

From the project root directory:

```bash
docker compose up -d
```

Configuration used by `docker-compose.yml`:

```text
Database: tallerexpress
Username:       postgres
Password:    postgres
Port:        5432
```

Verification:

```bash
docker ps
```

## 5. Running from IntelliJ IDEA

1. Open the project folder.
2. Select JDK 17 or higher.
3. Open the **Maven** window.
4. Run `Lifecycle > clean`.
5. Run `Lifecycle > compile`.
6. Run the class:

```text
com.tallerexpress.TallerExpressApp
```

It can also be run using Maven:

```bash
mvn clean compile
mvn exec:java
```

## 6. Features

### Replacement Parts

- Add.
- Edit.
- Activate/deactivate.
- Delete when there are no dependencies.
- List.
- Filter by category and supplier.
- Unique reference code.
- Validation of total and available stock.
- Non-negative price.
- Listed in text tables with `[ACTIVE]` / `[INACTIVE]`.

### Customers

- Add.
- Edit.
- List.
- Delete when there are no associated vehicles or orders.
- Validation of required fields.

### Vehicles

- Add a vehicle associated with a customer.
- Edit.
- List.
- Delete when there are no associated orders.
- View vehicles by customer.
- Validate unique license plate.
- Validate registered customer.

### Users and Authentication

- Login with username and password.
- `ADMIN` and `RECEPTIONIST` roles.
- Active/inactive users.
- User CRUD operations.
- The logged-in user cannot delete their own account.
- Only `ADMIN` can manage users.
- Decorator to create users with `RECEPTIONIST`, `ACTIVE`, and `createdAt` as default values.

### Service Orders

- Customer.
- Vehicle.
- Mechanic in charge.
- Automatic check-in date.
- Problem description.
- Diagnosis.
- Parts used.
- Status: `OPEN`, `IN_PROCESS`, `COMPLETED`, `CANCELED`.
- Status update.
- Cost calculation using quantity × unit price.
- Service history by vehicle.
- Inventory control.
- Inventory transaction log.


## 7. Applied OOP

- **Encapsulation:** private attributes and accessor methods in the models.
- **Abstraction:** DAO and Service interfaces.
- **Polymorphism:** implementations are used via interfaces.
- **Inheritance:** Specialized exceptions inherit from `BusinessException`.
- **Composition:** `ServiceOrder` contains a list of `OrderPart` objects.
- **Decorator:** `DefaultUserPropertiesDecorator` wraps `UserCreator` to add default properties.

## 8. HTTP Logs

Major operations generate console logs, for example:

```text
[date] POST /login -> admin
[date] POST /parts -> FILTER-001
[date] GET /parts -> /brakes
[date] PATCH /parts/1 -> FILTER-001
[date] DELETE /parts/1 ->
[date] POST /orders -> 10
[date] PATCH /orders/10 -> status=COMPLETED, cost=250000
```

The `GET`, `POST`, `PATCH`, and `DELETE` methods are used.

## 9. Creator Information and GitHub 

Name: Isaac David Ortiz Guzman.
Clan: Puerta de Oro.

https://github.com/IsaacRG5/TallerExpress.git

