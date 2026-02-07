-- roles
insert into t_role(role_id, role_code, name, status, created_at) values
  (1, 'sys_admin', 'System Admin', 1, current_timestamp()),
  (2, 'business_leader', 'Business Leader', 1, current_timestamp()),
  (3, 'business_user', 'Business User', 1, current_timestamp());

-- permissions (minimal set for bootstrapping)
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values
  (100, 'menu:root:view', 'MENU', 'Root', null, null, null, 0, 1),
  (110, 'menu:home:view', 'MENU', 'Home', '/home', null, 100, 10, 1),
  (120, 'menu:person:view', 'MENU', 'Person', '/person', null, 100, 20, 1),
  (130, 'menu:anomaly:view', 'MENU', 'Anomaly', '/anomaly', null, 100, 30, 1),
  (140, 'menu:stats:view', 'MENU', 'Stats', '/stats', null, 100, 40, 1),
  (150, 'menu:import:view', 'MENU', 'Import', '/import', null, 100, 50, 1),
  (160, 'menu:sys:view', 'MENU', 'System', '/sys', null, 100, 60, 1),
  (161, 'menu:sys:user:view', 'MENU', 'Users', '/sys/users', null, 160, 10, 1),
  (162, 'menu:sys:role:view', 'MENU', 'Roles', '/sys/roles', null, 160, 20, 1),
  (163, 'menu:sys:datascope:view', 'MENU', 'Data Scope', '/sys/datascope', null, 160, 30, 1),
  (164, 'menu:sys:rule:view', 'MENU', 'Rules', '/sys/rules', null, 160, 40, 1);

insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values
  (200, 'dict:read', 'API', 'Read dicts', null, null, null, 0, 1),
  (210, 'person:search', 'API', 'Search persons', null, null, null, 0, 1),
  (211, 'person:read', 'API', 'Read person', null, null, null, 0, 1),
  (212, 'person:export', 'API', 'Export persons', null, null, null, 0, 1),
  (220, 'anomaly:search', 'API', 'Search anomalies', null, null, null, 0, 1),
  (221, 'anomaly:read', 'API', 'Read anomaly', null, null, null, 0, 1),
  (222, 'anomaly:handle', 'API', 'Handle anomaly', null, null, null, 0, 1),
  (223, 'anomaly:export', 'API', 'Export anomalies', null, null, null, 0, 1),
  (224, 'stats:read', 'API', 'Read stats', null, null, null, 0, 1),
  (225, 'stats:export', 'API', 'Export stats', null, null, null, 0, 1),
  (240, 'import:template:read', 'API', 'Read import templates', null, null, null, 0, 1),
  (241, 'import:upload', 'API', 'Upload import file', null, null, null, 0, 1),
  (242, 'import:validate:read', 'API', 'Read import validation', null, null, null, 0, 1),
  (243, 'import:commit', 'API', 'Commit import', null, null, null, 0, 1),
  (244, 'quality:check', 'API', 'Run quality check', null, null, null, 0, 1),
  (245, 'quality:read', 'API', 'Read quality issues', null, null, null, 0, 1),
  (260, 'sys:user:search', 'API', 'Search users', null, null, null, 0, 1),
  (261, 'sys:user:create', 'API', 'Create user', null, null, null, 0, 1),
  (262, 'sys:user:update', 'API', 'Update user', null, null, null, 0, 1),
  (263, 'sys:user:disable', 'API', 'Disable user', null, null, null, 0, 1),
  (264, 'sys:user:resetpwd', 'API', 'Reset user password', null, null, null, 0, 1),
  (265, 'sys:role:manage', 'API', 'Manage roles', null, null, null, 0, 1),
  (266, 'sys:permission:read', 'API', 'Read permissions', null, null, null, 0, 1),
  (267, 'sys:datascope:update', 'API', 'Update data scope', null, null, null, 0, 1),
  (268, 'sys:rule:search', 'API', 'Search rules', null, null, null, 0, 1),
  (269, 'sys:rule:update', 'API', 'Update rules', null, null, null, 0, 1),
  (270, 'sys:metrics:update', 'API', 'Update metric definitions', null, null, null, 0, 1),
  (280, 'audit:read', 'API', 'Read audit logs', null, null, null, 0, 1),
  (230, 'assistant:chat', 'API', 'Assistant chat', null, null, null, 0, 1),
  (231, 'assistant:parse', 'API', 'Assistant parse', null, null, null, 0, 1);

-- role_permission: sys_admin gets everything we inserted
insert into t_role_permission(role_id, perm_id)
select 1 as role_id, perm_id from t_permission;

