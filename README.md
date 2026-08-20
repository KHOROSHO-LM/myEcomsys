# ECMS 电商后台管理系统

> 一个基于 Spring Boot 4 + Vue 3 的电商后台管理系统，覆盖商品 / 订单 / 客户 / 营销 / 内容 / 日志 / 仪表盘 7 大业务模块，集成 JWT 鉴权与全局异常处理，可作为 Java 全栈方向简历作品集。

## 技术栈

### 后端 `ecmsb`
- **JDK** 17（实际运行使用 23）
- **Spring Boot** 4.1.0
- **MyBatis Spring Boot** 4.0.0（XML Mapper）
- **MySQL Connector/J**
- **jjwt** 0.12.6（JWT 签发与解析）
- **at.favre.lib:bcrypt** 0.10.2（BCrypt 密码校验）
- **Maven** 构建

### 前端 `ecmsf`
- **Vue** 3.5（`<script setup>` SFC）
- **Vite** 8.2（构建与开发服务器）
- **Axios** 1.x（统一封装请求/响应拦截器）

### 基础设施
- **MySQL** 8.x
- **Vite Dev Server Proxy** 解决前后端联调跨域

## 项目结构

```
ecomsys/
├── ecmsb/                                  # 后端 Spring Boot 项目
│   ├── pom.xml                             # Maven 依赖
│   ├── mvnw / mvnw.cmd                     # Maven Wrapper
│   ├── .env.example                        # 环境变量模板（DB/JWT 配置）
│   └── src/
│       ├── main/
│       │   ├── java/org/example/ecms/
│       │   │   ├── EcmsApplication.java    # 启动入口
│       │   │   ├── common/                 # 通用基建
│       │   │   │   ├── Result.java          # 统一响应体 {code, message, data}
│       │   │   │   ├── BizException.java    # 业务异常
│       │   │   │   ├── GlobalExceptionHandler.java  # @RestControllerAdvice 全局异常处理
│       │   │   │   ├── JwtUtil.java         # JWT 签发/解析
│       │   │   │   └── JwtInterceptor.java  # HandlerInterceptor 鉴权拦截器
│       │   │   ├── config/
│       │   │   │   └── WebConfig.java       # CORS + 拦截器注册
│       │   │   ├── auth/                    # 鉴权模块
│       │   │   │   ├── AuthController.java  # /api/auth/login | info | logout
│       │   │   │   └── AuthService.java     # BCrypt 校验 + JWT 签发 + 登录日志
│       │   │   ├── controller/              # 7 个业务 Controller
│       │   │   ├── service/                 # 7 个业务 Service
│       │   │   ├── mapper/                  # MyBatis Mapper 接口
│       │   │   └── entity/                  # 实体与 VO
│       │   └── resources/
│       │       ├── application.yml          # dev 配置（带默认值）
│       │       ├── application-prod.yml     # prod 配置（强制注入）
│       │       └── mapper/*.xml             # MyBatis SQL 映射
│       └── test/                            # 测试
│
├── ecmsf/                                  # 前端 Vue 3 项目
│   ├── package.json
│   ├── vite.config.js                      # /api 代理到 8080
│   ├── sql.txt                             # 建表 + 初始化数据 SQL
│   └── src/
│       ├── main.js                         # 应用挂载
│       ├── App.vue                         # 顶层：登录态切换
│       ├── api/index.js                    # axios 实例 + 鉴权拦截器 + 各模块 API
│       └── views/
│           ├── login/Login.vue             # 登录页
│           ├── home/Home.vue               # 主框架：侧边栏 + 路由切换
│           ├── dashboard/Dashboard.vue     # 仪表盘
│           ├── product/Product.vue         # 商品管理
│           ├── order/Order.vue             # 订单管理
│           ├── customer/Customer.vue       # 客户管理
│           ├── marketing/Marketing.vue     # 营销中心
│           ├── content/Content.vue         # 内容管理
│           └── log/Log.vue                 # 系统日志
│
└── README.md                               # 本文件
```

## 核心功能

### 1. 鉴权链路（JWT + 自定义拦截器）

不引入 Spring Security，手写完整链路以展示底层机制：

