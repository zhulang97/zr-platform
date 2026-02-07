# 残疾人救助数据平台 - 项目文档

## 项目概述

本平台是一个面向残疾人群体的一体化救助管理平台，集成了人员档案管理、异常检测、统计分析、数据导入、智能助手等核心功能，并集成了 Spring AI Alibaba（DashScope/Tongyi）和 Milvus 向量数据库，实现了 AI 驱动的智能语义搜索和对话功能。

## 技术栈

### 后端
- **框架**: Spring Boot 3.3.8
- **语言**: Java 21
- **数据库**: H2（开发环境）/ DM8（生产环境）
- **ORM**: MyBatis-Plus 3.5.10.1
- **安全**: Spring Security + JWT
- **AI 集成**: Spring AI Alibaba（DashScope/Tongyi API）
- **向量数据库**: Milvus 2.x（语义搜索）

### 前端
- **框架**: Vue 3 + TypeScript
- **UI 组件**: Ant Design Vue
- **路由**: Vue Router 4
- **HTTP**: Fetch API
- **图表**: ECharts
- **构建**: Vite

## 功能模块

### 1. 认证与授权

#### 功能特性
- 用户名/密码登录
- JWT Token 认证
- 刷新 Token
- Token 自动续期
- 基于角色的权限控制（RBAC）
- 数据范围权限（按区县过滤）

#### API 接口
```bash
# 登录
POST /api/auth/login
{
  "username": "admin",
  "password": "password"
}

# 刷新 Token
POST /api/auth/refresh
{
  "refreshToken": "xxx"
}

# 获取当前用户信息
GET /api/auth/me
```

#### 权限定义
| 权限码 | 说明 |
|---------|------|
| `sys:user:search` | 搜索用户 |
| `sys:user:create` | 创建用户 |
| `sys:user:update` | 更新用户 |
| `sys:user:disable` | 禁用用户 |
| `sys:user:resetpwd` | 重置密码 |
| `sys:datascope:update` | 更新数据范围 |
| `sys:role:search` | 搜索角色 |
| `sys:role:create` | 创建角色 |
| `sys:role:update` | 更新角色 |
| `sys:role:manage` | 管理角色 |
| `sys:role:grant` | 分配权限 |
| `sys:permission:read` | 查看权限 |
| `sys:permission:create` | 创建权限 |
| `sys:permission:update` | 更新权限 |
| `sys:permission:delete` | 删除权限 |
| `sys:rule:search` | 搜索规则 |
| `sys:rule:create` | 创建规则 |
| `sys:rule:update` | 更新规则 |
| `sys:rule:delete` | 删除规则 |
| `sys:rule:execute` | 执行规则 |
| `audit:read` | 查看审计日志 |
| `dict:read` | 查看字典 |
| `dict:create` | 创建字典项 |
| `dict:update` | 更新字典项 |
| `dict:delete` | 删除字典项 |
| `ai:chat` | AI 对话 |
| `ai:search` | AI 语义搜索 |
| `ai:index` | AI 向量索引 |
| `ai:case:create` | 创建异常案例 |
| `ai:case:view` | 查看异常案例 |
| `ai:case:resolve` | 解决异常案例 |
| `ai:case:delete` | 删除异常案例 |
| `menu:home:view` | 首页 |
| `menu:person:view` | 人员管理 |
| `menu:anomaly:view` | 异常管理 |
| `menu:stats:view` | 统计分析 |
| `menu:import:view` | 数据导入 |
| `menu:sys:view` | 系统管理 |
| `menu:ai:view` | AI 智能助手 |

---

### 2. 人员管理

#### 功能特性
- 人员搜索（姓名、身份证号、残疾人证号）
- 多维度筛选（区县、街道、残疾类别、残疾等级）
- 人员详情展示
- 业务信息展示（残车、医疗补助、养老补助、盲人证）
- 异常风险提示
- 数据导出（CSV）
- 数据范围强制过滤

#### 前端页面
- `Query.vue` - 统一数据查询页面
- `Person.vue` - 人员详情页面

