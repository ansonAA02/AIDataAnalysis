# remediation-plan.md

生成日期：2026-05-26  
文档角色：修改方案主文档  
维护目标：集中维护从“当前 MVP 状态”走向“公司主流使用系统”的整改路线、分阶段任务和验收标准。

## 0. 使用规则

- 本文件只写“怎么改”，不重复展开问题证据；问题来源统一引用 `system-issues.md`。
- 每个阶段必须包含：目标、修改内容、验收标准、优先级。
- 新增整改任务时，优先并入现有阶段；只有明显跨阶段时再新增阶段。
- 已完成任务可在标题或条目后追加状态，但不要删除原始整改意图。
- 后续如果拆成 Sprint 或 Jira 任务，以本文件为母版，不再反向维护多份方案文档。

## 0.1 文档关系

- 问题来源主表：`docs/company-readiness/system-issues.md`
- 整改方案主表：`docs/company-readiness/remediation-plan.md`
- 旧文档映射和维护路线：`docs/company-readiness/useful-docs-map.md`

## 1. 改造原则

1. 先恢复可构建、可测试、可部署，再扩功能。
2. 先建立安全边界，再接真实用户和真实财务数据。
3. 先统一 API、错误、期间、AI client，再做复杂交互。
4. 先做最小可用文档，不一次性堆企业级文档。
5. 每个阶段都要有明确验收标准。

## 2. 阶段一：恢复工程可信度

目标：让项目在任意开发机和 CI 中可稳定构建、测试。

### 阶段一当前状态（2026-05-27）

- 当前总体状态：部分完成
- 已有明确证据完成的事项：
  - 前端依赖与构建已恢复，`npm run build` 本地通过
  - 前端测试边界已修复，`npm test` 本地通过
  - 根 README 已补齐
  - Docker 构建可复现性已明显改善，前后端镜像本地构建通过
  - 前后端 `.dockerignore` 已生效，前端 Docker build context 已从此前约 `212MB` 降至 `2.58kB`
- 已落地但仍需补最后闭环的事项：
  - Maven Wrapper 已提交，但当前机器缺少可用 JDK，`.\mvnw.cmd test` 本地未完全闭环
  - CI workflow 已提交，但“红灯禁止合并”属于远端仓库保护规则，尚需在 GitHub 仓库设置中确认
- 仍未完成的事项：
  - 依赖和镜像安全扫描尚未落地
  - 编码治理与乱码修复只完成了主线文档收敛，尚未完成全仓库清理

### 任务 0：建立改造基线和编码治理 [部分完成]

修改内容：

- 整理当前依赖版本、启动方式、环境变量、数据表、API、AI 调用链。
- 明确哪些代码是 MVP 临时实现，哪些需要进入生产改造。
- 修复历史中文文档和配置注释乱码，统一 UTF-8。
- 保留现有文档，但新增正式可读版本作为团队协作基准。

验收标准：

- 新增或更新一份现状基线说明。
- `docs/` 下正式交付文档用 UTF-8 可读。
- Docker、Caddy、SQL、AI prompt 中不再出现乱码注释。

优先级：P0

当前状态：

- 已完成：主线整改文档体系已建立，包括 `system-issues.md`、`remediation-plan.md`、`useful-docs-map.md`
- 已完成：新增 `README.md` 与 `Agent.md`，可作为当前阶段的工程与协作入口
- 未完成：历史乱码文件尚未全量清理，仓库内仍存在旧文档和注释可读性问题
- 结论：该任务已启动并完成主线文档收敛，但尚不能判定为完全完成

### 任务 1：修复前端依赖和构建 [已完成]

修改内容：

- 重新安装并锁定 `lucide-vue-next`。
- 确认 `package.json` 与 `package-lock.json` 一致。
- 清理不可复现的 `node_modules` 状态。

验收标准：

```text
cd frontend
npm ci
npm run build
```

必须通过。

优先级：P0

当前状态：

- 已验证本地 `npm run build` 通过
- 前端 Docker 构建通过
- `frontend/Dockerfile` 已改为 `npm ci`

验证结果：

```text
cd frontend
npm run build
```

通过。

### 任务 2：修复前端测试边界 [已完成]

修改内容：

- 将流式 AI 请求从组件内抽到统一 client，例如 `frontend/src/api/aiStream.ts`。
- 测试统一 mock AI client，而不是让组件直接访问真实后端。
- 更新 `AiAnalysisView.test.ts`，覆盖成功、失败、loading、SSE 中断。
- 修复 `App.test.ts` 和 `DashboardView.test.ts` 中与当前路由/组件不一致的断言。

