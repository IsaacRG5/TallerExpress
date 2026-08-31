# TallerExpress — Prueba de desempeño M5.1

Aplicación de escritorio en **Java SE 17+**, **JOptionPane**, **JDBC + PostgreSQL**, arquitectura por capas y excepciones personalizadas para la gestión de un taller mecánico.

El proyecto fue estructurado para cubrir el enunciado: repuestos, clientes, vehículos, usuarios/autenticación, órdenes de servicio, CRUD con JDBC, transacciones, validaciones, logs tipo HTTP y documentación. fileciteturn0file0L21-L28

## 1. Tecnologías

- Java 17 o superior
- Maven
- PostgreSQL 16+
- JDBC PostgreSQL
- Swing/JOptionPane
- POO, interfaces, herencia mediante excepciones especializadas, encapsulamiento, abstracción y polimorfismo

## 2. Estructura

```text
TallerExpress/
├── pom.xml
├── docker-compose.yml
├── .env.example
├── .gitignore
├── README.md
├── docs/
│   ├── diagrama-clases.md
│   ├── diagrama-casos-uso.md
│   └── screenshots/
├── src/main/java/com/tallerexpress/
│   ├── Main.java
│   ├── controller/AppController.java
│   ├── dao/                         # Interfaces DAO
│   ├── dao/impl/                    # JDBC
│   ├── decorator/                   # Decorator de usuario
│   ├── exception/                   # Excepciones personalizadas
│   ├── model/                       # Entidades y enums
│   ├── service/                     # Interfaces de negocio
│   ├── service/impl/                # Reglas de negocio
│   └── util/                        # DB, hash, logs y tablas
└── src/main/resources/db/schema.sql
```

La separación sigue las capas solicitadas por el enunciado: `controller`, `service`, `dao` y `model`. fileciteturn0file0L16-L26

## 3. Requisitos previos

1. Java 17+.
2. Maven.
3. PostgreSQL 16+ o Docker.
4. Git.

## 4. Base de datos

### Opción A — Docker

```bash
docker compose up -d
```

El contenedor crea automáticamente la base `tallerexpress`.

Después ejecuta `src/main/resources/db/schema.sql` desde DBeaver, pgAdmin o `psql`.

### Opción B — PostgreSQL local

Crea la base de datos `tallerexpress` y ejecuta el archivo `schema.sql`.

Variables opcionales:

```bash
export TE_DB_URL="jdbc:postgresql://localhost:5432/tallerexpress"
export TE_DB_USER="postgres"
export TE_DB_PASSWORD="postgres"
```

Si no defines variables, la aplicación usa esos valores como predeterminados.

## 5. Usuario inicial

- Usuario: `admin`
- Contraseña: `admin123`
- Rol: `ADMIN`
- Estado: `ACTIVO`

La contraseña se almacena como SHA-256, no como texto plano.

## 6. Ejecución

```bash
mvn clean compile
mvn exec:java
```

También puedes ejecutar `com.tallerexpress.Main` directamente desde NetBeans o IntelliJ.

## 7. Funcionalidades implementadas

### Repuestos

- Registrar.
- Editar.
- Activar/desactivar.
- Listar.
- Filtrar por categoría y proveedor.
- Código de referencia único.
- Stock total y disponible con validación.
- Tabla alineada con `[ACTIVO]` / `[INACTIVO]`.

El enunciado exige estos campos mínimos y el filtrado por categoría/proveedor. fileciteturn0file0L49-L56

### Clientes y vehículos

- Registrar clientes.
- Editar clientes.
- Activar/desactivar clientes.
- Registrar vehículos asociados.
- Placa única.
- Historial de vehículos por cliente.
- Validación de cliente registrado.

### Usuarios y autenticación

- Login.
- Roles `ADMIN` y `RECEPCIONISTA`.
- CRUD de usuarios.
- Logs `GET`, `POST`, `PATCH`, `DELETE` en consola.
- Decorator `DefaultUserPropertiesDecorator` que agrega automáticamente:
  - `role = RECEPCIONISTA`
  - `status = ACTIVO`
  - `createdAt = now()`

El decorator permite cumplir el requisito de agregar propiedades por defecto sin modificar la lógica base de `UserServiceImpl`. fileciteturn0file0L64-L69

### Órdenes de servicio

- Cliente.
- Vehículo.
- Mecánico.
- Fecha de ingreso.
- Problema.
- Diagnóstico.
- Repuestos utilizados.
- Estado.
- Costo total.
- Actualización de estado.
- Historial por vehículo.