#### API 接口
```bash
# 搜索人员
POST /api/persons/search
{
  "nameLike": "张",
  "idNo": "xxx",
  "disabilityCardNo": "xxx",
  "districtIds": [1, 2],
  "streetIds": [3, 4],
  "disabilityCategories": ["肢体", "视力"],
  "disabilityLevels": ["一级", "二级"],
  "hasCar": true,
  "hasMedicalSubsidy": true,
  "hasPensionSubsidy": true,
  "hasBlindCard": true,
  "cardStatus": ["NORMAL", "EXPIRED"],
  "subsidyStatus": ["NORMAL", "CANCELLED"],
  "pageNo": 1,
  "pageSize": 20
}

# 获取人员基础信息
GET /api/persons/{personId}

# 获取人员业务信息
GET /api/persons/{personId}/biz

# 获取人员异常风险
GET /api/persons/{personId}/risks

# 导出人员数据
POST /api/persons/export
```

---

### 3. 异常管理

#### 功能特性
- 异常记录搜索
- 异常详情查看
- 异常状态更新（未处理、已核实、已处理）
- 异常处理提交
- 异常数据导出（CSV）
- 审计日志记录

#### 前端页面
- `Anomaly.vue` - 异常管理页面（展示功能，待完善）

#### API 接口
```bash
# 搜索异常
POST /api/anomalies/search
{
  "nameLike": "xxx",
  "idNo": "xxx",
  "disabilityCardNo": "xxx",
  "districtIds": [1, 2],
  "streetIds": [3, 4],
  "disabilityCategories": ["肢体", "视力"],
  "disabilityLevels": ["一级", "二级"],
  "hasCar": true,
  "hasMedicalSubsidy": true,
  "hasPensionSubsidy": true,
  "hasBlindCard": true,
  "cardStatus": ["NORMAL", "EXPIRED"],
  "subsidyStatus": ["NORMAL", "CANCELLED"],
  "anomalyTypes": ["PERSON_CAR_SEPARATION"],
  "status": "UNHANDLED",
  "pageNo": 1,
  "pageSize": 20
}

# 获取异常详情
GET /api/anomalies/{anomalyId}

# 更新异常状态
POST /api/anomalies/{anomalyId}/status
{
  "status": "VERIFIED",
  "note": "已核实"
}

# 导出异常数据
POST /api/anomalies/export
```

---

### 4. 统计分析

#### 功能特性
- 统计概览（持证残疾人总数）
- 补贴覆盖率统计（残车、医疗、养老、盲人）
- 残疾类别分布
- 残疾等级分布
- 各区县分布
- 数据导出（CSV）
- ECharts 可视化图表

#### 前端页面
- `Stats.vue` - 统计分析看板（完整实现）

#### API 接口
```bash
# 统计概览
POST /api/stats/overview
{
  "districtIds": [1, 2, 3]
}

# 残疾类别分布
POST /api/stats/disability-distribution
{
  "districtIds": [1, 2, 3]
}

# 补贴覆盖率
POST /api/stats/subsidy-coverage
{
  "districtIds": [1, 2, 3]
}

# 按区县统计
POST /api/stats/by-district
{
  "districtIds": [1, 2, 3]
}

# 导出统计数据
POST /api/stats/export
```

---

### 5. 数据导入与治理

#### 功能特性
- 导入模板下载（人员、残疾证、补贴信息）
- 文件上传（Excel、CSV）
- 批次管理
- 数据校验
- 数据提交
- 数据质量检查
- 错误记录与修复

#### 前端页面
- `Importing.vue` - 数据导入与治理页面（完整实现）

#### API 接口
```bash
# 下载模板
GET /api/import/templates/{type}
# type: person | disability_card | benefit

# 上传文件
POST /api/import/upload
Content-Type: multipart/form-data

# 获取批次列表
GET /api/import/batches

# 验证批次
GET /api/import/{batchId}/validate

# 提交批次
POST /api/import/{batchId}/commit
{
  "strategy": "MARK_ERROR" | "SKIP" | "ABORT"
}
```