验收标准：

```text
cd frontend
npm test
```

必须通过。

优先级：P0

当前状态：

- `AiAnalysisView.vue` 已从直接裸 `fetch` 调用收敛为共享 `streamAiAnswer()` 客户端调用
- `AiAnalysisView.test.ts` 已改为 mock 共享 AI client
- `App.test.ts`、`DashboardView.test.ts` 当前已通过

验证结果：

```text
cd frontend
npm test
```

结果：`6 passed`, `20 passed`

### 任务 3：加入 Maven Wrapper [部分完成]

修改内容：

- 在仓库中提交 `mvnw`、`mvnw.cmd`、`.mvn/wrapper/*`。
- README 和 CI 使用 wrapper，而不是依赖本机 Maven。

验收标准：

```text
cd backend
./mvnw test
```

Windows 下：

```text
cd backend
mvnw.cmd test
```

必须通过。

优先级：P0

当前状态：

- 已提交 `backend/mvnw`、`backend/mvnw.cmd`、`backend/.mvn/wrapper/maven-wrapper.properties`
- `README.md` 与 `.github/workflows/ci.yml` 已改为使用 wrapper
- 当前机器执行 `.\mvnw.cmd test` 时失败，原因是本机环境缺少可用 JDK 编译器，不是 wrapper 文件缺失

验证结果：

```text
cd backend
.\mvnw.cmd test
```

当前结果：

```text
No compiler is provided in this environment. Perhaps you are running on a JRE rather than a JDK?
```

结论：

- 代码与流程层面已落地
- 本机验收未完全闭环，需在具备 JDK 21 的环境或 CI 中完成最终确认

### 任务 4：建立基础 CI [部分完成]

修改内容：

- 新增 CI workflow。
- 后端执行 `mvnw test`。
- 前端执行 `npm ci`、`npm test`、`npm run build`。
- Docker 构建不得默认跳过测试，除非 CI 已有明确测试门禁并在文档中说明。
- CI 失败禁止合并。

验收标准：

- Pull Request 能自动跑完整检查。
- 主分支不允许红灯合并。

优先级：P0

当前状态：

- 已新增 `.github/workflows/ci.yml`
- CI 已包含：
  - 前端 `npm ci`
  - 前端 `npm test`
  - 前端 `npm run build`
  - 后端 `./mvnw test`
  - 前后端 Docker 镜像构建
- “CI 失败禁止合并” 这一条需要远端仓库分支保护规则配合，无法仅从代码仓库内容中完成

结论：

- CI 工作流文件已落地
- 远端强制门禁仍需仓库设置完成后，才能判定完全完成

### 任务 4.5：修复 Docker 构建可复现性 [已完成]

修改内容：

- 后端 Dockerfile 默认执行测试，或改为 CI 测试通过后再构建镜像，并在 Dockerfile/CI 中明确职责。
- 前端 Dockerfile 使用 `npm ci` 替代 `npm install`。
- 固定基础镜像版本，避免漂移。

验收标准：

- 干净环境 Docker build 可复现。
- 镜像构建不会绕过质量门禁。
- 前端容器构建严格使用 lockfile。

优先级：P0

当前状态：

- `frontend/Dockerfile` 已改为 `npm ci`
- `backend/Dockerfile` 已明确由 CI 先执行测试，镜像构建阶段只生成制品
- 已补充 `frontend/.dockerignore` 与 `backend/.dockerignore`
- 本地已重新验证前后端镜像构建通过

验证结果：

```text
cd frontend
docker build --progress=plain -t ai-finance-frontend-ci-check .
```

通过，build context 为 `2.58kB`

```text
cd backend
docker build --progress=plain -t ai-finance-backend-ci-check .
```

通过，build context 为 `8.26kB`

结论：

- Docker 构建已具备可复现性
- `.dockerignore` 已实际生效，不再是纸面整改

### 任务 5：加入依赖和镜像安全扫描

修改内容：

- 前端加入 `npm audit` 或等价扫描。
- 后端加入 Maven dependency check 或等价扫描。
- Docker 镜像加入漏洞扫描。
- 定义高危漏洞阻断规则。

验收标准：

- CI 能输出依赖和镜像安全扫描结果。
- 高危漏洞不能进入主分支或生产镜像。

优先级：P1

### 任务 6：补根 README [已完成]

修改内容：