El costo se calcula como `cantidad × precioUnitario` y se suma para todos los repuestos. fileciteturn0file0L70-L82

## 8. Transacciones JDBC

El registro de una orden utiliza:

```text
setAutoCommit(false)
       ↓
insertar orden
       ↓
validar y descontar stock
       ↓
registrar repuestos de la orden
       ↓
registrar movimiento de inventario
       ↓
commit()

Si ocurre un error:
rollback()
```

La finalización/actualización también utiliza una transacción. El stock numérico se descuenta al registrar el consumo para evitar doble descuento; al finalizar se confirma el movimiento de inventario dentro de la misma transacción. Esto mantiene coherencia del inventario. El requisito del enunciado exige transacciones para registro y cierre de órdenes. fileciteturn0file0L95-L103

Todos los DAO usan `try-with-resources` para liberar conexiones, statements y resultsets.

## 9. Validaciones y excepciones

Excepciones personalizadas:

- `BusinessException`
- `DuplicateException`
- `StockException`
- `AuthenticationException`

Reglas:

- Código de repuesto único.
- Stock >= 0.
- Stock disponible <= stock total.
- Cliente activo para una orden.
- Placa única.
- Vehículo registrado y perteneciente al cliente.
- Cantidades de repuestos > 0.
- Orden válida.
- Costo final >= 0.

El enunciado exige captura de errores, mensajes en JOptionPane y detalles en consola. fileciteturn0file0L104-L116

## 10. POO aplicada

- **Encapsulamiento:** atributos privados + getters/setters.
- **Abstracción:** interfaces DAO y Service.
- **Polimorfismo:** controladores trabajan con interfaces de servicios.
- **Herencia:** excepciones especializadas heredan de `BusinessException`.
- **Composición:** `ServiceOrder` contiene una lista de `OrderPart`.
- **Decorator Pattern:** `DefaultUserPropertiesDecorator` envuelve `UserCreator`.

## 11. Diagramas

- Diagrama de clases: `docs/diagrama-clases.md`
- Diagrama de casos de uso: `docs/diagrama-casos-uso.md`

GitHub renderiza los diagramas Mermaid incluidos en Markdown.

## 12. Capturas de interfaz

La carpeta `docs/screenshots/` contiene vistas de referencia de las pantallas JOptionPane que debe mostrar la aplicación:

- `01-login.png`
- `02-menu-principal.png`
- `03-listado-repuestos.png`

Para una entrega académica final, se recomienda reemplazar estas vistas de referencia por capturas tomadas directamente al ejecutar el proyecto en el equipo del Coder.

## 13. Logs de llamadas HTTP simuladas

Ejemplos que aparecen en consola:

```text
[2026-08-31T13:30:00] POST /login -> admin
[2026-08-31T13:31:00] POST /repuestos -> FILTRO-001
[2026-08-31T13:32:00] PATCH /repuestos/1 -> FILTRO-001
[2026-08-31T13:33:00] GET /repuestos -> /frenos
[2026-08-31T13:34:00] POST /ordenes -> 10
[2026-08-31T13:35:00] PATCH /ordenes/10 -> estado=FINALIZADA
```

## 14. Entrega

Antes de subir a GitHub:

```bash
git init
git add .
git commit -m "feat: implement TallerExpress M5.1"
git branch -M main
git remote add origin TU_REPOSITORIO
 git push -u origin main
```

Luego comprime la carpeta completa como `TallerExpress.zip`.

## 15. Datos del Coder

Completar antes de entregar:

- Nombre: **[TU NOMBRE]**
- Clan: **[TU CLAN]**
- Correo: **[TU CORREO]**
- Documento: **[TU DOCUMENTO]**
- GitHub: **[URL DEL REPOSITORIO]**

## 16. Checklist de la prueba

- [x] Java SE 17+
- [x] JOptionPane
- [x] JDBC
- [x] PostgreSQL
- [x] Arquitectura por capas
- [x] DAO interfaces
- [x] SELECT / INSERT / UPDATE / DELETE
- [x] Repuestos
- [x] Clientes
- [x] Vehículos
- [x] Usuarios
- [x] Login y roles
- [x] Decorator para propiedades por defecto
- [x] Órdenes de servicio
- [x] Historial por vehículo
- [x] Cálculo de costo
- [x] Transacciones y rollback
- [x] try-with-resources
- [x] Excepciones personalizadas
- [x] Validaciones de negocio
- [x] Logs tipo HTTP
- [x] Tablas en JOptionPane
- [x] README
- [x] Diagrama de clases
- [x] Diagrama de casos de uso
- [x] Capturas de interfaz de referencia