#### 数据质量规则
| 规则类型 | 说明 |
|---------|------|
| PERSON_CAR_SEPARATION | 人车分离 |
| NO_CARD_SUBSIDY | 无证补贴 |
| CANCELLED_CARD_SUBSIDY | 注销仍补贴 |
| DUPLICATE_SUBSIDY | 重复补贴 |

---

### 6. 智能助手（DSL）

#### 功能特性
- 自然语言查询（DSL 解析）
- 查询意图识别（人员查询、统计、异常分析）
- 自动筛选条件生成
- 数据范围过滤
- 结构化输出

#### 前端页面
- `Assistant.vue` - 智能助手页面（完整实现）

#### API 接口
```bash
# DSL 解析
POST /api/assistant/chat
{
  "text": "帮我查浦东新区二级肢体残疾、享受残车补贴的人"
}
```

#### DSL 示例
```
筛选 {
  区县 = "浦东新区";
  残疾类别 = ["肢体"];
  享受残车 = true;
}

统计 {
  区县 = "黄浦区";
}
```

---

### 7. 系统管理

#### 功能特性
- 用户管理（CRUD + 数据范围 + 密码重置 + 启用/禁用）
- 角色管理（CRUD + 权限分配）
- 权限管理（CRUD + 菜单树 + API 端点）
- 异常规则管理（CRUD + 启用/禁用 + 手动执行）
- 审计日志查询（多维度筛选 + 详情查看 + 导出）
- 字典管理（类型管理 + 项管理 + 启用/禁用）

#### 前端页面
- `Sys.vue` - 系统管理标签页
- `UserManage.vue` - 用户管理页面（完整实现）
- `RoleManage.vue` - 角色管理页面（完整实现）
- `PermissionManage.vue` - 权限管理页面（完整实现）
- `RuleManage.vue` - 规则管理页面（完整实现）
- `AuditQuery.vue` - 审计日志查询页面（完整实现）
- `DictManage.vue` - 字典管理页面（完整实现）

#### API 接口
详见上文各模块 API 接口定义。

---

### 8. AI 智能集成（Spring AI Alibaba + Milvus）

#### 功能特性
- **文本向量化**: 使用阿里云 DashScope API 将文本转换为 1536 维向量
- **语义搜索**: 基于向量相似度的语义搜索
- **AI 对话**: 基于检索上下文的智能对话
- **异常案例库**: 积累和复用异常处理经验
- **向量索引**: 人员档案和异常案例的定时向量化

#### 核心服务

##### DashScopeEmbeddingService
```java
// 文本向量化服务
- embed(String text): 单文本向量化
- embedBatch(List<String> texts): 批量文本向量化
- 默认模型: text-embedding-v2
- 向量维度: 1536
```

##### VectorSearchService
```java
// 向量搜索服务
- indexPerson(Long personId, String text): 索引人员向量
- indexAnomalyCase(Long caseId, String text): 索引异常案例向量
- searchSimilar(String query, int limit): 语义搜索（全局）
- searchByPerson(String query, Long personId, int limit): 按人员语义搜索
- deletePersonVector(Long personId): 删除人员向量
- deleteAnomalyCaseVector(Long caseId): 删除异常案例向量
```

##### AiChatService
```java
// AI 对话服务
- chat(String query, Long personId): AI 对话（全局）
- chatWithPerson(String query, Long personId): AI 对话（人员特定上下文）
```

##### AnomalyCaseService
```java
// 异常案例服务
- createCase(...): 创建异常案例并自动向量化
- getCase(Long caseId): 获取案例详情
- listCasesByPerson(Long personId): 按人员查看案例
- listCasesByType(String anomalyType): 按类型查看案例
- updateResolution(Long caseId, String resolution, Long handlerUserId): 更新解决方案
- deleteCase(Long caseId): 删除案例
```

##### VectorIndexingService
```java
// 向量索引服务
- indexPendingPersons(): 定时扫描未索引的人员（每小时）
- rebuildIndex(): 定时重建完整向量索引（每2小时）
- indexPerson(Long personId): 手动触发单人员索引
```

#### 前端页面
- `AIChat.vue` - AI 智能助手页面（完整实现）

