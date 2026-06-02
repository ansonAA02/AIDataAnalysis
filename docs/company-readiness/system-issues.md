# system-issues.md

生成日期：2026-05-26  
文档角色：系统问题主文档  
维护目标：集中维护“当前系统还不能成为公司主流系统”的问题总表，作为整改、排期、评审和验收的唯一问题入口。

## 0. 使用规则

- 新发现的问题统一追加到本文件，不再分散写到多个整改文档中。
- 问题按 `P0 / P1 / P2` 维护，优先级变化时直接在本文件更新。
- 每个问题尽量保留 4 个要素：问题描述、影响、证据、整改方向。
- 旧文档中的分析内容可以作为素材引用，但主结论以本文件为准。
- 进入执行阶段后，可在问题标题后增加状态，例如“未开始 / 处理中 / 已解决 / 已验证”。

## 0.1 文档关系

- 问题总表维护在本文件：`docs/company-readiness/system-issues.md`
- 对应修改方案维护在：`docs/company-readiness/remediation-plan.md`
- 旧文档去留、吸收方式和后续维护规则维护在：`docs/company-readiness/useful-docs-map.md`

## 1. 当前系统定位

当前系统是一个 AI 财务分析 MVP，适合演示财务驾驶舱、预算偏差、风险提示、报告和 AI 问答能力。它还不是可直接承载真实企业财务数据和公网用户访问的生产系统。

当前技术栈：

- 后端：Spring Boot 3.4.5、Java 21、JPA、MySQL、Redis、Spring AI。
- 前端：Vue 3、Vite、TypeScript、Vue Router、Axios、ECharts、Vitest。
- 部署：Docker Compose、Caddy、MySQL、Redis。

## 2. P0 阻断级问题

### P0-0 中文编码和文档可读性问题 [部分解决]

问题：现有多份历史文档和部分配置注释出现乱码。虽然新文档已使用 UTF-8 写入，但旧文档仍不适合作为团队评审和交付材料。

影响：需求、架构、运维、修复计划无法被稳定阅读，团队协作和交接风险很高。

证据：

- `docs/superpowers/plans/2026-05-10-ai-finance-platform-implementation-plan.md`
- `docs/superpowers/reviews/2026-05-26-system-hardening-plan.md`
- `docs/superpowers/reviews/2026-05-26-system-issues-remediation.md`
- `docker-compose.yml`
- `docker-compose.prod.yml`
- `Caddyfile`

当前状态：

- 已完成：`docs/company-readiness/*` 主线文档已收敛，并可正常阅读
- 未完成：`docs/superpowers/*` 和部分历史注释仍存在乱码或可读性问题
- 结论：主线协作文档已可用，但全仓库编码治理尚未完成

### P0-1 前端当前不可构建 [已解决]

问题：`npm run build` 失败，TypeScript 无法解析 `lucide-vue-next`。

影响：无法形成可交付前端产物，CI/CD 也无法建立稳定门禁。

证据：

- `frontend/package.json`
- `frontend/src/components/AppLayout.vue`
- `frontend/src/components/KpiCard.vue`
- `frontend/src/components/AiAssistantFloating.vue`

验证结果：

```text
npm run build
error TS2307: Cannot find module 'lucide-vue-next'
```

最新验证：

```text
cd frontend
npm run build
```

结果：通过

当前状态：

- 已修复 `lucide-vue-next` 相关依赖与构建问题
- 前端 Docker 镜像构建也已通过
- 该问题已不再阻断本地与容器构建

### P0-2 前端测试失败且测试边界漂移 [已解决]

问题：`npm test` 失败。AI 页面测试仍然 mock `askAi()`，但页面实现已经改成直接 `fetch('/api/ai/ask/stream')`，导致测试尝试访问本地后端。

影响：测试不能证明真实行为正确，也不能作为合并门禁。

证据：

- `frontend/src/views/AiAnalysisView.vue`
- `frontend/src/__tests__/AiAnalysisView.test.ts`
- `frontend/src/api/ai.ts`

验证结果：

```text
npm test
AiAnalysisView: expected askAi spy to be called, but calls = 0
AI Stream Error: fetch failed ECONNREFUSED
```

最新验证：

```text
cd frontend
npm test
```

结果：`6 passed`, `20 passed`

当前状态：

- `AiAnalysisView` 已改为通过共享 AI client 做串流调用
- 对应测试已改为 mock 共享 client，而不是误打真实后端
- 当前前端测试边界已恢复可信

