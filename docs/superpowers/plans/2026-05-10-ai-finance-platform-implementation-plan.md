# AI Finance Platform Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 基于《AI 企业财务经营数据分析决策平台设计文档》渐进开发一个 Spring Boot + Vue + MySQL 的财务经营分析 MVP。

**Architecture:** 后端使用 Spring Boot 提供财务指标、Dashboard、真实 AI 分析、预算偏差和经营报告 API；前端使用 Vue 构建驾驶舱、AI 分析页、预算偏差页和经营报告页；MySQL 只存储内置示例数据。系统启动时通过 SQL 初始化脚本写入 12 个月完整示例财务数据，包括收入、成本、毛利、净利、现金流、预算值和业务线数据，保证首次运行即可展示完整 Dashboard。AI 第一版直接接入 DeepSeek 真实 AI，后端通过 `AiProvider` 接口隔离具体模型供应商，默认使用 OpenAI-compatible HTTP Provider，`base_url=https://api.deepseek.com`，`model=deepseek-v4-pro`，API Key 从 `DEEPSEEK_API_KEY` 环境变量读取，并启用 high reasoning 与 thinking。

**Tech Stack:** Spring Boot, Java, Maven, MySQL, Vue, Vite, TypeScript, ECharts, Axios, Vitest, JUnit, OpenAI-compatible AI API.

---

## 0. 补充约束

### 0.1 财务金额计算规范

- 所有金额字段、预算值、偏差值、汇总值和财务计算必须使用 `BigDecimal`。
- 金额单位统一为“万元”，后端 API 应在字段或元数据中明确单位。
- 百分比统一由后端计算并保留 2 位小数。
- 前端只展示后端返回的金额、百分比和趋势结果，不在前端重复计算财务指标。

### 0.2 示例数据设计原则

- 第一版内置最近 12 个月示例数据。
- 示例数据必须包含明显业务变化，用于支撑 Dashboard、预算偏差、AI 问答和经营报告。
- 数据中至少包含一次成本异常增长、一次现金流转负、一次预算超支、一次利润率下降。
- 示例数据必须包含收入、成本、毛利、净利、经营现金流、预算值和业务线数据。

### 0.3 AI 回答结构

AI 回答统一返回结构化内容，包含：

- 结论
- 数据依据
- 原因分析
- 风险提示
- 建议动作

### 0.4 风险等级与风险类型

风险等级统一为：

- `LOW`
- `MEDIUM`
- `HIGH`

风险类型统一为：

- `CASH_FLOW_RISK`
- `PROFIT_MARGIN_DROP`
- `COST_GROWTH_FAST`
- `BUDGET_OVERSPEND`
- `REVENUE_DECLINE`

### 0.5 前端视觉风格

- 系统采用企业级 SaaS 数据分析平台风格。
- 使用左侧菜单、顶部标题栏、卡片式布局和 ECharts 图表。
- 主色调为深蓝、科技蓝、白色和浅灰。
- 避免过度花哨的大屏风格。

### 0.6 开发流程

- 每完成一个 Step 并通过验证后，必须提交一次 git commit。
- 不得一次性实现多个模块。
- 不得实现 MVP 范围外功能。
- 当前工作区如果还不是 git 仓库，Step 1 之前必须先初始化 git；后续每个 Step 的回滚以对应 commit 为边界。

## 1. 需求文档审查

### 1.1 MVP 范围是否清晰

MVP 范围整体清晰，核心边界明确：

- 第一版只做财务经营分析，不扩展到销售、人力、供应链等综合经营域。
- 第一版只使用内置示例数据，不接入真实 Excel/CSV、ERP 或财务系统；示例数据必须由系统启动 SQL 初始化脚本写入。
- 第一版不做登录、权限、多租户、支付、审计级校验。
- 第一版 AI 直接接入 DeepSeek 真实 AI，通过 `AiProvider` 接口隔离供应商差异；禁止把 AI 调用逻辑写在 Controller 或前端页面中。
- 第一版产品形态以管理层财务驾驶舱为主，AI 问答、预算偏差、经营报告作为辅助页面。

这些边界适合 MVP，可以避免一开始陷入数据导入、权限系统和复杂 BI 的开发成本。真实 AI 接入只做后端 Provider 层和结构化回答，不扩展到复杂 Agent、工具调用或长期记忆。

### 1.2 可能遗漏的内容

- 缺少明确项目结构：需要定义 `backend/` 和 `frontend/` 的目录边界。
- 缺少 API 响应格式约定：建议统一返回 `code`、`message`、`data`，便于前端处理错误。
- 缺少示例数据范围：建议第一版准备最近 12 个月的月度数据，并通过 `schema.sql` 与 `data.sql` 在系统首次启动时初始化，支持趋势图、环比、预算偏差、业务线展示和报告生成。
- 缺少财务指标枚举：需要固定 `REVENUE`、`COST`、`GROSS_PROFIT`、`NET_PROFIT`、`OPERATING_CASH_FLOW` 等指标编码。
- 缺少 AI 问答范围：需要限制第一版只支持常见问题意图匹配，不能做开放式自由推理。
- 缺少 Provider 替换边界：需要单独定义 `AiProvider` 接口和 `OpenAiCompatibleAiProvider` 实现，避免真实 AI 调用逻辑散落在 Controller 或页面里。
- 缺少前端状态处理：需要规划 loading、empty、error、success 四类基础状态。

### 1.3 可能矛盾的地方

- 文档原先假设 AI 可用模板模拟，但补充要求改为直接接入真实 AI。实现时必须以真实 AI Provider 为准，模板只用于提示词和回答结构约束，不用于假装模型回答。
- 文档提到“选择月份或季度生成报告”，但数据设计只定义了月度期间。实现时可以通过 `finance_period.quarter` 支持季度聚合，第一版优先做月度报告，季度报告后置。
- 文档提到“业务线维度指标”，但前端页面没有明确业务线分析页。第一版可以保留业务线数据表和后端分析能力，但不单独做业务线页面。

### 1.4 可能过于复杂的地方

