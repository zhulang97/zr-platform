# 高德地图安全密钥配置说明

## ⚠️ 重要提示

高德地图 JS API 2.0 版本除了需要 **Key** 之外，还需要 **安全密钥 (SecurityConfig)** 才能正常使用。

## 🔧 配置步骤

### 1. 申请高德地图Key和安全密钥

1. 访问 [高德开放平台](https://lbs.amap.com/)
2. 注册开发者账号
3. 创建新应用
4. 添加 Key（选择「Web端(JS API)」）
5. 在 Key 详情页面获取 **安全密钥**

### 2. 后端配置

编辑 `backend/src/main/resources/application.yml`：

```yaml
# 高德地图配置
amap:
  key: ${AMAP_KEY:你的Key}
  securityConfig: ${AMAP_SECURITY_CONFIG:你的安全密钥}
```

**或通过环境变量配置（推荐）：**

```bash
export AMAP_KEY=6640d92bf13e62c5ef4421321f7a7cb1
export AMAP_SECURITY_CONFIG=你的安全密钥
```

### 3. 前端自动获取

前端页面（Dashboard.vue）启动时会自动调用 `/api/config/amap` 接口从后端获取配置，**无需在前端硬编码 Key**。

## 📝 配置检查清单

- [ ] 在高德开放平台创建应用
- [ ] 获取 Web端(JS API) Key
- [ ] 获取安全密钥（SecurityConfig）
- [ ] 在后端 application.yml 或环境变量中配置
- [ ] 重启后端服务
- [ ] 访问首页，检查地图是否正常加载

## 🔍 故障排查

### 地图不显示

**现象：** 页面空白，控制台报错 "INVALID_USER_KEY" 或 "USERKEY_PLAT_NOMATCH"

**解决：**
1. 检查 Key 是否正确
2. 确认 Key 类型是「Web端(JS API)」
3. 检查是否配置了安全密钥

### 地图显示但标注点不显示

**现象：** 地图可以显示，但没有任何标记点

**解决：**
1. 检查浏览器控制台是否有 JavaScript 错误
2. 确认数据已加载（查看 Network 面板 /api/dashboard/persons/locations）

### 安全密钥错误

**现象：** 控制台报错 "SecurityConfig is required"

**解决：**
1. 确认 application.yml 中配置了 securityConfig
2. 检查安全密钥是否与 Key 匹配

## 🚀 快速验证

启动应用后，访问 http://localhost:5173 登录，应该能看到：

1. 大屏首页正常加载
2. 统计数据卡片显示
3. **地图区域显示长宁区地图**
4. 地图上有彩色的残疾人分布点位
5. 点击点位可以查看详细信息

## 📁 相关文件

- 后端配置：`backend/src/main/resources/application.yml`
- 后端API：`backend/src/main/java/com/zhilian/zr/config/AppConfigController.java`
- 前端页面：`frontend/src/views/Dashboard.vue`
- 地理编码服务：`backend/src/main/java/com/zhilian/zr/geo/service/GeoCodingService.java`

## 💡 安全提示

1. **不要将 Key 和安全密钥提交到代码仓库**
2. **使用环境变量配置敏感信息**
3. **定期更换安全密钥**
4. **在高德开放平台设置IP白名单**（生产环境）

---

**配置完成后，重启后端服务即可正常使用地图功能！**
