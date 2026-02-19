# 智联助残数据平台 - 全局数据融合实施计划

## 一、方案概述

### 1.1 目标
将27张导入表作为主数据表，建立统一的数据融合机制，支撑一人一档、首页地图、智能助手、政策找人等所有功能。

### 1.2 核心设计
1. **新建基础信息表** `T_PERSON_INDEX` - 存储人员基础信息+经纬度
2. **字段分类配置** - 公共字段、业务字段、模块字段分类管理
3. **融合服务** - 相同字段值合并，不同字段值独立展示

---

## 二、任务总览

| 阶段 | 任务 | 预计时间 |
|------|------|---------|
| 阶段一 | 数据库设计 | 2小时 |
| 阶段二 | 后端基础服务开发 | 4小时 |
| 阶段三 | 一人档改造 | 3小时 |
| 阶段四 | 首页地图改造 | 2小时 |
| 阶段五 | 智能助手改造 | 2小时 |
| 阶段六 | 政策找人改造 | 2小时 |
| 阶段七 | 测试框架搭建 | 3小时 |
| 阶段八 | 自动化测试执行 | 4小时 |

---

## 三、详细任务清单

### 阶段一：数据库设计

#### 1.1 新建基础信息表 T_PERSON_INDEX
```sql
CREATE TABLE T_PERSON_INDEX (
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

CREATE INDEX IDX_PERSON_DISTRICT ON T_PERSON_INDEX(DISTRICT);
CREATE INDEX IDX_PERSON_STREET ON T_PERSON_INDEX(STREET);
CREATE INDEX IDX_PERSON_LOCATION ON T_PERSON_INDEX(LONGITUDE, LATITUDE);
```

#### 1.2 新建字段分类配置表 T_FIELD_CLASSIFY
```sql
CREATE TABLE T_FIELD_CLASSIFY (
    FIELD_CODE VARCHAR(50) PRIMARY KEY,
    FIELD_NAME VARCHAR(100),
    FIELD_CATEGORY VARCHAR(20),  -- COMMON/BIZ/MODULE
    MERGE_STRATEGY VARCHAR(20),  -- FIRST/LATEST/ALL
    DISPLAY_ORDER INT
);
```

#### 1.3 初始化字段分类数据
```sql
-- 公共基础字段
INSERT INTO T_FIELD_CLASSIFY VALUES ('ID_CARD', '身份证号', 'COMMON', 'FIRST', 1);
INSERT INTO T_FIELD_CLASSIFY VALUES ('NAME', '姓名', 'COMMON', 'LATEST', 2);
INSERT INTO T_FIELD_CLASSIFY VALUES ('GENDER', '性别', 'COMMON', 'LATEST', 3);
INSERT INTO T_FIELD_CLASSIFY VALUES ('AGE', '年龄', 'COMMON', 'LATEST', 4);
INSERT INTO T_FIELD_CLASSIFY VALUES ('PHONE', '联系电话', 'COMMON', 'LATEST', 5);
INSERT INTO T_FIELD_CLASSIFY VALUES ('DISTRICT', '区县', 'COMMON', 'LATEST', 6);
INSERT INTO T_FIELD_CLASSIFY VALUES ('STREET', '街道', 'COMMON', 'LATEST', 7);
INSERT INTO T_FIELD_CLASSIFY VALUES ('COMMITTEE', '居委', 'COMMON', 'LATEST', 8);
INSERT INTO T_FIELD_CLASSIFY VALUES ('CONTACT_ADDRESS', '联系地址', 'COMMON', 'LATEST', 9);
INSERT INTO T_FIELD_CLASSIFY VALUES ('HOUSEHOLD_ADDRESS', '户籍地址', 'COMMON', 'LATEST', 10);
INSERT INTO T_FIELD_CLASSIFY VALUES ('LONGITUDE', '经度', 'COMMON', 'LATEST', 11);
INSERT INTO T_FIELD_CLASSIFY VALUES ('LATITUDE', '纬度', 'COMMON', 'LATEST', 12);

-- 业务标识字段
INSERT INTO T_FIELD_CLASSIFY VALUES ('DISABILITY_CERT_NO', '残疾证号', 'BIZ', 'ALL', 20);
INSERT INTO T_FIELD_CLASSIFY VALUES ('DISABILITY_CATEGORY', '残疾类别', 'BIZ', 'ALL', 21);
INSERT INTO T_FIELD_CLASSIFY VALUES ('DISABILITY_LEVEL', '残疾等级', 'BIZ', 'ALL', 22);
INSERT INTO T_FIELD_CLASSIFY VALUES ('DISABILITY_STATUS', '残疾状态', 'BIZ', 'ALL', 23);
```

---

### 阶段二：后端基础服务开发

#### 2.1 新建实体类
- `PersonIndexEntity.java` - 基础信息表实体
- `FieldClassifyEntity.java` - 字段分类实体

#### 2.2 新建Mapper
- `PersonIndexMapper.java`
- `FieldClassifyMapper.java`

#### 2.3 新建服务类
- `PersonIndexService.java` - 基础信息索引服务
- `GeocodingService.java` - 高德API地址转经纬度服务
- `PersonDataFusionService.java` - 人员数据融合服务