- 报告生成如果一开始做 PDF/Word 导出会过重，第一版只做页面内结构化报告。
- AI 问答如果支持复杂 Agent、外部工具调用和跨会话记忆会过重，第一版只做基于当前示例财务数据上下文的单轮真实 AI 问答。
- 趋势分析如果同时做同比、环比、多维筛选和业务线拆解会过重，第一版优先做月度环比和预算偏差。
- 错误处理不需要复杂监控平台，第一版用统一响应、后端日志和前端友好提示即可。

### 1.5 第一版适合实现的内容

- Spring Boot + Vue + MySQL 基础项目结构。
- MySQL 内置示例数据初始化，首次运行即可支撑完整 Dashboard。
- 财务期间、财务指标、预算、业务线指标表。
- Dashboard 核心 KPI、趋势、风险 API。
- 财务分析服务：指标汇总、毛利率、净利率、预算偏差、环比变化、风险识别。
- 真实 AI 分析服务：经营摘要、常见问题回答、预算偏差解释、决策建议，并统一输出结构化回答。
- 页面内经营报告生成。
- Vue 首页驾驶舱、AI 分析页、预算偏差页、经营报告页。

### 1.6 应该后置的内容

- 用户登录、角色权限、多租户。
- Excel/CSV 上传和字段映射。
- ERP、财务系统、数据库连接器。
- 复杂 AI Agent、外部工具调用、长期记忆和多模型路由。
- PDF/Word 导出。
- 自定义报表设计器。
- 多账套、多币种、审计日志。
- 复杂预测模型和机器学习。

---

## 2. 建议项目结构

### 2.1 后端目录

- `backend/pom.xml`：Maven 依赖与构建配置。
- `backend/src/main/resources/application.yml`：应用配置。
- `backend/src/main/resources/db/schema.sql`：MySQL 表结构。
- `backend/src/main/resources/db/data.sql`：内置示例数据。
- `backend/src/main/java/com/aifinance/AiFinanceApplication.java`：Spring Boot 启动类。
- `backend/src/main/java/com/aifinance/common/ApiResponse.java`：统一 API 响应。
- `backend/src/main/java/com/aifinance/common/ErrorCode.java`：错误码。
- `backend/src/main/java/com/aifinance/finance/domain/*`：财务实体和枚举。
- `backend/src/main/java/com/aifinance/finance/repository/*`：数据访问。
- `backend/src/main/java/com/aifinance/finance/service/*`：财务分析服务。
- `backend/src/main/java/com/aifinance/dashboard/controller/*`：Dashboard API。
- `backend/src/main/java/com/aifinance/ai/controller/*`：AI 分析 API。
- `backend/src/main/java/com/aifinance/ai/provider/*`：AI Provider 接口与 OpenAI-compatible 真实 AI 实现。
- `backend/src/main/java/com/aifinance/report/controller/*`：经营报告 API。
- `backend/src/test/java/com/aifinance/*`：后端测试。

### 2.2 前端目录

- `frontend/package.json`：前端依赖与脚本。
- `frontend/vite.config.ts`：Vite 配置。
- `frontend/src/main.ts`：Vue 入口。
- `frontend/src/App.vue`：应用框架。
- `frontend/src/router/index.ts`：路由。
- `frontend/src/api/http.ts`：Axios 实例。
- `frontend/src/api/dashboard.ts`：Dashboard API 客户端。
- `frontend/src/api/ai.ts`：AI API 客户端。
- `frontend/src/api/budget.ts`：预算偏差 API 客户端。
- `frontend/src/api/report.ts`：报告 API 客户端。
- `frontend/src/types/finance.ts`：财务类型定义。
- `frontend/src/views/DashboardView.vue`：首页驾驶舱。
- `frontend/src/views/AiAnalysisView.vue`：AI 分析页。
- `frontend/src/views/BudgetVarianceView.vue`：预算偏差页。
- `frontend/src/views/ReportView.vue`：经营报告页。
- `frontend/src/components/*`：KPI 卡片、趋势图、风险列表、报告区块等组件。
- `frontend/src/__tests__/*`：前端测试。

---

## 3. 开发模块拆分

### 模块 1：基础项目结构与数据库初始化

**目标**

建立可运行的前后端项目骨架和 MySQL 初始化机制，不实现业务逻辑。后端启动配置必须支持自动执行 `schema.sql` 和 `data.sql`，使首次运行即可创建表并写入完整示例数据。

**涉及的前端文件**

- 新增：`frontend/package.json`
- 新增：`frontend/vite.config.ts`
- 新增：`frontend/index.html`
- 新增：`frontend/src/main.ts`
- 新增：`frontend/src/App.vue`
- 新增：`frontend/src/router/index.ts`

**涉及的后端文件**

- 新增：`backend/pom.xml`
- 新增：`backend/src/main/java/com/aifinance/AiFinanceApplication.java`
- 新增：`backend/src/main/resources/application.yml`
- 新增：`backend/src/main/resources/db/schema.sql`
- 新增：`backend/src/main/resources/db/data.sql`
- 新增：`backend/src/main/java/com/aifinance/common/ApiResponse.java`
- 新增：`backend/src/main/java/com/aifinance/common/ErrorCode.java`

**涉及的数据表**

- `finance_period`
- `finance_metric`
- `finance_budget`
- `business_line_metric`

**主要接口**

- `GET /api/health`

**测试或验证方式**

- 后端启动成功。
- `GET /api/health` 返回成功。
- MySQL 中能看到 4 张表。
- 首次启动后数据库已经完成表结构和示例数据初始化，不需要手动导入数据才能展示 Dashboard。
- 前端启动后能显示基础布局。

---

### 模块 2：财务指标数据模型与示例数据

**目标**

建立财务期间、指标、预算和业务线数据模型，写入最近 12 个月的内置示例数据。示例数据必须覆盖收入、成本、毛利、净利、经营现金流、预算值和业务线数据，保证 Dashboard、预算偏差、AI 分析和经营报告都有可用数据。

**涉及的前端文件**

- 新增：`frontend/src/types/finance.ts`