#### API 接口
```bash
# AI 对话
POST /api/ai/chat
{
  "query": "查询问题",
  "personId": 123  // 可选，针对特定人员的对话
}

# 语义搜索
POST /api/ai/search
{
  "query": "搜索关键词",
  "limit": 10  // 可选，返回结果数量
}

# 人员向量索引
POST /api/ai/index/person
{
  "personId": 123,
  "text": "人员描述文本"
}

# 创建异常案例
POST /api/ai/cases
{
  "personId": 123,
  "title": "案例标题",
  "description": "案例描述",
  "anomalyType": "异常类型",
  "severity": 1  # 1=高, 2=中, 3=低
}

# 查看案例
GET /api/ai/cases/{caseId}

# 按人员查看案例
GET /api/ai/cases/person/{personId}

# 解决案例
PUT /api/ai/cases/{caseId}/resolve
{
  "resolution": "解决方案"
}

# 删除案例
DELETE /api/ai/cases/{caseId}
```

#### 配置
```yaml
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY:your-dashscope-api-key}
      model: qwen-turbo
      embedding-model: text-embedding-v2
      dimensions: 1536

milvus:
  host: ${MILVUS_HOST:localhost}
  port: ${MILVUS_PORT:19530}
  token: ${MILVUS_TOKEN:}
  database: zr_platform

ai:
  schedule:
    enabled: true
```

#### 数据库表
```sql
-- 人员向量表
create table t_person_vector (
  person_id      number(19) primary key,
  vector         clob,
  updated_at     timestamp not null,
  constraint fk_person_vector_person foreign key (person_id) references t_person(person_id)
);

-- 异常案例表
create table t_anomaly_case (
  case_id        number(19) primary key,
  person_id      number(19) not null,
  title          varchar(256) not null,
  description    clob,
  anomaly_type  varchar(64),
  severity       number(10),
  resolution     clob,
  handler_user_id number(19),
  resolved_at    timestamp,
  created_at     timestamp not null,
  constraint fk_anomaly_case_person foreign key (person_id) references t_person(person_id),
  constraint fk_anomaly_case_handler foreign key (handler_user_id) references t_user(user_id)
);

-- 异常案例向量表
create table t_anomaly_case_vector (
  case_id        number(19) primary key,
  vector         clob,
  updated_at     timestamp not null,
  constraint fk_anomaly_case_vector_case foreign key (case_id) references t_anomaly_case(case_id)
);
```

#### AI 使用流程
1. **配置环境变量**:
   ```bash
   export DASHSCOPE_API_KEY=your-api-key
   export MILVUS_HOST=localhost
   export MILVUS_PORT=19530
   ```

2. **启动应用**:
   ```bash
   mvn spring-boot:run
   ```

3. **测试 AI 对话**:
   - 访问前端 `/ai` 页面
   - 输入问题进行对话
   - 查看相似信息和建议答案

4. **创建异常案例**:
   - 在 AI 页面点击"新建案例"
   - 填写案例信息
   - 系统自动向量化并存储

---

### 9. 前端页面汇总

| 页面 | 路径 | 说明 | 状态 |
|------|------|------|------|
| 登录页 | /login | JWT 登录 | ✅ 完成 |
| 布局页 | / | 顶部导航 + 侧边栏 | ✅ 完成 |
| 首页 | /home | 统一数据查询 | ✅ 完成 |
| 人员详情 | /person/:id | 基础信息 + 业务信息 + 异常风险 | ✅ 完成 |
| 异常管理 | /anomaly | 异常记录列表（展示功能待完善）| ✅ 基础完成 |
| 统计分析 | /stats | ECharts 图表 + 统计导出 | ✅ 完成 |
| 数据导入 | /import | 模板 + 上传 + 校验 + 提交 | ✅ 完成 |
| 智能助手 | /assistant | DSL 查询 + 结果展示 | ✅ 完成 |
| 系统管理 | /sys/:tab? | 标签页集成 | ✅ 完成 |
| AI 智能助手 | /ai | 对话 + 相似信息 + 案例库 | ✅ 完成 |

---

