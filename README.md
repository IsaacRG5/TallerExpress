# TallerExpress 

Aplicación de escritorio para un taller mecánico desarrollada con **Java SE 17+**, **JOptionPane**, **JDBC + PostgreSQL** y arquitectura por capas.

El sistema centraliza la gestión de repuestos, clientes, vehículos, usuarios y órdenes de servicio, con validaciones de negocio, excepciones personalizadas, autenticación por roles, logs tipo HTTP y transacciones JDBC.

## 1. Tecnologías

- Java SE 17+
- Maven
- PostgreSQL 16
- JDBC PostgreSQL
- Swing / JOptionPane
- Programación Orientada a Objetos
- Docker Compose (opcional para PostgreSQL)

## 2. Estructura del proyecto

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

La separación corresponde a las capas del proyecto: `controller`, `service`, `repository` y `model`. `repository` contiene las interfaces DAO y sus implementaciones JDBC.

## 3. Requisitos previos

- JDK 17 o superior.
- Maven 3.8+ o Maven integrado en IntelliJ IDEA.
- PostgreSQL 16+ o Docker Desktop.
- DBeaver, pgAdmin o `psql` para administrar la base de datos.

## 4. Configuración de PostgreSQL con Docker

Desde la carpeta raíz del proyecto:

```bash
docker compose up -d
```

Configuración utilizada por `docker-compose.yml`:

```text
Base de datos: tallerexpress
Usuario:       postgres
Contraseña:    postgres
Puerto:        5432
```

Verificación:

```bash
docker ps
```

## 5. Ejecución desde IntelliJ IDEA

1. Abrir la carpeta del proyecto.
2. Seleccionar JDK 17 o superior.
3. Abrir la ventana **Maven**.
4. Ejecutar `Lifecycle > clean`.
5. Ejecutar `Lifecycle > compile`.
6. Ejecutar la clase:

```text
com.tallerexpress.TallerExpressApp
```

También puede ejecutarse mediante Maven:

```bash
mvn clean compile
mvn exec:java
```

## 6. Funcionalidades

### Repuestos

- Registrar.
- Editar.
- Activar/desactivar.
- Eliminar cuando no existan dependencias.
- Listar.
- Filtrar por categoría y proveedor.
- Código de referencia único.
- Validación de stock total y disponible.
- Precio no negativo.
- Listados en tablas de texto con `[ACTIVO]` / `[INACTIVO]`.

### Clientes

- Registrar.
- Editar.
- Listar.
- Eliminar cuando no existan vehículos u órdenes asociadas.
- Validación de datos obligatorios.

### Vehículos

- Registrar asociado a un cliente.
- Editar.
- Listar.
- Eliminar cuando no tenga órdenes asociadas.
- Consultar vehículos por cliente.
- Validación de placa única.
- Validación de cliente registrado.

### Usuarios y autenticación

- Login con usuario y contraseña.
- Roles `ADMIN` y `RECEPCIONISTA`.
- Usuarios activos/inactivos.
- CRUD de usuarios.
- El usuario conectado no puede eliminarse a sí mismo.
- Solo `ADMIN` gestiona usuarios.
- Decorator para crear usuarios con `RECEPCIONISTA`, `ACTIVO` y `createdAt` por defecto.

### Órdenes de servicio

- Cliente.
- Vehículo.
- Mecánico responsable.
- Fecha de ingreso automática.
- Descripción del problema.
- Diagnóstico.
- Repuestos utilizados.
- Estado: `ABIERTA`, `EN_PROCESO`, `FINALIZADA`, `CANCELADA`.
- Actualización del estado.
- Cálculo del costo mediante cantidad × precio unitario.
- Historial de servicios por vehículo.
- Control de stock.
- Registro de movimientos de inventario.


## 7. POO aplicada

- **Encapsulamiento:** atributos privados y métodos de acceso en los modelos.
- **Abstracción:** interfaces DAO y Service.
- **Polimorfismo:** las implementaciones se utilizan mediante interfaces.
- **Herencia:** excepciones especializadas heredan de `BusinessException`.
- **Composición:** `ServiceOrder` contiene una lista de `OrderPart`.
- **Decorator:** `DefaultUserPropertiesDecorator` envuelve `UserCreator` para agregar propiedades por defecto.

## 8. Logs tipo HTTP

Las operaciones principales generan trazas en consola, por ejemplo:

```text
[fecha] POST /login -> admin
[fecha] POST /repuestos -> FILTRO-001
[fecha] GET /repuestos -> /frenos
[fecha] PATCH /repuestos/1 -> FILTRO-001
[fecha] DELETE /repuestos/1 ->
[fecha] POST /ordenes -> 10
[fecha] PATCH /ordenes/10 -> estado=FINALIZADA, costo=250000
```

Se utilizan los métodos `GET`, `POST`, `PATCH` y `DELETE`.



## 9. GitHub y ZIP



## 10. Checklist final

Antes de entregar, verificar:

- [ ] Java 17+ configurado.
- [ ] PostgreSQL funcionando.
- [ ] `schema.sql` ejecutado.
- [ ] Login probado.
- [ ] CRUD de usuarios probado.
- [ ] CRUD de repuestos probado.
- [ ] CRUD de clientes probado.
- [ ] CRUD de vehículos probado.
- [ ] Registro de orden probado.
- [ ] Actualización/finalización de orden probada.
- [ ] Inventario comprobado antes y después de una orden.
- [ ] Rollback comprobado con una operación inválida.
- [ ] README completado con datos del Coder.
- [ ] Capturas reales agregadas.
- [ ] GitHub público creado.
- [ ] ZIP final generado.
