ALTER TABLE users ADD COLUMN patient_type VARCHAR(16);

CREATE INDEX idx_users_patient_type ON users(patient_type);