```
前端 Login.vue
  └─ authApi.login({username, password})
       └─ POST /api/auth/login
            └─ AuthService.login()
                 ├─ SysUserMapper.selectByUsername()  查 sys_user
                 ├─ BCrypt.verifyer().verify()         校验密文
                 ├─ JwtUtil.generate()                 签 HS512 token
                 ├─ LoginLogMapper.insert()            落 login_log
                 └─ SysUserMapper.updateLastLogin()    更新最后登录
       ← 返回 {token, username, nickname, expireMinutes}

前端 setToken() → localStorage

后续请求
  └─ axios 请求拦截器自动附 Authorization: Bearer xxx
       └─ JwtInterceptor.preHandle()
            ├─ JwtUtil.parse()  解析 claims
            └─ request.setAttribute(uid/uname)
```

- **登录失败**：账号不存在 / 已禁用 / 密码错误均抛 `BizException(401)` 并落一条 `login_log(status=0)`
- **token 失效**：响应拦截器收到 401 → 清 localStorage → 派发 `unauthorized` 事件 → App.vue 切回登录页
- **启动校验**：App.vue `onMounted` 读 localStorage token → 调 `/api/auth/info` 校验有效则进主页

### 2. 全局异常处理

[GlobalExceptionHandler.java](ecmsb/src/main/java/org/example/ecms/common/GlobalExceptionHandler.java) 统一捕获：

| 异常类型 | HTTP 状态 | 响应体 |
|---|---|---|
| `BizException` | 200（业务码在 body.code） | `Result.error(code, msg)` |
| `JwtException`（token 失效） | 401 | `Result.error(401, "token 已失效")` |
| `NoHandlerFoundException` | 404 | `Result.error(404, "接口不存在")` |
| `HttpMessageNotReadableException` | 400 | `Result.error(400, "请求体格式错误")` |
| `Exception` 兜底 | 500 | `Result.error(500, "服务异常")` |

向前端隐藏堆栈，仅返回结构化错误。

### 3. 配置外部化

- `application.yml`：DB 三项与 JWT 密钥全部 `${VAR:default}` 形式，dev 环境保留默认值
- `application-prod.yml`：生产强制注入，未设环境变量会启动失败
- `.env.example`：列出所有可注入变量及说明

### 4. 业务模块

| 模块 | 前端页面 | 后端接口 | 数据表 |
|---|---|---|---|
| 仪表盘 | `dashboard/Dashboard.vue` | `/api/dashboard/stats`、`/recent-orders`、`/category-sales` | product / order / customer / coupon / seckill |
| 商品管理 | `product/Product.vue` | `/api/product/list`、`/categories`、`/{id}/skus`、`/{id}/images` | product / product_category / product_sku / product_image |
| 订单管理 | `order/Order.vue` | `/api/order/list`、`/{id}/items`、`/{id}/logs` | order / order_item / order_log |
| 客户管理 | `customer/Customer.vue` | `/api/customer/list`、`/levels`、`/{id}/addresses` | customer / customer_level / customer_address |
| 营销中心 | `marketing/Marketing.vue` | `/api/marketing/coupons`、`/coupons/{id}/issues`、`/seckills` | coupon / coupon_issue / seckill |
| 内容管理 | `content/Content.vue` | `/api/content/banners`、`/notices` | banner / notice |
| 系统日志 | `log/Log.vue` | `/api/log/operation`、`/login` | operation_log / login_log |

## 启动步骤

### 前置要求

- JDK 17+
- Node.js 18+
- MySQL 8.x

### 1. 准备数据库

```bash
# 登录 MySQL，创建库并导入表结构与初始化数据
mysql -uroot -p
```

```sql
CREATE DATABASE shop_admin DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE shop_admin;
SOURCE d:/Downloads/install/nj2/ecomsys/ecmsf/sql.txt;
```

导入后会自动创建：
- 全部业务表（sys_user / product / order / customer / coupon / seckill 等）
- 初始化数据，其中管理员账号为 `admin / admin123`（密码经 BCrypt 加密存储）

### 2. 启动后端

```bash
cd ecmsb

# 本地开发：直接跑（用 application.yml 内置默认值）
./mvnw.cmd spring-boot:run

# 生产：注入环境变量后跑
# Windows PowerShell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_URL="jdbc:mysql://your-mysql-host:3306/shop_admin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"
$env:DB_USER="root"
$env:DB_PASSWORD="your-password"
$env:JWT_SECRET="用 openssl rand -base64 48 生成的密钥"
./mvnw.cmd spring-boot:run
```

后端启动后监听 `http://localhost:8080`。

### 3. 启动前端

```bash
cd ecmsf
npm install
npm run dev
```

默认监听 `http://localhost:5173`，若被占用会自动切到 5174。

### 4. 访问系统

浏览器打开 http://localhost:5173，使用 `admin / admin123` 登录。

