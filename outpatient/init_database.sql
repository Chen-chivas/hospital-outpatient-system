-- ============================================
-- 医院门诊挂号系统 - 数据库初始化脚本
-- 创建日期：2026-06-11
-- ============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS hospital
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE hospital;

-- ============================================
-- 1. 系统用户表 (sys_user)
-- 存储所有用户：患者、医生、管理员
-- ============================================
DROP TABLE IF EXISTS `registration_order`;
DROP TABLE IF EXISTS `missed_appointment`;
DROP TABLE IF EXISTS `blacklist`;
DROP TABLE IF EXISTS `number_source`;
DROP TABLE IF EXISTS `stop_application`;
DROP TABLE IF EXISTS `suspension_application`;
DROP TABLE IF EXISTS `schedule`;
DROP TABLE IF EXISTS `doctor_info`;
DROP TABLE IF EXISTS `patient_info`;
DROP TABLE IF EXISTS `sys_department`;
DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
    `user_id`       BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    `username`      VARCHAR(50)     NOT NULL                 COMMENT '用户名',
    `password`      VARCHAR(255)    NOT NULL DEFAULT '123456' COMMENT '密码',
    `real_name`     VARCHAR(50)     NOT NULL                 COMMENT '真实姓名',
    `phone`         VARCHAR(20)     DEFAULT NULL             COMMENT '手机号',
    `id_card`       VARCHAR(18)     DEFAULT NULL             COMMENT '身份证号',
    `role`          VARCHAR(20)     NOT NULL DEFAULT 'PATIENT' COMMENT '角色: PATIENT/DOCTOR/ADMIN',
    `avatar`        VARCHAR(255)    DEFAULT NULL             COMMENT '头像',
    `status`        INT             NOT NULL DEFAULT 1       COMMENT '状态: 1=启用 0=禁用',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role` (`role`),
    KEY `idx_real_name` (`real_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- ============================================