- 在仓库根目录新增 `README.md`。
- 写清楚项目定位、技术栈、本地启动、测试、构建、Docker、环境变量和常见问题。
- 链接到 `docs/company-readiness/system-issues.md` 与 `docs/company-readiness/remediation-plan.md`。

验收标准：

- 新成员只看 README 就能启动、测试、构建项目。
- README 明确标注当前系统仍是 MVP，不可直接用于真实财务生产数据。

优先级：P1

当前状态：

- 根目录 `README.md` 已新增
- 已写入项目定位、技术栈、本地启动、测试命令、构建命令、Docker 用法、文档入口和当前 readiness 说明
- 已链接 `docs/company-readiness/system-issues.md` 与 `docs/company-readiness/remediation-plan.md`

结论：

- README 作为仓库入口文档已经可用

## 3. 阶段二：建立安全和 API 基线

目标：让系统具备基本生产安全边界和可控错误契约。

### 任务 1：引入认证授权

修改内容：

- 引入 Spring Security。
- 定义用户、角色、权限模型。
- 初期可采用 JWT 或 session，至少保护所有 `/api/**`。
- 区分管理员、财务分析用户、只读用户。

验收标准：

- 未登录访问业务 API 返回 401。
- 权限不足返回 403。
- 前端能处理登录过期。

优先级：P0

### 任务 1.5：补审计日志

修改内容：

- 记录登录、AI 提问、报告生成、未来数据导入、配置变更。
- 审计字段至少包含 userId、action、resource、time、traceId、result。
- 敏感内容脱敏保存。

验收标准：

- 可以追踪某个用户在某个时间访问了哪些财务分析能力。
- AI 问答和报告生成可审计。

优先级：P1

### 任务 2：收紧 CORS 和生产默认配置

修改内容：

- 生产环境禁止 `APP_CORS_ALLOWED_ORIGINS=*`。
- 生产 Compose 删除数据库密码默认值。
- 启动时校验关键环境变量，缺失则失败。

验收标准：

- 生产部署必须显式提供域名和密码。
- 非允许 origin 不能跨域访问 API。

优先级：P0

### 任务 3：统一错误模型

修改内容：

- 扩展 `ApiResponse`。
- 增加 `ErrorCode` 枚举。
- 新增全局 `@ControllerAdvice`。
- 统一返回 `code/message/details/traceId`。

建议错误格式：

```json
{
  "code": "VALIDATION_ERROR",
  "message": "请求参数不合法",
  "details": {
    "periodId": "必须为正整数"
  },
  "traceId": "..."
}
```

验收标准：

- 参数错误、业务错误、系统错误都有统一 JSON 响应。
- 前端统一展示错误。

优先级：P1

### 任务 4：请求参数校验

修改内容：

- 引入 `spring-boot-starter-validation`。
- DTO 添加 `@NotBlank`、`@NotNull`、`@Positive`、`@Size`。
- Controller 参数添加 `@Min`、`@Max`。

验收标准：

- 空 AI 问题、非法 periodId、过大 months 都返回明确校验错误。

优先级：P1

### 任务 5：修复生产 healthcheck 与容器安全基线

修改内容：

- 修复 MySQL healthcheck 变量插值，使用容器内变量或明确默认值策略。
- 给 backend、frontend、caddy 添加 healthcheck。
- 为容器增加合理资源限制。
- 后续逐步改为非 root、最小权限、只读文件系统。

验收标准：

- `.env` 缺失时不会静默使用空 healthcheck 凭据。
- Compose 能正确反映服务健康状态。

优先级：P1

## 4. 阶段三：数据治理和业务可信度

目标：让财务数据、指标和迁移过程可审计、可复现。

### 任务 1：引入数据库迁移工具

修改内容：

- 引入 Flyway 或 Liquibase。
- 把 `schema.sql` 改为版本化迁移。
- demo seed 与生产数据分离。
- 生产禁用 `spring.sql.init.mode: always`。

验收标准：

- 新环境能通过迁移创建表。
- 生产重启不会重复插入 demo 数据。
- 数据库变更有版本记录。

优先级：P1

### 任务 2：建立数据字典和指标口径

修改内容：

- 编写表结构说明。
- 定义收入、成本、毛利、净利、现金流、预算偏差、风险等级算法。
- 明确金额单位、精度、四舍五入规则。

验收标准：

- 业务、开发、测试能用同一份口径验证数字。

优先级：P1

### 任务 3：统一全局期间状态

修改内容：