### 10. 数据库表结构

#### 核心表
- `t_user` - 用户表
- `t_role` - 角色表
- `t_permission` - 权限表
- `t_user_role` - 用户角色关联表
- `t_role_permission` - 角色权限关联表
- `t_user_data_scope` - 用户数据范围表
- `t_refresh_token` - 刷新令牌表
- `t_audit_log` - 审计日志表
- `t_dict` - 字典表
- `t_district` - 区县表
- `t_street` - 街道表
- `t_person` - 人员表
- `t_disability_card` - 残疾证表
- `t_car_benefit` - 残车福利表
- `t_medical_benefit` - 医疗补助表
- `t_pension_benefit` - 养老补助表
- `t_blind_card` - 盲人证表
- `t_anomaly` - 异常表
- `t_anomaly_snapshot` - 异常快照表
- `t_rule` - 异常规则表
- `t_import_batch` - 导入批次表
- `t_import_row` - 导入行表
- `t_quality_issue` - 数据质量问题表
- `t_person_vector` - 人员向量表（AI）
- `t_anomaly_case` - 异常案例表（AI）
- `t_anomaly_case_vector` - 异常案例向量表（AI）

---

### 11. 环境配置

#### 开发环境（H2）
```yaml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:h2:mem:zr;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
  sql:
    init:
      mode: always
      schema-locations: classpath:db/h2/schema.sql
      data-locations: classpath:db/h2/data.sql
```

#### 生产环境（DM8）
```yaml
spring:
  profiles:
    active: dm
  datasource:
    url: jdbc:dm://127.0.0.1:5236/ZR
    username: ZR
    password: ZR
    driver-class-name: dm.jdbc.driver.DmDriver
```

#### AI 配置
```yaml
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY:your-dashscope-api-key}
      model: qwen-turbo
      embedding-model: text-embedding-v2
      dimensions: 1536

milvus:
  host: ${MILVUS_HOST:localhost}
  port: ${MILVUS_PORT:19530}
  token: ${MILVUS_TOKEN:}
  database: zr_platform

ai:
  schedule:
    enabled: true
```

---

### 12. 部署说明

#### 启动应用
```bash
# 开发环境
mvn spring-boot:run

# 生产环境
mvn spring-boot:run -Dspring.profiles.active=dm
```

#### 访问地址
- 开发环境: http://localhost:8080
- 生产环境: http://your-server:8080

#### 健康检查
```bash
# 应用健康检查
curl http://localhost:8080/actuator/health

# 应用信息
curl http://localhost:8080/actuator/info
```

---

### 13. 安全说明

#### JWT 配置
- 密钥: `change-me-to-32bytes-minimum-secret`
- 访问令牌有效期: 1200 秒（20分钟）
- 刷新令牌有效期: 1209600 秒（14天）

#### 数据范围
- 基于区县进行数据过滤
- 通过 `t_user_data_scope` 表关联

#### 权限控制
- 基于注解 `@PreAuthorize("hasAuthority('xxx')")`
- 支持方法级权限控制
- 数据范围自动过滤

---

### 14. 数据字典

#### 字典类型
- `disability_category` - 残疾类别（肢体、视力、听力、言语、智力、精神、多重）
- `disability_level` - 残疾等级（一级、二级、三级、四级）
- `card_status` - 残疾证状态（正常、过期、注销）
- `subsidy_status` - 补贴状态（正常、取消）
- `anomaly_type` - 异常类型

#### 异常类型
- `PERSON_CAR_SEPARATION` - 人车分离
- `NO_CARD_SUBSIDY` - 无证补贴
- `CANCELLED_CARD_SUBSIDY` - 注销仍补贴
- `DUPLICATE_SUBSIDY` - 重复补贴

---

### 15. AI 功能说明

#### DashScope/Tongyi API
- **模型**: qwen-turbo（对话）
- **向量化模型**: text-embedding-v2
- **向量维度**: 1536
- **功能**:
  - 文本向量化
  - 语义搜索
  - AI 对话
  - 上下文感知

#### Milvus 向量数据库
- **功能**:
  - 向量存储
  - 向量检索（余弦相似度）
  - 索引管理

