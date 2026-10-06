-- ============================================
-- 医院门诊挂号系统 - 测试数据插入脚本
-- ============================================
USE hospital;

-- ==================== 系统用户 ====================
INSERT INTO sys_user (user_id, username, password, real_name, phone, role, status) VALUES (1,  'admin',        '123456', '系统管理员', '13800000001', 'ADMIN',   1);
INSERT INTO sys_user (user_id, username, password, real_name, phone, role, status) VALUES (2,  'doctor_zhang', '123456', '张明远',     '13800000002', 'DOCTOR',  1);
INSERT INTO sys_user (user_id, username, password, real_name, phone, role, status) VALUES (3,  'doctor_li',    '123456', '李雪琴',     '13800000003', 'DOCTOR',  1);
INSERT INTO sys_user (user_id, username, password, real_name, phone, role, status) VALUES (4,  'doctor_wang',  '123456', '王建国',     '13800000004', 'DOCTOR',  1);
INSERT INTO sys_user (user_id, username, password, real_name, phone, role, status) VALUES (5,  'patient_liu',  '123456', '刘小明',     '13900000001', 'PATIENT', 1);
INSERT INTO sys_user (user_id, username, password, real_name, phone, role, status) VALUES (6,  'patient_zhao', '123456', '赵丽丽',     '13900000002', 'PATIENT', 1);
INSERT INTO sys_user (user_id, username, password, real_name, phone, role, status) VALUES (7,  'patient_chen', '123456', '陈大明',     '13900000003', 'PATIENT', 1);
INSERT INTO sys_user (user_id, username, password, real_name, phone, role, status) VALUES (8,  'doctor_sun',   '123456', '孙思远',     '13800000005', 'DOCTOR',  1);
INSERT INTO sys_user (user_id, username, password, real_name, phone, role, status) VALUES (9,  'doctor_zhou',  '123456', '周文静',     '13800000006', 'DOCTOR',  1);
INSERT INTO sys_user (user_id, username, password, real_name, phone, role, status) VALUES (10, 'patient_huang','123456', '黄小梅',     '13900000004', 'PATIENT', 1);
INSERT INTO sys_user (user_id, username, password, real_name, phone, role, status) VALUES (11, 'doctor_wu',    '123456', '吴志强',     '13800000007', 'DOCTOR',  1);
INSERT INTO sys_user (user_id, username, password, real_name, phone, role, status) VALUES (12, 'doctor_zheng', '123456', '郑海燕',     '13800000008', 'DOCTOR',  1);
INSERT INTO sys_user (user_id, username, password, real_name, phone, role, status) VALUES (13, 'patient_yang', '123456', '杨建国',     '13900000005', 'PATIENT', 1);
INSERT INTO sys_user (user_id, username, password, real_name, phone, role, status) VALUES (14, 'doctor_ma',    '123456', '马晓峰',     '13800000009', 'DOCTOR',  1);
INSERT INTO sys_user (user_id, username, password, real_name, phone, role, status) VALUES (15, 'doctor_lin',   '123456', '林美琪',     '13800000010', 'DOCTOR',  1);

-- ==================== 科室 ====================
INSERT INTO sys_department (dept_id, dept_name, dept_code, parent_id, dept_desc, sort_num, status) VALUES (1,  '内科',       'NK',   0, '诊治内科常见病、多发病及疑难杂症', 1, 1);
INSERT INTO sys_department (dept_id, dept_name, dept_code, parent_id, dept_desc, sort_num, status) VALUES (2,  '外科',       'WK',   0, '开展各类外科手术及微创治疗',      2, 1);
INSERT INTO sys_department (dept_id, dept_name, dept_code, parent_id, dept_desc, sort_num, status) VALUES (3,  '儿科',       'EK',   0, '专注儿童疾病诊疗与健康管理',      3, 1);
INSERT INTO sys_department (dept_id, dept_name, dept_code, parent_id, dept_desc, sort_num, status) VALUES (4,  '妇产科',     'FCK',  0, '妇科疾病诊疗及孕产期保健',        4, 1);
INSERT INTO sys_department (dept_id, dept_name, dept_code, parent_id, dept_desc, sort_num, status) VALUES (5,  '骨科',       'GK',   0, '骨骼、关节及脊柱疾病诊治',        5, 1);
INSERT INTO sys_department (dept_id, dept_name, dept_code, parent_id, dept_desc, sort_num, status) VALUES (6,  '眼科',       'YK',   0, '眼部疾病诊疗及视力矫正',          6, 1);
INSERT INTO sys_department (dept_id, dept_name, dept_code, parent_id, dept_desc, sort_num, status) VALUES (7,  '皮肤科',     'PFK',  0, '各类皮肤疾病诊断与治疗',          7, 1);
INSERT INTO sys_department (dept_id, dept_name, dept_code, parent_id, dept_desc, sort_num, status) VALUES (8,  '神经内科',   'SJNK', 0, '神经系统疾病诊断与治疗',          8, 1);
INSERT INTO sys_department (dept_id, dept_name, dept_code, parent_id, dept_desc, sort_num, status) VALUES (9,  '心血管内科', 'XXGNK',0, '心脏及血管疾病诊治',              9, 1);
INSERT INTO sys_department (dept_id, dept_name, dept_code, parent_id, dept_desc, sort_num, status) VALUES (10, '耳鼻喉科',   'EBH',  0, '耳鼻咽喉疾病诊疗',                10, 1);

