ALTER TABLE bills ADD COLUMN payment_method VARCHAR(16);

CREATE INDEX idx_bills_payment_method ON bills(payment_method);