- 前端建立全局 period state。
- Dashboard、AI、预算、报告、悬浮助手共享同一期间。
- URL query 或 localStorage 保存当前期间。

验收标准：

- 用户切换期间后，所有模块分析同一 periodId。
- 测试覆盖期间切换。

优先级：P1

## 5. 阶段四：AI 治理

目标：让 AI 能力稳定、可控、安全、可测试。

### 任务 1：统一 AI client [已完成]

修改内容：

- 前端统一 `askAi`、`streamAiAnswer`、`getSuggestions`。
- 所有 AI 请求集中处理 base URL、鉴权、traceId、超时、错误。
- 组件不直接写裸 `fetch`。

验收标准：

- AI 页面和悬浮助手共用同一套 client。
- 测试可以稳定 mock。

优先级：P1

当前状态：

- `AiAnalysisView.vue` 与 `AiAssistantFloating.vue` 已统一使用共享 `streamAiAnswer()`
- 组件内裸 `fetch` 已移除
- 已新增 `AiAssistantFloating.test.ts`，验证浮窗走共享 AI client

验证结果：

```text
cd frontend
npm test
```

结果：`7 passed`, `22 passed`

结论：

- AI 页面和悬浮助手已共用同一套 streaming client
- 当前前端 AI client 边界已完成统一

### 任务 2：Prompt 和模型配置版本化

修改内容：

- 将核心 prompt 外部化或集中管理。
- 记录 prompt version、model、temperature/reasoning 参数。
- 缓存 key 包含 prompt version 和数据版本。
- 每次 prompt 修改保留变更说明。

验收标准：

- 能追踪某次 AI 答案使用的 prompt 版本和模型配置。
- Prompt 修改不会无声影响旧缓存。

优先级：P0

### 任务 3：后端 AI 调用弹性治理

修改内容：

- 增加超时。
- 增加限流。
- 增加重试和熔断。
- 给 AI 预热配置专用线程池。
- 启动预热可配置关闭。
- 避免同类自调用绕过 Spring cache proxy；必要时拆分缓存服务。

验收标准：

- AI 服务慢或不可用时，后端不会拖垮整体服务。
- 日志可看到 AI 延迟、失败原因、降级路径。
- 缓存预热确实命中缓存，不在启动阶段无控制消耗模型额度。

优先级：P1

### 任务 4：AI 成本、用量和限流治理

修改内容：

- 记录用户、periodId、问题、模型、耗时、成功/失败、token 用量。
- 为 AI 问答设置用户级和系统级限流。
- 增加成本预算阈值，超过阈值时降级或拒绝。

验收标准：

- 可以按天查看 AI 调用量、失败率、平均耗时和成本趋势。
- 异常高频调用会被限制。

优先级：P0

### 任务 5：AI 输出安全和 schema 校验 [部分完成]

修改内容：

- 明确 AI JSON schema。
- 后端校验模型输出，不合格则降级。
- 前端禁止直接 `v-html` 渲染 AI 原文，必要时使用安全 Markdown 渲染器和 sanitizer。
- 增加 prompt 注入和敏感信息防护说明。
- 用户可见错误不得拼接底层异常细节；详细错误只进入结构化日志。

验收标准：

- 恶意 HTML 不会执行。
- 非 JSON 或字段缺失不会导致前端崩溃。
- 用户看到的是安全、可理解的错误文案，工程细节只在日志中可查。

优先级：P1

当前状态：

- 已完成：前端 `AiAssistantFloating.vue` 已移除 `v-html`，改为安全文本渲染
- 已完成：新增测试覆盖恶意 HTML 文本不会按原样注入 DOM
- 未完成：后端 AI 输出 schema 校验、降级策略、prompt 注入防护说明仍未落地

验证结果：

```text
cd frontend
npm test
```

结果：`7 passed`, `22 passed`

结论：

- 前端渲染层的明显 XSS 风险已缓解
- 完整的 AI 输出安全治理仍需后端配套收尾

### 任务 6：AI 评测集

修改内容：

- 固定一组典型财务问题和期望输出结构。
- 每次 prompt 或模型参数变更后运行评测。
- 评估格式合规、数字引用、幻觉风险、建议可执行性。

验收标准：

- AI 改动有回归评估结果。
- 明显退化不能进入生产。

优先级：P2

## 6. 阶段五：部署、运维和可观测性

目标：让系统可部署、可监控、可回滚。

### 任务 1：补生产健康检查

修改内容：