**涉及的后端文件**

- 新增：`backend/src/main/java/com/aifinance/finance/domain/FinancePeriod.java`
- 新增：`backend/src/main/java/com/aifinance/finance/domain/FinanceMetric.java`
- 新增：`backend/src/main/java/com/aifinance/finance/domain/FinanceBudget.java`
- 新增：`backend/src/main/java/com/aifinance/finance/domain/BusinessLineMetric.java`
- 新增：`backend/src/main/java/com/aifinance/finance/domain/MetricCode.java`
- 新增：`backend/src/main/java/com/aifinance/finance/repository/FinancePeriodRepository.java`
- 新增：`backend/src/main/java/com/aifinance/finance/repository/FinanceMetricRepository.java`
- 新增：`backend/src/main/java/com/aifinance/finance/repository/FinanceBudgetRepository.java`
- 新增：`backend/src/main/java/com/aifinance/finance/repository/BusinessLineMetricRepository.java`
- 修改：`backend/src/main/resources/db/schema.sql`
- 修改：`backend/src/main/resources/db/data.sql`

**涉及的数据表**

- `finance_period`
- `finance_metric`
- `finance_budget`
- `business_line_metric`

**主要接口**

- `GET /api/finance/periods`
- `GET /api/finance/metrics?periodId={periodId}`

**测试或验证方式**

- Repository 测试能读取 12 个月期间数据。
- 每个月包含收入、成本、毛利、净利、经营现金流、经营费用等关键指标。
- 每个核心指标都有预算值。
- 每个月至少包含 2 条业务线数据，例如“企业服务”和“订阅业务”。
- 示例数据至少包含一次成本异常增长、一次现金流转负、一次预算超支、一次利润率下降。
- 实体中的金额字段全部映射为 `BigDecimal`，不得使用 `double`、`float` 或 `Integer` 表示金额。
- 首次启动后无需额外导入 Excel/CSV，即可调用 Dashboard API 得到完整数据。
- API 返回数据结构与前端类型一致。

---

### 模块 3：Dashboard API

**目标**

提供首页驾驶舱所需的 KPI、趋势、风险摘要和 AI 摘要入口数据。

**涉及的前端文件**

- 新增：`frontend/src/api/dashboard.ts`
- 修改：`frontend/src/types/finance.ts`

**涉及的后端文件**

- 新增：`backend/src/main/java/com/aifinance/dashboard/controller/DashboardController.java`
- 新增：`backend/src/main/java/com/aifinance/dashboard/dto/DashboardOverviewResponse.java`
- 新增：`backend/src/main/java/com/aifinance/dashboard/dto/TrendPointResponse.java`
- 新增：`backend/src/main/java/com/aifinance/dashboard/dto/RiskAlertResponse.java`
- 修改：`backend/src/main/java/com/aifinance/finance/service/FinanceAnalysisService.java`

**涉及的数据表**

- `finance_period`
- `finance_metric`
- `finance_budget`

**主要接口**

- `GET /api/dashboard/overview?periodId={periodId}`
- `GET /api/dashboard/trends?months=12`
- `GET /api/dashboard/risks?periodId={periodId}`

**测试或验证方式**

- Controller 测试验证接口状态码和字段完整性。
- 趋势接口返回最近 12 个月数据。
- 风险接口至少能识别成本增长、利润率下降、现金流为负或预算偏差过大。

---

### 模块 4：财务分析服务

**目标**

封装财务计算逻辑，避免 Controller 和 AI 模块重复计算。

**涉及的前端文件**

- 无直接页面文件；前端通过 Dashboard、预算、报告 API 间接使用。

**涉及的后端文件**

- 新增：`backend/src/main/java/com/aifinance/finance/service/FinanceAnalysisService.java`
- 新增：`backend/src/main/java/com/aifinance/finance/service/FinanceMetricCalculator.java`
- 新增：`backend/src/main/java/com/aifinance/finance/dto/FinancialSnapshot.java`
- 新增：`backend/src/main/java/com/aifinance/finance/dto/VarianceResult.java`
- 新增：`backend/src/main/java/com/aifinance/finance/dto/RiskSignal.java`
- 新增：`backend/src/main/java/com/aifinance/finance/domain/RiskLevel.java`
- 新增：`backend/src/main/java/com/aifinance/finance/domain/RiskType.java`
- 新增：`backend/src/test/java/com/aifinance/finance/service/FinanceMetricCalculatorTest.java`
- 新增：`backend/src/test/java/com/aifinance/finance/service/FinanceAnalysisServiceTest.java`

**涉及的数据表**

- `finance_period`
- `finance_metric`
- `finance_budget`
- `business_line_metric`

**主要接口**

此模块主要是内部服务，不直接暴露独立 API。被 Dashboard、AI、预算、报告模块调用。

**测试或验证方式**

- 单元测试验证毛利率、净利率、预算偏差率、环比变化计算。
- 单元测试验证除数为 0 时返回明确状态，不抛出未处理异常。
- 单元测试验证风险规则结果稳定。
- 单元测试验证所有金额计算使用 `BigDecimal`，百分比保留 2 位小数。
- 单元测试验证风险等级只返回 `LOW`、`MEDIUM`、`HIGH`。
- 单元测试验证风险类型只返回 `CASH_FLOW_RISK`、`PROFIT_MARGIN_DROP`、`COST_GROWTH_FAST`、`BUDGET_OVERSPEND`、`REVENUE_DECLINE`。

---

### 模块 5：真实 AI 分析服务

**目标**

直接接入 DeepSeek 真实 AI，使用提示词模板约束回答结构，同时通过可替换的 `AiProvider` 接口隔离具体模型供应商。第一版默认实现 OpenAI-compatible HTTP Provider，默认 base URL 为 `https://api.deepseek.com`，默认模型为 `deepseek-v4-pro`，API Key 从 `DEEPSEEK_API_KEY` 环境变量读取，请求体启用 `reasoning_effort=high` 和 `extra_body.thinking.type=enabled`。

**涉及的前端文件**

