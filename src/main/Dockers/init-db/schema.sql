-- Ejecutar este archivo conectado a la base de datos tallerexpress.

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN','RECEPCIONISTA')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVO' CHECK (status IN ('ACTIVO','INACTIVO')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS clients (
    id SERIAL PRIMARY KEY,
    document VARCHAR(30) NOT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    email VARCHAR(120),
    address VARCHAR(180),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vehicles (
    id SERIAL PRIMARY KEY,
    client_id INT NOT NULL REFERENCES clients(id),
    plate VARCHAR(10) NOT NULL UNIQUE,
    brand VARCHAR(60) NOT NULL,
    model VARCHAR(60) NOT NULL,
    vehicle_year INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS spare_parts (
    id SERIAL PRIMARY KEY,
    reference_code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(80) NOT NULL,
    supplier VARCHAR(120) NOT NULL,
    stock_total INT NOT NULL CHECK (stock_total >= 0),
    stock_available INT NOT NULL CHECK (stock_available >= 0 AND stock_available <= stock_total),
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS service_orders (
    id SERIAL PRIMARY KEY,
    client_id INT NOT NULL REFERENCES clients(id),
    vehicle_id INT NOT NULL REFERENCES vehicles(id),
    mechanic_name VARCHAR(120) NOT NULL,
    entry_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    problem_description TEXT NOT NULL,
    diagnosis TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'ABIERTA' CHECK (status IN ('ABIERTA','EN_PROCESO','FINALIZADA','CANCELADA')),
    final_cost NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (final_cost >= 0)
);

CREATE TABLE IF NOT EXISTS order_parts (
    order_id INT NOT NULL REFERENCES service_orders(id) ON DELETE CASCADE,
    spare_part_id INT NOT NULL REFERENCES spare_parts(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
    PRIMARY KEY(order_id, spare_part_id)
);

CREATE TABLE IF NOT EXISTS inventory_movements (
    id SERIAL PRIMARY KEY,
    spare_part_id INT NOT NULL REFERENCES spare_parts(id),
    order_id INT REFERENCES service_orders(id),
    movement_type VARCHAR(30) NOT NULL,
    quantity INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_vehicles_client ON vehicles(client_id);
CREATE INDEX IF NOT EXISTS idx_parts_category ON spare_parts(category);
CREATE INDEX IF NOT EXISTS idx_parts_supplier ON spare_parts(supplier);
CREATE INDEX IF NOT EXISTS idx_orders_vehicle ON service_orders(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_orders_client ON service_orders(client_id);

-- Usuario inicial: admin / admin123
INSERT INTO users(username,password_hash,full_name,role,status)
VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrador', 'ADMIN', 'ACTIVO')
ON CONFLICT (username) DO NOTHING;