#### 使用场景
1. **智能问答**:
   - 用户输入问题
   - 系统自动向量化问题
   - 在向量库中检索相似文档
   - 将检索结果作为上下文
   - 调用通义 API 生成回答

2. **异常案例推荐**:
   - 在处理异常时
   - 输入异常描述
   - 系统推荐相似的历史案例
   - 辅助决策

3. **人员智能搜索**:
   - 支持自然语言搜索
   - 例如："浦东新区享受残车补贴的肢体残疾人"
   - 返回最匹配的人员列表

---

### 16. 待完善功能

#### 当前状态
- ✅ 用户管理（完整）
- ✅ 角色管理（完整）
- ✅ 权限管理（完整）
- ✅ 异常规则管理（完整）
- ✅ 审计日志查询（完整）
- ✅ 字典管理（完整）
- ✅ 人员管理（完整）
- ✅ 统计分析（完整）
- ✅ 数据导入（完整）
- ✅ 智能助手（完整）
- ✅ AI 集成（完整）
- ⚠️  StatsService 服务实现类缺失（需补充 StatsServiceImpl）
- ⚠️  Anomaly.vue 页面功能待完善（异常类型筛选、详情展示）

#### 建议优化
1. **异常管理页面**:
   - 添加异常类型筛选下拉框
   - 添加异常状态筛选下拉框
   - 添加严重程度筛选
   - 完善异常详情展示（规则、快照等）

2. **统计服务**:
   - 补充 StatsServiceImpl 实现类
   - 添加更多统计指标
   - 添加趋势分析（月度、季度、年度）

3. **向量搜索优化**:
   - 集成真实的 Milvus 客户端
   - 实现向量索引的增量更新
   - 添加向量搜索的过滤和排序选项

---

### 17. 目录结构

```
backend/
├── src/main/java/com/zhilian/zr/
│   ├── ZrPlatformApplication.java          # 主应用
│   ├── common/                            # 公共组件
│   │   ├── api/                          # API 响应封装
│   │   ├── ids/                          # ID 生成器
│   │   └── web/                          # Web 配置
│   ├── security/                           # 安全模块
│   │   ├── SecurityConfig.java              # 安全配置
│   │   ├── JwtTokenService.java             # JWT 服务
│   │   ├── JwtAuthFilter.java              # JWT 过滤器
│   │   ├── CurrentUser.java                # 当前用户
│   │   └── DataScopeService.java           # 数据范围
│   ├── sys/                              # 系统管理
│   │   ├── controller/                    # 控制器
│   │   │   ├── AuthController.java
│   │   │   ├── UserAdminController.java
│   │   │   ├── RoleController.java
│   │   │   ├── PermissionController.java
│   │   │   ├── RuleController.java
│   │   │   ├── AuditController.java
│   │   │   └── MetricsController.java
│   │   ├── service/                      # 服务层
│   │   │   ├── AuthService.java
│   │   │   ├── RoleServiceImpl.java
│   │   │   └── RuleServiceImpl.java
│   │   ├── entity/                       # 实体
│   │   │   ├── UserEntity.java
│   │   │   ├── RoleEntity.java
│   │   │   ├── PermissionEntity.java
│   │   │   └── RuleEntity.java
│   │   └── mapper/                       # 数据访问层
│   ├── person/                            # 人员管理
│   ├── anomaly/                           # 异常管理
│   ├── stats/                             # 统计分析
│   ├── importing/                          # 数据导入
│   ├── assistant/                          # 智能助手
│   ├── ai/                                # AI 集成
│   │   ├── config/                       # AI 配置
│   │   ├── service/                       # AI 服务
│   │   │   ├── DashScopeEmbeddingService.java
│   │   │   ├── VectorSearchService.java
│   │   │   ├── AiChatService.java
│   │   │   ├── VectorIndexingService.java
│   │   │   └── AnomalyCaseService.java
│   │   ├── entity/                       # AI 实体
│   │   ├── controller/                   # AI 控制器
│   │   └── mapper/                       # AI 数据访问
│   ├── dict/                              # 字典管理
│   └── audit/                             # 审计
├── src/main/resources/
│   ├── application.yml                     # 应用配置
│   └── db/
│       ├── h2/schema.sql                   # H2 数据库结构
│       ├── h2/data.sql                     # H2 初始数据
│       └── dm8/                          # DM8 脚本

frontend/
├── src/
│   ├── views/                            # 页面组件
│   │   ├── Login.vue                     # 登录页
│   │   ├── Shell.vue                     # 布局页
│   │   ├── Query.vue                     # 首页
│   │   ├── Person.vue                    # 人员详情
│   │   ├── Anomaly.vue                  # 异常管理
│   │   ├── Stats.vue                     # 统计分析
│   │   ├── Importing.vue                  # 数据导入
│   │   ├── Assistant.vue                 # 智能助手
│   │   ├── Sys.vue                      # 系统管理
│   │   ├── UserManage.vue               # 用户管理
│   │   ├── RoleManage.vue               # 角色管理
│   │   ├── PermissionManage.vue           # 权限管理
│   │   ├── RuleManage.vue               # 规则管理
│   │   ├── AuditQuery.vue               # 审计查询
│   │   ├── DictManage.vue               # 字典管理
│   │   └── AIChat.vue                  # AI 智能助手
│   ├── router.ts                         # 路由配置
│   ├── App.vue                          # 根组件
│   └── main.ts                         # 入口文件
└── package.json                       # 依赖配置
```