- 新增：`frontend/src/api/ai.ts`
- 修改：`frontend/src/types/finance.ts`

**涉及的后端文件**

- 新增：`backend/src/main/java/com/aifinance/ai/provider/AiProvider.java`
- 新增：`backend/src/main/java/com/aifinance/ai/provider/OpenAiCompatibleAiProvider.java`
- 新增：`backend/src/main/java/com/aifinance/ai/config/AiProperties.java`
- 新增：`backend/src/main/java/com/aifinance/ai/service/AiAnalysisService.java`
- 新增：`backend/src/main/java/com/aifinance/ai/controller/AiAnalysisController.java`
- 新增：`backend/src/main/java/com/aifinance/ai/dto/AiAnalysisContext.java`
- 新增：`backend/src/main/java/com/aifinance/ai/dto/AiQuestionRequest.java`
- 新增：`backend/src/main/java/com/aifinance/ai/dto/AiAnswerResponse.java`
- 新增：`backend/src/main/java/com/aifinance/ai/dto/AiSummaryResponse.java`
- 新增：`backend/src/test/java/com/aifinance/ai/provider/OpenAiCompatibleAiProviderTest.java`
- 新增：`backend/src/test/java/com/aifinance/ai/service/AiAnalysisServiceTest.java`

**涉及的数据表**

- `finance_period`
- `finance_metric`
- `finance_budget`
- `business_line_metric`

**主要接口**

- `GET /api/ai/summary?periodId={periodId}`
- `POST /api/ai/ask`
- `GET /api/ai/suggestions?periodId={periodId}`

**测试或验证方式**

- 测试固定问题“为什么本月利润下降？”会携带当前期间财务上下文调用 `AiProvider`。
- 使用 mock HTTP server 测试 `OpenAiCompatibleAiProvider` 请求格式、鉴权头、模型名和响应解析。
- 测试默认配置指向 DeepSeek：base URL 为 `https://api.deepseek.com`，model 为 `deepseek-v4-pro`，API Key 环境变量名为 `DEEPSEEK_API_KEY`。
- 测试请求体包含 `reasoning_effort=high` 和 `extra_body.thinking.type=enabled`。
- 测试 AI 回答结构包含结论、数据依据、原因分析、风险提示、建议动作。
- 测试真实 AI 调用失败时返回明确错误，不伪造模板答案。
- 验证 Controller 只依赖 `AiAnalysisService`，不直接调用模型 API。

---

### 模块 6：经营报告服务

**目标**

基于财务分析服务和真实 AI 分析服务生成页面内结构化经营分析报告。

**涉及的前端文件**

- 新增：`frontend/src/api/report.ts`
- 修改：`frontend/src/types/finance.ts`

**涉及的后端文件**

- 新增：`backend/src/main/java/com/aifinance/report/service/ReportService.java`
- 新增：`backend/src/main/java/com/aifinance/report/controller/ReportController.java`
- 新增：`backend/src/main/java/com/aifinance/report/dto/ReportResponse.java`
- 新增：`backend/src/main/java/com/aifinance/report/dto/ReportSectionResponse.java`
- 新增：`backend/src/test/java/com/aifinance/report/service/ReportServiceTest.java`

**涉及的数据表**

- `finance_period`
- `finance_metric`
- `finance_budget`
- `business_line_metric`

**主要接口**

- `GET /api/reports/monthly?periodId={periodId}`

**测试或验证方式**

- 报告包含经营概览、收入分析、成本分析、利润分析、现金流分析、预算执行、风险、建议。
- 报告内容引用的数据与 `FinanceAnalysisService` 返回结果一致。
- 不实现 PDF/Word 导出。

---

### 模块 7：Vue 首页驾驶舱

**目标**

实现管理层首页，用户打开后能快速看到经营状态和风险。视觉风格采用企业级 SaaS 数据分析平台：左侧菜单、顶部标题栏、卡片式布局、ECharts 图表，主色调为深蓝、科技蓝、白色和浅灰。

**涉及的前端文件**

- 新增：`frontend/src/views/DashboardView.vue`
- 新增：`frontend/src/components/KpiCard.vue`
- 新增：`frontend/src/components/TrendChart.vue`
- 新增：`frontend/src/components/RiskAlertList.vue`
- 新增：`frontend/src/components/AiSummaryCard.vue`
- 新增：`frontend/src/components/AppLayout.vue`
- 新增：`frontend/src/styles/theme.css`
- 修改：`frontend/src/router/index.ts`
- 修改：`frontend/src/App.vue`
- 修改：`frontend/src/api/dashboard.ts`
- 修改：`frontend/src/api/ai.ts`

**涉及的后端文件**

- 使用模块 3 和模块 5 已完成的 API，无新增后端文件。

**涉及的数据表**

- `finance_period`
- `finance_metric`
- `finance_budget`

**主要接口**

- `GET /api/dashboard/overview?periodId={periodId}`
- `GET /api/dashboard/trends?months=12`
- `GET /api/dashboard/risks?periodId={periodId}`
- `GET /api/ai/summary?periodId={periodId}`

**测试或验证方式**

- 页面显示 KPI 卡片、趋势图、风险提醒、AI 摘要。
- 页面使用左侧菜单、顶部标题栏、卡片式布局和 ECharts 图表。
- 前端只展示后端返回的金额、百分比和风险等级，不在页面中计算财务指标。
- 接口 loading 时显示加载状态。
- 接口失败时显示错误提示。
- 用户可以跳转到 AI 分析页、预算偏差页、经营报告页。

---

### 模块 8：AI 分析页

**目标**

提供固定问题快捷入口和文本输入框，展示真实 AI 基于示例财务数据生成的结构化分析回答。

**涉及的前端文件**

- 新增：`frontend/src/views/AiAnalysisView.vue`
- 新增：`frontend/src/components/QuestionShortcutList.vue`
- 新增：`frontend/src/components/AiAnswerPanel.vue`
- 修改：`frontend/src/router/index.ts`
- 修改：`frontend/src/api/ai.ts`

**涉及的后端文件**