-- 2. 科室表 (sys_department)
-- ============================================
CREATE TABLE `sys_department` (
    `dept_id`       INT             NOT NULL AUTO_INCREMENT  COMMENT '科室ID',
    `dept_name`     VARCHAR(50)     NOT NULL                 COMMENT '科室名称',
    `dept_code`     VARCHAR(20)     DEFAULT NULL             COMMENT '科室编码',
    `parent_id`     INT             NOT NULL DEFAULT 0       COMMENT '上级科室ID, 0=一级科室',
    `dept_desc`     VARCHAR(255)    DEFAULT NULL             COMMENT '科室描述',
    `sort_num`      INT             NOT NULL DEFAULT 0       COMMENT '排序号',
    `status`        INT             NOT NULL DEFAULT 1       COMMENT '状态: 1=启用 0=禁用',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`dept_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科室表';

-- ============================================
-- 3. 患者信息表 (patient_info)
-- ============================================
CREATE TABLE `patient_info` (
    `patient_id`        BIGINT          NOT NULL             COMMENT '患者ID(关联sys_user.user_id)',
    `gender`            VARCHAR(4)      DEFAULT NULL         COMMENT '性别',
    `age`               INT             DEFAULT NULL         COMMENT '年龄',
    `address`           VARCHAR(255)    DEFAULT NULL         COMMENT '住址',
    `emergency_contact` VARCHAR(50)     DEFAULT NULL         COMMENT '紧急联系人',
    `emergency_phone`   VARCHAR(20)     DEFAULT NULL         COMMENT '紧急联系电话',
    `is_special`        INT             NOT NULL DEFAULT 0   COMMENT '是否特殊患者: 1=是 0=否',
    `special_type`      VARCHAR(50)     DEFAULT NULL         COMMENT '特殊类型(如:老年人/军人/孕妇)',
    PRIMARY KEY (`patient_id`),
    CONSTRAINT `fk_patient_user` FOREIGN KEY (`patient_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者信息表';

-- ============================================
-- 4. 医生信息表 (doctor_info)
-- ============================================
CREATE TABLE `doctor_info` (
    `doctor_id`         BIGINT          NOT NULL             COMMENT '医生ID(关联sys_user.user_id)',
    `dept_id`           INT             NOT NULL             COMMENT '所属科室ID',
    `title`             VARCHAR(20)     DEFAULT NULL         COMMENT '职称: 主任医师/副主任医师/主治医师/住院医师',
    `specialty`         VARCHAR(255)    DEFAULT NULL         COMMENT '专业擅长',
    `consultation_fee`  DECIMAL(10,2)   NOT NULL DEFAULT 0.00 COMMENT '普通挂号费',
    `expert_fee`        DECIMAL(10,2)   DEFAULT NULL         COMMENT '专家挂号费',
    PRIMARY KEY (`doctor_id`),
    KEY `idx_dept_id` (`dept_id`),
    CONSTRAINT `fk_doctor_user` FOREIGN KEY (`doctor_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_doctor_dept` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生信息表';

-- ============================================
-- 5. 排班表 (schedule)
-- ============================================
CREATE TABLE `schedule` (
    `schedule_id`       BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '排班ID',
    `doctor_id`         BIGINT          NOT NULL                 COMMENT '医生ID',
    `dept_id`           INT             NOT NULL                 COMMENT '科室ID',
    `schedule_date`     DATE            NOT NULL                 COMMENT '排班日期',
    `start_time`        TIME            NOT NULL                 COMMENT '出诊开始时间',
    `end_time`          TIME            NOT NULL                 COMMENT '出诊结束时间',
    `time_slot_duration` INT            NOT NULL DEFAULT 60      COMMENT '每个时段时长(分钟)',
    `total_slots`       INT             NOT NULL DEFAULT 8       COMMENT '总时段数',
    `slots_per_time`    INT             NOT NULL DEFAULT 5       COMMENT '每个时段号源数',
    `registration_fee`  DECIMAL(10,2)   NOT NULL DEFAULT 0.00    COMMENT '挂号费',
    `appointment_cycle` INT             NOT NULL DEFAULT 7       COMMENT '预约周期(天)',
    `status`            INT             NOT NULL DEFAULT 1       COMMENT '状态: 1=正常 0=已取消',
    `create_by`         BIGINT          DEFAULT NULL             COMMENT '创建人ID',
    `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`schedule_id`),
    KEY `idx_doctor_id` (`doctor_id`),
    KEY `idx_dept_id_date` (`dept_id`, `schedule_date`),
    KEY `idx_schedule_date` (`schedule_date`),
    CONSTRAINT `fk_schedule_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor_info` (`doctor_id`),
    CONSTRAINT `fk_schedule_dept` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排班表';

-- ============================================
-- 6. 号源表 (number_source)
-- ============================================
CREATE TABLE `number_source` (
    `source_id`         BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '号源ID',
    `schedule_id`       BIGINT          NOT NULL                 COMMENT '所属排班ID',
    `slot_number`       INT             NOT NULL                 COMMENT '时段序号(第几时段)',
    `slot_start_time`   TIME            NOT NULL                 COMMENT '时段开始时间',
    `slot_end_time`     TIME            NOT NULL                 COMMENT '时段结束时间',
    `total_count`       INT             NOT NULL DEFAULT 0       COMMENT '总号源数',
    `remaining_count`   INT             NOT NULL DEFAULT 0       COMMENT '剩余号源数',
    `locked_count`      INT             NOT NULL DEFAULT 0       COMMENT '锁定号源数',
    `status`            INT             NOT NULL DEFAULT 1       COMMENT '状态: 1=可预约 2=已约满 0=已停诊',
    `release_time`      DATETIME        DEFAULT NULL             COMMENT '号源释放时间',
    `update_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`source_id`),
    KEY `idx_schedule_id` (`schedule_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_source_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`schedule_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分时段号源表';

-- ============================================
-- 7. 挂号订单表 (registration_order)
-- ============================================
CREATE TABLE `registration_order` (
    `order_id`          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '订单ID',
    `serial_no`         VARCHAR(30)     NOT NULL                 COMMENT '就诊流水号',
    `patient_id`        BIGINT          NOT NULL                 COMMENT '患者ID',
    `source_id`         BIGINT          NOT NULL                 COMMENT '号源ID',
    `schedule_id`       BIGINT          NOT NULL                 COMMENT '排班ID',
    `doctor_id`         BIGINT          NOT NULL                 COMMENT '医生ID',
    `dept_id`           INT             NOT NULL                 COMMENT '科室ID',
    `order_time`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `lock_expire_time`  DATETIME        NOT NULL                 COMMENT '锁定过期时间(30分钟未支付自动取消)',
    `pay_time`          DATETIME        DEFAULT NULL             COMMENT '支付时间',
    `pay_method`        VARCHAR(20)     DEFAULT NULL             COMMENT '支付方式: ONLINE/WINDOW',
    `pay_amount`        DECIMAL(10,2)   NOT NULL DEFAULT 0.00    COMMENT '支付金额',
    `order_status`      VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '订单状态: PENDING/PAID/CANCELLED/MISSED',
    `cancel_time`       DATETIME        DEFAULT NULL             COMMENT '取消时间',
    `cancel_reason`     VARCHAR(255)    DEFAULT NULL             COMMENT '取消原因',
    `create_channel`    VARCHAR(20)     DEFAULT 'WEB'            COMMENT '创建渠道: WEB/WECHAT/WINDOW',
    `visit_date`        DATE            DEFAULT NULL             COMMENT '就诊日期',
    `update_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`order_id`),
    UNIQUE KEY `uk_serial_no` (`serial_no`),
    KEY `idx_patient_id` (`patient_id`),
    KEY `idx_source_id` (`source_id`),
    KEY `idx_order_status` (`order_status`),
    KEY `idx_lock_expire` (`order_status`, `lock_expire_time`),
    KEY `idx_visit_date` (`visit_date`, `order_status`),
    CONSTRAINT `fk_order_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient_info` (`patient_id`),
    CONSTRAINT `fk_order_source` FOREIGN KEY (`source_id`) REFERENCES `number_source` (`source_id`),
    CONSTRAINT `fk_order_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`schedule_id`),
    CONSTRAINT `fk_order_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor_info` (`doctor_id`),
    CONSTRAINT `fk_order_dept` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='挂号订单表';

-- ============================================
-- 8. 黑名单表 (blacklist)
-- ============================================
CREATE TABLE `blacklist` (
    `black_id`          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '黑名单ID',
    `patient_id`        BIGINT          NOT NULL                 COMMENT '患者ID',
    `missed_count`      INT             NOT NULL DEFAULT 0       COMMENT '累计爽约次数',
    `is_limited`        INT             NOT NULL DEFAULT 0       COMMENT '是否受限: 1=受限 0=正常',
    `limit_start_time`  DATETIME        DEFAULT NULL             COMMENT '限制开始时间',
    `limit_end_time`    DATETIME        DEFAULT NULL             COMMENT '限制结束时间',
    `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`black_id`),
    UNIQUE KEY `uk_patient_id` (`patient_id`),
    CONSTRAINT `fk_blacklist_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient_info` (`patient_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者黑名单表';

-- ============================================
-- 9. 爽约记录表 (missed_appointment)
-- ============================================
CREATE TABLE `missed_appointment` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    `patient_id`        BIGINT          NOT NULL                 COMMENT '患者ID',
    `order_id`          BIGINT          NOT NULL                 COMMENT '订单ID',
    `missed_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '爽约时间',
    `reason`            VARCHAR(255)    DEFAULT NULL             COMMENT '爽约原因',
    PRIMARY KEY (`id`),
    KEY `idx_patient_id` (`patient_id`),
    KEY `idx_order_id` (`order_id`),
    CONSTRAINT `fk_missed_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient_info` (`patient_id`),
    CONSTRAINT `fk_missed_order` FOREIGN KEY (`order_id`) REFERENCES `registration_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爽约记录表';

-- ============================================
-- 10. 停诊申请表 (stop_application)
-- ============================================
CREATE TABLE `stop_application` (
    `apply_id`          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '申请ID',
    `doctor_id`         BIGINT          NOT NULL                 COMMENT '医生ID',
    `schedule_id`       BIGINT          NOT NULL                 COMMENT '排班ID',
    `stop_reason`       VARCHAR(500)    NOT NULL                 COMMENT '停诊原因',
    `proof_url`         VARCHAR(255)    DEFAULT NULL             COMMENT '证明材料URL',
    `apply_time`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `auditor_id`        BIGINT          DEFAULT NULL             COMMENT '审核人ID',
    `audit_time`        DATETIME        DEFAULT NULL             COMMENT '审核时间',
    `audit_status`      VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '审核状态: PENDING/APPROVED/REJECTED',
    `audit_remark`      VARCHAR(255)    DEFAULT NULL             COMMENT '审核备注',
    PRIMARY KEY (`apply_id`),
    KEY `idx_doctor_id` (`doctor_id`),
    KEY `idx_audit_status` (`audit_status`),
    CONSTRAINT `fk_stop_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor_info` (`doctor_id`),
    CONSTRAINT `fk_stop_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`schedule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='停诊申请表';

-- ============================================
-- 11. 暂停接诊申请表 (suspension_application)
-- ============================================
CREATE TABLE `suspension_application` (
    `application_id`    BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '申请ID',
    `doctor_id`         BIGINT          NOT NULL                 COMMENT '医生ID',
    `suspension_date`   DATE            NOT NULL                 COMMENT '暂停日期',
    `reason`            VARCHAR(500)    NOT NULL                 COMMENT '暂停原因',
    `status`            VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/APPROVED/REJECTED',
    `apply_time`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `audit_time`        DATETIME        DEFAULT NULL             COMMENT '审核时间',
    `auditor_id`        BIGINT          DEFAULT NULL             COMMENT '审核人ID',
    `audit_comment`     VARCHAR(255)    DEFAULT NULL             COMMENT '审核意见',
    PRIMARY KEY (`application_id`),
    KEY `idx_doctor_id` (`doctor_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_suspension_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor_info` (`doctor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='暂停接诊申请表';

-- ============================================
-- 插入初始测试数据
-- ============================================

-- ==================== 系统用户 ====================
INSERT INTO `sys_user` (`user_id`, `username`, `password`, `real_name`, `phone`, `role`, `status`) VALUES
(1,  'admin',          '123456', '系统管理员', '13800000001', 'ADMIN',   1),
(2,  'doctor_zhang',   '123456', '张明远',     '13800000002', 'DOCTOR',  1),
(3,  'doctor_li',      '123456', '李雪琴',     '13800000003', 'DOCTOR',  1),
(4,  'doctor_wang',    '123456', '王建国',     '13800000004', 'DOCTOR',  1),
(5,  'patient_liu',    '123456', '刘小明',     '13900000001', 'PATIENT', 1),
(6,  'patient_zhao',   '123456', '赵丽丽',     '13900000002', 'PATIENT', 1),
(7,  'patient_chen',   '123456', '陈大明',     '13900000003', 'PATIENT', 1),
(8,  'doctor_sun',     '123456', '孙思远',     '13800000005', 'DOCTOR',  1),
(9,  'doctor_zhou',    '123456', '周文静',     '13800000006', 'DOCTOR',  1),
(10, 'patient_huang',  '123456', '黄小梅',     '13900000004', 'PATIENT', 1),
(11, 'doctor_wu',      '123456', '吴志强',     '13800000007', 'DOCTOR',  1),
(12, 'doctor_zheng',   '123456', '郑海燕',     '13800000008', 'DOCTOR',  1),
(13, 'patient_yang',   '123456', '杨建国',     '13900000005', 'PATIENT', 1),
(14, 'doctor_ma',      '123456', '马晓峰',     '13800000009', 'DOCTOR',  1),
(15, 'doctor_lin',     '123456', '林美琪',     '13800000010', 'DOCTOR',  1);

-- ==================== 科室 ====================
INSERT INTO `sys_department` (`dept_id`, `dept_name`, `dept_code`, `parent_id`, `dept_desc`, `sort_num`, `status`) VALUES
(1,  '内科',       'NK',   0, '诊治内科常见病、多发病及疑难杂症', 1, 1),
(2,  '外科',       'WK',   0, '开展各类外科手术及微创治疗',      2, 1),
(3,  '儿科',       'EK',   0, '专注儿童疾病诊疗与健康管理',      3, 1),
(4,  '妇产科',     'FCK',  0, '妇科疾病诊疗及孕产期保健',        4, 1),
(5,  '骨科',       'GK',   0, '骨骼、关节及脊柱疾病诊治',        5, 1),
(6,  '眼科',       'YK',   0, '眼部疾病诊疗及视力矫正',          6, 1),
(7,  '皮肤科',     'PFK',  0, '各类皮肤疾病诊断与治疗',          7, 1),
(8,  '神经内科',   'SJNK', 0, '神经系统疾病诊断与治疗',          8, 1),
(9,  '心血管内科', 'XXGNK',0, '心脏及血管疾病诊治',              9, 1),
(10, '耳鼻喉科',   'EBH',  0, '耳鼻咽喉疾病诊疗',                10, 1);

-- ==================== 患者信息 ====================
INSERT INTO `patient_info` (`patient_id`, `gender`, `age`, `address`, `emergency_contact`, `emergency_phone`, `is_special`, `special_type`) VALUES
(5,  '男', 35, '北京市朝阳区建国路100号',     '刘芳', '13910000001', 0, NULL),
(6,  '女', 28, '北京市海淀区中关村大街55号',  '赵伟', '13910000002', 0, NULL),
(7,  '男', 65, '北京市西城区西单北大街20号',  '陈小红','13910000003', 1, '老年人'),
(10, '女', 42, '北京市东城区王府井大街8号',   '黄强', '13910000004', 0, NULL),
(13, '男', 72, '北京市丰台区方庄路15号',      '杨丽', '13910000005', 1, '老年人');

-- ==================== 医生信息 ====================
INSERT INTO `doctor_info` (`doctor_id`, `dept_id`, `title`, `specialty`, `consultation_fee`, `expert_fee`) VALUES
(2,  1,  '主任医师', '擅长高血压、糖尿病等慢性病管理及疑难杂症诊治',         15.00,  50.00),
(3,  1,  '副主任医师', '擅长呼吸系统疾病及感染性疾病诊治',                   12.00,  30.00),
(4,  2,  '主任医师', '擅长普外科手术及微创腹腔镜手术',                       20.00,  60.00),
(8,  3,  '主任医师', '擅长儿童呼吸系统疾病及生长发育评估',                   15.00,  40.00),
(9,  4,  '副主任医师', '擅长高危妊娠管理及妇科微创手术',                     12.00,  35.00),
(11, 5,  '主任医师', '擅长关节置换及脊柱微创手术',                           20.00,  60.00),
(12, 6,  '主治医师', '擅长白内障手术及眼底疾病诊治',                         10.00,  25.00),
(14, 9,  '主任医师', '擅长冠心病介入治疗及心律失常诊治',                     20.00,  60.00),
(15, 10, '副主任医师', '擅长耳显微外科及鼻内镜手术',                         12.00,  30.00);

-- ==================== 排班和号源(未来7天自动生成，这里只插入样例) ====================
-- 注意：排班和号源由定时任务 ScheduleGenerateService 自动生成
-- 此处仅插入当天示例数据方便测试

-- 为内科张明远医生插入今天的排班
INSERT INTO `schedule` (`schedule_id`, `doctor_id`, `dept_id`, `schedule_date`, `start_time`, `end_time`, `time_slot_duration`, `total_slots`, `slots_per_time`, `registration_fee`, `status`, `create_by`) VALUES
(1, 2, 1, CURDATE() + INTERVAL 1 DAY, '08:00', '12:00', 60, 4, 5, 15.00, 1, 1),
(2, 3, 1, CURDATE() + INTERVAL 1 DAY, '08:00', '12:00', 60, 4, 5, 12.00, 1, 1),
(3, 4, 2, CURDATE() + INTERVAL 1 DAY, '08:00', '12:00', 60, 4, 5, 20.00, 1, 1),
(4, 8, 3, CURDATE() + INTERVAL 1 DAY, '08:00', '12:00', 60, 4, 5, 15.00, 1, 1),
(5, 9, 4, CURDATE() + INTERVAL 1 DAY, '08:00', '12:00', 60, 4, 5, 12.00, 1, 1),
(6, 11, 5, CURDATE() + INTERVAL 1 DAY, '08:00', '12:00', 60, 4, 5, 20.00, 1, 1),
(7, 12, 6, CURDATE() + INTERVAL 1 DAY, '08:00', '12:00', 60, 4, 5, 10.00, 1, 1),
(8, 14, 9, CURDATE() + INTERVAL 1 DAY, '08:00', '12:00', 60, 4, 5, 20.00, 1, 1),
(9, 15, 10, CURDATE() + INTERVAL 1 DAY, '08:00', '12:00', 60, 4, 5, 12.00, 1, 1);

-- ==================== 号源（每个排班4个时段，每时段5个号） ====================
-- 排班1的号源（内科张明远）
INSERT INTO `number_source` (`source_id`, `schedule_id`, `slot_number`, `slot_start_time`, `slot_end_time`, `total_count`, `remaining_count`, `locked_count`, `status`) VALUES
(1,  1, 1, '08:00', '09:00', 5, 5, 0, 1),
(2,  1, 2, '09:00', '10:00', 5, 5, 0, 1),
(3,  1, 3, '10:00', '11:00', 5, 5, 0, 1),
(4,  1, 4, '11:00', '12:00', 5, 5, 0, 1),
-- 排班2的号源（内科李雪琴）
(5,  2, 1, '08:00', '09:00', 5, 5, 0, 1),
(6,  2, 2, '09:00', '10:00', 5, 5, 0, 1),
(7,  2, 3, '10:00', '11:00', 5, 5, 0, 1),
(8,  2, 4, '11:00', '12:00', 5, 5, 0, 1),
-- 排班3的号源（外科王建国）
(9,  3, 1, '08:00', '09:00', 5, 5, 0, 1),
(10, 3, 2, '09:00', '10:00', 5, 5, 0, 1),
(11, 3, 3, '10:00', '11:00', 5, 5, 0, 1),
(12, 3, 4, '11:00', '12:00', 5, 5, 0, 1),
-- 排班4的号源（儿科孙思远）
(13, 4, 1, '08:00', '09:00', 5, 5, 0, 1),
(14, 4, 2, '09:00', '10:00', 5, 5, 0, 1),
(15, 4, 3, '10:00', '11:00', 5, 5, 0, 1),
(16, 4, 4, '11:00', '12:00', 5, 5, 0, 1),
-- 排班5的号源（妇产科周文静）
(17, 5, 1, '08:00', '09:00', 5, 5, 0, 1),
(18, 5, 2, '09:00', '10:00', 5, 5, 0, 1),
(19, 5, 3, '10:00', '11:00', 5, 5, 0, 1),
(20, 5, 4, '11:00', '12:00', 5, 5, 0, 1),
-- 排班6-9的号源
(21, 6, 1, '08:00', '09:00', 5, 5, 0, 1),
(22, 6, 2, '09:00', '10:00', 5, 5, 0, 1),
(23, 6, 3, '10:00', '11:00', 5, 5, 0, 1),
(24, 6, 4, '11:00', '12:00', 5, 5, 0, 1),
(25, 7, 1, '08:00', '09:00', 5, 5, 0, 1),
(26, 7, 2, '09:00', '10:00', 5, 5, 0, 1),
(27, 7, 3, '10:00', '11:00', 5, 5, 0, 1),
(28, 7, 4, '11:00', '12:00', 5, 5, 0, 1),
(29, 8, 1, '08:00', '09:00', 5, 5, 0, 1),
(30, 8, 2, '09:00', '10:00', 5, 5, 0, 1),
(31, 8, 3, '10:00', '11:00', 5, 5, 0, 1),
(32, 8, 4, '11:00', '12:00', 5, 5, 0, 1),
(33, 9, 1, '08:00', '09:00', 5, 5, 0, 1),
(34, 9, 2, '09:00', '10:00', 5, 5, 0, 1),
(35, 9, 3, '10:00', '11:00', 5, 5, 0, 1),
(36, 9, 4, '11:00', '12:00', 5, 5, 0, 1);

-- 提示
SELECT '✅ 数据库初始化完成！共创建11张表，插入15个用户、10个科室、9条排班、36个号源。' AS message;
SELECT '请运行 ./mvnw spring-boot:run 启动项目，访问 http://localhost:8080' AS tip;
