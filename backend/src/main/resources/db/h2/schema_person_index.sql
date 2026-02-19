-- 人员索引表 - 用于存储融合后的人员基础信息
-- 创建时间: 2026-02-17

-- 1. 人员索引表
CREATE TABLE IF NOT EXISTS T_PERSON_INDEX (
    ID_CARD VARCHAR(18) PRIMARY KEY,
    NAME VARCHAR(64),
    GENDER VARCHAR(8),
    AGE INT,
    PHONE VARCHAR(32),
    DISTRICT VARCHAR(64),
    STREET VARCHAR(64),
    COMMITTEE VARCHAR(64),
    CONTACT_ADDRESS VARCHAR(256),
    HOUSEHOLD_ADDRESS VARCHAR(256),
    DISABILITY_CATEGORY VARCHAR(32),
    DISABILITY_LEVEL VARCHAR(16),
    LONGITUDE DECIMAL(10,7),
    LATITUDE DECIMAL(10,7),
    LOCATION_STATUS VARCHAR(20) DEFAULT 'PENDING',
    FIRST_SEEN_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_SEEN_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX IF NOT EXISTS IDX_PERSON_DISTRICT ON T_PERSON_INDEX(DISTRICT);
CREATE INDEX IF NOT EXISTS IDX_PERSON_STREET ON T_PERSON_INDEX(STREET);
CREATE INDEX IF NOT EXISTS IDX_PERSON_LOCATION ON T_PERSON_INDEX(LONGITUDE, LATITUDE);
CREATE INDEX IF NOT EXISTS IDX_PERSON_CATEGORY ON T_PERSON_INDEX(DISABILITY_CATEGORY);

-- 2. 字段分类配置表
CREATE TABLE IF NOT EXISTS T_FIELD_CLASSIFY (
    FIELD_CODE VARCHAR(50) PRIMARY KEY,
    FIELD_NAME VARCHAR(100),
    FIELD_CATEGORY VARCHAR(20),
    MERGE_STRATEGY VARCHAR(20),
    DISPLAY_ORDER INT
);

-- 3. 初始化字段分类数据
-- 公共基础字段
INSERT INTO T_FIELD_CLASSIFY (FIELD_CODE, FIELD_NAME, FIELD_CATEGORY, MERGE_STRATEGY, DISPLAY_ORDER) VALUES 
('ID_CARD', '身份证号', 'COMMON', 'FIRST', 1),
('NAME', '姓名', 'COMMON', 'LATEST', 2),
('GENDER', '性别', 'COMMON', 'LATEST', 3),
('AGE', '年龄', 'COMMON', 'LATEST', 4),
('PHONE', '联系电话', 'COMMON', 'LATEST', 5),
('DISTRICT', '区县', 'COMMON', 'LATEST', 6),
('STREET', '街道', 'COMMON', 'LATEST', 7),
('COMMITTEE', '居委', 'COMMON', 'LATEST', 8),
('CONTACT_ADDRESS', '联系地址', 'COMMON', 'LATEST', 9),
('HOUSEHOLD_ADDRESS', '户籍地址', 'COMMON', 'LATEST', 10),
('LONGITUDE', '经度', 'COMMON', 'LATEST', 11),
('LATITUDE', '纬度', 'COMMON', 'LATEST', 12);

-- 业务标识字段
INSERT INTO T_FIELD_CLASSIFY (FIELD_CODE, FIELD_NAME, FIELD_CATEGORY, MERGE_STRATEGY, DISPLAY_ORDER) VALUES 
('DISABILITY_CERT_NO', '残疾证号', 'BIZ', 'ALL', 20),
('DISABILITY_CATEGORY', '残疾类别', 'BIZ', 'ALL', 21),
('DISABILITY_LEVEL', '残疾等级', 'BIZ', 'ALL', 22),
('DISABILITY_STATUS', '残疾状态', 'BIZ', 'ALL', 23);

-- 4. 模块字段映射表 (27个模块 -> 公共字段的映射关系)
CREATE TABLE IF NOT EXISTS T_MODULE_FIELD_MAPPING (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    MODULE_CODE VARCHAR(50) NOT NULL,
    MODULE_FIELD_CODE VARCHAR(50) NOT NULL,
    COMMON_FIELD_CODE VARCHAR(50) NOT NULL,
    CONSTRAINT UK_MODULE_FIELD UNIQUE (MODULE_CODE, MODULE_FIELD_CODE)
);

-- 初始化映射数据
INSERT INTO T_MODULE_FIELD_MAPPING (MODULE_CODE, MODULE_FIELD_CODE, COMMON_FIELD_CODE) VALUES
-- 残疾人认证模块 (英文字段名)
('disabled_cert', 'ID_CARD', 'ID_CARD'),
('disabled_cert', 'NAME', 'NAME'),
('disabled_cert', 'GENDER', 'GENDER'),
('disabled_cert', 'DISTRICT', 'DISTRICT'),
('disabled_cert', 'STREET', 'STREET'),
('disabled_cert', 'COMMITTEE', 'COMMITTEE'),
('disabled_cert', 'CONTACT_ADDRESS', 'CONTACT_ADDRESS'),
('disabled_cert', 'HOUSEHOLD_ADDRESS', 'HOUSEHOLD_ADDRESS'),
('disabled_cert', 'CONTACT_PHONE', 'PHONE'),
('disabled_cert', 'DISABILITY_CATEGORY', 'DISABILITY_CATEGORY'),
('disabled_cert', 'DISABILITY_LEVEL', 'DISABILITY_LEVEL'),
('disabled_cert', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),
('disabled_cert', 'STATUS', 'DISABILITY_STATUS'),
-- 残疾人认证模块 (中文字段名 - 生产文件格式)
('disabled_cert', '证件号码', 'ID_CARD'),
('disabled_cert', '姓名', 'NAME'),
('disabled_cert', '性别', 'GENDER'),
('disabled_cert', '区县', 'DISTRICT'),
('disabled_cert', '街道', 'STREET'),
('disabled_cert', '居委', 'COMMITTEE'),
('disabled_cert', '联系地址', 'CONTACT_ADDRESS'),
('disabled_cert', '户口地址', 'HOUSEHOLD_ADDRESS'),
('disabled_cert', '联系电话', 'PHONE'),
('disabled_cert', '残疾类别', 'DISABILITY_CATEGORY'),
('disabled_cert', '残疾等级', 'DISABILITY_LEVEL'),
('disabled_cert', '残疾证号', 'DISABILITY_CERT_NO'),
('disabled_cert', '状态', 'DISABILITY_STATUS'),

-- 残疾管理模块
('disabled_mgmt', 'ID_CARD', 'ID_CARD'),
('disabled_mgmt', 'NAME', 'NAME'),
('disabled_mgmt', 'GENDER', 'GENDER'),
('disabled_mgmt', 'HOUSEHOLD_DISTRICT', 'DISTRICT'),
('disabled_mgmt', 'HOUSEHOLD_STREET', 'STREET'),
('disabled_mgmt', 'CONTACT_ADDRESS', 'CONTACT_ADDRESS'),
('disabled_mgmt', 'CONTACT_PHONE', 'PHONE'),
('disabled_mgmt', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 康复医疗/器材补助
('rehab_subsidy', 'ID_CARD', 'ID_CARD'),
('rehab_subsidy', 'NAME', 'NAME'),
('rehab_subsidy', 'HOUSEHOLD_DISTRICT', 'DISTRICT'),
('rehab_subsidy', 'HOUSEHOLD_STREET', 'STREET'),
('rehab_subsidy', 'DISABILITY_CATEGORY', 'DISABILITY_CATEGORY'),
('rehab_subsidy', 'DISABILITY_LEVEL', 'DISABILITY_LEVEL'),
('rehab_subsidy', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 养老补助
('pension_subsidy', 'ID_CARD', 'ID_CARD'),
('pension_subsidy', 'NAME', 'NAME'),
('pension_subsidy', 'GENDER', 'GENDER'),
('pension_subsidy', 'ACCOUNT_DISTRICT', 'DISTRICT'),
('pension_subsidy', 'ACCOUNT_STREET', 'STREET'),
('pension_subsidy', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 个人证管理
('personal_cert', 'ID_CARD', 'ID_CARD'),
('personal_cert', 'NAME', 'NAME'),
('personal_cert', 'STREET', 'STREET'),
('personal_cert', 'ISSUE_DISTRICT', 'DISTRICT'),
('personal_cert', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 残疾家庭情况
('family_situation', 'ID_CARD', 'ID_CARD'),
('family_situation', 'NAME', 'NAME'),
('family_situation', 'COMMITTEE', 'COMMITTEE'),
('family_situation', 'DISABILITY_CATEGORY', 'DISABILITY_CATEGORY'),
('family_situation', 'DISABILITY_LEVEL', 'DISABILITY_LEVEL'),
('family_situation', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),
('family_situation', 'CONTACT_PHONE', 'PHONE'),

-- 借信卡
('loan_card', 'ID_CARD', 'ID_CARD'),
('loan_card', 'NAME', 'NAME'),
('loan_card', 'BIRTHDAY', 'AGE'),
('loan_card', 'HOUSEHOLD_ADDRESS', 'HOUSEHOLD_ADDRESS'),
('loan_card', 'HOME_ADDRESS', 'CONTACT_ADDRESS'),
('loan_card', 'CONTACT_PHONE', 'PHONE'),
('loan_card', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 慢性病/康护
('chronic_disease', 'ID_CARD', 'ID_CARD'),
('chronic_disease', 'NAME', 'NAME'),
('chronic_disease', 'HOUSEHOLD_DISTRICT', 'DISTRICT'),
('chronic_disease', 'HOUSEHOLD_STREET', 'STREET'),
('chronic_disease', 'DISABILITY_CATEGORY', 'DISABILITY_CATEGORY'),
('chronic_disease', 'AGE', 'AGE'),
('chronic_disease', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 机构养护
('institution_care', 'ID_CARD', 'ID_CARD'),
('institution_care', 'NAME', 'NAME'),
('institution_care', 'HOUSEHOLD_DISTRICT', 'DISTRICT'),
('institution_care', 'HOUSEHOLD_STREET', 'STREET'),
('institution_care', 'DISABILITY_CATEGORY', 'DISABILITY_CATEGORY'),
('institution_care', 'AGE', 'AGE'),
('institution_care', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 人工耳蜗
('cochlear_implant', 'ID_CARD', 'ID_CARD'),
('cochlear_implant', 'NAME', 'NAME'),
('cochlear_implant', 'STREET', 'STREET'),
('cochlear_implant', 'DISABILITY_CATEGORY', 'DISABILITY_CATEGORY'),
('cochlear_implant', 'AGE', 'AGE'),
('cochlear_implant', 'CONTACT_PHONE', 'PHONE'),
('cochlear_implant', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 康复体矫
('rehab_orthodontics', 'ID_CARD', 'ID_CARD'),
('rehab_orthodontics', 'NAME', 'NAME'),
('rehab_orthodontics', 'STREET', 'STREET'),
('rehab_orthodontics', 'DISABILITY_CATEGORY', 'DISABILITY_CATEGORY'),
('rehab_orthodontics', 'AGE', 'AGE'),
('rehab_orthodontics', 'CONTACT_PHONE', 'PHONE'),
('rehab_orthodontics', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 儿童康复矫正
('child_rehab', 'ID_CARD', 'ID_CARD'),
('child_rehab', 'NAME', 'NAME'),
('child_rehab', 'HOUSEHOLD_DISTRICT', 'DISTRICT'),
('child_rehab', 'HOUSEHOLD_STREET', 'STREET'),
('child_rehab', 'AGE', 'AGE'),
('child_rehab', 'DISABILITY_CATEGORY', 'DISABILITY_CATEGORY'),
('child_rehab', 'BIRTHDAY', 'AGE'),
('child_rehab', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 交通补贴
('traffic_subsid', 'ID_CARD', 'ID_CARD'),
('traffic_subsid', 'NAME', 'NAME'),
('traffic_subsid', 'HOUSEHOLD_ADDRESS', 'HOUSEHOLD_ADDRESS'),
('traffic_subsid', 'HOUSEHOLD_COMMITTEE', 'COMMITTEE'),
('traffic_subsid', 'CONTACT_PHONE', 'PHONE'),
('traffic_subsid', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 精准帮扶
('precise_help', 'ID_CARD', 'ID_CARD'),
('precise_help', 'NAME', 'NAME'),
('precise_help', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 残疾人申请
('disabled_application', 'ID_CARD', 'ID_CARD'),
('disabled_application', 'NAME', 'NAME'),
('disabled_application', 'HOUSEHOLD_DISTRICT', 'DISTRICT'),
('disabled_application', 'HOUSEHOLD_STREET', 'STREET'),
('disabled_application', 'DISABILITY_CATEGORY', 'DISABILITY_CATEGORY'),
('disabled_application', 'DISABILITY_LEVEL', 'DISABILITY_LEVEL'),
('disabled_application', 'AGE', 'AGE'),
('disabled_application', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 辅助器具
('assistive_device', 'ID_CARD', 'ID_CARD'),
('assistive_device', 'NAME', 'NAME'),
('assistive_device', 'HOUSEHOLD_DISTRICT', 'DISTRICT'),
('assistive_device', 'HOUSEHOLD_STREET', 'STREET'),
('assistive_device', 'DISABILITY_CATEGORY', 'DISABILITY_CATEGORY'),
('assistive_device', 'DISABILITY_LEVEL', 'DISABILITY_LEVEL'),
('assistive_device', 'CONTACT_PHONE', 'PHONE'),
('assistive_device', 'HOUSEHOLD_ADDRESS', 'HOUSEHOLD_ADDRESS'),
('assistive_device', 'RECEIVER_PHONE', 'PHONE'),
('assistive_device', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 体检
('health_check', 'ID_CARD', 'ID_CARD'),
('health_check', 'NAME', 'NAME'),
('health_check', 'HOUSEHOLD_DISTRICT', 'DISTRICT'),
('health_check', 'HOUSEHOLD_STREET', 'STREET'),
('health_check', 'DISABILITY_CATEGORY', 'DISABILITY_CATEGORY'),
('health_check', 'DISABILITY_LEVEL', 'DISABILITY_LEVEL'),
('health_check', 'AGE', 'AGE'),
('health_check', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 两项补贴
('two_subsidies', 'ID_CARD', 'ID_CARD'),
('two_subsidies', 'NAME', 'NAME'),
('two_subsidies', 'HOUSEHOLD_DISTRICT', 'DISTRICT'),
('two_subsidies', 'HOUSEHOLD_STREET', 'STREET'),
('two_subsidies', 'DISABILITY_CATEGORY', 'DISABILITY_CATEGORY'),
('two_subsidies', 'DISABILITY_LEVEL', 'DISABILITY_LEVEL'),
('two_subsidies', 'AGE', 'AGE'),
('two_subsidies', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 就业
('employment', 'ID_CARD', 'ID_CARD'),
('employment', 'NAME', 'NAME'),
('employment', 'GENDER', 'GENDER'),
('employment', 'DISABILITY_LEVEL', 'DISABILITY_LEVEL'),
('employment', 'HOUSEHOLD_ADDRESS', 'HOUSEHOLD_ADDRESS'),
('employment', 'HOUSEHOLD_STREET', 'STREET'),
('employment', 'HOUSEHOLD_COMMITTEE', 'COMMITTEE'),
('employment', 'CONTACT_PHONE', 'PHONE'),
('employment', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 职业技能培训
('vocational_training', 'ID_CARD', 'ID_CARD'),
('vocational_training', 'NAME', 'NAME'),
('vocational_training', 'DISABILITY_CATEGORY', 'DISABILITY_CATEGORY'),
('vocational_training', 'DISABILITY_LEVEL', 'DISABILITY_LEVEL'),
('vocational_training', 'AGE', 'AGE'),
('vocational_training', 'HOUSEHOLD_ADDRESS', 'HOUSEHOLD_ADDRESS'),
('vocational_training', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 阳光机构
('sunshine_institution', 'ID_CARD', 'ID_CARD'),
('sunshine_institution', 'NAME', 'NAME'),
('sunshine_institution', 'DISTRICT', 'DISTRICT'),
('sunshine_institution', 'STREET', 'STREET'),
('sunshine_institution', 'GENDER', 'GENDER'),
('sunshine_institution', 'AGE', 'AGE'),
('sunshine_institution', 'DISABILITY_CATEGORY', 'DISABILITY_CATEGORY'),
('sunshine_institution', 'DISABILITY_LEVEL', 'DISABILITY_LEVEL'),
('sunshine_institution', 'BIRTHDAY', 'AGE'),
('sunshine_institution', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 基本状况调查
('basic_survey', 'ID_CARD', 'ID_CARD'),
('basic_survey', 'NAME', 'NAME'),
('basic_survey', 'ADDRESS', 'CONTACT_ADDRESS'),
('basic_survey', 'HOUSEHOLDER_NAME', 'HOUSEHOLDER_NAME'),
('basic_survey', 'HOUSEHOLDER_ID_CARD', 'HOUSEHOLDER_ID_CARD'),
('basic_survey', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 美伦车
('meilun_car', 'ID_CARD', 'ID_CARD'),
('meilun_car', 'NAME', 'NAME'),
('meilun_car', 'STREET', 'STREET'),
('meilun_car', 'GENDER', 'GENDER'),
('meilun_car', 'DISABILITY_LEVEL', 'DISABILITY_LEVEL'),
('meilun_car', 'HOUSEHOLD_ADDRESS', 'HOUSEHOLD_ADDRESS'),
('meilun_car', 'RESIDENCE_ADDRESS', 'CONTACT_ADDRESS'),
('meilun_car', 'TELEPHONE', 'PHONE'),
('meilun_car', 'MOBILE_PHONE', 'PHONE'),
('meilun_car', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 助学
('education_aid', 'ID_CARD', 'ID_CARD'),
('education_aid', 'NAME', 'NAME'),
('education_aid', 'AGE', 'AGE'),
('education_aid', 'GENDER', 'GENDER'),
('education_aid', 'CONTACT_PHONE', 'PHONE'),
('education_aid', 'HOUSEHOLD_ADDRESS', 'HOUSEHOLD_ADDRESS'),
('education_aid', 'RESIDENCE_ADDRESS', 'CONTACT_ADDRESS'),
('education_aid', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 节日慰问金
('festival_condolence', 'ID_CARD', 'ID_CARD'),
('festival_condolence', 'NAME', 'NAME'),
('festival_condolence', 'COMMITTEE', 'COMMITTEE'),
('festival_condolence', 'DISABILITY_CATEGORY', 'DISABILITY_CATEGORY'),
('festival_condolence', 'CONTACT_PHONE', 'PHONE'),
('festival_condolence', 'RESIDENCE_ADDRESS', 'CONTACT_ADDRESS'),
('festival_condolence', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 临时困难金
('temporary_aid', 'ID_CARD', 'ID_CARD'),
('temporary_aid', 'NAME', 'NAME'),
('temporary_aid', 'STREET', 'STREET'),
('temporary_aid', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO'),

-- 结对帮扶
('pairing_help', 'ID_CARD', 'ID_CARD'),
('pairing_help', 'NAME', 'NAME'),
('pairing_help', 'GENDER', 'GENDER'),
('pairing_help', 'HOUSEHOLD_ADDRESS', 'HOUSEHOLD_ADDRESS'),
('pairing_help', 'HOME_ADDRESS', 'CONTACT_ADDRESS'),
('pairing_help', 'DISABILITY_CERT_NO', 'DISABILITY_CERT_NO');