- 使用模块 5 已完成的 AI API，无新增后端文件。

**涉及的数据表**

- `finance_period`
- `finance_metric`
- `finance_budget`
- `business_line_metric`

**主要接口**

- `POST /api/ai/ask`
- `GET /api/ai/suggestions?periodId={periodId}`

**测试或验证方式**

- 点击快捷问题能得到回答。
- 输入未知问题能得到稳定兜底回答。
- 回答内容展示指标引用、原因拆解和建议动作。
- 回答内容按结论、数据依据、原因分析、风险提示、建议动作展示。
- 前端不保存或展示 API Key，真实 AI 配置只存在后端环境变量中。

---

### 模块 9：预算偏差页

**目标**

展示预算值、实际值、偏差金额、偏差率和 AI 偏差解释。

**涉及的前端文件**

- 新增：`frontend/src/views/BudgetVarianceView.vue`
- 新增：`frontend/src/components/BudgetVarianceTable.vue`
- 新增：`frontend/src/components/VarianceExplanationCard.vue`
- 新增：`frontend/src/api/budget.ts`
- 修改：`frontend/src/router/index.ts`

**涉及的后端文件**

- 新增：`backend/src/main/java/com/aifinance/budget/controller/BudgetController.java`
- 新增：`backend/src/main/java/com/aifinance/budget/dto/BudgetVarianceResponse.java`
- 新增：`backend/src/test/java/com/aifinance/budget/controller/BudgetControllerTest.java`
- 修改：`backend/src/main/java/com/aifinance/finance/service/FinanceAnalysisService.java`
- 修改：`backend/src/main/java/com/aifinance/ai/service/AiAnalysisService.java`

**涉及的数据表**

- `finance_period`
- `finance_metric`
- `finance_budget`

**主要接口**

- `GET /api/budget/variances?periodId={periodId}`
- `GET /api/budget/explanation?periodId={periodId}`

**测试或验证方式**

- 预算偏差表能展示核心指标的实际值、预算值、偏差金额、偏差率。
- 正向和负向偏差有明确标记。
- AI 偏差解释来自后端真实 AI 分析服务，不直接写在前端。

---

### 模块 10：经营报告页

**目标**

前端展示月度经营分析报告，支持选择期间并生成页面内结构化报告。

**涉及的前端文件**

- 新增：`frontend/src/views/ReportView.vue`
- 新增：`frontend/src/components/ReportSection.vue`
- 新增：`frontend/src/components/PeriodSelector.vue`
- 修改：`frontend/src/router/index.ts`
- 修改：`frontend/src/api/report.ts`

**涉及的后端文件**

- 使用模块 6 已完成的报告 API，无新增后端文件。

**涉及的数据表**

- `finance_period`
- `finance_metric`
- `finance_budget`
- `business_line_metric`

**主要接口**

- `GET /api/finance/periods`
- `GET /api/reports/monthly?periodId={periodId}`

**测试或验证方式**

- 用户选择月份后能生成报告。
- 报告包含固定章节。
- 报告失败时页面显示友好错误。
- 第一版不导出 PDF/Word。

---

## 4. 渐进式 Implementation Plan

通用执行规则：

- 每个 Step 只能实现该 Step 定义的文件和能力，不得顺手实现后续模块。
- 每个 Step 验证通过后必须创建一次 git commit，commit message 应能说明该 Step 的业务结果。
- 每个 Step 的回滚优先使用该 Step 对应 commit 作为边界；如果尚未提交，则按该 Step 的“如果失败如何回滚”删除或恢复文件。

### Step 0：初始化 git 仓库

**修改哪些文件**

- 无。

**新增哪些文件**

- `.gitignore`

**如何验证**

- 执行 `git status`，确认当前目录是 git 仓库。
- `.gitignore` 排除 `node_modules/`、`target/`、`.env`、IDE 临时文件和构建产物。
- 创建初始 commit，作为后续逐步回滚的基线。

**如果失败如何回滚**

- 如果 git 初始化位置错误，删除错误目录中的 `.git` 并在项目根目录重新初始化。
- 如果 `.gitignore` 配置错误，修改后重新提交初始 commit。

---

### Step 1：创建后端 Spring Boot 骨架

**修改哪些文件**

- 无。

**新增哪些文件**

- `backend/pom.xml`
- `backend/src/main/java/com/aifinance/AiFinanceApplication.java`
- `backend/src/main/resources/application.yml`
- `backend/src/main/java/com/aifinance/common/ApiResponse.java`
- `backend/src/main/java/com/aifinance/common/ErrorCode.java`
- `backend/src/main/java/com/aifinance/common/HealthController.java`

**如何验证**

- 在 `backend/` 执行 Maven 测试，确认项目能编译。
- 启动后端，访问 `GET /api/health`，预期返回成功响应。

**如果失败如何回滚**

- 删除本步骤新增的 `backend/` 文件。
- 如果已经使用 git，回滚本步骤新增文件即可。

---

### Step 2：创建前端 Vue 骨架

**修改哪些文件**

- 无。

**新增哪些文件**

- `frontend/package.json`
- `frontend/vite.config.ts`
- `frontend/index.html`
- `frontend/src/main.ts`
- `frontend/src/App.vue`
- `frontend/src/router/index.ts`

**如何验证**

- 在 `frontend/` 安装依赖并启动开发服务器。
- 浏览器打开前端地址，预期看到基础页面和导航占位。

**如果失败如何回滚**

- 删除本步骤新增的 `frontend/` 文件。
- 如果依赖安装失败，删除 `frontend/node_modules` 和 lock 文件后重装。

---

### Step 3：定义 MySQL 表结构

**修改哪些文件**

- `backend/src/main/resources/application.yml`

**新增哪些文件**

- `backend/src/main/resources/db/schema.sql`

**如何验证**

- 使用 MySQL 执行 `schema.sql`。
- 确认存在 `finance_period`、`finance_metric`、`finance_budget`、`business_line_metric` 四张表。
- 确认 `application.yml` 已配置 Spring Boot SQL 初始化路径，启动时会加载 `classpath:db/schema.sql` 和 `classpath:db/data.sql`。