## 验证鉴权链路（可选）

```bash
# 不带 token → 401
curl http://localhost:8080/api/dashboard/stats

# 登录获取 token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 带 token 访问业务接口 → 200
curl http://localhost:8080/api/dashboard/stats \
  -H "Authorization: Bearer <上一步返回的 token>"
```

## 关键设计决策

| 决策 | 选择 | 理由 |
|---|---|---|
| 鉴权方案 | JWT + 自定义 `HandlerInterceptor` | 不引入 Spring Security 避免过度工程；手写链路更能展示底层机制 |
| 密码加密 | BCrypt（强度 10） | 业界标准，sql.txt 初始化数据已使用 |
| 密码校验库 | `at.favre.lib:bcrypt` | 比 spring-security-crypto 更轻量，零额外概念 |
| 前后端通信 | REST + 统一 `Result<T>` 响应体 | 前端 axios 拦截器统一解包 |
| 前端状态 | `App.vue` `loggedIn` ref + `v-if` 切换 | 项目无 vue-router/pinia，避免为简历项目增加心智负担 |
| 配置外部化 | `${VAR:default}` 双环境（dev/prod） | dev 友好，prod 强制注入 |

## 简化项说明（避免误导）

为聚焦核心，本项目**有意未实现**以下功能，简历面试时可主动说明简化范围：

- 单 JWT，未做 Refresh Token
- 仅登录校验，未做 RBAC 权限注解
- 业务接口以查询为主，未实现完整的增删改
- 列表接口未分页（直接全表返回）
- 无前端路由守卫（用 `App.vue` 监听 401 事件替代）

## 目录与文件导航

### 后端核心文件

| 文件 | 职责 |
|---|---|
| [EcmsApplication.java](ecmsb/src/main/java/org/example/ecms/EcmsApplication.java) | Spring Boot 启动入口 |
| [Result.java](ecmsb/src/main/java/org/example/ecms/common/Result.java) | 统一响应体 `{code, message, data}` |
| [JwtUtil.java](ecmsb/src/main/java/org/example/ecms/common/JwtUtil.java) | JWT 签发与解析 |
| [JwtInterceptor.java](ecmsb/src/main/java/org/example/ecms/common/JwtInterceptor.java) | 鉴权拦截器 + IP 工具 |
| [GlobalExceptionHandler.java](ecmsb/src/main/java/org/example/ecms/common/GlobalExceptionHandler.java) | 全局异常处理 |
| [BizException.java](ecmsb/src/main/java/org/example/ecms/common/BizException.java) | 业务异常基类 |
| [AuthController.java](ecmsb/src/main/java/org/example/ecms/auth/AuthController.java) | `/api/auth/login`、`/info`、`/logout` |
| [AuthService.java](ecmsb/src/main/java/org/example/ecms/auth/AuthService.java) | BCrypt 校验 + 签 JWT + 落日志 |
| [WebConfig.java](ecmsb/src/main/java/org/example/ecms/config/WebConfig.java) | CORS + 拦截器注册 |
| [DashboardMapper.xml](ecmsb/src/main/resources/mapper/DashboardMapper.xml) | 仪表盘统计 SQL |
| [application.yml](ecmsb/src/main/resources/application.yml) | dev 配置 |
| [application-prod.yml](ecmsb/src/main/resources/application-prod.yml) | prod 配置 |

### 前端核心文件

| 文件 | 职责 |
|---|---|
| [main.js](ecmsf/src/main.js) | 应用挂载 |
| [App.vue](ecmsf/src/App.vue) | 登录态切换 + 启动校验 + 401 监听 |
| [api/index.js](ecmsf/src/api/index.js) | axios 实例 + 鉴权拦截器 + 全部 API |
| [Login.vue](ecmsf/src/views/login/Login.vue) | 登录页（真实调用 `/api/auth/login`） |
| [Home.vue](ecmsf/src/views/home/Home.vue) | 主框架（侧边栏 + `<component :is>` 路由切换） |
| [Dashboard.vue](ecmsf/src/views/dashboard/Dashboard.vue) | 仪表盘 |
| [vite.config.js](ecmsf/vite.config.js) | Vite 配置 + `/api` 代理到 8080 |

### 数据库

| 文件 | 职责 |
|---|---|
| [sql.txt](ecmsf/sql.txt) | 建表语句 + 初始化数据（含 admin 账号 BCrypt 密文） |

## 后续可完善的功能路线图

当前版本已具备完整鉴权链路与 7 大业务模块的查询能力，作为简历项目可继续朝以下方向迭代。按优先级与价值排序：

