# S 级三件套完善计划：登录鉴权 + 全局异常 + 配置外部化

## Context

当前 `ecomsys` 项目（Spring Boot 4 + MyBatis 后端 `ecmsb` + Vue 3 + Vite 前端 `ecmsf`）功能广度足够（7 业务模块 + Dashboard），但作为简历作品集有 3 个硬伤：

1. **登录是假登录**：[Login.vue:6-8](file:///d:/Downloads/install/nj2/ecomsys/ecmsf/src/views/login/Login.vue#L6-L8) 把账号密码硬编码在前端，用 `setTimeout` 模拟；后端无任何鉴权，所有 `/api/**` 裸奔。
2. **无全局异常处理**：无 `@RestControllerAdvice`，DB/SQL/参数异常会直接把 Spring 默认错误栈吐给前端，`Result.error()` 定义了却没用上。
3. **DB 密码硬编码进 git**：[application.yml:10](file:///d:/Downloads/install/nj2/ecomsys/ecmsb/src/main/resources/application.yml#L10) `password: Lmi503606707` 已提交。

本计划一次性清掉这 3 个硬伤，让项目达到"简历可投递"门槛。**关键技术选型：JWT + 自定义 `HandlerInterceptor`**（不引入 Spring Security，避免过度工程；JWT 是面试高频考点，比 Session 更能展示技术深度）。

## 关键事实（来自探查）

- [sql.txt:25-40](file:///d:/Downloads/install/nj2/ecomsys/ecmsf/sql.txt#L25-L40) `sys_user` 表：`id / username / password(BCrypt) / status / last_login_time / last_login_ip`
- [sql.txt:412-415](file:///d:/Downloads/install/nj2/ecomsys/ecmsf/sql.txt#L412-L415) 已初始化 `admin` 账号，明文 `admin123` → 数据库存的是 BCrypt 密文
- [sql.txt:374-406](file:///d:/Downloads/install/nj2/ecomsys/ecmsf/sql.txt#L374-L406) `login_log` 表已存在，记录 `username / login_type / ip / status / message`，可复用做登录日志
- 后端 Service 风格：直接 `@Service` class（非接口+impl），无 Lombok（手写 getter/setter，见 [Result.java](file:///d:/Downloads/install/nj2/ecomsys/ecmsb/src/main/java/org/example/ecms/common/Result.java)），Mapper 用 XML
- 前端 axios 响应拦截器已解包 `Result{code,message,data}`，[api/index.js:9-24](file:///d:/Downloads/install/nj2/ecomsys/ecmsf/src/api/index.js#L9-L24)

## 实施方案

### 一、后端：登录鉴权链路（JWT + 拦截器）

#### 1.1 加依赖（[pom.xml](file:///d:/Downloads/install/nj2/ecomsys/ecmsb/pom.xml)）
新增 3 个轻量依赖，**不引入 spring-boot-starter-security**：
- `io.jsonwebtoken:jjwt-api:0.12.6` + `jjwt-impl` + `jjwt-jackson`（runtime）— JWT 签发/解析
- `at.favre.lib:bcrypt:0.10.2` — BCrypt 校验（比 spring-security-crypto 更轻量，零额外概念）

#### 1.2 配置 JWT 密钥（[application.yml](file:///d:/Downloads/install/nj2/ecomsys/ecmsb/src/main/resources/application.yml)）
新增 `ecms.jwt` 自定义配置段，密钥与过期时间外部化：
```yaml
ecms:
  jwt:
    secret: ${JWT_SECRET:dGhpcy1pcy1hLWRlbW8tc2VjcmV0LWtleS1mb3ItZWNtcy1wcm9qZWN0LWRvLW5vdC11c2UtaW4tcHJvZHViYXRpb24}
    expire-minutes: ${JWT_EXPIRE:120}
```

#### 1.3 新建实体与 Mapper
- `entity/SysUser.java` — 对应 `sys_user` 表（手写 getter/setter，与现有 `entity` 风格一致）
- `entity/LoginLog.java` — 对应 `login_log` 表
- `mapper/SysUserMapper.java` + `resources/mapper/SysUserMapper.xml` — `selectByUsername(String)`
- `mapper/LoginLogMapper.java` + `resources/mapper/LoginLogMapper.xml` — `insert(LoginLog)`

#### 1.4 新建 `common/JwtUtil.java`
- `generate(userId, username)` → 签发 HS256 token，claims 含 `uid`、`uname`，过期时间从配置读
- `parse(token)` → 返回 `Claims`，签名错误/过期抛 `JwtException`
- `@Component`，`@Value("${ecms.jwt.secret}")` 注入密钥

#### 1.5 新建 `common/BizException.java` + `common/GlobalExceptionHandler.java`
- `BizException(int code, String msg)` — 业务异常基类
- `@RestControllerAdvice` 全局捕获：
  - `BizException` → 用其 code/msg
  - `JwtException`（Expired/Signature） → 401 + "token 失效"
  - `NoHandlerFoundException` → 404
  - `Exception` 兜底 → 500 + "服务异常"（不向前端泄露堆栈）
- 统一返回 `Result.error(code, msg)`

#### 1.6 新建 `auth/AuthController.java` + `auth/AuthService.java`
包路径 `org.example.ecms.auth`：
- `POST /api/auth/login`，body `{username, password}`
  - 查 `sys_user`，用 BCrypt 校验密码
  - 失败 → 记 `login_log(status=0)` + 抛 `BizException(401, "账号或密码错误")`
  - 成功 → 签 JWT、记 `login_log(status=1)`、更新 `sys_user.last_login_time/ip`、返回 `{token, username, expireMinutes}`
- `GET /api/auth/info` — 从当前请求解析 token，返回用户名（前端校验登录态）
- `POST /api/auth/logout` — JWT 无状态，前端清 token 即可；后端仅记 `login_log(login_type=logout)`（可选）

#### 1.7 新建 `common/JwtInterceptor.java` + 注册到 [WebConfig.java](file:///d:/Downloads/install/nj2/ecomsys/ecmsb/src/main/java/org/example/ecms/config/WebConfig.java)
- 实现 `HandlerInterceptor.preHandle`：
  - 从 `Authorization: Bearer xxx` 取 token
  - 缺失/解析失败 → `response.setStatus(401)` + 写 JSON `Result.error(401, "未登录或 token 失效")` + `return false`
  - 成功 → 把 `userId/username` 塞进 `request.setAttribute` 供 Controller 用
- 在 `WebConfig.addInterceptors` 注册 `/**`，**排除**：`/api/auth/login`、`/api/auth/captcha`（预留）、`/error`
- IP 获取工具方法放 `JwtInterceptor.getClientIp(HttpServletRequest)`，处理 `X-Forwarded-For`

### 二、后端：配置外部化

#### 2.1 [application.yml](file:///d:/Downloads/install/nj2/ecomsys/ecmsb/src/main/resources/application.yml) 改造
- DB url/username/password 全部改 `${DB_URL:...}` / `${DB_USER:root}` / `${DB_PASSWORD:}`
- 保留原值作为默认（本地开发方便），但生产用环境变量覆盖
- 新增 `spring.profiles.active: ${SPRING_PROFILES_ACTIVE:dev}`

#### 2.2 新建 `application-prod.yml`
- 内容与 dev 一致但**不带默认值**：`password: ${DB_PASSWORD}`（生产强制注入）
- 用于 `--spring.profiles.active=prod` 时

#### 2.3 新建 [ecmsb/.env.example](file:///d:/Downloads/install/nj2/ecomsys/ecmsb/.env.example)
列出所有外部化变量及说明（DB_*, JWT_SECRET, JWT_EXPIRE, SPRING_PROFILES_ACTIVE），便于招聘者一键配置。

### 三、前端：登录态对接

#### 3.1 [api/index.js](file:///d:/Downloads/install/nj2/ecomsys/ecmsf/src/api/index.js) 改造
- 新增 `authApi`：`login(body) / info() / logout()`
- **请求拦截器**：从 `localStorage.getItem('ecms_token')` 取 token，存在则加 `Authorization: Bearer ${token}`
- **响应拦截器扩展**：401 时清 `localStorage` 的 token、派发 `window.dispatchEvent(new CustomEvent('unauthorized'))` 让 App.vue 跳登录

#### 3.2 [Login.vue](file:///d:/Downloads/install/nj2/ecomsys/ecmsf/src/views/login/Login.vue) 改造
- 删掉 `VALID_USERNAME/PASSWORD` 硬编码与 `setTimeout` mock
- 改成 `authApi.login({username, password})`，成功后 `localStorage.setItem('ecms_token', token)` + `emit('login-success', username)`

#### 3.3 [App.vue](file:///d:/Downloads/install/nj2/ecomsys/ecmsf/src/App.vue) 改造
- 初始化时读 `localStorage.getItem('ecms_token')`，存在则调 `authApi.info()` 校验；有效则 `loggedIn=true`
- 监听 `window.addEventListener('unauthorized', ...)`，收到后清 token + 切回登录页

### 四、关键文件清单

**后端新增**：
- `ecmsb/src/main/java/org/example/ecms/entity/SysUser.java`
- `ecmsb/src/main/java/org/example/ecms/entity/LoginLog.java`
- `ecmsb/src/main/java/org/example/ecms/mapper/SysUserMapper.java` + `resources/mapper/SysUserMapper.xml`
- `ecmsb/src/main/java/org/example/ecms/mapper/LoginLogMapper.java` + `resources/mapper/LoginLogMapper.xml`
- `ecmsb/src/main/java/org/example/ecms/common/JwtUtil.java`
- `ecmsb/src/main/java/org/example/ecms/common/JwtInterceptor.java`
- `ecmsb/src/main/java/org/example/ecms/common/BizException.java`
- `ecmsb/src/main/java/org/example/ecms/common/GlobalExceptionHandler.java`
- `ecmsb/src/main/java/org/example/ecms/auth/AuthController.java`
- `ecmsb/src/main/java/org/example/ecms/auth/AuthService.java`
- `ecmsb/src/main/resources/application-prod.yml`
- `ecmsb/.env.example`

**后端修改**：
- [pom.xml](file:///d:/Downloads/install/nj2/ecomsys/ecmsb/pom.xml) — 加 3 依赖
- [application.yml](file:///d:/Downloads/install/nj2/ecomsys/ecmsb/src/main/resources/application.yml) — DB 配置外部化 + JWT 配置段
- [WebConfig.java](file:///d:/Downloads/install/nj2/ecomsys/ecmsb/src/main/java/org/example/ecms/config/WebConfig.java) — 注册 JwtInterceptor

**前端修改**：
- [api/index.js](file:///d:/Downloads/install/nj2/ecomsys/ecmsf/src/api/index.js) — authApi + 请求拦截器 + 401 处理
- [Login.vue](file:///d:/Downloads/install/nj2/ecomsys/ecmsf/src/views/login/Login.vue) — 真实登录
- [App.vue](file:///d:/Downloads/install/nj2/ecomsys/ecmsf/src/App.vue) — 启动校验 + 401 监听

## 验证方式

1. **后端单元自测**（无测试框架也能跑）：
   - 启动 `mvnw spring-boot:run`
   - 不带 token 直接 `curl http://localhost:8080/api/dashboard/stats` → 期望 401 + `Result.error` JSON
   - `curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}"` → 期望返回 `{code:200, data:{token, username, expireMinutes}}`
   - 用返回的 token `curl -H "Authorization: Bearer xxx" http://localhost:8080/api/dashboard/stats` → 期望 200 + 数据
   - 故意输错密码 → 期望 `BizException(401)` + `login_log` 表新增一条 status=0 记录
   - 故意访问不存在接口 → 期望 `GlobalExceptionHandler` 返回 404 而非 Spring 默认页

2. **前端联调**：
   - `npm run dev` 启动
   - 登录页输入 admin/admin123 → 进入 Home
   - F12 Network 看 `/api/auth/login` 真实请求；后续请求带 `Authorization` 头
   - 刷新页面 → 仍保持登录态（localStorage token + `/api/auth/info` 校验）
   - 手动清 localStorage token 刷新 → 跳回登录页
   - 改 token 任一位再请求 → 期望 401 + 自动跳登录页

3. **配置外部化验证**：
   - 不设环境变量启动 → 用 application.yml 默认值能跑（dev 友好）
   - 设 `DB_PASSWORD=xxx JWT_SECRET=yyy SPRING_PROFILES_ACTIVE=prod` 启动 → 走 application-prod.yml，无默认值则未注入变量会启动失败（强制要求生产配置）

## 不做的事（避免过度工程）

- ❌ 不引入 Spring Security（手写 JWT + Interceptor 已足够展示鉴权链路）
- ❌ 不做 refresh token（简历项目单 token 即可，README 注明简化）
- ❌ 不做 RBAC 权限注解（仅做登录校验，多角色超出范围）
- ❌ 不做前端路由守卫（项目无 vue-router，用 App.vue 监听 401 即可）
- ❌ 不写新单元测试（S 级阶段先打通链路，单测在 A 级阶段补）