**如果失败如何回滚**

- 删除新建表，恢复 `application.yml` 数据库配置到上一步状态。
- 如果表结构不符合预期，先删除空表，再重新执行修正后的 schema。

---

### Step 4：写入内置示例数据

**修改哪些文件**

- `backend/src/main/resources/db/schema.sql`
- `backend/src/main/resources/application.yml`

**新增哪些文件**

- `backend/src/main/resources/db/data.sql`

**如何验证**

- 首次启动后由 Spring Boot 自动执行 `data.sql`，不需要手动导入。
- 查询最近 12 个月数据，确认 `finance_period` 中存在连续 12 个财务期间。
- 确认每个月至少包含收入、成本、毛利、净利、经营现金流、经营费用等关键指标。
- 确认每个核心指标都有预算值，预算偏差 API 后续可直接计算。
- 确认每个月至少包含 2 条业务线数据，Dashboard 首次运行即可展示完整经营数据。
- 确认示例数据包含一次成本异常增长、一次现金流转负、一次预算超支、一次利润率下降。
- 启动后端并调用 Dashboard 相关接口时，不应因为缺少示例数据返回空 Dashboard。

**如果失败如何回滚**

- 清空四张示例数据表。
- 恢复 `data.sql` 到上一个可执行版本。
- 如果启动初始化失败，先禁用或回退 `application.yml` 中的 SQL 初始化配置，再修复脚本。

---

### Step 5：建立财务实体、枚举和 Repository

**修改哪些文件**

- 无。

**新增哪些文件**

- `backend/src/main/java/com/aifinance/finance/domain/FinancePeriod.java`
- `backend/src/main/java/com/aifinance/finance/domain/FinanceMetric.java`
- `backend/src/main/java/com/aifinance/finance/domain/FinanceBudget.java`
- `backend/src/main/java/com/aifinance/finance/domain/BusinessLineMetric.java`
- `backend/src/main/java/com/aifinance/finance/domain/MetricCode.java`
- `backend/src/main/java/com/aifinance/finance/repository/FinancePeriodRepository.java`
- `backend/src/main/java/com/aifinance/finance/repository/FinanceMetricRepository.java`
- `backend/src/main/java/com/aifinance/finance/repository/FinanceBudgetRepository.java`
- `backend/src/main/java/com/aifinance/finance/repository/BusinessLineMetricRepository.java`
- `backend/src/test/java/com/aifinance/finance/repository/FinanceRepositoryTest.java`

**如何验证**

- 运行 Repository 测试，确认能读取期间、指标、预算和业务线数据。
- 测试 `MetricCode` 枚举覆盖所有核心指标。

**如果失败如何回滚**

- 删除本步骤新增的 domain、repository、repository test 文件。
- 保留数据库脚本，便于重新映射实体。

---

### Step 6：实现财务期间和指标查询 API

**修改哪些文件**

- `frontend/src/types/finance.ts` 如果尚未存在则在后续前端步骤创建。

**新增哪些文件**

- `backend/src/main/java/com/aifinance/finance/controller/FinanceController.java`
- `backend/src/main/java/com/aifinance/finance/dto/FinancePeriodResponse.java`
- `backend/src/main/java/com/aifinance/finance/dto/FinanceMetricResponse.java`
- `backend/src/test/java/com/aifinance/finance/controller/FinanceControllerTest.java`

**如何验证**

- `GET /api/finance/periods` 返回 12 个期间。
- `GET /api/finance/metrics?periodId={periodId}` 返回该期间核心指标。

**如果失败如何回滚**

- 删除本步骤新增的 controller、dto、test 文件。
- Repository 层保持不变。

---

### Step 7：实现财务计算核心服务

**修改哪些文件**

- 无。

**新增哪些文件**

- `backend/src/main/java/com/aifinance/finance/service/FinanceMetricCalculator.java`
- `backend/src/main/java/com/aifinance/finance/dto/FinancialSnapshot.java`
- `backend/src/main/java/com/aifinance/finance/dto/VarianceResult.java`
- `backend/src/main/java/com/aifinance/finance/dto/RiskSignal.java`
- `backend/src/main/java/com/aifinance/finance/domain/RiskLevel.java`
- `backend/src/main/java/com/aifinance/finance/domain/RiskType.java`
- `backend/src/test/java/com/aifinance/finance/service/FinanceMetricCalculatorTest.java`

**如何验证**

- 单元测试验证毛利、毛利率、净利率、预算偏差、预算偏差率。
- 单元测试覆盖预算为 0 和上期值为 0 的计算场景。
- 单元测试验证金额输入和输出均使用 `BigDecimal`。
- 单元测试验证百分比结果统一保留 2 位小数。
- 单元测试验证风险等级和风险类型枚举值符合补充要求。

**如果失败如何回滚**

- 删除本步骤新增的 calculator、dto、test 文件。
- 不影响已完成的数据读取 API。

---

### Step 8：实现 FinanceAnalysisService

**修改哪些文件**

- 无。

**新增哪些文件**

- `backend/src/main/java/com/aifinance/finance/service/FinanceAnalysisService.java`
- `backend/src/test/java/com/aifinance/finance/service/FinanceAnalysisServiceTest.java`

**如何验证**

- 测试指定期间能生成 `FinancialSnapshot`。
- 测试最近 12 个月趋势数据可计算。
- 测试风险信号能识别利润率下降、成本增长过快、现金流为负、预算偏差过大。

**如果失败如何回滚**

- 删除本步骤新增服务和测试。
- 保留 Step 7 的纯计算器，便于重新组织服务。

---

### Step 9：实现 Dashboard API

**修改哪些文件**

- 无。

**新增哪些文件**

- `backend/src/main/java/com/aifinance/dashboard/controller/DashboardController.java`
- `backend/src/main/java/com/aifinance/dashboard/dto/DashboardOverviewResponse.java`
- `backend/src/main/java/com/aifinance/dashboard/dto/TrendPointResponse.java`
- `backend/src/main/java/com/aifinance/dashboard/dto/RiskAlertResponse.java`
- `backend/src/test/java/com/aifinance/dashboard/controller/DashboardControllerTest.java`

