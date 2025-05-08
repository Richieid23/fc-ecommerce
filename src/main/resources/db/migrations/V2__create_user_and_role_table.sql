-- Create User Table
CREATE TABLE IF NOT EXISTS "users" (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create Role Table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create User Role Junction Table
CREATE TABLE IF NOT EXISTS users_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES "users" (id),
    FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- Add user_id column to table product
ALTER TABLE products
ADD COLUMN user_id BIGINT;

-- Add foreign key constraint
ALTER TABLE products
ADD CONSTRAINT fk_product_user
FOREIGN KEY (user_id) REFERENCES "users" (id);

-- Add Indexes
CREATE INDEX idx_user_username ON "users" (username);
CREATE INDEX idx_user_email ON "users" (email);
CREATE INDEX idx_role_name ON roles (name);
CREATE INDEX idx_product_user_id ON products (user_id);

-- Insert some default role
INSERT INTO roles (name, description) VALUES
('ROLE_USER', 'Standard user role'),
('ROLE_ADMIN', 'Administrative user role');