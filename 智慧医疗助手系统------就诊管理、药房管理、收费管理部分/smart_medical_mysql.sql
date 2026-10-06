CREATE DATABASE IF NOT EXISTS smart_medical
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE smart_medical;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS dispense_record;
DROP TABLE IF EXISTS financial_report;
DROP TABLE IF EXISTS charge_record;
DROP TABLE IF EXISTS prescription;
DROP TABLE IF EXISTS medical_record;
DROP TABLE IF EXISTS drug_inventory;
DROP TABLE IF EXISTS doctor;
DROP TABLE IF EXISTS patient;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE patient (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    patient_no VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(50) NOT NULL,
    gender VARCHAR(10),
    birthday DATE,
    phone VARCHAR(20),
    id_card VARCHAR(18),
    allergy_history TEXT,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE doctor (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    doctor_no VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(50) NOT NULL,
    department VARCHAR(100),
    title VARCHAR(50),
    phone VARCHAR(20),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE medical_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    chief_complaint TEXT,
    present_illness TEXT,
    physical_exam TEXT,
    diagnosis VARCHAR(500),
    diagnosis_code VARCHAR(50),
    status VARCHAR(20) DEFAULT '进行中',
    record_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NULL,
    CONSTRAINT fk_medical_patient FOREIGN KEY (patient_id) REFERENCES patient(id),
    CONSTRAINT fk_medical_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE prescription (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    medical_record_id BIGINT,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    prescription_no VARCHAR(50) UNIQUE NOT NULL,
    drug_name VARCHAR(100) NOT NULL,
    specification VARCHAR(100),
    quantity INT DEFAULT 1,
    usage_desc VARCHAR(200),
    dosage VARCHAR(100),
    duration INT,
    unit_price DECIMAL(10,2),
    total_amount DECIMAL(10,2),
    status VARCHAR(20) DEFAULT '待审核',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    audit_time DATETIME NULL,
    audit_result VARCHAR(50),
    reject_reason VARCHAR(500),
    INDEX idx_prescription_patient (patient_id),
    INDEX idx_prescription_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE drug_inventory (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    drug_code VARCHAR(50) UNIQUE NOT NULL,
    drug_name VARCHAR(100) NOT NULL,
    specification VARCHAR(100),
    manufacturer VARCHAR(200),
    current_stock INT DEFAULT 0,
    min_stock INT DEFAULT 10,
    max_stock INT DEFAULT 1000,
    batch_no VARCHAR(50),
    production_date DATE,
    expiry_date DATE,
    purchase_price DECIMAL(10,2),
    retail_price DECIMAL(10,2),
    location VARCHAR(50),
    lock_quantity INT DEFAULT 0,
    INDEX idx_drug_name (drug_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE charge_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    patient_name VARCHAR(50) NOT NULL,
    charge_no VARCHAR(50) UNIQUE NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    insurance_amount DECIMAL(10,2) DEFAULT 0,
    personal_account DECIMAL(10,2) DEFAULT 0,
    cash_amount DECIMAL(10,2) DEFAULT 0,
    self_pay DECIMAL(10,2) DEFAULT 0,
    payment_method VARCHAR(20),
    status VARCHAR(20) DEFAULT '待支付',
    charge_time DATETIME NULL,
    invoice_no VARCHAR(50),
    insurance_serial_no VARCHAR(50),
    remark VARCHAR(500),
    prescription_id BIGINT,
    INDEX idx_charge_patient (patient_id),
    INDEX idx_charge_status (status),
    INDEX idx_charge_prescription (prescription_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE dispense_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    pharmacist_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    drug_name VARCHAR(100),
    quantity INT,
    dispense_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT '已发药',
    INDEX idx_dispense_prescription (prescription_id),
    INDEX idx_dispense_patient (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE financial_report (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    report_date DATE NOT NULL,
    total_income DECIMAL(10,2) DEFAULT 0,
    cash_amount DECIMAL(10,2) DEFAULT 0,
    wechat_amount DECIMAL(10,2) DEFAULT 0,
    alipay_amount DECIMAL(10,2) DEFAULT 0,
    insurance_amount DECIMAL(10,2) DEFAULT 0,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_report_date (report_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO patient (patient_no, name, gender, birthday, phone, allergy_history) VALUES
('P1001', '张三', '男', '1990-01-15', '13800138001', '青霉素过敏'),
('P1002', '李四', '女', '1985-06-20', '13800138002', '无'),
('P1003', '王五', '男', '1978-12-10', '13800138003', '磺胺类药物过敏');

INSERT INTO doctor (doctor_no, name, department, title) VALUES
('D001', '张医生', '内科', '主任医师'),
('D002', '李医生', '外科', '主治医师');

INSERT INTO drug_inventory (drug_code, drug_name, specification, manufacturer, current_stock, min_stock, retail_price, location, expiry_date) VALUES
('D001', '阿莫西林胶囊', '0.5g*20粒', '华北制药', 25, 50, 25.00, 'A-01-01', DATE_ADD(CURDATE(), INTERVAL 2 MONTH)),
('D002', '布洛芬缓释胶囊', '0.3g*24粒', '中美史克', 80, 50, 35.00, 'A-01-02', DATE_ADD(CURDATE(), INTERVAL 6 MONTH)),
('D003', '头孢克肟片', '0.1g*12片', '白云山制药', 60, 50, 45.00, 'A-01-03', DATE_ADD(CURDATE(), INTERVAL 1 MONTH));

INSERT INTO medical_record (patient_id, doctor_id, chief_complaint, status) VALUES
(1, 1, '发热、咳嗽3天', '待接诊'),
(2, 1, '头痛、恶心2天', '待接诊');

SELECT 'smart_medical MySQL database initialized' AS message;
