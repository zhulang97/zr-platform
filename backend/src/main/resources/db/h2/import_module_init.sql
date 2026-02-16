-- 导入模块配置数据
-- T_IMPORT_MODULE
INSERT INTO T_IMPORT_MODULE (MODULE_CODE, MODULE_NAME, TABLE_NAME, CATEGORY, ID_CARD_FIELD, CERT_NO_FIELD, SORT_ORDER, IS_ACTIVE, PERMISSION_CODE) VALUES
('disabled_cert', '残疾人认证', 'T_IMP_DISABLED_CERT', '基本保障', 'ID_CARD', 'DISABILITY_CERT_NO', 1, 1, 'import:disabled_cert'),
('disabled_mgmt', '残疾管理', 'T_IMP_DISABLED_MGMT', '基本保障', 'ID_CARD', 'DISABILITY_CERT_NO', 2, 1, 'import:disabled_mgmt'),
('rehab_subsidy', '康复医疗/器材补助', 'T_IMP_REHAB_SUBSIDY', '医疗康复', 'ID_CARD', 'DISABILITY_CERT_NO', 3, 1, 'import:rehab_subsidy'),
('pension_subsidy', '养老补助', 'T_IMP_PENSION_SUBSIDY', '基本保障', 'ID_CARD', 'DISABILITY_CERT_NO', 4, 1, 'import:pension_subsidy'),
('personal_cert', '个人证管理', 'T_IMP_PERSONAL_CERT', '证件管理', 'ID_CARD', 'DISABILITY_CERT_NO', 5, 1, 'import:personal_cert'),
('family_situation', '残疾家庭情况', 'T_IMP_FAMILY_SITUATION', '家庭服务', 'ID_CARD', 'DISABILITY_CERT_NO', 6, 1, 'import:family_situation'),
('loan_card', '借信卡', 'T_IMP_LOAN_CARD', '其他服务', 'ID_CARD', 'DISABILITY_CERT_NO', 7, 1, 'import:loan_card'),
('chronic_disease', '慢性病/康护', 'T_IMP_CHRONIC_DISEASE', '医疗康复', 'ID_CARD', 'DISABILITY_CERT_NO', 8, 1, 'import:chronic_disease'),
('institution_care', '机构养护', 'T_IMP_INSTITUTION_CARE', '医疗康复', 'ID_CARD', 'DISABILITY_CERT_NO', 9, 1, 'import:institution_care'),
('cochlear_implant', '人工耳蜗', 'T_IMP_COCHLEAR_IMPLANT', '医疗康复', 'ID_CARD', 'DISABILITY_CERT_NO', 10, 1, 'import:cochlear_implant'),
('rehab_orthodontics', '康复体矫', 'T_IMP_REHAB_ORTHODONTICS', '医疗康复', 'ID_CARD', 'DISABILITY_CERT_NO', 11, 1, 'import:rehab_orthodontics'),
('child_rehab', '儿童康复矫正', 'T_IMP_CHILD_REHAB', '医疗康复', 'ID_CARD', 'DISABILITY_CERT_NO', 12, 1, 'import:child_rehab'),
('traffic_subsidy', '交通补贴', 'T_IMP_TRAFFIC_SUBSIDY', '补贴发放', 'ID_CARD', 'DISABILITY_CERT_NO', 13, 1, 'import:traffic_subsidy'),
('precise_help', '精准帮扶', 'T_IMP_PRECISE_HELP', '补贴发放', 'ID_CARD', 'DISABILITY_CERT_NO', 14, 1, 'import:precise_help'),
('disabled_application', '残疾人（人/团体申请）', 'T_IMP_DISABLED_APPLICATION', '其他服务', 'ID_CARD', 'DISABILITY_CERT_NO', 15, 1, 'import:disabled_application'),
('assistive_device', '辅助器具', 'T_IMP_ASSISTIVE_DEVICE', '辅助服务', 'ID_CARD', 'DISABILITY_CERT_NO', 16, 1, 'import:assistive_device'),
('health_check', '体检', 'T_IMP_HEALTH_CHECK', '医疗康复', 'ID_CARD', 'DISABILITY_CERT_NO', 17, 1, 'import:health_check'),
('two_subsidies', '两项补贴', 'T_IMP_TWO_SUBSIDIES', '补贴发放', 'ID_CARD', 'DISABILITY_CERT_NO', 18, 1, 'import:two_subsidies'),
('employment', '就业', 'T_IMP_EMPLOYMENT', '就业培训', 'ID_CARD', 'DISABILITY_CERT_NO', 19, 1, 'import:employment'),
('vocational_training', '职业技能培训', 'T_IMP_VOCATIONAL_TRAINING', '就业培训', 'ID_CARD', 'DISABILITY_CERT_NO', 20, 1, 'import:vocational_training'),
('sunshine_institution', '阳光机构', 'T_IMP_SUNSHINE_INSTITUTION', '其他服务', 'ID_CARD', 'DISABILITY_CERT_NO', 21, 1, 'import:sunshine_institution'),
('basic_survey', '残疾人基本状况调查', 'T_IMP_BASIC_SURVEY', '调查统计', 'ID_CARD', 'DISABILITY_CERT_NO', 22, 1, 'import:basic_survey'),
('meilun_car', '美伦车', 'T_IMP_MEILUN_CAR', '其他服务', 'ID_CARD', 'DISABILITY_CERT_NO', 23, 1, 'import:meilun_car'),
('education_aid', '助学', 'T_IMP_EDUCATION_AID', '其他服务', 'ID_CARD', 'DISABILITY_CERT_NO', 24, 1, 'import:education_aid'),
('festival_condolence', '节日慰问金（回、国、困）', 'T_IMP_FESTIVAL_CONDOLENCE', '补贴发放', 'ID_CARD', 'DISABILITY_CERT_NO', 25, 1, 'import:festival_condolence'),
('temporary_aid', '临时困难金', 'T_IMP_TEMPORARY_AID', '补贴发放', 'ID_CARD', 'DISABILITY_CERT_NO', 26, 1, 'import:temporary_aid'),
('pairing_help', '结对帮扶', 'T_IMP_PAIRING_HELP', '帮扶服务', 'ID_CARD', 'DISABILITY_CERT_NO', 27, 1, 'import:pairing_help');