**如何验证**

- `GET /api/dashboard/overview?periodId={periodId}` 返回 KPI 总览。
- `GET /api/dashboard/trends?months=12` 返回趋势序列。
- `GET /api/dashboard/risks?periodId={periodId}` 返回风险提醒。

**如果失败如何回滚**

- 删除 Dashboard controller、dto、test。
- `FinanceAnalysisService` 保持可用。

---

### Step 10：定义真实 AI Provider 接口和 OpenAI-compatible 实现

**修改哪些文件**

- `backend/src/main/resources/application.yml`

**新增哪些文件**

- `backend/src/main/java/com/aifinance/ai/provider/AiProvider.java`
- `backend/src/main/java/com/aifinance/ai/provider/OpenAiCompatibleAiProvider.java`
- `backend/src/main/java/com/aifinance/ai/config/AiProperties.java`
- `backend/src/main/java/com/aifinance/ai/dto/AiAnalysisContext.java`
- `backend/src/main/java/com/aifinance/ai/dto/AiQuestionRequest.java`
- `backend/src/main/java/com/aifinance/ai/dto/AiAnswerResponse.java`
- `backend/src/main/java/com/aifinance/ai/dto/AiSummaryResponse.java`
- `backend/src/main/java/com/aifinance/ai/dto/OpenAiChatRequest.java`
- `backend/src/main/java/com/aifinance/ai/dto/OpenAiChatResponse.java`
- `backend/src/test/java/com/aifinance/ai/provider/OpenAiCompatibleAiProviderTest.java`

**如何验证**

- 测试 `AiProvider` 能接收财务上下文、用户问题和回答结构要求。
- 测试 `OpenAiCompatibleAiProvider` 从环境变量读取 DeepSeek base URL、API Key 和 model。
- 使用 mock HTTP server 验证真实 AI 请求格式，不在单元测试中真实消耗模型额度。
- 验证请求路径为 OpenAI-compatible chat completions，消息包含 system prompt 和 user prompt。
- 验证请求体包含 `model=deepseek-v4-pro`、`stream=false`、`reasoning_effort=high`、`extra_body.thinking.type=enabled`。
- 测试解析后的回答包含结论、数据依据、原因分析、风险提示、建议动作。

**如果失败如何回滚**

- 删除 AI provider、config、dto、test。
- 不影响 Dashboard 和财务分析模块。

---

### Step 11：实现 AI 分析 API

**修改哪些文件**

- 无。

**新增哪些文件**

- `backend/src/main/java/com/aifinance/ai/service/AiAnalysisService.java`
- `backend/src/main/java/com/aifinance/ai/controller/AiAnalysisController.java`
- `backend/src/test/java/com/aifinance/ai/service/AiAnalysisServiceTest.java`
- `backend/src/test/java/com/aifinance/ai/controller/AiAnalysisControllerTest.java`

**如何验证**

- `GET /api/ai/summary?periodId={periodId}` 返回经营摘要。
- `POST /api/ai/ask` 对常见财务问题返回真实 AI 结构化分析。
- `GET /api/ai/suggestions?periodId={periodId}` 返回建议列表。
- AI 回答包含结论、数据依据、原因分析、风险提示、建议动作。

**如果失败如何回滚**

- 删除 AI service、controller、test。
- 保留 `AiProvider` 接口和 `OpenAiCompatibleAiProvider`，便于重新组织业务服务。

---

### Step 12：实现预算偏差 API

**修改哪些文件**

- `backend/src/main/java/com/aifinance/finance/service/FinanceAnalysisService.java`
- `backend/src/main/java/com/aifinance/ai/service/AiAnalysisService.java`

**新增哪些文件**

- `backend/src/main/java/com/aifinance/budget/controller/BudgetController.java`
- `backend/src/main/java/com/aifinance/budget/dto/BudgetVarianceResponse.java`
- `backend/src/main/java/com/aifinance/budget/dto/BudgetExplanationResponse.java`
- `backend/src/test/java/com/aifinance/budget/controller/BudgetControllerTest.java`

**如何验证**

- `GET /api/budget/variances?periodId={periodId}` 返回核心指标偏差。
- `GET /api/budget/explanation?periodId={periodId}` 返回真实 AI 生成的结构化解释。
- 偏差率由后端以 `BigDecimal` 计算并保留 2 位小数。

**如果失败如何回滚**

- 删除 budget controller、dto、test。
- 恢复 `FinanceAnalysisService` 和 `AiAnalysisService` 到上一步状态。

---

### Step 13：实现经营报告 API

**修改哪些文件**

- 无。

**新增哪些文件**

- `backend/src/main/java/com/aifinance/report/service/ReportService.java`
- `backend/src/main/java/com/aifinance/report/controller/ReportController.java`
- `backend/src/main/java/com/aifinance/report/dto/ReportResponse.java`
- `backend/src/main/java/com/aifinance/report/dto/ReportSectionResponse.java`
- `backend/src/test/java/com/aifinance/report/service/ReportServiceTest.java`
- `backend/src/test/java/com/aifinance/report/controller/ReportControllerTest.java`

**如何验证**

- `GET /api/reports/monthly?periodId={periodId}` 返回固定章节报告。
- 报告章节包含经营概览、收入、成本、利润、现金流、预算、风险、建议。

**如果失败如何回滚**

- 删除 report service、controller、dto、test。
- Dashboard、AI 和预算模块保持不变。

---

### Step 14：创建前端 API 客户端和类型定义

**修改哪些文件**

- `frontend/src/router/index.ts`

**新增哪些文件**

- `frontend/src/api/http.ts`
- `frontend/src/api/dashboard.ts`
- `frontend/src/api/ai.ts`
- `frontend/src/api/budget.ts`
- `frontend/src/api/report.ts`
- `frontend/src/api/finance.ts`
- `frontend/src/types/finance.ts`

**如何验证**