### P0-3 后端构建不可复现 [部分解决]

问题：本机没有 `mvn`，项目也没有 Maven Wrapper。

影响：新机器、CI、协作者无法保证一致构建后端。

证据：

- `backend/pom.xml`
- 仓库根目录和 `backend/` 下没有 `mvnw` / `mvnw.cmd`

验证结果：

```text
mvn test
The term 'mvn' is not recognized
```

最新验证：

```text
cd backend
.\mvnw.cmd test
```

结果：

```text
No compiler is provided in this environment. Perhaps you are running on a JRE rather than a JDK?
```

当前状态：

- 已新增 `mvnw`、`mvnw.cmd` 与 `.mvn/wrapper/*`
- CI 已切换为使用 wrapper
- 当前机器因缺少可用 JDK 编译器，尚未完成本机闭环验证
- 结论：仓库层面的可复现性问题已明显缓解，但本地环境验证尚未完全闭环

### P0-4 缺少认证授权

问题：后端未引入 Spring Security，也没有登录、Token、RBAC、租户隔离或用户上下文。

影响：财务数据、报告和 AI 问答接口不能直接暴露到公网或真实企业环境。

证据：

- `backend/pom.xml`
- `backend/src/main/java/com/aifinance/*`
- `docker-compose.prod.yml`

### P0-5 生产 CORS 默认过宽

问题：生产 Compose 中 `APP_CORS_ALLOWED_ORIGINS` 默认是 `*`。

影响：浏览器侧跨域访问边界不受控，叠加无认证会放大数据泄露风险。

证据：

- `docker-compose.prod.yml`
- `backend/src/main/java/com/aifinance/common/WebConfig.java`

### P0-6 生产默认密码不可接受

问题：生产 Compose 对 MySQL root/user 密码提供默认值，例如 `root_password`、`ai_finance_password`。

影响：如果部署者忘记覆盖环境变量，会以弱默认密码上线。

证据：

- `docker-compose.prod.yml`
- `.env.example`

### P0-7 AI Prompt 和模型配置未版本化治理

问题：AI prompt、模型参数、结构化输出要求主要散落在代码和配置中，缺少版本、变更记录和回归评测基线。

影响：模型输出变化后难以追踪原因，也难以判断一次 prompt 修改是否让财务分析质量变差。

证据：

- `backend/src/main/java/com/aifinance/ai/provider/OpenAiCompatibleAiProvider.java`
- `backend/src/main/java/com/aifinance/ai/config/AiProperties.java`
- `backend/src/main/resources/application.yml`

## 3. P1 高优先级问题

### P1-1 缺少统一异常处理和请求校验

问题：请求参数和 DTO 缺少 `@Valid`、Bean Validation 注解和全局 `@ControllerAdvice`。

影响：非法 `periodId`、`months`、AI question 等可能返回 Spring 默认错误，不利于前端统一处理。

证据：

- `backend/src/main/java/com/aifinance/ai/dto/AiAskRequest.java`
- `backend/src/main/java/com/aifinance/dashboard/controller/DashboardController.java`
- `backend/src/main/java/com/aifinance/ai/controller/AiAnalysisController.java`
- `backend/src/main/java/com/aifinance/common/ApiResponse.java`
- `backend/src/main/java/com/aifinance/common/ErrorCode.java`

### P1-2 数据库初始化方式不适合生产

问题：`spring.sql.init.mode: always` 会在应用启动时持续执行 schema/data 初始化。当前没有 Flyway 或 Liquibase。

影响：生产重启可能污染数据或引发初始化冲突；数据库变更无法审计和回滚。

证据：

- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/db/schema.sql`
- `backend/src/main/resources/db/data.sql`

### P1-3 当前期间状态不一致

问题：Dashboard、AI、预算、报告模块的期间选择逻辑不统一，部分页面固定 `periodId = 4`。

影响：用户可能以为自己在分析同一个期间，实际不同页面请求的是不同上下文，影响财务分析可信度。

证据：

- `frontend/src/views/DashboardView.vue`
- `frontend/src/views/AiAnalysisView.vue`
- `frontend/src/views/BudgetVarianceView.vue`
- `frontend/src/views/ReportView.vue`
- `frontend/src/components/AiAssistantFloating.vue`

### P1-4 AI client 分裂 [已解决]

问题：部分 AI 请求走 `frontend/src/api/ai.ts`，流式问答在组件内直接 `fetch`。

影响：错误处理、超时、鉴权、traceId、测试 mock、SSE 协议无法统一治理。

证据：

- `frontend/src/api/ai.ts`
- `frontend/src/views/AiAnalysisView.vue`
- `frontend/src/components/AiAssistantFloating.vue`

当前状态：

- 已完成：`AiAnalysisView` 已改为使用共享 `streamAiAnswer()`
- 已完成：`AiAssistantFloating` 已改为使用共享 `streamAiAnswer()`
- 已完成：新增组件测试，验证悬浮助手通过共享 AI client 工作
- 结论：前端 AI client 已完成统一，组件内不再直接写裸 `fetch`

### P1-5 AI provider 缺少生产级弹性治理

问题：AI 调用缺少统一超时、限流、熔断、重试、并发隔离、输出 schema 校验和 prompt 注入防护。

影响：外部模型慢请求或异常会影响系统稳定性；模型输出不稳定会影响前端展示和业务可信度。

证据：

- `backend/src/main/java/com/aifinance/ai/provider/OpenAiCompatibleAiProvider.java`
- `backend/src/main/java/com/aifinance/ai/service/AiAnalysisService.java`

### P1-6 AI 缓存预热有启动风险

问题：启动后使用默认 `CompletableFuture.runAsync()` 预热最近数据的 AI 缓存；同时在同一个 service 内调用带缓存注解的方法，可能绕过 Spring cache proxy，导致预热并没有按预期写入缓存。

影响：部署启动阶段可能放大外部 AI 调用、消耗模型额度、拉长启动稳定期；缓存预热效果也可能不可靠。

证据：

- `backend/src/main/java/com/aifinance/ai/service/AiAnalysisService.java`

### P1-7 前端存在 XSS 风险 [已解决]

问题：AI 悬浮助手中使用 `v-html` 渲染模型返回文本。

影响：如果模型或后端返回 HTML/脚本内容，存在跨站脚本风险。

证据：

- `frontend/src/components/AiAssistantFloating.vue`

当前状态：

- `AiAssistantFloating.vue` 已移除 `v-html`
- AI 文本已改为安全文本渲染，换行通过 CSS 保留，不再拼接 HTML
- 已新增测试覆盖恶意 HTML 文本不会按原样注入 DOM
- 该问题已解决

### P1-8 缺少 AI 成本、用量和限流治理

问题：当前未看到按用户、期间、问题、模型记录 token、耗时、调用结果和成本预算的机制。

影响：AI 接口容易被滥用或意外放大成本；也无法分析 AI 失败率和性能趋势。

证据：

- `backend/src/main/java/com/aifinance/ai/provider/OpenAiCompatibleAiProvider.java`
- `backend/src/main/java/com/aifinance/ai/service/AiAnalysisService.java`

### P1-9 缺少审计日志

问题：登录、AI 提问、报告生成、配置变更、未来数据导入等关键行为缺少审计记录。

影响：真实企业场景下无法追踪谁访问了财务数据、谁触发了 AI 分析、谁导出了报告。

证据：

- `backend/src/main/java/com/aifinance/*`

### P1-10 部署缺少健康检查和可观测性

问题：生产 Compose 只有 MySQL healthcheck；后端、前端、Caddy 无健康检查。后端没有 Actuator、Micrometer、Prometheus、OpenTelemetry 等观测能力。

影响：服务是否 ready、是否可用、何处故障不可被自动判断。

证据：

- `docker-compose.prod.yml`
- `backend/src/main/java/com/aifinance/common/HealthController.java`
- `backend/pom.xml`

### P1-11 缺少 CI/CD 质量门禁 [部分解决]

问题：仓库未发现 GitHub Actions、GitLab CI、Jenkinsfile 或其他流水线配置；后端 Dockerfile 构建跳过测试，前端 Dockerfile 使用 `npm install` 而不是锁文件严格安装。

影响：失败代码容易进入主分支或镜像；前端依赖解析可能与本地或 CI 不一致，导致构建不可复现。

证据：

- `backend/Dockerfile`
- `frontend/Dockerfile`
- `frontend/package.json`
- 仓库无 `.github/workflows/`

当前状态：

- 已完成：仓库已新增 `.github/workflows/ci.yml`
- 已完成：CI 中已包含前端安装、测试、构建，后端 wrapper 测试，以及前后端 Docker 镜像构建
- 未完成：远端仓库的“CI 失败禁止合并”分支保护规则尚未确认开启
- 结论：CI 工作流已落地，但质量门禁尚未完全闭环

### P1-12 缺少依赖和镜像安全扫描

问题：未看到 npm audit、Maven dependency check、镜像漏洞扫描等流水线门禁。

影响：依赖漏洞和基础镜像漏洞可能进入生产。

证据：

- `frontend/package.json`
- `backend/pom.xml`
- `backend/Dockerfile`
- `frontend/Dockerfile`

### P1-13 生产 MySQL healthcheck 变量插值不稳

问题：生产 Compose 的 MySQL healthcheck 使用 `${MYSQL_USER}` 和 `${MYSQL_PASSWORD}`，没有 Compose 默认值兜底，也不是容器内 `$${...}` 形式。

影响：缺少 `.env` 或变量名不一致时，健康检查可能使用空账号执行，导致后端依赖健康状态卡住或误判。

证据：

- `docker-compose.prod.yml`

### P1-14 仓库缺少根 README [已解决]

问题：仓库根目录没有 `README.md`，启动、测试、构建、部署和故障排查说明分散在 `docs/superpowers`。

影响：新成员无法从仓库入口获得统一操作方式，也不利于社会公司主流项目交接。

证据：

- 仓库根目录无 `README.md`
- `docs/superpowers/*`

当前状态：

- 根目录 `README.md` 已新增
- 已补充项目定位、技术栈、本地启动、测试、构建、Docker 与文档入口
- 该问题已解决

## 4. P2 中优先级问题

### P2-1 Caddy 生产入口过于简化

问题：Caddy 只监听 HTTP 80，没有域名、TLS、HSTS、安全响应头、访问日志、限流、超时和错误页。

影响：不符合公网生产入口基本要求。

证据：

- `Caddyfile`

### P2-1.5 AI Provider 暴露底层异常细节

问题：AI Provider 在部分失败场景中把底层异常信息拼入用户可见响应。

影响：可能暴露供应商错误、网络细节、模型名或配置状态，不利于安全治理和用户体验。

证据：

- `backend/src/main/java/com/aifinance/ai/provider/OpenAiCompatibleAiProvider.java`

### P2-2 导航存在死路由 [已解决]

问题：前端侧边栏存在 `/settings` 入口，但路由表没有 settings 页面；也没有 404 fallback。

影响：用户可能进入空白或不可用页面。

证据：

- `frontend/src/components/AppLayout.vue`
- `frontend/src/router/index.ts`

当前状态：

- 路由表中已补充 `/settings`
- 已新增 `frontend/src/views/SettingsView.vue`
- 该问题已解决

### P2-3 数据字典和指标口径不足

问题：数据库表结构存在，但收入、成本、毛利、净利、预算偏差、风险等级等业务口径没有可审阅文档。

影响：财务系统难以被业务、审计、开发共同确认。

证据：

- `backend/src/main/resources/db/schema.sql`
- `backend/src/main/java/com/aifinance/finance/service/FinanceMetricCalculator.java`

### P2-4 文档治理不足 [部分解决]

问题：现有 `docs/superpowers` 下部分中文文档出现乱码，不适合作为正式交付文档。

影响：团队协作、评审、交接和外部展示都受影响。

证据：

- `docs/superpowers/plans/2026-05-10-ai-finance-platform-implementation-plan.md`
- `docs/superpowers/reviews/2026-05-26-system-hardening-plan.md`
- `docs/superpowers/reviews/2026-05-26-system-issues-remediation.md`

当前状态：

- 已完成：`docs/company-readiness/` 已建立三份主线文档，并明确维护规则
- 未完成：旧 `docs/superpowers/*` 文档仍保留为素材库，其中部分存在乱码和维护分叉历史
- 结论：文档治理主线已建立，但历史文档清理和编码修复仍未完成

## 5. 总体判断

当前系统已经具备 MVP 功能骨架，但缺少主流公司生产系统的关键能力：

- 稳定构建和测试。
- 认证授权和安全边界。
- 统一 API 错误契约。
- 数据库迁移治理。
- AI 调用治理。
- 可观测性和发布回滚。
- 可维护的产品、技术、运维文档。

最优先的目标不是继续扩页面，而是先把系统变成“任何人能构建、测试、部署、排障、审计”的工程系统。