### S 级 · 把现有"只读"补成"完整 CRUD"

目前所有 Controller 仅 `@GetMapping`，名为"商品管理"实际只能查看——这是面试官最易察觉的硬伤。

| 功能 | 涉及改动 | 价值 |
|---|---|---|
| **商品增删改** | 新增 `/api/product` 的 `POST/PUT/DELETE` 接口 + 前端表单弹窗 | 证明能写业务而非只读 |
| **订单状态流转** | `/api/order/{id}/status` 接口实现"待发货→待收货→已完成"流转 + 前端按钮 | 展示业务流程理解 |
| **上下架切换** | `/api/product/{id}/status` 一键上下架 | 真实后台高频操作 |
| **客户禁用/启用** | `/api/customer/{id}/status` | 风控场景必备 |

配套要补：参数校验（`spring-boot-starter-validation` + `@Valid`）、批量删除（`DELETE /api/product?ids=1,2,3`）。

### A 级 · 列表分页与搜索过滤

现状所有列表接口全表 `SELECT *` 返回，数据量上来必出问题。

- **后端分页**：统一 `PageResult<T>` 响应体 + MyBatis `PageHelper` 或手写 `LIMIT/OFFSET`
- **接口签名**：`/api/product/list?page=1&size=20&keyword=手机&categoryId=5&status=1`
- **前端**：通用 `<Pagination>` 组件 + 搜索栏（关键词、分类下拉、状态下拉）
- **性能**：给 `order.created_at`、`product.status` 等高频过滤字段加索引

### A 级 · 数据可视化升级

当前仪表盘只有数字卡片 + 简易进度条，不够直观。

- 引入 **ECharts**（或 Vue 项目惯用的 `vue-echarts` 封装）
- 折线图：近 30 天订单量趋势
- 饼图：订单状态分布（待付款/待发货/已完成…）
- 柱状图：分类销量 TOP 10
- 数据地图：客户地域分布（如果 `customer_address` 有省市区字段）

### A 级 · 工程基建补全

| 项 | 说明 |
|---|---|
| **API 文档** | 接入 `springdoc-openapi-starter-webmvc-ui`（Swagger 3），访问 `/swagger-ui.html` 自动生成接口文档 |
| **Service 层单测** | 给 `AuthService`、`DashboardService` 写 JUnit 测试，覆盖登录成功/失败/统计正确性 |
| **Docker 化** | `Dockerfile` + `docker-compose.yml` 一键起 MySQL + 后端 + 前端，招聘者 clone 即跑 |
| **CI/CD** | GitHub Actions：push 自动跑测试 + 构建，给 README 加 build 徽章 |

### B 级 · 鉴权链路深化

当前是单 JWT + 登录拦截，可继续展示鉴权设计能力：

- **Refresh Token**：双 token 机制，access token 短期（15min）+ refresh token 长期（7d），减少用户重新登录
- **RBAC 权限注解**：`@PreAuthorize("hasAuthority('product:delete')")` 自定义注解 + 在 `JwtInterceptor` 里校验 `sys_user_role` 关联
- **接口限流**：登录接口加 `Bucket4j` 或手写令牌桶，防暴力破解
- **登录验证码**：图形验证码 / 滑块，防机器撞库

### B 级 · 前端工程化升级

当前前端无路由、无状态管理、无类型系统——这是 Vue 岗简历的硬伤。

- **vue-router**：替换 `App.vue` 的 `v-if` 切换，路由表 `/dashboard` `/product` 等可分享 URL
- **Pinia**：登录态、用户信息、菜单折叠状态进 store，组件间共享
- **UI 组件库**：`element-plus` 或 `naive-ui`，替换大量手写样式
- **TypeScript**：`.vue` 改 `<script setup lang="ts">`，定义接口类型
- **ESLint + Prettier**：统一代码风格

### B 级 · 真实场景增强

| 功能 | 说明 |
|---|---|
| **文件上传** | 商品图片上传到 OSS / 本地存储，`/api/upload` 接收 MultipartFile |
| **Excel 导入导出** | 商品批量导入 / 订单列表导出，用 `EasyExcel` |
| **操作日志切面** | `@Aspect` + `@OperationLog` 注解自动记录操作人到 `operation_log` 表 |
| **消息通知** | 新订单 WebSocket 实时推送到仪表盘，或站内信 |
| **数据字典** | 订单状态、客户等级等枚举集中管理，前端不再硬编码 `statusMap` |