- 后端引入 Actuator。
- 增加 liveness/readiness。
- readiness 检查 DB、Redis、关键配置。
- Compose 给 backend、frontend、caddy 添加 healthcheck。

验收标准：

- 容器启动不等于服务 ready。
- 依赖异常时 readiness 能反映失败。

优先级：P1

### 任务 2：补日志、指标、告警

修改内容：

- 日志增加 traceId、userId、path、status、latency。
- 引入 Micrometer 指标。
- 记录 AI latency、AI failure rate、API p95、错误率。
- 定义告警规则。

验收标准：

- 一次用户请求能从前端、后端、AI 调用串起来定位。
- AI 异常率升高能被发现。

优先级：P2

### 任务 3：升级 Caddy 生产入口

修改内容：

- 配置正式域名。
- 启用 HTTPS。
- 加安全响应头。
- 加访问日志。
- 设置 API 超时和错误页。

验收标准：

- 生产只通过 HTTPS 访问。
- 安全头和访问日志可验证。

优先级：P2

### 任务 4：备份和回滚

修改内容：

- MySQL 备份策略。
- Redis 数据策略。
- 发布失败回滚步骤。
- 数据迁移失败处理流程。

验收标准：

- 至少完成一次备份恢复演练。
- 发布失败能回到上一个版本。

优先级：P2

## 7. 阶段六：产品主流化

目标：从演示系统变为可持续使用的企业应用。

### 任务 1：补齐信息架构

修改内容：

- 修复 `/settings` 死路由。
- 增加 404 页面。
- 明确 welcome/dashboard 默认入口。
- 建立路由地图文档。

验收标准：

- 所有导航入口都有页面。
- 不存在无法解释的空白路由。

优先级：P2

### 任务 2：企业功能闭环

修改内容：

- 用户和组织。
- 角色权限。
- 审计日志。
- 报告导出。
- AI 对话历史。
- 数据来源说明。
- 分享和协作。

验收标准：

- 真实团队可以多人使用。
- 管理员可以追踪谁看了什么、问了什么、导出了什么。

优先级：P2

## 8. 最小必需文档

当前不要一次写十几份。先补 5 份小文档，每份 1 到 3 页即可。

### 1. README.md

内容：

- 项目是什么。
- 技术栈。
- 本地启动。
- 测试和构建命令。
- 常见问题。

### 2. docs/api.md

内容：

- API 列表。
- 参数。
- 响应。
- 错误格式。
- SSE 流式协议。

### 3. docs/deployment.md

内容：

- local/staging/prod 环境变量。
- Docker Compose 启动。
- Caddy 配置。
- 健康检查。

### 4. docs/data-dictionary.md

内容：

- 数据表。
- 字段解释。
- 财务指标口径。
- 单位和精度。

### 5. docs/release-runbook.md

内容：

- 发布步骤。
- 验证清单。
- 回滚步骤。
- 故障排查入口。

## 9. 推荐执行顺序

第一周：

1. 修复前端构建。
2. 修复前端测试。
3. 加 Maven Wrapper。
4. 加基础 CI。
5. 写 README。

第二周：

1. 加认证授权最小版。
2. 收紧生产 CORS 和默认密码。
3. 加统一错误模型和参数校验。
4. 写 API 文档。

第三周：

1. 引入 Flyway 或 Liquibase。
2. 拆分 demo seed 和生产数据。
3. 统一全局期间状态。
4. 写数据字典。

第四周：

1. 统一 AI client。
2. AI 调用加超时、限流、降级。
3. 去掉不安全 `v-html`。
4. 写 AI provider 文档，可先并入 API 文档。

第五周：

1. 加 Actuator readiness/liveness。
2. 增加 Compose healthcheck。
3. 补日志 traceId。
4. 写部署和发布 runbook。

## 10. 完成标准

达到以下标准后，可以认为系统从 MVP 进入“公司内部可试运行”阶段：

- 前端和后端测试全部通过。
- CI 自动跑测试和构建。
- 所有业务 API 有认证保护。
- 生产不允许默认密码和 `CORS=*`。
- 数据库变更走迁移工具。
- AI 请求有超时、降级和安全渲染。
- 所有导航入口可用。
- 有 README、API、部署、数据字典、发布回滚 5 份文档。

达到以下标准后，才适合接近“外部客户试点”：

- 有用户、组织、角色、审计日志。
- 有备份恢复演练。
- 有监控告警。
- 有 HTTPS、安全头、访问日志。
- 有真实数据导入和数据血缘说明。
- 有 AI 输出引用证据和历史记录。