- 前端 TypeScript 编译通过。
- API 客户端函数路径与后端接口一致。

**如果失败如何回滚**

- 删除本步骤新增的 API 和类型文件。
- 路由恢复到基础页面。

---

### Step 15：实现 Vue 首页驾驶舱

**修改哪些文件**

- `frontend/src/App.vue`
- `frontend/src/router/index.ts`

**新增哪些文件**

- `frontend/src/views/DashboardView.vue`
- `frontend/src/components/KpiCard.vue`
- `frontend/src/components/TrendChart.vue`
- `frontend/src/components/RiskAlertList.vue`
- `frontend/src/components/AiSummaryCard.vue`
- `frontend/src/components/AppLayout.vue`
- `frontend/src/styles/theme.css`
- `frontend/src/__tests__/DashboardView.test.ts`

**如何验证**

- 首页显示 KPI、趋势、风险、AI 摘要。
- 页面符合企业级 SaaS 数据分析平台风格：左侧菜单、顶部标题栏、卡片式布局、深蓝/科技蓝/白色/浅灰配色。
- 趋势图使用 ECharts 展示。
- 前端不重新计算金额、利润率、偏差率或风险等级。
- 后端接口不可用时显示错误状态。
- 测试验证核心区块渲染。

**如果失败如何回滚**

- 删除 Dashboard 页面和组件。
- 路由恢复到基础首页。

---

### Step 16：实现 Vue AI 分析页

**修改哪些文件**

- `frontend/src/router/index.ts`

**新增哪些文件**

- `frontend/src/views/AiAnalysisView.vue`
- `frontend/src/components/QuestionShortcutList.vue`
- `frontend/src/components/AiAnswerPanel.vue`
- `frontend/src/__tests__/AiAnalysisView.test.ts`

**如何验证**

- 点击快捷问题能展示真实 AI 结构化回答。
- 输入未知问题能展示兜底回答。
- 测试验证 loading、success、error 三种状态。

**如果失败如何回滚**

- 删除 AI 分析页和相关组件。
- 移除 AI 路由。

---

### Step 17：实现 Vue 预算偏差页

**修改哪些文件**

- `frontend/src/router/index.ts`

**新增哪些文件**

- `frontend/src/views/BudgetVarianceView.vue`
- `frontend/src/components/BudgetVarianceTable.vue`
- `frontend/src/components/VarianceExplanationCard.vue`
- `frontend/src/__tests__/BudgetVarianceView.test.ts`

**如何验证**

- 页面显示预算值、实际值、偏差金额、偏差率。
- 页面显示真实 AI 生成的偏差解释。
- 测试验证空状态和接口错误状态。

**如果失败如何回滚**

- 删除预算偏差页和相关组件。
- 移除预算偏差路由。

---

### Step 18：实现 Vue 经营报告页

**修改哪些文件**

- `frontend/src/router/index.ts`

**新增哪些文件**

- `frontend/src/views/ReportView.vue`
- `frontend/src/components/ReportSection.vue`
- `frontend/src/components/PeriodSelector.vue`
- `frontend/src/__tests__/ReportView.test.ts`

**如何验证**

- 用户选择期间后能生成结构化报告。
- 报告显示固定章节。
- 测试验证期间切换和报告渲染。

**如果失败如何回滚**

- 删除报告页和相关组件。
- 移除报告路由。

---

### Step 19：前后端联调

**修改哪些文件**

- `frontend/src/api/http.ts`
- `backend/src/main/resources/application.yml`

**新增哪些文件**

- 可选新增：`README.md`

**如何验证**

- 后端和前端同时启动。
- 从首页进入 AI 分析、预算偏差、经营报告页面，主要流程都能完成。
- 浏览器控制台无明显接口错误。

**如果失败如何回滚**

- 恢复 API base URL 和后端 CORS 配置。
- 若 README 内容不准确，删除或修正 README。

---

### Step 20：最终验收与范围检查

**修改哪些文件**

- `README.md` 如已创建则补充启动说明。

**新增哪些文件**

- 无。

**如何验证**

- 后端测试全部通过。
- 前端测试全部通过。
- 手动验收以下流程：
  - 打开首页，30 秒内能看懂经营状态。
  - 查看收入、成本、利润、现金流趋势。
  - 询问“为什么本月利润下降？”并得到真实 AI 结构化回答。
  - 查看预算偏差解释。
  - 生成月度经营分析报告。
- 确认已通过后端 `AiProvider` 接入真实 AI，API Key 只来自环境变量且不会提交到仓库。
- 确认没有实现登录权限、Excel/CSV 上传、ERP 对接、PDF/Word 导出等 MVP 范围外功能。
- 确认每个已完成 Step 都有对应 git commit。

**如果失败如何回滚**

- 根据失败模块回滚到对应 Step。
- 如果最终验收发现功能越界，删除越界功能并恢复 MVP 范围。

---

## 5. 实施顺序建议

优先顺序：

1. 后端基础和数据先行：Step 1 到 Step 6。
2. 财务分析能力闭环：Step 7 到 Step 9。
3. AI、预算、报告后端：Step 10 到 Step 13。
4. 前端 API 和页面：Step 14 到 Step 18。
5. 联调和验收：Step 19 到 Step 20。

每完成一个 Step 都应独立验证。不要一次性实现多个页面或多个后端模块，否则问题定位会变困难。

## 6. 自审结果

- 需求覆盖：设计文档中的驾驶舱、AI 问答、预算偏差、经营报告、示例数据、AI Provider 扩展点均已映射到开发模块和步骤。
- 范围控制：计划明确排除了登录权限、Excel/CSV 上传、ERP 对接、PDF/Word 导出、复杂 Agent 和多模型路由。
- 模块边界：财务计算集中在 `FinanceAnalysisService` 和 `FinanceMetricCalculator`；真实 AI 调用集中在 `AiProvider` 与 `OpenAiCompatibleAiProvider`；前端页面只通过 API 客户端访问后端。
- 渐进开发：每个 Step 都有文件范围、验证方式和回滚方式，且要求验证通过后创建 git commit。