-- ==================== 患者信息 ====================
INSERT INTO patient_info (patient_id, gender, age, address, emergency_contact, emergency_phone, is_special, special_type) VALUES (5,  '男', 35, '北京市朝阳区建国路100号',     '刘芳', '13910000001', 0, NULL);
INSERT INTO patient_info (patient_id, gender, age, address, emergency_contact, emergency_phone, is_special, special_type) VALUES (6,  '女', 28, '北京市海淀区中关村大街55号',  '赵伟', '13910000002', 0, NULL);
INSERT INTO patient_info (patient_id, gender, age, address, emergency_contact, emergency_phone, is_special, special_type) VALUES (7,  '男', 65, '北京市西城区西单北大街20号',  '陈小红','13910000003', 1, '老年人');
INSERT INTO patient_info (patient_id, gender, age, address, emergency_contact, emergency_phone, is_special, special_type) VALUES (10, '女', 42, '北京市东城区王府井大街8号',   '黄强', '13910000004', 0, NULL);
INSERT INTO patient_info (patient_id, gender, age, address, emergency_contact, emergency_phone, is_special, special_type) VALUES (13, '男', 72, '北京市丰台区方庄路15号',      '杨丽', '13910000005', 1, '老年人');

-- ==================== 医生信息 ====================
INSERT INTO doctor_info (doctor_id, dept_id, title, specialty, consultation_fee, expert_fee) VALUES (2,  1,  '主任医师',   '擅长高血压、糖尿病等慢性病管理及疑难杂症诊治',   15.00, 50.00);
INSERT INTO doctor_info (doctor_id, dept_id, title, specialty, consultation_fee, expert_fee) VALUES (3,  1,  '副主任医师', '擅长呼吸系统疾病及感染性疾病诊治',               12.00, 30.00);
INSERT INTO doctor_info (doctor_id, dept_id, title, specialty, consultation_fee, expert_fee) VALUES (4,  2,  '主任医师',   '擅长普外科手术及微创腹腔镜手术',                 20.00, 60.00);
INSERT INTO doctor_info (doctor_id, dept_id, title, specialty, consultation_fee, expert_fee) VALUES (8,  3,  '主任医师',   '擅长儿童呼吸系统疾病及生长发育评估',             15.00, 40.00);
INSERT INTO doctor_info (doctor_id, dept_id, title, specialty, consultation_fee, expert_fee) VALUES (9,  4,  '副主任医师', '擅长高危妊娠管理及妇科微创手术',                 12.00, 35.00);
INSERT INTO doctor_info (doctor_id, dept_id, title, specialty, consultation_fee, expert_fee) VALUES (11, 5,  '主任医师',   '擅长关节置换及脊柱微创手术',                     20.00, 60.00);
INSERT INTO doctor_info (doctor_id, dept_id, title, specialty, consultation_fee, expert_fee) VALUES (12, 6,  '主治医师',   '擅长白内障手术及眼底疾病诊治',                   10.00, 25.00);
INSERT INTO doctor_info (doctor_id, dept_id, title, specialty, consultation_fee, expert_fee) VALUES (14, 9,  '主任医师',   '擅长冠心病介入治疗及心律失常诊治',               20.00, 60.00);
INSERT INTO doctor_info (doctor_id, dept_id, title, specialty, consultation_fee, expert_fee) VALUES (15, 10, '副主任医师', '擅长耳显微外科及鼻内镜手术',                     12.00, 30.00);

