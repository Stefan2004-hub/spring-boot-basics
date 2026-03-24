ALTER TABLE orders
    ALTER COLUMN created_at TYPE DATE
    USING created_at::date;
