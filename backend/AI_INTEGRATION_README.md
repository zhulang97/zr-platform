# AI 集成模块

## 概述

本模块实现了基于 Spring AI Alibaba (DashScope/Tongyi) 和 Milvus 向量数据库的 AI 智能助手功能。

## 功能特性

### 1. 文本向量化
- **DashScopeEmbeddingService**: 使用阿里云 DashScope API 将文本转换为向量
- 支持单文本和批量文本向量化
- 默认模型: text-embedding-v2
- 向量维度: 1536

### 2. 向量搜索
- **VectorSearchService**: 实现语义搜索功能
- 支持人员档案向量搜索
- 支持异常案例向量搜索
- 使用余弦相似度计算

### 3. AI 对话
- **AiChatService**: 实现智能对话功能
- 基于检索到的相关上下文生成回答
- 支持人员特定上下文的对话
- 支持通用知识问答

### 4. 异常案例管理
- **AnomalyCaseEntity**: 异常案例实体
- **AnomalyCaseService**: 异常案例 CRUD 服务
- 支持案例创建、查看、解决、删除
- 自动将新案例向量化

### 5. 向量索引
- **VectorIndexingService**: 定时向量索引服务
- 定时扫描未索引的人员档案
- 定时重建完整向量索引
- 支持单个人手动触发索引

## API 接口

### AI 对话
```
POST /api/ai/chat
{
  "query": "查询问题",
  "personId": 123  // 可选，针对特定人员的对话
}
```

### 语义搜索
```
POST /api/ai/search
{
  "query": "搜索关键词",
  "limit": 10  // 可选，返回结果数量
}
```

### 人员向量索引
```
POST /api/ai/index/person
{
  "personId": 123,
  "text": "人员描述文本"
}
```

### 异常案例管理
```
# 创建案例
POST /api/ai/cases
{
  "personId": 123,
  "title": "案例标题",
  "description": "案例描述",
  "anomalyType": "异常类型",
  "severity": 1
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

## 配置

### application.yml
```yaml
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
      model: qwen-turbo
      embedding-model: text-embedding-v2
      dimensions: 1536

milvus:
  host: localhost
  port: 19530
  token: ${MILVUS_TOKEN}
  database: zr_platform

ai:
  schedule:
    enabled: true
```

## 数据库表

### t_person_vector
存储人员档案的向量化数据：
- person_id: 人员ID（主键）
- vector: 向量数据（CLOB）
- updated_at: 更新时间

### t_anomaly_case
存储异常案例：
- case_id: 案例ID（主键）
- person_id: 关联人员ID
- title: 标题
- description: 描述
- anomaly_type: 异常类型
- severity: 严重程度
- resolution: 解决方案
- handler_user_id: 处理人ID
- resolved_at: 解决时间
- created_at: 创建时间

### t_anomaly_case_vector
存储异常案例的向量化数据：
- case_id: 案例ID（主键）
- vector: 向量数据（CLOB）
- updated_at: 更新时间

## 使用流程

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

## 前端页面

### AIChat.vue
- 对话界面：支持用户和 AI 消息展示
- 相似信息：展示语义搜索结果
- 异常案例库：管理异常案例
- 案例管理：创建、查看、解决案例

## 权限要求

- `ai:chat` - AI 对话权限
- `ai:search` - 语义搜索权限
- `ai:index` - 向量索引权限
- `ai:case:create` - 创建案例权限
- `ai:case:view` - 查看案例权限
- `ai:case:resolve` - 解决案例权限
- `ai:case:delete` - 删除案例权限

## 注意事项

1. **DashScope API 配额限制**，需要注意调用频率
2. **Milvus 连接**需要确保 Milvus 服务正常运行
3. **向量索引**是定时执行的，也可以手动触发
4. **语义搜索**结果按相似度排序，相似度范围 0-1
5. **案例库**用于积累和复用异常处理经验

## 扩展建议

1. 添加更多向量索引类型（规则、政策文档等）
2. 实现 Milvus 集群的负载均衡
3. 添加向量搜索结果的过滤和排序选项
4. 实现案例的分类和标签系统
5. 添加 AI 对话历史记录功能