-- ==================== 排班 ====================
INSERT INTO schedule (schedule_id, doctor_id, dept_id, schedule_date, start_time, end_time, time_slot_duration, total_slots, slots_per_time, registration_fee, status, create_by) VALUES (1, 2, 1, CURDATE() + INTERVAL 1 DAY, '08:00', '12:00', 60, 4, 5, 15.00, 1, 1);
INSERT INTO schedule (schedule_id, doctor_id, dept_id, schedule_date, start_time, end_time, time_slot_duration, total_slots, slots_per_time, registration_fee, status, create_by) VALUES (2, 3, 1, CURDATE() + INTERVAL 1 DAY, '08:00', '12:00', 60, 4, 5, 12.00, 1, 1);
INSERT INTO schedule (schedule_id, doctor_id, dept_id, schedule_date, start_time, end_time, time_slot_duration, total_slots, slots_per_time, registration_fee, status, create_by) VALUES (3, 4, 2, CURDATE() + INTERVAL 1 DAY, '08:00', '12:00', 60, 4, 5, 20.00, 1, 1);
INSERT INTO schedule (schedule_id, doctor_id, dept_id, schedule_date, start_time, end_time, time_slot_duration, total_slots, slots_per_time, registration_fee, status, create_by) VALUES (4, 8, 3, CURDATE() + INTERVAL 1 DAY, '08:00', '12:00', 60, 4, 5, 15.00, 1, 1);
INSERT INTO schedule (schedule_id, doctor_id, dept_id, schedule_date, start_time, end_time, time_slot_duration, total_slots, slots_per_time, registration_fee, status, create_by) VALUES (5, 9, 4, CURDATE() + INTERVAL 1 DAY, '08:00', '12:00', 60, 4, 5, 12.00, 1, 1);
INSERT INTO schedule (schedule_id, doctor_id, dept_id, schedule_date, start_time, end_time, time_slot_duration, total_slots, slots_per_time, registration_fee, status, create_by) VALUES (6, 11,5, CURDATE() + INTERVAL 1 DAY, '08:00', '12:00', 60, 4, 5, 20.00, 1, 1);
INSERT INTO schedule (schedule_id, doctor_id, dept_id, schedule_date, start_time, end_time, time_slot_duration, total_slots, slots_per_time, registration_fee, status, create_by) VALUES (7, 12,6, CURDATE() + INTERVAL 1 DAY, '08:00', '12:00', 60, 4, 5, 10.00, 1, 1);
INSERT INTO schedule (schedule_id, doctor_id, dept_id, schedule_date, start_time, end_time, time_slot_duration, total_slots, slots_per_time, registration_fee, status, create_by) VALUES (8, 14,9, CURDATE() + INTERVAL 1 DAY, '08:00', '12:00', 60, 4, 5, 20.00, 1, 1);
INSERT INTO schedule (schedule_id, doctor_id, dept_id, schedule_date, start_time, end_time, time_slot_duration, total_slots, slots_per_time, registration_fee, status, create_by) VALUES (9, 15,10,CURDATE() + INTERVAL 1 DAY, '08:00', '12:00', 60, 4, 5, 12.00, 1, 1);

-- ==================== 号源（每个排班4个时段 * 9 = 36条） ====================
-- 排班1 (schedule_id=1): 内科张明远
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (1, 1, '08:00', '09:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (1, 2, '09:00', '10:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (1, 3, '10:00', '11:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (1, 4, '11:00', '12:00', 5, 5, 0, 1);
-- 排班2
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (2, 1, '08:00', '09:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (2, 2, '09:00', '10:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (2, 3, '10:00', '11:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (2, 4, '11:00', '12:00', 5, 5, 0, 1);
-- 排班3
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (3, 1, '08:00', '09:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (3, 2, '09:00', '10:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (3, 3, '10:00', '11:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (3, 4, '11:00', '12:00', 5, 5, 0, 1);
-- 排班4
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (4, 1, '08:00', '09:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (4, 2, '09:00', '10:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (4, 3, '10:00', '11:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (4, 4, '11:00', '12:00', 5, 5, 0, 1);
-- 排班5
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (5, 1, '08:00', '09:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (5, 2, '09:00', '10:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (5, 3, '10:00', '11:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (5, 4, '11:00', '12:00', 5, 5, 0, 1);
-- 排班6
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (6, 1, '08:00', '09:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (6, 2, '09:00', '10:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (6, 3, '10:00', '11:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (6, 4, '11:00', '12:00', 5, 5, 0, 1);
-- 排班7
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (7, 1, '08:00', '09:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (7, 2, '09:00', '10:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (7, 3, '10:00', '11:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (7, 4, '11:00', '12:00', 5, 5, 0, 1);
-- 排班8
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (8, 1, '08:00', '09:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (8, 2, '09:00', '10:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (8, 3, '10:00', '11:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (8, 4, '11:00', '12:00', 5, 5, 0, 1);
-- 排班9
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (9, 1, '08:00', '09:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (9, 2, '09:00', '10:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (9, 3, '10:00', '11:00', 5, 5, 0, 1);
INSERT INTO number_source (schedule_id, slot_number, slot_start_time, slot_end_time, total_count, remaining_count, locked_count, status) VALUES (9, 4, '11:00', '12:00', 5, 5, 0, 1);

SELECT 'All test data inserted successfully!' AS result;