-- business_user minimal
insert into t_role_permission(role_id, perm_id) values
  (3, 110), (3, 120),
  (3, 200), (3, 210), (3, 211), (3, 212), (3, 230), (3, 231);

-- business_leader = business_user + menu/anomaly/stats placeholders
insert into t_role_permission(role_id, perm_id)
select 2 as role_id, perm_id from t_role_permission where role_id = 3;
insert into t_role_permission(role_id, perm_id) values (2, 130), (2, 140);

-- admin user is bootstrapped by backend at startup

-- demo dicts
insert into t_district(id, name, code) values
  (310115, 'Pudong New Area', '310115');

insert into t_street(id, district_id, name, code) values
  (310115001, 310115, 'Lujiazui', '001'),
  (310115002, 310115, 'Zhangjiang', '002');

-- dicts
insert into t_dict(dict_type, dict_code, dict_name, sort, status) values
  ('disability_category', 'LIMB', 'Limb', 10, 1),
  ('disability_category', 'VISION', 'Vision', 20, 1),
  ('disability_level', '1', 'Level 1', 10, 1),
  ('disability_level', '2', 'Level 2', 20, 1),
  ('disability_level', '3', 'Level 3', 30, 1),
  ('disability_level', '4', 'Level 4', 40, 1),
  ('card_status', 'NORMAL', 'Normal', 10, 1),
  ('card_status', 'CANCELLED', 'Cancelled', 20, 1),
  ('card_status', 'EXPIRED', 'Expired', 30, 1),
  ('anomaly_type', 'PERSON_CAR_SEPARATION', 'Person-Car Separation', 10, 1),
  ('anomaly_type', 'NO_CARD_SUBSIDY', 'No Card Subsidy', 20, 1),
  ('anomaly_type', 'CANCELLED_CARD_SUBSIDY', 'Cancelled Card Still Subsidy', 30, 1),
  ('anomaly_type', 'DUPLICATE_SUBSIDY', 'Duplicate Subsidy', 40, 1);

-- demo persons
insert into t_person(person_id, name, id_no, gender, birthday, phone, district_id, street_id, address, created_at, updated_at) values
  (10001, 'Zhang San', '310115199001011234', 'M', date '1990-01-01', null, 310115, 310115001, null, current_timestamp(), current_timestamp()),
  (10002, 'Li Si', '310115198512123456', 'F', date '1985-12-12', null, 310115, 310115002, null, current_timestamp(), current_timestamp());

insert into t_disability_card(card_id, person_id, card_no, category_code, level_code, issue_date, status, expire_date, cancel_date) values
  (20001, 10001, 'D-0001', 'LIMB', '2', date '2020-01-01', 'NORMAL', null, null),
  (20002, 10002, 'D-0002', 'VISION', '3', date '2019-06-01', 'CANCELLED', null, date '2024-01-01');

insert into t_car_benefit(person_id, has_car, plate_no, annual_inspection_status, car_owner_id_no, last_inspection_date, updated_at) values
  (10001, 1, 'SH-A12345', 'OK', '310115199001011234', date '2025-01-01', current_timestamp()),
  (10002, 1, 'SH-B99999', 'DUE', '310115199001011234', date '2023-01-01', current_timestamp());

insert into t_medical_benefit(person_id, enabled, status, last_pay_date, updated_at) values
  (10001, 1, 'NORMAL', date '2025-12-01', current_timestamp()),
  (10002, 0, 'STOPPED', date '2023-06-01', current_timestamp());

insert into t_pension_benefit(person_id, enabled, status, last_pay_date, updated_at) values
  (10001, 1, 'NORMAL', date '2025-12-01', current_timestamp());

insert into t_rule(rule_id, rule_code, name, category, enabled, severity, params_json, description, updated_at) values
  (30001, 'CAR_SEPARATION', 'Person-Car Separation', 'ANOMALY', 1, 'HIGH', null, 'car_owner_id_no != person.id_no', current_timestamp()),
  (30002, 'CANCELLED_CARD_SUBSIDY', 'Cancelled Card Still Subsidy', 'ANOMALY', 1, 'HIGH', null, 'card_status=CANCELLED but has subsidy', current_timestamp());

insert into t_anomaly(anomaly_id, rule_id, person_id, anomaly_type, hit_time, status, handler_user_id, handle_note, handled_at) values
  (40001, 30001, 10002, 'PERSON_CAR_SEPARATION', current_timestamp(), 'UNHANDLED', null, null, null);

insert into t_anomaly_snapshot(anomaly_id, snapshot_json, created_at) values
  (40001, '{"evidence":"car_owner_id_no != person.id_no","carOwnerIdNo":"310115199001011234"}', current_timestamp());