#### 2.4 改造导入服务
- 修改 `ImportModuleServiceImpl.java`
- 导入完成后同步更新 T_PERSON_INDEX
- 调用高德API转换地址为经纬度

---

### 阶段三：一人档改造

#### 3.1 后端改造
- 改造 `PersonDetailService.java`
- 新增融合数据查询接口

#### 3.2 前端改造
- 改造 `Person.vue`
- 公共基础信息区 - 融合展示
- 业务信息卡片 - 按模块展示
- 模块专属信息 - 折叠展示

---

### 阶段四：首页地图改造

#### 4.1 后端改造
- 改造 `DashboardService.java`
- 从 T_PERSON_INDEX 查询地图数据

#### 4.2 前端改造
- 改造 `Dashboard.vue`
- 数据源切换到新服务

---

### 阶段五：智能助手改造

#### 5.1 后端改造
- 改造 `AssistantService.java`
- 跨27表语义搜索能力

#### 5.2 前端改造
- 改造 `AIChat.vue`
- 搜索结果展示优化

---

### 阶段六：政策找人改造

#### 6.1 后端改造
- 改造 `PolicyMatchService.java`
- 跨表条件匹配逻辑

#### 6.2 前端改造
- 改造 `PolicySearch.vue`
- 匹配结果展示优化

---

### 阶段七：测试框架搭建

#### 7.1 安装Playwright
```bash
cd frontend
npm init playwright@latest -- --yes --quiet
```

#### 7.2 配置测试环境
- 创建 `tests/` 目录
- 创建 `playwright.config.ts` 配置文件

#### 7.3 编写测试用例
- `tests/e2e/import.spec.ts` - 导入流程测试
- `tests/e2e/person.spec.ts` - 一人档测试
- `tests/e2e/map.spec.ts` - 地图测试
- `tests/e2e/api.spec.ts` - API接口测试

---

### 阶段八：自动化测试执行

#### 8.1 启动服务
- 启动后端服务
- 启动前端服务

#### 8.2 执行测试
```bash
cd frontend
npx playwright test
```

#### 8.3 问题修复
- 分析测试失败原因
- 修复代码问题
- 重新执行测试
- 循环直到所有测试通过

---

## 四、验收标准

### 4.1 功能验收
- [ ] 导入数据后自动更新 T_PERSON_INDEX
- [ ] 一人档页面正确展示融合数据
- [ ] 首页地图正确显示人员位置
- [ ] 智能助手可跨表搜索
- [ ] 政策找人多表匹配

### 4.2 测试验收
- [ ] 所有E2E测试用例通过
- [ ] 无阻断性问题
- [ ] 关键业务流程覆盖

---

## 五、技术要点

### 5.1 高德API调用
```java
@Service
public class GeocodingService {
    
    public GeocodingResult geocode(String address) {
        String url = "https://restapi.amap.com/v3/geocode/geo?key=" + amapKey + "&address=" + address;
        // 调用API返回经纬度
    }
}
```

### 5.2 数据融合逻辑
```java
public MergedPersonData mergeByIdCard(String idCard) {
    // 1. 查询所有表中该人员数据
    // 2. 融合公共字段（去重）
    // 3. 融合业务字段（去重）
    // 4. 保留模块专属字段
}
```

---

## 六、文件清单

### 新建文件
1. `backend/src/main/java/com/zhilian/zr/person/entity/PersonIndexEntity.java`
2. `backend/src/main/java/com/zhilian/zr/person/entity/FieldClassifyEntity.java`
3. `backend/src/main/java/com/zhilian/zr/person/mapper/PersonIndexMapper.java`
4. `backend/src/main/java/com/zhilian/zr/person/mapper/FieldClassifyMapper.java`
5. `backend/src/main/java/com/zhilian/zr/person/service/PersonIndexService.java`
6. `backend/src/main/java/com/zhilian/zr/person/service/GeocodingService.java`
7. `backend/src/main/java/com/zhilian/zr/person/service/PersonDataFusionService.java`
8. `backend/src/main/resources/db/h2/schema_person_index.sql`
9. `frontend/tests/e2e/import.spec.ts`
10. `frontend/tests/e2e/person.spec.ts`
11. `frontend/tests/e2e/map.spec.ts`
12. `frontend/tests/e2e/api.spec.ts`
13. `frontend/playwright.config.ts`

### 修改文件
1. `backend/src/main/java/com/zhilian/zr/importing/service/impl/ImportModuleServiceImpl.java`
2. `backend/src/main/java/com/zhilian/zr/person/service/PersonDetailService.java`
3. `frontend/src/views/Person.vue`
4. `frontend/src/views/Dashboard.vue`
5. `frontend/src/views/AIChat.vue`
6. `frontend/src/views/PolicySearch.vue`

---

## 七、任务执行顺序

```
1. 创建数据库表 (schema_person_index.sql)
2. 创建后端实体类
3. 创建后端Mapper
4. 创建后端Service
5. 改造导入服务
6. 改造一人档
7. 改造首页地图
8. 改造智能助手
9. 改造政策找人
10. 安装Playwright
11. 编写测试用例
12. 启动服务
13. 执行测试
14. 修复问题
15. 重新测试
16. 完成
```

---

*创建时间: 2026-02-17*
*版本: v1.0*
