-- Add user_id column to product table
ALTER TABLE products
ADD COLUMN user_id BIGINT;

-- Add foreign key constraint
ALTER TABLE products
ADD CONSTRAINT fk_product_user
FOREIGN KEY (user_id) REFERENCES users (id);

-- Add index on user_id for better performance
CREATE INDEX idx_products_user_id ON products (user_id);