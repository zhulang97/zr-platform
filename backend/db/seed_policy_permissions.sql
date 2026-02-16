-- 添加政策找人功能相关权限

-- 1. 添加菜单权限
INSERT INTO t_permission (perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) VALUES
(2001, 'menu:policy:view', 'MENU', '政策找人菜单', '/policy', 'GET', NULL, 50, 1);

-- 2. 添加功能权限
INSERT INTO t_permission (perm_id, perm_code, perm_type, name, path, method, parent_id, sort, status) VALUES
(2101, 'policy:create', 'BUTTON', '上传政策', '/api/policies/upload-url', 'POST', 2001, 1, 1),
(2102, 'policy:read', 'BUTTON', '查看政策', '/api/policies', 'GET', 2001, 2, 1),
(2103, 'policy:analyze', 'BUTTON', '分析政策', '/api/policies/{id}/analyze', 'POST', 2001, 3, 1),
(2104, 'policy:update', 'BUTTON', '更新政策', '/api/policies/{id}/title', 'PUT', 2001, 4, 1),
(2105, 'policy:delete', 'BUTTON', '删除政策', '/api/policies/{id}', 'DELETE', 2001, 5, 1);

-- 3. 给管理员角色分配所有政策权限
-- 假设管理员角色 role_id = 1
INSERT INTO t_role_permission (role_id, perm_id) VALUES
(1, 2001),
(1, 2101),
(1, 2102),
(1, 2103),
(1, 2104),
(1, 2105);
