-- DM8 seed data for RBAC.
-- This script does not create the initial admin password.
-- The backend bootstraps an initial 'admin' user on first run.

-- roles
insert into t_role(role_id, role_code, name, status, created_at) values (1, 'sys_admin', 'System Admin', 1, systimestamp);
insert into t_role(role_id, role_code, name, status, created_at) values (2, 'business_leader', 'Business Leader', 1, systimestamp);
insert into t_role(role_id, role_code, name, status, created_at) values (3, 'business_user', 'Business User', 1, systimestamp);

-- permissions: MENU
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (100, 'menu:root:view', 'MENU', 'Root', null, null, null, 0, 1);

insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (110, 'menu:home:view', 'MENU', 'Home', '/home', null, 100, 10, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (120, 'menu:person:view', 'MENU', 'Person', '/person', null, 100, 20, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (130, 'menu:anomaly:view', 'MENU', 'Anomaly', '/anomaly', null, 100, 30, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (140, 'menu:stats:view', 'MENU', 'Stats', '/stats', null, 100, 40, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (150, 'menu:import:view', 'MENU', 'Import', '/import', null, 100, 50, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (160, 'menu:sys:view', 'MENU', 'System', '/sys', null, 100, 60, 1);

insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (161, 'menu:sys:user:view', 'MENU', 'Users', '/sys/users', null, 160, 10, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (162, 'menu:sys:role:view', 'MENU', 'Roles', '/sys/roles', null, 160, 20, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (163, 'menu:sys:datascope:view', 'MENU', 'Data Scope', '/sys/datascope', null, 160, 30, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (164, 'menu:sys:rule:view', 'MENU', 'Rules', '/sys/rules', null, 160, 40, 1);

-- permissions: API
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (200, 'dict:read', 'API', 'Read dicts', null, null, null, 0, 1);

insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (210, 'person:search', 'API', 'Search persons', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (211, 'person:read', 'API', 'Read person', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (212, 'person:export', 'API', 'Export persons', null, null, null, 0, 1);

insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (220, 'anomaly:search', 'API', 'Search anomalies', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (221, 'anomaly:read', 'API', 'Read anomaly', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (222, 'anomaly:handle', 'API', 'Handle anomaly', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (223, 'anomaly:export', 'API', 'Export anomalies', null, null, null, 0, 1);

insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (224, 'stats:read', 'API', 'Read stats', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (225, 'stats:export', 'API', 'Export stats', null, null, null, 0, 1);

insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (240, 'import:template:read', 'API', 'Read import templates', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (241, 'import:upload', 'API', 'Upload import file', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (242, 'import:validate:read', 'API', 'Read import validation', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (243, 'import:commit', 'API', 'Commit import', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (244, 'quality:check', 'API', 'Run quality check', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (245, 'quality:read', 'API', 'Read quality issues', null, null, null, 0, 1);

insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (260, 'sys:user:search', 'API', 'Search users', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (261, 'sys:user:create', 'API', 'Create user', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (262, 'sys:user:update', 'API', 'Update user', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (263, 'sys:user:disable', 'API', 'Disable user', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (264, 'sys:user:resetpwd', 'API', 'Reset user password', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (265, 'sys:role:manage', 'API', 'Manage roles', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (266, 'sys:permission:read', 'API', 'Read permissions', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (267, 'sys:datascope:update', 'API', 'Update data scope', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (268, 'sys:rule:search', 'API', 'Search rules', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (269, 'sys:rule:update', 'API', 'Update rules', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (270, 'sys:metrics:update', 'API', 'Update metric definitions', null, null, null, 0, 1);

insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (280, 'audit:read', 'API', 'Read audit logs', null, null, null, 0, 1);

insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (230, 'assistant:chat', 'API', 'Assistant chat', null, null, null, 0, 1);
insert into t_permission(perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) values (231, 'assistant:parse', 'API', 'Assistant parse', null, null, null, 0, 1);

-- role_permission
-- sys_admin: all permissions
insert into t_role_permission(role_id, perm_id)
select 1 as role_id, perm_id from t_permission;

-- business_user
insert into t_role_permission(role_id, perm_id) values (3, 110);
insert into t_role_permission(role_id, perm_id) values (3, 120);
insert into t_role_permission(role_id, perm_id) values (3, 200);
insert into t_role_permission(role_id, perm_id) values (3, 210);
insert into t_role_permission(role_id, perm_id) values (3, 211);
insert into t_role_permission(role_id, perm_id) values (3, 212);
insert into t_role_permission(role_id, perm_id) values (3, 230);
insert into t_role_permission(role_id, perm_id) values (3, 231);

-- business_leader = business_user + anomaly/stats
insert into t_role_permission(role_id, perm_id)
select 2 as role_id, perm_id from t_role_permission where role_id = 3;
insert into t_role_permission(role_id, perm_id) values (2, 130);
insert into t_role_permission(role_id, perm_id) values (2, 140);
insert into t_role_permission(role_id, perm_id) values (2, 220);
insert into t_role_permission(role_id, perm_id) values (2, 221);
insert into t_role_permission(role_id, perm_id) values (2, 222);
insert into t_role_permission(role_id, perm_id) values (2, 223);
insert into t_role_permission(role_id, perm_id) values (2, 224);
insert into t_role_permission(role_id, perm_id) values (2, 225);