-- T_IMPORT_MODULE_FIELD 字段定义
-- 模块1: 残疾人认证 (20个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('disabled_cert', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 1),
('disabled_cert', 'ZHENG_JIAN_HAO_MA', '证件号码', 'ZHENG_JIAN_HAO_MA', 'VARCHAR', 1, 1, 2),
('disabled_cert', 'NIAN_LING', '年龄', 'NIAN_LING', 'INT', 0, 0, 3),
('disabled_cert', 'XING_BIE', '性别', 'XING_BIE', 'VARCHAR', 0, 0, 4),
('disabled_cert', 'QU_XIAN', '区县', 'QU_XIAN', 'VARCHAR', 0, 0, 5),
('disabled_cert', 'JIE_DAO', '街道', 'JIE_DAO', 'VARCHAR', 0, 0, 6),
('disabled_cert', 'JU_WEI', '居委', 'JU_WEI', 'VARCHAR', 0, 0, 7),
('disabled_cert', 'HU_JI_LEI_BIE', '户籍类别', 'HU_JI_LEI_BIE', 'VARCHAR', 0, 0, 8),
('disabled_cert', 'ZHUANG_TAI', '状态', 'ZHUANG_TAI', 'VARCHAR', 0, 0, 9),
('disabled_cert', 'CAN_JI_LEI_BIE', '残疾类别', 'CAN_JI_LEI_BIE', 'VARCHAR', 0, 0, 10),
('disabled_cert', 'CAN_JI_DENG_JI', '残疾等级', 'CAN_JI_DENG_JI', 'VARCHAR', 0, 0, 11),
('disabled_cert', 'SHUANG_CE_DAN_CE', '双侧或单侧', 'SHUANG_CE_DAN_CE', 'VARCHAR', 0, 0, 12),
('disabled_cert', 'HU_KOU_DI_ZHI', '户口地址', 'HU_KOU_DI_ZHI', 'VARCHAR', 0, 0, 13),
('disabled_cert', 'LIAN_XI_DI_ZHI', '联系地址', 'LIAN_XI_DI_ZHI', 'VARCHAR', 0, 0, 14),
('disabled_cert', 'LIAN_XI_DIAN_HUA', '联系电话', 'LIAN_XI_DIAN_HUA', 'VARCHAR', 0, 0, 15),
('disabled_cert', 'GONG_ZUO_JIU_YE', '工作/就业', 'GONG_ZUO_JIU_YE', 'VARCHAR', 0, 0, 16),
('disabled_cert', 'CHU_CI_SHEN_LING_RI_QI', '初次申领日期', 'CHU_CI_SHEN_LING_RI_QI', 'DATE', 0, 0, 17),
('disabled_cert', 'YE_WU_SHEN_QING_LEI_XING', '业务申请类型', 'YE_WU_SHEN_QING_LEI_XING', 'VARCHAR', 0, 0, 18),
('disabled_cert', 'SHEN_QING_RI_QI', '申请日期', 'SHEN_QING_RI_QI', 'DATE', 0, 0, 19),
('disabled_cert', 'BEI_ZHU', '备注', 'BEI_ZHU', 'VARCHAR', 0, 0, 20);

-- 模块2: 残疾管理 (19个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('disabled_mgmt', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 1),
('disabled_mgmt', 'ZHENG_JIAN_HAO', '证件号', 'ZHENG_JIAN_HAO', 'VARCHAR', 1, 1, 2),
('disabled_mgmt', 'HU_JI_QU_XIAN', '户籍区县', 'HU_JI_QU_XIAN', 'VARCHAR', 0, 0, 3),
('disabled_mgmt', 'HU_JI_QU', '户籍区', 'HU_JI_QU', 'VARCHAR', 0, 0, 4),
('disabled_mgmt', 'JU_ZHU_DI', '居住地', 'JU_ZHU_DI', 'VARCHAR', 0, 0, 5),
('disabled_mgmt', 'LIAN_XI_DIAN_HUA', '联系电话', 'LIAN_XI_DIAN_HUA', 'VARCHAR', 0, 0, 6),
('disabled_mgmt', 'LIAN_XI_DI_ZHI', '联系地址', 'LIAN_XI_DI_ZHI', 'VARCHAR', 0, 0, 7),
('disabled_mgmt', 'CHE_PAI_HAO', '车牌号', 'CHE_PAI_HAO', 'VARCHAR', 0, 0, 8),
('disabled_mgmt', 'CHE_LIANG_LEI_XING', '车辆类型', 'CHE_LIANG_LEI_XING', 'VARCHAR', 0, 0, 9),
('disabled_mgmt', 'CHU_CI_FA_FANG_RI_QI', '初次发放日期', 'CHU_CI_FA_FANG_RI_QI', 'DATE', 0, 0, 10),
('disabled_mgmt', 'SHANG_PAI_RI_QI', '上牌日期', 'SHANG_PAI_RI_QI', 'DATE', 0, 0, 11),
('disabled_mgmt', 'CHONG_ZHI_RI_QI', '重置日期', 'CHONG_ZHI_RI_QI', 'DATE', 0, 0, 12),
('disabled_mgmt', 'CAN_JI_LEI_XING_HAO', '残疾类型号', 'CAN_JI_LEI_XING_HAO', 'VARCHAR', 0, 0, 13),
('disabled_mgmt', 'ZHUANG_TAI', '状态', 'ZHUANG_TAI', 'VARCHAR', 0, 0, 14),
('disabled_mgmt', 'TOU_BAO_QING_KUANG', '投保情况', 'TOU_BAO_QING_KUANG', 'VARCHAR', 0, 0, 15),
('disabled_mgmt', 'NIAN_JIAN_QING_KUANG', '年检情况', 'NIAN_JIAN_QING_KUANG', 'VARCHAR', 0, 0, 16),
('disabled_mgmt', 'CHU_CI_REN_DING_RI_QI', '初次认定日期', 'CHU_CI_REN_DING_RI_QI', 'DATE', 0, 0, 17),
('disabled_mgmt', 'BU_TIE_YUE_FEN', '补贴月份', 'BU_TIE_YUE_FEN', 'VARCHAR', 0, 0, 18),
('disabled_mgmt', 'BU_TIE_JIN_E', '补贴金额', 'BU_TIE_JIN_E', 'DECIMAL', 0, 0, 19);

-- 模块3: 康复医疗/器材补助 (9个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('rehab_subsidy', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 1),
('rehab_subsidy', 'SHEN_FEN_ZHENG_HAO', '身份证号', 'SHEN_FEN_ZHENG_HAO', 'VARCHAR', 1, 1, 2),
('rehab_subsidy', 'HU_JI_QU_XIAN', '户籍区县', 'HU_JI_QU_XIAN', 'VARCHAR', 0, 0, 3),
('rehab_subsidy', 'HU_JI_JIE_DAO', '户籍街道', 'HU_JI_JIE_DAO', 'VARCHAR', 0, 0, 4),
('rehab_subsidy', 'CAN_JI_LEI_BIE', '残疾类别', 'CAN_JI_LEI_BIE', 'VARCHAR', 0, 0, 5),
('rehab_subsidy', 'CAN_JI_DENG_JI', '残疾等级', 'CAN_JI_DENG_JI', 'VARCHAR', 0, 0, 6),
('rehab_subsidy', 'YI_BAO_BAN_FA', '医保办法', 'YI_BAO_BAN_FA', 'VARCHAR', 0, 0, 7),
('rehab_subsidy', 'ZHANG_HU_XIN_XI', '账户信息', 'ZHANG_HU_XIN_XI', 'VARCHAR', 0, 0, 8),
('rehab_subsidy', 'HUI_PAN_RI_QI', '回盘日期', 'HUI_PAN_RI_QI', 'DATE', 0, 0, 9);

-- 模块4: 养老补助 (8个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('pension_subsidy', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 1),
('pension_subsidy', 'ZHANG_HU_QU_XIAN', '账户区县', 'ZHANG_HU_QU_XIAN', 'VARCHAR', 0, 0, 2),
('pension_subsidy', 'ZHANG_HU_JIE_ZHEN', '账户街镇', 'ZHANG_HU_JIE_ZHEN', 'VARCHAR', 0, 0, 3),
('pension_subsidy', 'SHEN_FEN_ZHENG_HAO', '身份证号', 'SHEN_FEN_ZHENG_HAO', 'VARCHAR', 1, 1, 4),
('pension_subsidy', 'XING_BIE', '性别', 'XING_BIE', 'VARCHAR', 0, 0, 5),
('pension_subsidy', 'JIAO_FEI_QI_SHI_NIAN_YUE', '缴费起始年月', 'JIAO_FEI_QI_SHI_NIAN_YUE', 'VARCHAR', 0, 0, 6),
('pension_subsidy', 'JIAO_FEI_JIE_ZHI_NIAN_YUE', '缴费截止年月', 'JIAO_FEI_JIE_ZHI_NIAN_YUE', 'VARCHAR', 0, 0, 7),
('pension_subsidy', 'BU_ZHU_JIN_E', '补助金额', 'BU_ZHU_JIN_E', 'DECIMAL', 0, 0, 8);

-- 模块5: 个人证管理 (11个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('personal_cert', 'GE_REN_BIAO_SHI', '个人标识', 'GE_REN_BIAO_SHI', 'VARCHAR', 0, 0, 1),
('personal_cert', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 2),
('personal_cert', 'ZHENG_JIAN_HAO_MA', '证件号码', 'ZHENG_JIAN_HAO_MA', 'VARCHAR', 1, 1, 3),
('personal_cert', 'SHEN_LING_RI_QI', '申领日期', 'SHEN_LING_RI_QI', 'DATE', 0, 0, 4),
('personal_cert', 'JIE_ZHEN', '街镇', 'JIE_ZHEN', 'VARCHAR', 0, 0, 5),
('personal_cert', 'FA_ZHENG_QU', '发证区', 'FA_ZHENG_QU', 'VARCHAR', 0, 0, 6),
('personal_cert', 'SHEN_HE_RI_QI', '审核日期', 'SHEN_HE_RI_QI', 'DATE', 0, 0, 7),
('personal_cert', 'SHI_FOU_ZHI_ZHENG', '是否制证', 'SHI_FOU_ZHI_ZHENG', 'VARCHAR', 0, 0, 8),
('personal_cert', 'DAI_BAN_REN_ZHENG', '代办人证', 'DAI_BAN_REN_ZHENG', 'VARCHAR', 0, 0, 9),
('personal_cert', 'KUAI_DI_DAN_HAO', '快递单号', 'KUAI_DI_DAN_HAO', 'VARCHAR', 0, 0, 10),
('personal_cert', 'SHEN_PI_ZHUANG_TAI', '审批状态', 'SHEN_PI_ZHUANG_TAI', 'VARCHAR', 0, 0, 11);

-- 模块6: 残疾家庭情况 (12个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('family_situation', 'JU_WEI_HUI', '居委会', 'JU_WEI_HUI', 'VARCHAR', 0, 0, 1),
('family_situation', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 2),
('family_situation', 'CAN_JI_ZHENG_HAO', '残疾证号', 'CAN_JI_ZHENG_HAO', 'VARCHAR', 1, 1, 3),
('family_situation', 'CAN_JI_LEI_BIE', '残疾类别', 'CAN_JI_LEI_BIE', 'VARCHAR', 0, 0, 4),
('family_situation', 'DENG_JI', '等级', 'DENG_JI', 'VARCHAR', 0, 0, 5),
('family_situation', 'GAI_ZAO_DI_DIAN', '改造地点', 'GAI_ZAO_DI_DIAN', 'VARCHAR', 0, 0, 6),
('family_situation', 'GAI_ZAO_LEI_XING', '改造类型', 'GAI_ZAO_LEI_XING', 'VARCHAR', 0, 0, 7),
('family_situation', 'GAI_ZAO_JIN_DU', '改造进度', 'GAI_ZAO_JIN_DU', 'VARCHAR', 0, 0, 8),
('family_situation', 'GAI_ZAO_DAN_WEI', '改造单位', 'GAI_ZAO_DAN_WEI', 'VARCHAR', 0, 0, 9),
('family_situation', 'GAI_ZAO_JIN_E', '改造金额', 'GAI_ZAO_JIN_E', 'DECIMAL', 0, 0, 10),
('family_situation', 'GAI_ZAO_FU_ZE_REN', '改造负责人', 'GAI_ZAO_FU_ZE_REN', 'VARCHAR', 0, 0, 11),
('family_situation', 'LIAN_XI_FANG_SHI', '联系方式', 'LIAN_XI_FANG_SHI', 'VARCHAR', 0, 0, 12);

-- 模块7: 借信卡 (7个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('loan_card', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 1),
('loan_card', 'SHENG_RI', '生日', 'SHENG_RI', 'DATE', 0, 0, 2),
('loan_card', 'CAN_JI_REN_ZHENG_HAO', '残疾人证号', 'CAN_JI_REN_ZHENG_HAO', 'VARCHAR', 1, 1, 3),
('loan_card', 'HU_JI_DI_ZHI', '户籍地址', 'HU_JI_DI_ZHI', 'VARCHAR', 0, 0, 4),
('loan_card', 'JIA_TING_ZHU_ZHI', '家庭住址', 'JIA_TING_ZHU_ZHI', 'VARCHAR', 0, 0, 5),
('loan_card', 'DIAN_HUA', '电话', 'DIAN_HUA', 'VARCHAR', 0, 0, 6),
('loan_card', 'SHEN_QING_TAO_CAN', '申请的套餐种类（固定/流量/宽带）', 'SHEN_QING_TAO_CAN', 'VARCHAR', 0, 0, 7);

-- 模块8: 慢性病/康护 (10个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('chronic_disease', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 1),
('chronic_disease', 'ZHENG_JIAN_HAO', '证件号', 'ZHENG_JIAN_HAO', 'VARCHAR', 1, 1, 2),
('chronic_disease', 'HU_JI_QU_XIAN', '户籍区县', 'HU_JI_QU_XIAN', 'VARCHAR', 0, 0, 3),
('chronic_disease', 'HU_JI_JIE_DAO', '户籍街道', 'HU_JI_JIE_DAO', 'VARCHAR', 0, 0, 4),
('chronic_disease', 'CAN_JI_LEI_BIE', '残疾类别', 'CAN_JI_LEI_BIE', 'VARCHAR', 0, 0, 5),
('chronic_disease', 'NIAN_LING', '年龄', 'NIAN_LING', 'INT', 0, 0, 6),
('chronic_disease', 'FU_WU_REN_YUAN_XING_MING', '服务人员姓名', 'FU_WU_REN_YUAN_XING_MING', 'VARCHAR', 0, 0, 7),
('chronic_disease', 'FU_WU_REN_YUAN_CHU_SHENG', '服务人员出生年月', 'FU_WU_REN_YUAN_CHU_SHENG', 'VARCHAR', 0, 0, 8),
('chronic_disease', 'FU_WU_REN_YUAN_LIAN_XI', '服务人员联系方式', 'FU_WU_REN_YUAN_LIAN_XI', 'VARCHAR', 0, 0, 9),
('chronic_disease', 'FU_WU_REN_YUAN_JI_GOU', '服务人员服务机构', 'FU_WU_REN_YUAN_JI_GOU', 'VARCHAR', 0, 0, 10);

-- 模块9: 机构养护 (12个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('institution_care', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 1),
('institution_care', 'ZHENG_JIAN_HAO', '证件号', 'ZHENG_JIAN_HAO', 'VARCHAR', 1, 1, 2),
('institution_care', 'HU_JI_QU_XIAN', '户籍区县', 'HU_JI_QU_XIAN', 'VARCHAR', 0, 0, 3),
('institution_care', 'HU_JI_JIE_DAO', '户籍街道', 'HU_JI_JIE_DAO', 'VARCHAR', 0, 0, 4),
('institution_care', 'CAN_JI_LEI_BIE', '残疾类别', 'CAN_JI_LEI_BIE', 'VARCHAR', 0, 0, 5),
('institution_care', 'NIAN_LING', '年龄', 'NIAN_LING', 'INT', 0, 0, 6),
('institution_care', 'HU_LI_JI_GOU', '护理机构', 'HU_LI_JI_GOU', 'VARCHAR', 0, 0, 7),
('institution_care', 'REN_ZHU_SHEN_HE_RI_QI', '人住审核日期', 'REN_ZHU_SHEN_HE_RI_QI', 'DATE', 0, 0, 8),
('institution_care', 'TUI_CHU_SHEN_HE_RI_QI', '退出审核日期', 'TUI_CHU_SHEN_HE_RI_QI', 'DATE', 0, 0, 9),
('institution_care', 'TUI_CHU_YUAN_YIN', '退出原因', 'TUI_CHU_YUAN_YIN', 'VARCHAR', 0, 0, 10),
('institution_care', 'BU_TIE_YUE_FEN', '补贴月份', 'BU_TIE_YUE_FEN', 'VARCHAR', 0, 0, 11),
('institution_care', 'BU_TIE_JIN_E', '补贴金额', 'BU_TIE_JIN_E', 'DECIMAL', 0, 0, 12);

-- 模块10: 人工耳蜗 (15个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('cochlear_implant', 'JIE_ZHEN', '街镇', 'JIE_ZHEN', 'VARCHAR', 0, 0, 1),
('cochlear_implant', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 2),
('cochlear_implant', 'ZHENG_JIAN_HAO', '证件号', 'ZHENG_JIAN_HAO', 'VARCHAR', 1, 1, 3),
('cochlear_implant', 'CAN_JI_LEI_BIE', '残疾类别', 'CAN_JI_LEI_BIE', 'VARCHAR', 0, 0, 4),
('cochlear_implant', 'LIAN_XI_FANG_SHI', '联系方式', 'LIAN_XI_FANG_SHI', 'VARCHAR', 0, 0, 5),
('cochlear_implant', 'NIAN_LING', '年龄', 'NIAN_LING', 'INT', 0, 0, 6),
('cochlear_implant', 'SHEN_QING_RI_QI', '申请日期', 'SHEN_QING_RI_QI', 'DATE', 0, 0, 7),
('cochlear_implant', 'SHOU_SHU_RI_QI', '手术日期', 'SHOU_SHU_RI_QI', 'DATE', 0, 0, 8),
('cochlear_implant', 'KAI_JI_RI_QI', '开机日期', 'KAI_JI_RI_QI', 'DATE', 0, 0, 9),
('cochlear_implant', 'SHOU_SHU_YI_YUAN', '手术医院', 'SHOU_SHU_YI_YUAN', 'VARCHAR', 0, 0, 10),
('cochlear_implant', 'ZHI_RU_ER', '植入耳', 'ZHI_RU_ER', 'VARCHAR', 0, 0, 11),
('cochlear_implant', 'QI_YONG_ER', '启用耳', 'QI_YONG_ER', 'VARCHAR', 0, 0, 12),
('cochlear_implant', 'SHOU_SHU_JIN_E', '手术金额', 'SHOU_SHU_JIN_E', 'DECIMAL', 0, 0, 13),
('cochlear_implant', 'ZI_FEI_JIN_E', '自费金额', 'ZI_FEI_JIN_E', 'DECIMAL', 0, 0, 14),
('cochlear_implant', 'BU_TIE_JIN_E', '补贴金额', 'BU_TIE_JIN_E', 'DECIMAL', 0, 0, 15);

-- 模块11: 康复体矫 (11个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('rehab_orthodontics', 'JIE_ZHEN', '街镇', 'JIE_ZHEN', 'VARCHAR', 0, 0, 1),
('rehab_orthodontics', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 2),
('rehab_orthodontics', 'ZHENG_JIAN_HAO', '证件号', 'ZHENG_JIAN_HAO', 'VARCHAR', 1, 1, 3),
('rehab_orthodontics', 'CAN_JI_LEI_BIE', '残疾类别', 'CAN_JI_LEI_BIE', 'VARCHAR', 0, 0, 4),
('rehab_orthodontics', 'LIAN_XI_FANG_SHI', '联系方式', 'LIAN_XI_FANG_SHI', 'VARCHAR', 0, 0, 5),
('rehab_orthodontics', 'NIAN_LING', '年龄', 'NIAN_LING', 'INT', 0, 0, 6),
('rehab_orthodontics', 'JI_YIN_CE_YAN_JIE_GUO', '基因测验结果', 'JI_YIN_CE_YAN_JIE_GUO', 'VARCHAR', 0, 0, 7),
('rehab_orthodontics', 'SHEN_QING_SHI_JIAN', '申请时间', 'SHEN_QING_SHI_JIAN', 'TIMESTAMP', 0, 0, 8),
('rehab_orthodontics', 'JIAN_CE_YI_YUAN', '检测医院', 'JIAN_CE_YI_YUAN', 'VARCHAR', 0, 0, 9),
('rehab_orthodontics', 'JIAN_CE_RI_QI', '检测日期', 'JIAN_CE_RI_QI', 'DATE', 0, 0, 10),
('rehab_orthodontics', 'BU_TIE_JIN_E', '补贴金额', 'BU_TIE_JIN_E', 'DECIMAL', 0, 0, 11);

-- 模块12: 儿童康复矫正 (14个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('child_rehab', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 1),
('child_rehab', 'ZHENG_JIAN_HAO', '证件号', 'ZHENG_JIAN_HAO', 'VARCHAR', 1, 1, 2),
('child_rehab', 'HU_JI_QU_XIAN', '户籍区县', 'HU_JI_QU_XIAN', 'VARCHAR', 0, 0, 3),
('child_rehab', 'HU_JI_JIE_DAO', '户籍街道', 'HU_JI_JIE_DAO', 'VARCHAR', 0, 0, 4),
('child_rehab', 'NIAN_LING', '年龄', 'NIAN_LING', 'INT', 0, 0, 5),
('child_rehab', 'CAN_JI_LEI_BIE', '残疾类别', 'CAN_JI_LEI_BIE', 'VARCHAR', 0, 0, 6),
('child_rehab', 'BAO_BAO_KA_LEI_BIE', '宝宝卡类别', 'BAO_BAO_KA_LEI_BIE', 'VARCHAR', 0, 0, 7),
('child_rehab', 'CHU_SHENG_RI_QI', '出生日期', 'CHU_SHENG_RI_QI', 'DATE', 0, 0, 8),
('child_rehab', 'SHEN_QING_RI_QI', '申请日期', 'SHEN_QING_RI_QI', 'DATE', 0, 0, 9),
('child_rehab', 'PING_GU_YI_YUAN', '评估医院', 'PING_GU_YI_YUAN', 'VARCHAR', 0, 0, 10),
('child_rehab', 'KANG_FU_JI_GOU', '康复机构', 'KANG_FU_JI_GOU', 'VARCHAR', 0, 0, 11),
('child_rehab', 'JI_GOU_DI_ZHI', '机构地址', 'JI_GOU_DI_ZHI', 'VARCHAR', 0, 0, 12),
('child_rehab', 'BU_TIE_JIN_E', '补贴金额', 'BU_TIE_JIN_E', 'DECIMAL', 0, 0, 13),
('child_rehab', 'BU_TIE_NIAN_LING', '补贴年龄', 'BU_TIE_NIAN_LING', 'VARCHAR', 0, 0, 14);

-- 模块13: 交通补贴 (14个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('traffic_subsidy', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 1),
('traffic_subsidy', 'ZHENG_JIAN_HAO', '证件号', 'ZHENG_JIAN_HAO', 'VARCHAR', 1, 1, 2),
('traffic_subsidy', 'YU_BO_YUE_FEN', '预拨月份', 'YU_BO_YUE_FEN', 'VARCHAR', 0, 0, 3),
('traffic_subsidy', 'BU_FA_YUE_FEN', '补发月份', 'BU_FA_YUE_FEN', 'VARCHAR', 0, 0, 4),
('traffic_subsidy', 'BU_TIE_JIN_E', '补贴金额', 'BU_TIE_JIN_E', 'DECIMAL', 0, 0, 5),
('traffic_subsidy', 'HU_JI_DI_ZHI', '户籍地址', 'HU_JI_DI_ZHI', 'VARCHAR', 0, 0, 6),
('traffic_subsidy', 'HU_JI_DIAN_WEI', '户籍点委', 'HU_JI_DIAN_WEI', 'VARCHAR', 0, 0, 7),
('traffic_subsidy', 'LIAN_XI_DIAN_HUA', '联系电话', 'LIAN_XI_DIAN_HUA', 'VARCHAR', 0, 0, 8),
('traffic_subsidy', 'CAN_JI_REN_JIAO_SE', '残疾人角色', 'CAN_JI_REN_JIAO_SE', 'VARCHAR', 0, 0, 9),
('traffic_subsidy', 'LAI_YUAN', '来源', 'LAI_YUAN', 'VARCHAR', 0, 0, 10),
('traffic_subsidy', 'KAI_HU_YIN_HANG', '开户银行', 'KAI_HU_YIN_HANG', 'VARCHAR', 0, 0, 11),
('traffic_subsidy', 'ZHI_HANG_MING_CHENG', '支行名称', 'ZHI_HANG_MING_CHENG', 'VARCHAR', 0, 0, 12),
('traffic_subsidy', 'KAI_HU_REN_XING_MING', '开户人姓名', 'KAI_HU_REN_XING_MING', 'VARCHAR', 0, 0, 13),
('traffic_subsidy', 'YIN_HANG_ZHANG_HAO', '银行账号', 'YIN_HANG_ZHANG_HAO', 'VARCHAR', 0, 0, 14);

-- 模块14: 精准帮扶 (10个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('precise_help', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 1),
('precise_help', 'SHEN_FEN_ZHENG', '身份证', 'SHEN_FEN_ZHENG', 'VARCHAR', 1, 1, 2),
('precise_help', 'BU_TIE_JIN_E', '补贴金额', 'BU_TIE_JIN_E', 'DECIMAL', 0, 0, 3),
('precise_help', 'SHI_FOU_FANG_SHI', '是否访视', 'SHI_FOU_FANG_SHI', 'VARCHAR', 0, 0, 4),
('precise_help', 'KAI_HU_REN', '开户人', 'KAI_HU_REN', 'VARCHAR', 0, 0, 5),
('precise_help', 'KAI_HU_HANG', '开户行', 'KAI_HU_HANG', 'VARCHAR', 0, 0, 6),
('precise_help', 'KAI_HU_ZHI_HANG', '开户支行', 'KAI_HU_ZHI_HANG', 'VARCHAR', 0, 0, 7),
('precise_help', 'YIN_HANG_ZHANG_HAO', '银行账号', 'YIN_HANG_ZHANG_HAO', 'VARCHAR', 0, 0, 8),
('precise_help', 'BU_TIE_YUAN_YIN', '补贴原因', 'BU_TIE_YUAN_YIN', 'VARCHAR', 0, 0, 9),
('precise_help', 'BU_TIE_YUAN_YIN_MIAO_SHU', '补贴原因描述', 'BU_TIE_YUAN_YIN_MIAO_SHU', 'VARCHAR', 0, 0, 10);

-- 模块15: 残疾人（人/团体申请） (10个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('disabled_application', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 1),
('disabled_application', 'ZHENG_JIAN_HAO', '证件号', 'ZHENG_JIAN_HAO', 'VARCHAR', 1, 1, 2),
('disabled_application', 'HU_JI_QU_XIAN', '户籍区县', 'HU_JI_QU_XIAN', 'VARCHAR', 0, 0, 3),
('disabled_application', 'HU_JI_JIE_DAO', '户籍街道', 'HU_JI_JIE_DAO', 'VARCHAR', 0, 0, 4),
('disabled_application', 'CAN_JI_LEI_BIE', '残疾类别', 'CAN_JI_LEI_BIE', 'VARCHAR', 0, 0, 5),
('disabled_application', 'CAN_JI_DENG_JI', '残疾等级', 'CAN_JI_DENG_JI', 'VARCHAR', 0, 0, 6),
('disabled_application', 'NIAN_LING', '年龄', 'NIAN_LING', 'INT', 0, 0, 7),
('disabled_application', 'SHI_XIANG_MING_CHENG', '事项名称', 'SHI_XIANG_MING_CHENG', 'VARCHAR', 0, 0, 8),
('disabled_application', 'TUI_SONG_SHI_JIAN', '推送时间', 'TUI_SONG_SHI_JIAN', 'TIMESTAMP', 0, 0, 9),
('disabled_application', 'YI_XIANG', '意向', 'YI_XIANG', 'VARCHAR', 0, 0, 10);

-- 模块16: 辅助器具 (25个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('assistive_device', 'JIN_DU', '进度', 'JIN_DU', 'VARCHAR', 0, 0, 1),
('assistive_device', 'SHEN_QING_LAI_YUAN', '申请来源', 'SHEN_QING_LAI_YUAN', 'VARCHAR', 0, 0, 2),
('assistive_device', 'SHEN_QING_DAN_HAO', '申请单号', 'SHEN_QING_DAN_HAO', 'VARCHAR', 0, 0, 3),
('assistive_device', 'DING_DAN_HAO', '订单号', 'DING_DAN_HAO', 'VARCHAR', 0, 0, 4),
('assistive_device', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 5),
('assistive_device', 'ZHENG_JIAN_HAO', '证件号', 'ZHENG_JIAN_HAO', 'VARCHAR', 1, 1, 6),
('assistive_device', 'HU_JI_QU_XIAN', '户籍区县', 'HU_JI_QU_XIAN', 'VARCHAR', 0, 0, 7),
('assistive_device', 'HU_JI_JIE_DAO', '户籍街道', 'HU_JI_JIE_DAO', 'VARCHAR', 0, 0, 8),
('assistive_device', 'CAN_JI_LEI_BIE', '残疾类别', 'CAN_JI_LEI_BIE', 'VARCHAR', 0, 0, 9),
('assistive_device', 'CAN_JI_DENG_JI', '残疾等级', 'CAN_JI_DENG_JI', 'VARCHAR', 0, 0, 10),
('assistive_device', 'CAN_JI_BU_WEI', '残疾部位', 'CAN_JI_BU_WEI', 'VARCHAR', 0, 0, 11),
('assistive_device', 'PING_DING_JIE_GUO', '评定结果', 'PING_DING_JIE_GUO', 'VARCHAR', 0, 0, 12),
('assistive_device', 'SHOU_HUO_REN', '收货人', 'SHOU_HUO_REN', 'VARCHAR', 0, 0, 13),
('assistive_device', 'SHOU_HUO_DIAN_HUA', '收货电话', 'SHOU_HUO_DIAN_HUA', 'VARCHAR', 0, 0, 14),
('assistive_device', 'SHOU_HUO_DI_ZHI', '收货地址', 'SHOU_HUO_DI_ZHI', 'VARCHAR', 0, 0, 15),
('assistive_device', 'SHEN_QING_RI_QI', '申请日期', 'SHEN_QING_RI_QI', 'DATE', 0, 0, 16),
('assistive_device', 'SHEN_HE_RI_QI', '审核日期', 'SHEN_HE_RI_QI', 'DATE', 0, 0, 17),
('assistive_device', 'LING_QU_RI_QI', '领取日期', 'LING_QU_RI_QI', 'DATE', 0, 0, 18),
('assistive_device', 'SHEN_QING_ZHUANG_TAI', '申请状态', 'SHEN_QING_ZHUANG_TAI', 'VARCHAR', 0, 0, 19),
('assistive_device', 'SHEN_QING_TONG_GUO', '申请通过', 'SHEN_QING_TONG_GUO', 'VARCHAR', 0, 0, 20),
('assistive_device', 'HU_JI_DI_ZHI', '户籍地址', 'HU_JI_DI_ZHI', 'VARCHAR', 0, 0, 21),
('assistive_device', 'BU_TIE_JIN_E', '补贴金额', 'BU_TIE_JIN_E', 'DECIMAL', 0, 0, 22),
('assistive_device', 'DAN_WEI_JIN_E', '单位金额', 'DAN_WEI_JIN_E', 'DECIMAL', 0, 0, 23),
('assistive_device', 'GE_REN_CHENG_DAN', '个人承担', 'GE_REN_CHENG_DAN', 'DECIMAL', 0, 0, 24),
('assistive_device', 'BU_TIE_JIN_E_2', '补贴金额2', 'BU_TIE_JIN_E_2', 'DECIMAL', 0, 0, 25);

-- 模块17: 体检 (9个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('health_check', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 1),
('health_check', 'ZHENG_JIAN_HAO', '证件号', 'ZHENG_JIAN_HAO', 'VARCHAR', 1, 1, 2),
('health_check', 'HU_JI_QU_XIAN', '户籍区县', 'HU_JI_QU_XIAN', 'VARCHAR', 0, 0, 3),
('health_check', 'HU_JI_JIE_DAO', '户籍街道', 'HU_JI_JIE_DAO', 'VARCHAR', 0, 0, 4),
('health_check', 'CAN_JI_LEI_BIE', '残疾类别', 'CAN_JI_LEI_BIE', 'VARCHAR', 0, 0, 5),
('health_check', 'CAN_JI_DENG_JI', '残疾等级', 'CAN_JI_DENG_JI', 'VARCHAR', 0, 0, 6),
('health_check', 'NIAN_LING', '年龄', 'NIAN_LING', 'INT', 0, 0, 7),
('health_check', 'TI_JIAN_JI_GOU', '体检机构', 'TI_JIAN_JI_GOU', 'VARCHAR', 0, 0, 8),
('health_check', 'TI_JIAN_RI_QI', '体检日期', 'TI_JIAN_RI_QI', 'DATE', 0, 0, 9);

-- 模块18: 两项补贴 (12个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('two_subsidies', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 1),
('two_subsidies', 'ZHENG_JIAN_HAO', '证件号', 'ZHENG_JIAN_HAO', 'VARCHAR', 1, 1, 2),
('two_subsidies', 'HU_JI_QU_XIAN', '户籍区县', 'HU_JI_QU_XIAN', 'VARCHAR', 0, 0, 3),
('two_subsidies', 'HU_JI_JIE_DAO', '户籍街道', 'HU_JI_JIE_DAO', 'VARCHAR', 0, 0, 4),
('two_subsidies', 'CAN_JI_LEI_BIE', '残疾类别', 'CAN_JI_LEI_BIE', 'VARCHAR', 0, 0, 5),
('two_subsidies', 'CAN_JI_DENG_JI', '残疾等级', 'CAN_JI_DENG_JI', 'VARCHAR', 0, 0, 6),
('two_subsidies', 'NIAN_LING', '年龄', 'NIAN_LING', 'INT', 0, 0, 7),
('two_subsidies', 'SHEN_QING_SHI_JIAN', '申请时间', 'SHEN_QING_SHI_JIAN', 'TIMESTAMP', 0, 0, 8),
('two_subsidies', 'BU_TIE_LEI_XING', '补贴类型', 'BU_TIE_LEI_XING', 'VARCHAR', 0, 0, 9),
('two_subsidies', 'BU_TIE_BIAO_ZHUN', '补贴标准', 'BU_TIE_BIAO_ZHUN', 'VARCHAR', 0, 0, 10),
('two_subsidies', 'BU_TIE_JIN_E', '补贴金额', 'BU_TIE_JIN_E', 'DECIMAL', 0, 0, 11),
('two_subsidies', 'ZHUANG_TAI', '状态', 'ZHUANG_TAI', 'VARCHAR', 0, 0, 12);

-- 模块19: 就业 (24个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('employment', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 1),
('employment', 'XING_BIE', '性别', 'XING_BIE', 'VARCHAR', 0, 0, 2),
('employment', 'CAN_JI_ZHENG_HAO', '残疾证号', 'CAN_JI_ZHENG_HAO', 'VARCHAR', 0, 0, 3),
('employment', 'ZHENG_JIAN_HAO', '证件号', 'ZHENG_JIAN_HAO', 'VARCHAR', 1, 1, 4),
('employment', 'CAN_JI_DENG_JI', '残疾登记', 'CAN_JI_DENG_JI', 'VARCHAR', 0, 0, 5),
('employment', 'JIU_YE_ZHUANG_KUANG', '就业状况', 'JIU_YE_ZHUANG_KUANG', 'VARCHAR', 0, 0, 6),
('employment', 'CONG_SHI_HANG_YE', '从事行业', 'CONG_SHI_HANG_YE', 'VARCHAR', 0, 0, 7),
('employment', 'JIU_YE_TU_JING', '就业途径', 'JIU_YE_TU_JING', 'VARCHAR', 0, 0, 8),
('employment', 'WEI_JIU_YE_YUAN_YIN', '未就业原因', 'WEI_JIU_YE_YUAN_YIN', 'VARCHAR', 0, 0, 9),
('employment', 'DAN_WEI_MING_CHENG', '单位名称', 'DAN_WEI_MING_CHENG', 'VARCHAR', 0, 0, 10),
('employment', 'LAO_DONG_HE_TONG', '劳动合同', 'LAO_DONG_HE_TONG', 'VARCHAR', 0, 0, 11),
('employment', 'GU_DING_QI_XIAN_HE_TONG', '固定期限合同', 'GU_DING_QI_XIAN_HE_TONG', 'VARCHAR', 0, 0, 12),
('employment', 'QI_DONG_SHI_JIAN', '启动时间', 'QI_DONG_SHI_JIAN', 'DATE', 0, 0, 13),
('employment', 'JIE_ZHI_SHI_JIAN', '截止时间', 'JIE_ZHI_SHI_JIAN', 'DATE', 0, 0, 14),
('employment', 'JIU_YE_YE_TAI', '就业业态', 'JIU_YE_YE_TAI', 'VARCHAR', 0, 0, 15),
('employment', 'SHENG_HUO_ZI_LI_NENG_LI', '生活自理能力', 'SHENG_HUO_ZI_LI_NENG_LI', 'VARCHAR', 0, 0, 16),
('employment', 'JIU_YE_YI_YUAN', '就业意愿', 'JIU_YE_YI_YUAN', 'VARCHAR', 0, 0, 17),
('employment', 'HU_KOU_SUO_ZAI_DI_ZHI', '户口所在地址', 'HU_KOU_SUO_ZAI_DI_ZHI', 'VARCHAR', 0, 0, 18),
('employment', 'HU_KOU_SUO_ZAI_JIE_DAO', '户口所在街道', 'HU_KOU_SUO_ZAI_JIE_DAO', 'VARCHAR', 0, 0, 19),
('employment', 'HU_KOU_SUO_ZAI_CUN', '户口所在村/社区', 'HU_KOU_SUO_ZAI_CUN', 'VARCHAR', 0, 0, 20),
('employment', 'LIAN_XI_FANG_SHI', '联系方式', 'LIAN_XI_FANG_SHI', 'VARCHAR', 0, 0, 21),
('employment', 'SHOU_JI_HAO', '手机号', 'SHOU_JI_HAO', 'VARCHAR', 0, 0, 22),
('employment', 'DIAN_HUA_HAO_MA', '电话号码', 'DIAN_HUA_HAO_MA', 'VARCHAR', 0, 0, 23),
('employment', 'LU_RU_DI_QU', '录入地区', 'LU_RU_DI_QU', 'VARCHAR', 0, 0, 24);

-- 模块20: 职业技能培训 (9个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('vocational_training', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 1),
('vocational_training', 'CAN_JI_REN_ZHENG', '残疾人证', 'CAN_JI_REN_ZHENG', 'VARCHAR', 0, 0, 2),
('vocational_training', 'CAN_JI_LEI_BIE', '残疾类别', 'CAN_JI_LEI_BIE', 'VARCHAR', 0, 0, 3),
('vocational_training', 'CAN_JI_DENG_JI', '残疾等级', 'CAN_JI_DENG_JI', 'VARCHAR', 0, 0, 4),
('vocational_training', 'NIAN_LING', '年龄', 'NIAN_LING', 'INT', 0, 0, 5),
('vocational_training', 'HU_JI_DI', '户籍地', 'HU_JI_DI', 'VARCHAR', 0, 0, 6),
('vocational_training', 'PEI_XUN_XIANG_MU', '培训项目名称', 'PEI_XUN_XIANG_MU', 'VARCHAR', 0, 0, 7),
('vocational_training', 'KE_CHENG_LEI_BIE', '课程类别', 'KE_CHENG_LEI_BIE', 'VARCHAR', 0, 0, 8),
('vocational_training', 'PEI_XUN_DENG_JI', '培训等级', 'PEI_XUN_DENG_JI', 'VARCHAR', 0, 0, 9);

-- 模块21: 阳光机构 (14个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('sunshine_institution', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 1),
('sunshine_institution', 'SHEN_FEN_ZHENG_HAO', '身份证号', 'SHEN_FEN_ZHENG_HAO', 'VARCHAR', 1, 1, 2),
('sunshine_institution', 'YANG_GUANG_HUI_DI', '阳光惠地', 'YANG_GUANG_HUI_DI', 'VARCHAR', 0, 0, 3),
('sunshine_institution', 'ZHU_CE_RI_QI', '注册日期', 'ZHU_CE_RI_QI', 'DATE', 0, 0, 4),
('sunshine_institution', 'YOU_XIAO_ZHUANG_TAI', '有效状态', 'YOU_XIAO_ZHUANG_TAI', 'VARCHAR', 0, 0, 5),
('sunshine_institution', 'QU_XIAN', '区县', 'QU_XIAN', 'VARCHAR', 0, 0, 6),
('sunshine_institution', 'JIE_DAO', '街道', 'JIE_DAO', 'VARCHAR', 0, 0, 7),
('sunshine_institution', 'JI_GOU_MING_CHENG', '机构名称', 'JI_GOU_MING_CHENG', 'VARCHAR', 0, 0, 8),
('sunshine_institution', 'XING_BIE', '性别', 'XING_BIE', 'VARCHAR', 0, 0, 9),
('sunshine_institution', 'NIAN_LING', '年龄', 'NIAN_LING', 'INT', 0, 0, 10),
('sunshine_institution', 'CAN_JI_LEI_BIE', '残疾类别', 'CAN_JI_LEI_BIE', 'VARCHAR', 0, 0, 11),
('sunshine_institution', 'CAN_JI_DENG_JI', '残疾等级', 'CAN_JI_DENG_JI', 'VARCHAR', 0, 0, 12),
('sunshine_institution', 'XUE_YUAN_CHU_SHENG_NIAN_YUE', '学员出生年月', 'XUE_YUAN_CHU_SHENG_NIAN_YUE', 'VARCHAR', 0, 0, 13),
('sunshine_institution', 'YUE_FEN', '月份', 'YUE_FEN', 'VARCHAR', 0, 0, 14);

-- 模块22: 残疾人基本状况调查 (18个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('basic_survey', 'DI_ZHI', '地址', 'DI_ZHI', 'VARCHAR', 0, 0, 1),
('basic_survey', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 2),
('basic_survey', 'CAN_JI_REN_ZHENG_HAO', '残疾人证号', 'CAN_JI_REN_ZHENG_HAO', 'VARCHAR', 1, 1, 3),
('basic_survey', 'HUN_YIN_ZHUANG_KUANG', '婚姻状况', 'HUN_YIN_ZHUANG_KUANG', 'VARCHAR', 0, 0, 4),
('basic_survey', 'LIAN_XI_REN_XING_MING', '联系人姓名', 'LIAN_XI_REN_XING_MING', 'VARCHAR', 0, 0, 5),
('basic_survey', 'HE_LIAN_XI_REN_GUAN_XI', '和联系人关系', 'HE_LIAN_XI_REN_GUAN_XI', 'VARCHAR', 0, 0, 6),
('basic_survey', 'SHI_FOU_ZAI_YANG_LAO', '是否在养老/护理机构', 'SHI_FOU_ZAI_YANG_LAO', 'VARCHAR', 0, 0, 7),
('basic_survey', 'JU_ZHU_XIN_XI', '居住信息（家庭住址/房屋住址）', 'JU_ZHU_XIN_XI', 'VARCHAR', 0, 0, 8),
('basic_survey', 'JIA_TING_JING_JI', '家庭经济情况', 'JIA_TING_JING_JI', 'VARCHAR', 0, 0, 9),
('basic_survey', 'CAN_JI_ZHUANG_KUANG', '残疾状况', 'CAN_JI_ZHUANG_KUANG', 'VARCHAR', 0, 0, 10),
('basic_survey', 'GONG_ZUO_JIU_YE', '工作就业情况', 'GONG_ZUO_JIU_YE', 'VARCHAR', 0, 0, 11),
('basic_survey', 'SHE_HUI_BAO_ZHANG', '社会保障情况', 'SHE_HUI_BAO_ZHANG', 'VARCHAR', 0, 0, 12),
('basic_survey', 'JIAN_KANG_ZHUANG_KUANG', '健康状况', 'JIAN_KANG_ZHUANG_KUANG', 'VARCHAR', 0, 0, 13),
('basic_survey', 'JI_GOU_KANG_FU', '机构康复/辅助器具服务情况', 'JI_GOU_KANG_FU', 'VARCHAR', 0, 0, 14),
('basic_survey', 'WEN_HUA_TI_YU', '文化体育情况', 'WEN_HUA_TI_YU', 'VARCHAR', 0, 0, 15),
('basic_survey', 'JIA_TING_JIE_GOU', '家庭结构', 'JIA_TING_JIE_GOU', 'VARCHAR', 0, 0, 16),
('basic_survey', 'HU_ZHU_XING_MING', '户主姓名', 'HU_ZHU_XING_MING', 'VARCHAR', 0, 0, 17),
('basic_survey', 'HU_ZHU_SHEN_FEN_ZHENG', '户主身份证号', 'HU_ZHU_SHEN_FEN_ZHENG', 'VARCHAR', 0, 0, 18);

-- 模块23: 美伦车 (15个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('meilun_car', 'JIE_ZHEN', '街镇', 'JIE_ZHEN', 'VARCHAR', 0, 0, 1),
('meilun_car', 'XING_BIE', '性别', 'XING_BIE', 'VARCHAR', 0, 0, 2),
('meilun_car', 'SHEN_FEN_ZHENG_HAO', '身份证号', 'SHEN_FEN_ZHENG_HAO', 'VARCHAR', 1, 1, 3),
('meilun_car', 'CAN_JI_DENG_JI', '残疾等级', 'CAN_JI_DENG_JI', 'VARCHAR', 0, 0, 4),
('meilun_car', 'CAN_JI_REN_ZHENG', '残疾人证', 'CAN_JI_REN_ZHENG', 'VARCHAR', 0, 0, 5),
('meilun_car', 'HU_JI_DI', '户籍地', 'HU_JI_DI', 'VARCHAR', 0, 0, 6),
('meilun_car', 'JU_ZHU_DI', '居住地', 'JU_ZHU_DI', 'VARCHAR', 0, 0, 7),
('meilun_car', 'YOU_ZHENG_BIAN_MA', '邮政编码', 'YOU_ZHENG_BIAN_MA', 'VARCHAR', 0, 0, 8),
('meilun_car', 'GU_HUA_HAO_MA', '固话号码', 'GU_HUA_HAO_MA', 'VARCHAR', 0, 0, 9),
('meilun_car', 'SHOU_JI_HAO_MA', '手机号码', 'SHOU_JI_HAO_MA', 'VARCHAR', 0, 0, 10),
('meilun_car', 'KAI_HU_HANG', '开户行', 'KAI_HU_HANG', 'VARCHAR', 0, 0, 11),
('meilun_car', 'YIN_HANG_KA_ZHANG_HAO', '银行卡账号', 'YIN_HANG_KA_ZHANG_HAO', 'VARCHAR', 0, 0, 12),
('meilun_car', 'SHEN_QING_SHI_JIAN', '申请时间', 'SHEN_QING_SHI_JIAN', 'TIMESTAMP', 0, 0, 13),
('meilun_car', 'BEI_ZHU', '备注', 'BEI_ZHU', 'VARCHAR', 0, 0, 14),
('meilun_car', 'BU_TIE_JIN_E', '补贴金额', 'BU_TIE_JIN_E', 'DECIMAL', 0, 0, 15);

-- 模块24: 助学 (9个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('education_aid', 'XUE_SHENG', '学生', 'XUE_SHENG', 'VARCHAR', 1, 0, 1),
('education_aid', 'NIAN_LING', '年龄', 'NIAN_LING', 'INT', 0, 0, 2),
('education_aid', 'XING_BIE', '性别', 'XING_BIE', 'VARCHAR', 0, 0, 3),
('education_aid', 'CAN_JI_ZHENG_HAO', '残疾证号', 'CAN_JI_ZHENG_HAO', 'VARCHAR', 1, 1, 4),
('education_aid', 'XUE_XIAO', '学校', 'XUE_XIAO', 'VARCHAR', 0, 0, 5),
('education_aid', 'ZHUAN_YE', '专业', 'ZHUAN_YE', 'VARCHAR', 0, 0, 6),
('education_aid', 'LIAN_XI_FANG_SHI', '联系方式', 'LIAN_XI_FANG_SHI', 'VARCHAR', 0, 0, 7),
('education_aid', 'HU_JI_DI', '户籍地', 'HU_JI_DI', 'VARCHAR', 0, 0, 8),
('education_aid', 'JU_ZHU_DI', '居住地', 'JU_ZHU_DI', 'VARCHAR', 0, 0, 9);

-- 模块25: 节日慰问金（回、国、困） (10个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('festival_condolence', 'DA_BING_KUN_NAN', '大病困难', 'DA_BING_KUN_NAN', 'VARCHAR', 0, 0, 1),
('festival_condolence', 'CAN_JI_REN_XING_MING', '残疾人姓名', 'CAN_JI_REN_XING_MING', 'VARCHAR', 1, 0, 2),
('festival_condolence', 'CAN_JI_LEI_BIE', '残疾类别', 'CAN_JI_LEI_BIE', 'VARCHAR', 0, 0, 3),
('festival_condolence', 'CAN_JI_ZHENG_HAO', '残疾证号', 'CAN_JI_ZHENG_HAO', 'VARCHAR', 1, 1, 4),
('festival_condolence', 'HUAN_BING_QING_KUANG', '患病情况', 'HUAN_BING_QING_KUANG', 'VARCHAR', 0, 0, 5),
('festival_condolence', 'LIAN_XI_DIAN_HUA', '联系电话', 'LIAN_XI_DIAN_HUA', 'VARCHAR', 0, 0, 6),
('festival_condolence', 'JU_ZHU_DI_ZHI', '居住地址', 'JU_ZHU_DI_ZHI', 'VARCHAR', 0, 0, 7),
('festival_condolence', 'JIN_E', '金额', 'JIN_E', 'DECIMAL', 0, 0, 8),
('festival_condolence', 'JU_WEI', '居委', 'JU_WEI', 'VARCHAR', 0, 0, 9),
('festival_condolence', 'XIAN_JU_ZHU_DI_ZHI', '现居住地址', 'XIAN_JU_ZHU_DI_ZHI', 'VARCHAR', 0, 0, 10);

-- 模块26: 临时困难金 (7个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('temporary_aid', 'JIE_ZHEN', '街镇', 'JIE_ZHEN', 'VARCHAR', 0, 0, 1),
('temporary_aid', 'SHOU_KUAN_REN_XING_MING', '收款人姓名', 'SHOU_KUAN_REN_XING_MING', 'VARCHAR', 1, 0, 2),
('temporary_aid', 'CAN_JI_REN_ZHENG_HAO', '残疾人证号', 'CAN_JI_REN_ZHENG_HAO', 'VARCHAR', 1, 1, 3),
('temporary_aid', 'SHOU_KUAN_REN_ZHANG_HAO', '收款人账号', 'SHOU_KUAN_REN_ZHANG_HAO', 'VARCHAR', 0, 0, 4),
('temporary_aid', 'SHOU_KUAN_REN_KAI_HU_HANG', '收款人开户行', 'SHOU_KUAN_REN_KAI_HU_HANG', 'VARCHAR', 0, 0, 5),
('temporary_aid', 'SHOU_KUAN_REN_KAI_HU_ZHI_HANG', '收款人开户支行', 'SHOU_KUAN_REN_KAI_HU_ZHI_HANG', 'VARCHAR', 0, 0, 6),
('temporary_aid', 'BU_TIE_JIN_E', '补贴金额', 'BU_TIE_JIN_E', 'DECIMAL', 0, 0, 7);

-- 模块27: 结对帮扶 (14个字段)
INSERT INTO T_IMPORT_MODULE_FIELD (MODULE_CODE, FIELD_CODE, FIELD_NAME, DB_COLUMN, DATA_TYPE, IS_REQUIRED, IS_UNIQUE, SORT_ORDER) VALUES
('pairing_help', 'XING_MING', '姓名', 'XING_MING', 'VARCHAR', 1, 0, 1),
('pairing_help', 'XING_BIE', '性别', 'XING_BIE', 'VARCHAR', 0, 0, 2),
('pairing_help', 'HU_JI_DI', '户籍地', 'HU_JI_DI', 'VARCHAR', 0, 0, 3),
('pairing_help', 'SHEN_FEN_ZHENG_HAO', '身份证号', 'SHEN_FEN_ZHENG_HAO', 'VARCHAR', 1, 1, 4),
('pairing_help', 'JIA_TING_ZHU_ZHI', '家庭住址', 'JIA_TING_ZHU_ZHI', 'VARCHAR', 0, 0, 5),
('pairing_help', 'HUN_YIN_ZHUANG_KUANG', '婚姻状况', 'HUN_YIN_ZHUANG_KUANG', 'VARCHAR', 0, 0, 6),
('pairing_help', 'JIU_YE_ZHUANG_KUANG', '就业状况', 'JIU_YE_ZHUANG_KUANG', 'VARCHAR', 0, 0, 7),
('pairing_help', 'JIA_TING_QING_KUANG', '家庭情况', 'JIA_TING_QING_KUANG', 'VARCHAR', 0, 0, 8),
('pairing_help', 'BANG_FU_YUAN_YIN', '帮扶原因', 'BANG_FU_YUAN_YIN', 'VARCHAR', 0, 0, 9),
('pairing_help', 'BANG_FU_LEI_XING', '助学/助残/助医/助困', 'BANG_FU_LEI_XING', 'VARCHAR', 0, 0, 10),
('pairing_help', 'REN_YUAN_LEI_BIE', '人员类别', 'REN_YUAN_LEI_BIE', 'VARCHAR', 0, 0, 11),
('pairing_help', 'BEN_REN_JING_JI', '本人经济情况', 'BEN_REN_JING_JI', 'VARCHAR', 0, 0, 12),
('pairing_help', 'JIA_TING_JING_JI', '家庭经济情况', 'JIA_TING_JING_JI', 'VARCHAR', 0, 0, 13),
('pairing_help', 'QI_TA_WEN_TI', '其他问题及诉求', 'QI_TA_WEN_TI', 'VARCHAR', 0, 0, 14);

COMMIT;
