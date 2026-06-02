# useful-docs-map.md

生成日期：2026-05-26  
文档角色：旧文档去留、合并和维护路线说明文档  
维护目标：说明哪些旧文档仍然有用、哪些内容已经被主文档吸收、后续应该维护哪条文档主线。

## 0. 结论先行

当前只维护这 3 份主文档：

1. `docs/company-readiness/system-issues.md`
2. `docs/company-readiness/remediation-plan.md`
3. `docs/company-readiness/useful-docs-map.md`

维护规则：

- 新问题统一写入 `system-issues.md`
- 新方案统一写入 `remediation-plan.md`
- 旧 `docs/superpowers/*` 文档保留为素材和历史记录，不再继续作为整改主线追加内容

## 0.1 本文档解决什么问题

- 哪些旧文档有价值，应该继续参考
- 哪些旧文档只适合作为历史材料
- 旧文档内容应该合并到哪份主文档
- 后续团队应该维护哪一条文档线，避免重复维护

## 1. 推荐主线文档

以下 2 个文件建议作为现在的主线文档，后续整改优先维护它们。

当前维护规则：

- 新问题统一写入 `system-issues.md`。
- 新方案统一写入 `remediation-plan.md`。
- 旧 `superpowers` 文档只作为素材库，不再继续追加整改内容。

### 1.1 `docs/company-readiness/system-issues.md`

用途：当前系统问题总表。

保留原因：

- 已按 P0/P1/P2 分类。
- 覆盖前端、后端、安全、部署、数据、AI、测试、文档治理。
- 每个问题都有影响和证据文件。

建议用法：

- 作为后续 issue/backlog 的来源。
- 每解决一个问题，就在对应条目旁补状态或迁移到任务系统。

### 1.2 `docs/company-readiness/remediation-plan.md`

用途：公司主流化修改方案。

保留原因：

- 已按阶段组织：工程可信度、安全/API、数据治理、AI 治理、部署运维、产品主流化。
- 每个阶段有修改内容、验收标准和优先级。
- 适合直接拆成开发 Sprint。

建议用法：

- 作为后续 4 到 6 周改造路线。
- 每个任务落地时补充 owner、状态、PR 链接和验收结果。

## 2. 应合并吸收的素材文档

以下 3 个文件有价值，但不建议作为当前主线。它们应该被抽取内容，合并到上面 2 个主文档或后续最小文档中。

### 2.1 `docs/superpowers/reviews/2026-05-26-system-issues-remediation.md`

用途：更细的问题整改分析。

有用内容：

- P0/P1 问题描述、影响、根因、整改方向、验收标准。
- 前端构建失败、AI 契约漂移、固定 periodId、统一错误模型、CORS、Maven Wrapper 等问题。

合并建议：

- 把未覆盖的问题补进 `system-issues.md`。
- 把验收标准补进 `remediation-plan.md`。

保留方式：

- 保留为历史审计记录。
- 后续不再作为主维护文档。

### 2.2 `docs/superpowers/reviews/2026-05-26-system-hardening-plan.md`

用途：系统完善方向和目标架构。

有用内容：

- 公司主流系统的基线定义。
- 后端目标模块：`common`、`finance`、`dashboard`、`ai`、`report`、`security`、`ops`。
- 前端目标结构：`features/*`、`shared/*`、`app/*`。
- 部署目标：local、CI、staging、production 分层。

合并建议：

- 把目标架构方向合并到 `remediation-plan.md` 的阶段规划中。
- 后续拆出 `docs/architecture.md` 时可直接引用。

保留方式：

- 保留为架构素材。
- 不作为问题主表。

### 2.3 `docs/superpowers/specs/2026-05-10-ai-finance-platform-design.md`

用途：原始产品和系统设计文档。

有用内容：

- 产品定位：AI 企业财务经营数据分析决策平台。
- MVP 范围和非目标。
- 目标用户：CEO、财务负责人、经营分析师。
- 数据表、指标口径、API、前端模块、AI 能力边界、测试策略。

合并建议：

- 抽取到后续 3 个最小文档：
  - `docs/product.md`
  - `docs/api.md`
  - `docs/data-dictionary.md`

保留方式：

- 保留为产品设计基线。
- 后续不要直接在里面继续堆整改问题。

## 3. 只作历史参考的文档

### `docs/superpowers/plans/2026-05-10-ai-finance-platform-implementation-plan.md`

用途：早期实现计划。

有用内容：

- 详细模块拆解。
- 一些测试点和实现顺序。
- 已知差异说明，例如固定 `periodId`、AI 兜底依赖模型、Spring AI provider 设计。

不建议作为主线原因：

- 内容很长，偏“从零实现计划”，不适合当前整改管理。
- 很多内容已经被当前代码实现或替代。
- 适合作查历史，不适合作指导下一阶段公司化整改。

建议处理：

- 保留，但标记为历史实现计划。
- 后续只抽取仍有价值的测试点和已知差异。

## 4. 推荐最终文档结构

当前只维护这些有用文件即可：

```text
docs/
  company-readiness/
    system-issues.md
    remediation-plan.md
    useful-docs-map.md
  superpowers/
    specs/
      2026-05-10-ai-finance-platform-design.md
    reviews/
      2026-05-26-system-issues-remediation.md
      2026-05-26-system-hardening-plan.md
    plans/
      2026-05-10-ai-finance-platform-implementation-plan.md
```

后续真正需要新增的最小正式文档：

```text
README.md
docs/api.md
docs/deployment.md
docs/data-dictionary.md
docs/release-runbook.md
```

## 5. 当前结论

真正应该用于你现在目标的文件是：

1. `docs/company-readiness/system-issues.md`
2. `docs/company-readiness/remediation-plan.md`
3. `docs/superpowers/reviews/2026-05-26-system-issues-remediation.md`
4. `docs/superpowers/reviews/2026-05-26-system-hardening-plan.md`
5. `docs/superpowers/specs/2026-05-10-ai-finance-platform-design.md`

只作历史参考的文件是：

1. `docs/superpowers/plans/2026-05-10-ai-finance-platform-implementation-plan.md`

不建议继续新增内容到 `docs/superpowers/plans`。后续整改统一写入 `docs/company-readiness` 或拆成正式产品/API/部署/数据/发布文档。

## 6. 已吸收进主线的内容

以下旧文档内容已经被吸收到 `company-readiness` 主线：

- `2026-05-26-system-issues-remediation.md` 中的构建失败、AI 契约漂移、固定 periodId、异常治理、CORS、Maven Wrapper、Docker 质量门禁问题。
- `2026-05-26-system-hardening-plan.md` 中的后端目标模块、前端 feature-based 结构、本地/CI/Staging/Production 分层。
- `2026-05-10-ai-finance-platform-design.md` 中的产品定位、MVP 范围、目标用户、数据表、指标口径、AI 能力边界。

后续如果继续整理，优先把旧设计文档拆成：

- `docs/product.md`
- `docs/api.md`
- `docs/data-dictionary.md`
- `docs/deployment.md`
- `docs/release-runbook.md`