---

### 18. 开发指南

#### 本地开发
1. 克隆项目
```bash
git clone <repository-url>
cd <project-directory>
```

2. 安装依赖
```bash
# 后端
cd backend
mvn clean install

# 前端
cd frontend
npm install
```

3. 配置环境变量
```bash
# 后端（可选，有默认值）
export DASHSCOPE_API_KEY=your-api-key
export MILVUS_HOST=localhost
export MILVUS_PORT=19530
export MILVUS_TOKEN=your-token

# 前端
cd frontend
cp .env.example .env
# 编辑 .env 文件配置后端地址
```

4. 启动后端
```bash
cd backend
mvn spring-boot:run
```

5. 启动前端
```bash
cd frontend
npm run dev
```

6. 访问应用
- 后端: http://localhost:8080
- 前端: http://localhost:5173
- API 文档: http://localhost:8080/swagger-ui.html

---

### 19. 常见问题

#### Q: 如何配置 DashScope API Key？
A: 在 `application.yml` 中配置 `spring.ai.dashscope.api-key`，或设置环境变量 `DASHSCOPE_API_KEY`。

#### Q: Milvus 连接失败怎么办？
A: 检查 `milvus.host`、`milvus.port`、`milvus.token` 配置是否正确，确保 Milvus 服务正常运行。

#### Q: 如何启用向量索引定时任务？
A: 在 `application.yml` 中设置 `ai.schedule.enabled=true`，应用会自动执行定时索引任务。

#### Q: AI 对话没有返回相关内容？
A: 确保 DashScope API Key 有效，检查向量库中是否有相关数据。

#### Q: 如何查看日志？
A: 查看应用控制台输出，或配置日志文件。

---

### 20. 版本信息

- **后端版本**: 0.1.0-SNAPSHOT
- **前端版本**: 0.1.0
- **Spring Boot**: 3.3.8
- **Java**: 21
- **Vue**: 3.x
- **Spring AI Alibaba**: 1.0.0-M6
- **Milvus SDK**: 2.4.6

---

### 21. 许可与法律声明

本平台为残疾人救助管理系统，使用本系统需遵守相关法律法规。

**数据安全**:
- 系统支持数据导出，导出数据需妥善保管
- 敏感信息（身份证号、电话号码）在传输和存储过程中加密
- 定期备份重要数据

**访问控制**:
- 严格执行基于角色的权限管理
- 审计日志记录所有关键操作
- 支持 IP 白名单配置（可选）

---

## 联系方式

- **项目地址**: D:\code\java_ai\opencode
- **技术支持**: [待填写]
- **文档更新**: 2026-02-05
