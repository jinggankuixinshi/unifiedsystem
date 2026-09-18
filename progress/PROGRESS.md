# UnifiedSystem 工作进度记录

- 日期：2026-09-16
- 范围：项目清理、SQL 初始化修复、登录问题修复、系统完成度分析
- 相关文档：`ARCHITECTURE.md`（架构设计）、`docs/sql/`（数据库脚本）

---

## 一、本次完成内容

### 1. 清理与项目无关的部署文件和脚本 ✅

| 操作 | 对象 | 说明 |
|---|---|---|
| 已删除 | `.progress/`（整个目录） | 旧 AI 会话快照（路径为 Linux 环境 `/home/jinggankuixinshi/...`），含 `start-v2.sh` Linux 一键部署脚本，与当前 Windows 项目无关 |
| 已删除 | 根目录 `package-lock.json` | 92 字节空 lockfile，前后端各自已有锁文件 |
| 已删除 | 根目录 `start.bat` | Windows 启动脚本，已按需求剔除 |
| 保留 | `UnifiedSystemFrontend/.npmrc` | npmmirror 镜像配置 |

当前根目录仅保留：`ARCHITECTURE.md`、`docs/`、`skills/`、`UnifiedSystemBackend/`、`UnifiedSystemFrontend/`。

### 2. SQL 初始化脚本字符集问题修复 ✅

**问题定位：**
- `01_unified_system_db.sql` 带 UTF-8 BOM（`EF BB BF`），是 6 个脚本中唯一带 BOM 的 → `mysql < 01.sql` 时 BOM 拼进首条 `DROP DATABASE` → `ERROR 1064`，且旧 `start.bat` 用 `2>nul` 吞错导致静默失败
- `00_create_users.sql` 含中文但缺少 `SET NAMES utf8mb4`（其余 5 个脚本均有）
- 初始化命令未显式指定客户端字符集

**修改：**

| 文件 | 修改 |
|---|---|
| `docs/sql/01_unified_system_db.sql` | 字节级剥离 BOM（34741 → 34738 字节） |
| `docs/sql/00_create_users.sql:1` | 首行插入 `SET NAMES utf8mb4;`（在任何非 ASCII 内容之前） |
| `docs/sql/00_create_users.sql` 头部命令 | 两条 mysql 命令补充 `--default-character-set=utf8mb4` |
| `ARCHITECTURE.md`（初始化顺序） | `mysql` 命令补充 `--default-character-set=utf8mb4` |

**验证结果：** 6 个 SQL 文件全部 UTF-8 无 BOM、严格 UTF-8 校验通过。

### 3. 登录失败（BCrypt）问题修复 ✅

**问题定位：**
- 控制台日志：`WARN ... BCryptPasswordEncoder: Encoded password does not look like BCrypt` + 业务异常 2001
- 根因：种子密码 `.ABpBGjDkDJVFVpmIUfGHdIZyHqXZnbq` 仅 32 字符，不符合 BCrypt 格式（应为 `$2a$10$` + 22 位盐 + 31 位哈希 = 60 字符）
- 链路：`SysUserServiceImpl.java:52` → `passwordEncoder.matches()`；`SecurityConfig.java:53` → `new BCryptPasswordEncoder()`

**修改方案（按需求确认）：** 保持角色区分，密码规则 = **账号 + 123**，用项目同款 Spring Security 6.3.4（对应 Boot 3.3.5）的 `BCrypt.hashpw(pw, gensalt(10))` 生成哈希。

**修改：** `docs/sql/01_unified_system_db.sql:81-99` —— 12 条种子密码全部替换为合法 BCrypt 哈希，注释同步为「账号+123」。

**验证结果：** 从 SQL 文件提取 12 条哈希 → `BCrypt.checkpw(账号+123, 哈希)` → **12/12 ALL_OK**。

**初始密码对照表：**

| 账号 | 密码 | 账号 | 密码 |
|---|---|---|---|
| admin | admin123 | logimanager | logimanager123 |
| boss | boss123 | sales | sales123 |
| prodmanager | prodmanager123 | salesmanager | salesmanager123 |
| prodworker | prodworker123 | finance | finance123 |
| warehouse | warehouse123 | finmanager | finmanager123 |
| logiop | logiop123 | cashier | cashier123 |

**存量库修复（待执行，共 12 条）：**

```sql
USE unified_system_db;
UPDATE sys_user SET password='$2a$10$l01cW8oc6mtprUNgeV7gZuz3MjYR4yDV47/l6KIxIWVgocCv3k42y' WHERE username='admin';
UPDATE sys_user SET password='$2a$10$t79wTdPdGD3z3lQRC.c58O0DnI7Fkq8auUlqe9Y7q25R8pTGW5NrK' WHERE username='boss';
UPDATE sys_user SET password='$2a$10$65waHHbHcuBH5dQK6eWbueEzlaMiyBrinv4uCenGEUFtJo3Z.V3ne' WHERE username='prodmanager';
UPDATE sys_user SET password='$2a$10$PUiWn8xYF98Ufp8f/5siEu2.ENkkvCEdOz09BRv3.46W1XiMd2cSO' WHERE username='prodworker';
UPDATE sys_user SET password='$2a$10$/5M8H3H.McjAulXudJ4B0.tkPaJKfSeheVBTNKb3VNMK9A2E43gey' WHERE username='warehouse';
UPDATE sys_user SET password='$2a$10$QhWgB6t8OvR/cRYYYNtjeOo899kHIWu5bBUHajnom0W8hW0fpCvaq' WHERE username='logiop';
UPDATE sys_user SET password='$2a$10$xLhcv3nwc1Kag13CyXTDWeREYwF3VTMZbtSeO8UTGzy2NoWEShZ4.' WHERE username='logimanager';
UPDATE sys_user SET password='$2a$10$hFar4p.8MH.s63MweAjTI.fpQWugDp.VxvN7n1/P.8NtcCbas5sp.' WHERE username='sales';
UPDATE sys_user SET password='$2a$10$JwLoEDJAsHLEBEaweBu3SelJ0nPaxrJaqkare9ywKhEmQBOECYEX.' WHERE username='salesmanager';
UPDATE sys_user SET password='$2a$10$HfkwSlsJiQnqhaTa1F/R7.5bZZTwSr9xIx3n2U/OI07wy.GZ15YN2' WHERE username='finance';
UPDATE sys_user SET password='$2a$10$BPHUcvnK72ToBLfQJvgrg.Ms4yQ90VYdx6FclndM/N/PYMaR1G5Eu' WHERE username='finmanager';
UPDATE sys_user SET password='$2a$10$GEP6wVDdrnbZx47tCkzQFujcjmhwS2i2yltu0ouG9J0/NBtpmuch2' WHERE username='cashier';
```

---

## 二、系统完成度分析（2026-09-16）

### 2.1 模块完成度总览

| 层 | 模块 | 完成度 | 状态定性 |
|---|---|---:|---|
| 后端 | unified-common（基础设施） | 70% | 框架件可用，工作流引擎为简化版 |
| 后端 | unified-system（系统管理） | 70% | RBAC 真实可用，考勤/消息/审计断链 |
| 后端 | unified-production（生产） | 65% | 核心逻辑真实，状态机/AQL 缺失 |
| 后端 | unified-logistics（物流） | 30% | 只有建单骨架，存在致命 bug |
| 后端 | unified-sales（销售） | 45% | 客户可用，合同/价格/售后缺失 |
| 后端 | unified-finance（财务） | 40% | 凭证/应收应付半成品，报销明细丢失 |
| 后端 | unified-report（报表） | 5% | 仅桩代码，且未纳入构建 |
| 前端 | 整体（35 个页面） | 60% | UI 完成度高，约 10 个页面后端接口落空 |
| **合计** | **系统整体** | **约 55%** | **管理后台可用、业务闭环未打通** |

### 2.2 整体流式控制（审批工作流）完成度：约 30%

**引擎能力矩阵（`WorkflowEngine.java`，146 行，4 个公开方法）：**

| 能力 | 状态 |
|---|---|
| 发起实例、按 node_order 线性推进 | ✅ 实现 |
| 通过/驳回（终态）、推送上级 | ✅ 实现 |
| 条件分支（AMOUNT_RANGE/PERCENTAGE） | ❌ conditionType/conditionConfig 全项目 0 读取，SQL 预置的采购 4 档/销售 4 档/调拨 3 档/报销 3 档条件节点被当作单一线性链推进 |
| 审批人解析（DEPT_MANAGER/ROLE/SPECIFIC_USER/POSITION） | ❌ 无解析代码 |
| 审批人权限校验 | ❌ 仅 hasAnyRole('admin','boss') |
| 委托 DELEGATE / 撤销 CANCELLED | ❌ 无逻辑/无端点 |
| 审批结果回写业务单据（approvalStatus） | ❌ 全系统无置 1/2 的代码 |
| 审批通知推送（WebSocket/站内信） | ❌ 断链 |
| 并发防重/原子推进 | ❌ 无 |

**各业务审批链集成状态：**

| 审批链 | 发起 | 流转 | 回写 | 后续流程 | 综合 |
|---|---|---|---|---|---|
| 采购申请（4 档金额） | ✅ | ⚠️ 线性错走 | ❌ | ❌ | 30% |
| 销售报单（价格 4 档） | ✅ | ⚠️ 线性错走 | ❌ | ❌ | 30% |
| 调拨（3 档金额） | ✅ | ⚠️ 线性错走 | ❌ | ❌ | 25% |
| 费用报销（3 档金额） | ✅ | ⚠️ 线性错走 | ❌ | ❌ | 25% |
| 请假/加班 | ❌ 模板已预置但代码不调用 | — | — | — | 5% |
| 合同签署 | ❌ | — | — | — | 0% |

**闭环断点链：**
```
建单 ✅ → 发起审批 ✅ → 线性流转 ⚠️(条件分支失效) → 审批操作 ✅
  → 回写业务状态 ❌ → 触发后续流程(入库/发货/打款) ❌ → 站内通知 ❌ → 审计留痕 ❌
```

### 2.3 端到端业务流程（对照 ARCHITECTURE.md 4.1~4.5）

| 流程 | 完成度 | 断点 |
|---|---:|---|
| 4.1 生产入库 | 50% | 完工不自动生成入库；二次质检 AQL 未实现 |
| 4.2 采购入库 | 40% | 审批闭环缺失（永远待审）；质检后入库需手工操作 |
| 4.3 发货出库 | 15% | 拣货 FIFO 无实现；四联单无实现；物流仓出库硬编码 bug |
| 4.4 合同生成与签署 | 10% | 生成方法孤儿；电子章/PDF/e签宝全未实现 |
| 4.5 考勤与薪资 | 35% | 打卡无规则判定；请假/加班审批断链；社保公积金手工输入 |

### 2.4 关键阻塞项 Top 7（按影响排序）

1. **工作流条件分支与审批人解析未实现** → 所有分级审批失效（`unified-common/.../workflow/WorkflowEngine.java:69-113`）
2. **审批结果不回写业务单据** → 采购/调拨/销售/报销全链路停在"待审批"
3. **物流仓出库硬编码 `productId=0`** → 出库功能实际不可用（`unified-logistics/.../service/LogisticsWarehouseService.java:70`）
4. **分页插件缺失** → 全系统分页查询失效（`unified-common/.../config/MybatisPlusConfig.java` 无 PaginationInnerInterceptor）
5. **报销明细在接口层被丢弃** → 报销金额恒 0（`unified-finance/.../controller/ExpenseController.java:37` 传 `List.of()`）
6. **销售订单创建接口双 `@RequestBody` + 前端 items 走 query** → 单据创建不可用
7. **销售合同/价格/售后、财务报表前后端落空** → 6 个页面"看起来完整但跑不通"

---

## 三、待办 / 下一步

> 注：本节为首轮（2026-09-16 白天）结论，已被文末第八节的二轮路线图取代，仅作历史记录。

| 优先级 | 事项 | 备注 |
|---|---|---|
| P0 | 存量库执行上述 12 条 UPDATE（用户自行执行） | 执行后可用 admin/admin123 登录 |
| P0 | 补 MybatisPlusConfig 分页拦截器 | 一处修复，全系统列表分页恢复 |
| P0 | 修复物流仓出库硬编码 productId=0 | `LogisticsWarehouseService.java:70` |
| P1 | WorkflowEngine 补条件分支（金额区间/百分比）与审批人解析 | 需同时对齐 SQL 模板的 condition_config JSON 字段名 |
| P1 | 审批结果回写业务单据（approvalStatus）+ 站内通知 | 打通审批闭环 |
| P1 | 修复报销明细丢弃、销售订单创建接口签名 | |
| P2 | 补合同/价格/售后后端、财务报表模块 | 前后端接口对齐 |
| P2 | 考勤接入工作流、打卡规则判定 | |
| P2 | 前端看板接真实统计接口；Electron 打包链路修复 | |

---

## 四、验证方法备忘

- SQL 文件编码检查（PowerShell）：
  ```powershell
  $b = [System.IO.File]::ReadAllBytes('docs\sql\01_unified_system_db.sql')
  "BOM=$($b[0] -eq 0xEF -and $b[1] -eq 0xBB -and $b[2] -eq 0xBF)"
  ```
- BCrypt 哈希校验（与后端同版本 6.3.4）：
  ```powershell
  java -cp "$env:USERPROFILE\.m2\repository\org\springframework\security\spring-security-crypto\6.3.4\spring-security-crypto-6.3.4.jar" CheckHashes.java <含 user|password|hash 行(UTF-8)的文件>
  ```
- 登录验证：启动前端后使用 `admin / admin123`，控制台不应再出现 `Encoded password does not look like BCrypt`。

---

# 第二轮全量审计（2026-09-16 晚）

## 五、增量完成：部门 / 角色 / 权限（资源）拖拽排序 ✅

首轮指出 sortOrder 是"同级显示顺序"而非层级数字；本轮已将三页从"弹窗手填排序号"升级为拖拽排序。

**后端（unified-system）：**

| 变更 | 位置 |
|---|---|
| 新增 `SortItemDTO{id, sortOrder}` | `dto/SortItemDTO.java` |
| 新增 `PUT /api/system/departments/sort`、`/roles/sort`、`/resources/sort` | 三个 Controller |
| `sortDepts / sortRoles / sortResources`：校验 id 存在、部门/资源限定同一父级、逐条更新 sortOrder | 三个 ServiceImpl |
| 创建时自动 `nextSortOrder()`（同级 max+1）；部门/资源改父级时自动排到新父级末尾 | DepartmentServiceImpl:65,129-136；ResourceServiceImpl:67,84,132-139 |
| 树查询补 `orderByAsc(id)` 稳定排序 | 同上 |

**前端：**

| 变更 | 位置 |
|---|---|
| 新增 SortableJS 封装（拖拽手柄、同父级限制、无变化不提交） | `src/utils/dragSort.ts` |
| 三页新增拖拽手柄列；部门：展开子级的节点禁拖；角色：有搜索关键字时禁拖 | department/role/resource Index.vue |
| 弹窗移除手填"排序"输入，拖拽后整组重写 `sortOrder = index + 1` 并保存 | 同上 |
| `sortablejs@^1.15.7` 依赖已声明 | package.json |

**验证：** 后端 `mvn compile` ✅ 通过；前端本次新增代码无 TS 报错（存量 47 个 TS 错误见第六节，与本次改动无关）。

## 六、第二轮完成度矩阵（对照首轮修订）

| 层 | 模块 | 首轮 | 二轮 | 修订说明 |
|---|---|---:|---:|---|
| 后端 | unified-common | 70% | 70% | 工作流仍为线性简化版；分页拦截器仍缺失；Redis/Caffeine 建而不用 |
| 后端 | unified-system | 70% | 72% | 拖拽排序补齐；用户/字典 CRUD 完整；考勤/消息/审计仍断链 |
| 后端 | unified-production | 65% | 58% | 深审下修：质检 AQL 仅记录桩；计划/排期/工单状态机不完整；入库流水 balance_after=0；出库明细 warehouseId 语义错位 |
| 后端 | unified-logistics | 30% | 30% | productId=0 硬编码仍在；拣货空壳；库存流水表零写入 |
| 后端 | unified-sales | 45% | 28% | 深审下修：报单入口双 @RequestBody 不可用；价格/合同/售后无 Controller；合同生成零调用 |
| 后端 | unified-finance | 40% | 40% | 报销金额恒 0；应收付结清状态错误；预算执行死代码；3 张表零代码 |
| 后端 | unified-report | 5% | 5% | 桩代码，未纳入父构建 |
| 前端 | 35 个页面 | 60% | 55% | 拖拽排序 +；但 `npm run build` 因 47 个 TS 错误失败；18 处调用不存在的后端接口 |
| **合计** | **系统整体** | **~55%** | **~55%** | **管理后台可用；业务闭环未打通，最大瓶颈仍是"审批只进不出"** |

## 七、二轮新发现问题（相对首轮新增）

### P0（新发现）

| # | 问题 | 证据 |
|---|---|---|
| 1 | **前端 query 参数被系统性丢弃**：`request.get(url, {pageNum, pageSize, keyword})` 把查询对象当成 axios config，第二参必须是 `{ params: {...} }`。分页/搜索/筛选静默失效，且造成 47 个 TS2353 错误使 `npm run build` 失败 | 14+ 文件：user:134、production/purchase:128、logistics/warehouse:146、sales/order:136、finance/expense:94…（role:152 已修复可参照） |
| 2 | **前端调用后端不存在的接口 18 处**（点了必报错）：销售 合同/价格/售后 全部（无 Controller）、`PUT/DELETE /finance/accounts/{id}`、`PUT /finance/budgets/{id}`、考勤 `PUT .../approve` ×2、`GET /logistics/pickings`（后端只有 POST）、调拨 `PUT .../receive`（后端是 `POST .../sign`） | contract/Index.vue:62,73,77；price:93,121,101,133；aftersale:77,89,96；account:199,212；budget:116；attendance:309,317；picking:86；transfer:159 |
| 3 | **前后端契约不匹配 4 组**：销售订单（双 `@RequestBody` + items 走 query）、凭证（前端 body vs 后端 `@RequestParam summary`）、应收/应付收付款（body vs query）、物流调拨/发货明细（body vs `@RequestParam`）。均 400 | SalesOrderController:39-40；VoucherController:41-42；Receivable/PayableController:37-41；TransferController:42；PickingShippingController:51 |
| 4 | **finance 全部 Service 缺类级 `@DS("finance")`**（Mapper 上有，但事务在 Service 开启、dynamic-datasource 按事务绑定数据源），事务型写操作可能落到 system 库 | ExpenseService:28、VoucherService:26、ReceivableService:27、PayableService:27、BudgetService:23、SalaryService:24 |
| 5 | **request.ts 去重缺陷**：key 只含 url+method+params 对象引用（不含实际值/body），同 URL 的并发请求互相 abort；取消/异常路径不清理 pendingMap 造成 map 泄漏；与各页 catch 叠加出现双 toast | request.ts:19,32-34,48-60 |

### P1（首轮已提，二轮确认仍未修复）

分页拦截器缺失（全系统 `page()` 返回全量且 total=0）；审批通过不回写业务单据（4 业务 `approvalStatus` 永为 0）；WorkflowEngine 忽略条件分支/审批人解析；物流 `productId=0`（LogisticsWarehouseService.java:70）；报销明细被 `List.of()` 丢弃（ExpenseController.java:37）；考勤审批端点缺失且未接工作流；`@Auditable` 零使用导致审计空表；`sendMessage` 无调用方导致消息/推送死链；SequenceGenerator 纯内存计数（重启/多实例重号）；登录安全缺口（401 语义、禁用用户旧 token 可用、logout 不清理 latestToken）。

### 结构性风险（新增确认）

- **跨库事务**：production 等模块类级 `@DS + @Transactional` 内调用 WorkflowEngine（wf_* 表在 system 库），无 `@DSTransactional`，无跨库原子性。
- **Electron 打包链断裂**：`main` 指向不存在的 `electron/main.js`（TS 未编译）、`public/icon.ico` 缺失、vite 未设 `base: './'`、打印 IPC 忽略入参且无调用方、四联单未实现。
- **Dashboard 全部写死数据**，无接口调用。
- **权限码体系未接线**：登录返回不含 permissions、前端无权限指令/按钮控制、菜单静态、管理员菜单对所有人可见。

## 八、下一步最优开发方向与计划（二轮修订）

**判断依据：** 当前两个"验收基础"问题是红的（前端 build 失败 + 全系统分页失效），必须先修；随后最高杠杆是打通"审批回写"（一条修复解锁采购/销售/调拨/报销/考勤 5 条链）；再按业务价值排序闭环销售链与采购链。

### Phase 0 — 恢复可验收状态（约 0.5~1 天）
1. 前端 14+ 处查询参数改为 `{ params: {...} }`，`npm run build` 转绿（47 个 TS 错误清零）
2. `MybatisPlusConfig` 补 `PaginationInnerInterceptor`（1 个文件恢复全系统分页）
3. `request.ts` 去重逻辑修复（key 纳入实际参数值；错误/取消路径清理 pendingMap）
4. 约定并统一：axios 二次参数一律 `{ params }`（写入 AGENTS/规范）

**验收：** 前后端构建全绿；所有列表页分页、搜索、筛选可用。

### Phase 1 — 审批闭环（最高杠杆，约 2~3 天）
1. WorkflowEngine：条件分支求值（AMOUNT_RANGE / PERCENTAGE）、同模板多 node_order 分支筛选、审批人解析（DEPT_MANAGER/ROLE/SPECIFIC_USER）与权限校验、DELEGATE、终态幂等
2. 回写机制：定义 `ApprovalCallback` 接口（或 Spring 事件），purchase_request / sales_order / transfer / expense / leave / overtime 各自实现 → 通过/驳回回写 `approvalStatus`，并调用 `SysMessageService.sendMessage` 推送站内信 + WebSocket
3. 修复契约类 P0：报销明细透传、销售订单 DTO、物流调拨/发货 DTO 化
4. 考勤请假/加班接入 startWorkflow；处理跨库事务（`@DSTransactional` 或去外层事务）

**验收：** 四类单据按金额分档流转，逐级审批/驳回后业务状态与通知正确。

### Phase 2 — 打通销售链与采购链（约 4~6 天）
- **销售链**：报单 → 价格异常分级（含特批理由）→ 审批 → 合同生成/PDF → 发货 → FIFO 拣货 → 出库扣减 → 应收生成（补 Contract/Price/Aftersale Controller）
- **采购链**：审批 → 到货质检 → 入库单 → 库存与流水（补出入库/流水查询接口）
- **物流链**：调拨签收闭环（更新状态 + 两仓库存移动 + 流水）

**验收：** 两条主链可端到端演示，库存与应收/应付台账同步正确。

### Phase 3 — 财务与考勤薪资闭环（约 4~6 天）
- 应收/应付：结清状态机（1 部分/2 结清/3 逾期）、超额校验、核销流水查询
- 凭证：审核/过账、日期透传、账户余额联动；资金日报
- 报销：审批回写 → 预算执行累计 → 出款 → 凭证
- 考勤：att_schedule 规则判定（迟到/早退/缺勤）、月末汇总 → 工资计算（社保公积金配置化）

### Phase 4 — 看板 / 报表 / 桌面端（约 2~3 天）
- Dashboard 接真实统计接口；unified-report 纳入构建（或轻量统计 API 替代）
- Electron：main/preload 编译产物、图标、`base:'./'`、四联单打印模板

### Phase 5 — 加固与清理（持续）
- 审计切面接入（注意参数脱敏，勿记密码）、SequenceGenerator 改 DB 序列、WebSocket 会话管理、登录安全（401/禁用/登出）、前端权限码接线（菜单+按钮）、死代码与未用依赖清理（xlsx、api/modules 空壳）、补测试

**暂缓项：** e签宝真实对接、JimuReport 完整嵌入、AQL 完整抽样表（先用阈值判定）、万级并发压测。

---

# Phase 0 执行记录与运行时验证（2026-09-16 深夜）

## 九、Phase 0 全部完成 ✅

| 项 | 内容 | 结果 |
|---|---|---|
| 问题 1 | 前端 33 处 query 参数改为 `{ params: {...} }`（含 api/modules 全部函数）；修复 TS7053×13、TS7006、TS7031；删除无引用 `utils/composables.ts` | `npm run typecheck` 0 错误、`npm run build` 通过 |
| 问题 1 关联 | 字典管理前后端契约：接口返回 List，前端原按 `records/total` 读取导致列表恒空 → 改为按 List 渲染并本地分页 | 字典类型/数据可正常显示 |
| 问题 2 | 前端全面隐藏：菜单+路由移除 销售价格/合同/售后、拣货管理、财务报表 5 页（页面文件保留）；隐藏 账户编辑/删除、预算编辑、考勤审批按钮；调拨签收由 `PUT /receive` 改调已有 `POST /{id}/sign` | 无 404/405 入口 |
| 问题 3 | 契约统一单 @RequestBody DTO：新增 `SalesOrderCreateDTO`、`VoucherCreateDTO`、`PaymentDTO`、`TransferCreateDTO`、`ShippingCreateDTO`；凭证日期改为透传；补 sales/transfer 创建路径空值兜底 | 4 组接口实测 200 |
| 问题 4 | `@DS` 补齐：finance 6 个 Service、logistics 4 个、sales 2 个（Contract/SalesOrder）；**WorkflowEngine 增加 `@DS("system")`**（关键：否则类级 @DS 会让工作流查询落到业务库）；4 处创建方法（采购/报单/调拨/报销）改 `@DSTransactional` | 跨库写入实测正确 |
| 问题 5 | request.ts：去重 key 纳入实际 params/body、同 key 才 abort、成功/失败统一清理（取消不误删）；错误提示统一由拦截器负责，清除约 60 处 view 重复 catch 提示 | 构建通过，无重复 toast |
| 附加 | `MybatisPlusConfig` 补 `PaginationInnerInterceptor` + `unified-common/pom.xml` 增 `mybatis-plus-jsqlparser`（MP 3.5.9+ 分页拦截器独立包） | 分页实测 total=12、records=5 |
| 附加 | `mvn compile` 全模块通过 | ✅ |

**验证说明：** 以新代码启动后端（admin/admin123）实测：
- 分页：`GET /system/users?pageNum=1&pageSize=5` → total=12、records=5 ✅
- 凭证：`POST /finance/vouchers`（body DTO）→ VCH 创建成功、`voucherDate=2026-09-01` 透传生效 ✅
- 调拨：`POST /logistics/transfers` → TR 单成功、totalValue=100、wf_instance(pending) ✅
- 报单：`POST /sales/orders` → SO 单成功、totalAmount=200、wf_instance(pending) ✅
- 采购：`POST /production/purchase` → PR 单成功、total=20、wf_instance(pending) ✅
- 应收：创建 + `POST /finance/receivables/{id}/payment`（body DTO）→ received=200 ✅
- 跨库：业务单落各自库（logistics/sales/finance/production），wf_instance 落 system 库；`@DSTransactional` 未出现回滚异常 ✅

## 十、执行中新发现（未修，纳入后续 Phase）

| 级别 | 问题 | 处理建议 |
|---|---|---|
| P1 | `SequenceGenerator` 重启重号被实锤：重启后凭证重发 `VCH20260916000001` 撞唯一键 → 500 | Phase 5 改 DB 序列（`sys_sequence` 表）或 Redis |
| P1 | `fin_receivable.sales_order_id` NOT NULL，但前端表单销售单 ID 可空 → 传空必 500 | Phase 1 前端必填校验或后端允许为空 |
| P2 | `prod_material` 无种子数据，物料下拉为空（本地库） | 补种子 SQL 或前端先建物料 |
| P2 | 生产仓库页 `POST /production/warehouse/inbound|outbound` 的 items 走 query 参数，后端结构待对齐（同 4 组契约问题族） | Phase 1/2 一并改 body DTO |
| 备查 | 本地 Redis 5.0.14 无 ACL（文档按 Redis 7 三账户设计）；当前运行链路未用 Redis，不受影响 | 启用锁/序列号时再适配 |

**下一步：** 进入 Phase 1（审批闭环：工作流条件分支 + 审批人解析 + ApprovalCallback 回写 + 站内通知；报销明细透传、销售订单特批理由、考勤接工作流）。

---

# Phase 1 执行记录（审批闭环，已编码完成，待联调验收）

## 十一、Phase 1 交付清单

### 数据模型（`docs/sql/06_phase1_upgrade.sql` 存量库增量 + `01/04` 主脚本同步）
| 变更 | 说明 |
|---|---|
| `wf_node_template.branch_no` | 分支归属字段，6 个模板种子全部重写（采购4链/销售4链/调拨3链/报销3链/请假1/加班1） |
| `wf_instance.branch_no / applicant_id / delegate_user_id / delegate_node_order` | 实例锁定分支、发起人（待办过滤）、委托状态 |
| `sys_department.leader_id` | 部门负责人用户ID（DEPT_MANAGER 解析），种子指向 prodmanager/logimanager/salesmanager/finmanager |
| `sal_sales_order.low_price_reason` | 低价特批理由（中度及以上异常强制必填） |

### 引擎（`unified-common`）
- `WorkflowEngine` 重写：启动时按 metrics（amount/ratio/type）匹配分支并锁定节点链；`PUSH_UP`=直达分支终审；委托/撤销；条件更新防并发；事件发布
- 条件匹配：`{"min":..,"max":..,"types":[..]}`（min 含 max 不含；types 支持调拨类型分支）
- 新增 `WorkflowApproverResolver` 接口（system 实现：ROLE/SPECIFIC_USER/DEPT_MANAGER/admin 兜底）、`WorkflowEvent` 事件
- `SequenceGenerator` 改 `sys_sequence` 表原子序列（静态 API 不变，重启/多实例不重号）

### 回写与通知
- 审批结果监听（AFTER_COMMIT + @DS）回写：采购(production)/调拨(logistics)/报单(sales)/报销(finance)/请假加班(system) 的 `approvalStatus`
- `WorkflowNotificationListener`：待办提醒（当前节点审批人）、结果通知（申请人），落站内信 + WebSocket

### 业务配套与前端
- 创建单据传指标：采购(amount)/报单(worstRatio)/调拨(amount+type)/报销(amount)
- 报销明细透传（`ExpenseCreateDTO`）；报销列表 status 过滤
- 销售报单低价理由字段 + 后端强制校验；考勤请假/加班接入工作流
- 前端审批台：待我审批/我的已办/我的申请 + 上推/委托/撤销 + 审批时间线；部门负责人改用户选择

### 验收动作（必需）
1. 执行增量 SQL：`mysql --default-character-set=utf8mb4 -u unified_dev -p < docs/sql/06_phase1_upgrade.sql`
2. 重启后端（IDEA 重新 Run）
3. 逐档实测：采购 <5000 / 5000-50000 / >200000；报销 <5000 / >20000；调拨常规+特殊；请假/加班；验证：分支正确、approvalStatus 回写、待办按人可见、站内信到达、重启不重号

---

# Phase 1.5 执行记录（角色等级 + 人事审批，已编码完成，待联调验收）

## 十二、Phase 1.5 交付清单

### 数据模型（`docs/sql/07_phase15_upgrade.sql` 存量库增量 + `01` 主脚本同步）
| 变更 | 说明 |
|---|---|
| `sys_role.level` | 等级：总经理100 / 系统管理员90 / 人事高管80 / 部门高管60 / 主管40(预留) / 专员30 / 员工20 |
| 人事建制 | 人事部（dept 6）、hr_manager(80)、hr_staff(30)、hrmanager/hrstaff 用户（密码=账号+123）、资源授权 |
| `wf_node_template.node_level` | 节点审批层级（上推判定）；并加唯一索引 (template_id, branch_no, node_order) |
| 请假/加班模板重写 | 3 分支：普通(<60)→部门最高管+人事专员；部门高管[60,80)→人事高管+总经理；≥80→总经理 |
| 模板 7 `hr_account_op` | 人事账号操作审批（人事专员→人事高管；人事高管→总经理） |
| 新表 `hr_account_op` | 操作单：op_type(register/enable/disable/delete)、payload、approval_status、executed |

### 引擎扩展（common，全部 SPI 化，符合"新增业务不改引擎"目标）
- **条件评估 SPI**：`WorkflowConditionEvaluator` + 内置 5 个评估器（NONE/AMOUNT_RANGE/PERCENTAGE/TYPE/APPLICANT_LEVEL），新增维度=业务模块加 Bean
- **审批人策略 SPI**：`WorkflowApproverStrategy` + 实现（ROLE/SPECIFIC_USER/DEPT_MANAGER/**DEPT_TOP 部门最高管**/**ROLE_LEVEL_UP 等级逐级上推**）
- **自审自动跳过**：当前节点唯一审批人=申请人时逐节点跳过（记录 wf_record=skip），链尾跳过则自动通过
- **上推改按等级**：沿链找"节点层级 > 当前节点层级"的第一个节点；无 node_level 时按审批人解析兜底
- **通知名称 DB 回退**：通知/告警名称优先模板名（wf_template.template_name），新增业务无需改通知代码
- **空审批人告警**：节点解析不到审批人 → 记录 ERROR + 通知 admin 兜底

### 人事账号操作业务
- `POST /api/hr/account-ops`：admin 直接执行；hr_manager/hr_staff 提交审批单（自动 startWorkflow）
- 审批通过 → `HrAccountOpApprovalListener` 自动执行（注册=创建账号/初始密码账号+123；启用/禁用/删除=改用户状态或逻辑删除），失败标记 executed=-1
- 前端：用户管理页按角色分流（admin 直管；hr 显示"账号操作"弹窗提交）；角色管理加"等级"字段；user-info 返回角色

### SQL 脚本规整（2026-09-18）
- `01_unified_system_db.sql` 已升级为**全量初始化脚本 v1.2**（含 06/07 增量全部内容，附版本头与执行说明），新环境执行 `00 -> 01->05` 即可
- `06/07` 增量脚本保留，仅用于旧库原地升级（头部已注明"内容已合入全量脚本"）
- 一致性校验：01 与 07 的 45 条节点种子逐行一致；全部脚本 UTF-8 无 BOM

### 代码内 SQL 清零（2026-09-18）
- 原则：所有 SQL 一律放 `resources/mapper/*.xml`，代码只用参数化方法/Wrapper，禁止字符串拼接 SQL
- 改造：
  - `@Select` 注解 SQL → `unified-common/src/main/resources/mapper/SysSequenceMapper.xml`（序列行锁查询）
  - `setSql("quantity = quantity - " + deduct)` → `unified-production/src/main/resources/mapper/ProdWarehouseMapper.xml#deductStock`（参数化原子扣减）
- 扫描结果：`@Select/@Update/@Insert/.setSql/.last` 全项目为 0；业务查询统一走 MyBatis-Plus 参数化 Wrapper

### 依赖替代自研 — Phase 1.6 批次一（2026-09-18）
| 项 | 依赖 | 替代/收益 | 验证 |
|---|---|---|---|
| API 文档 | springdoc-openapi 2.6.0 | 在线文档 `/swagger-ui.html`（SecurityConfig 放行） | 编译通过 |
| 健康/指标 | spring-boot-starter-actuator | `/actuator/health·info·metrics` | 编译通过 |
| 对象映射 | MapStruct 1.6.3（含 lombok-binding 注解处理器） | 试点 `SysUserConverter`，替换 `BeanUtils.copyProperties`/手写 toVO，编译期生成杜绝字段错配 | 编译通过（生成 Impl） |
| 加解密 | Spring Security Crypto `Encryptors.delux`（AES-256/GCM） | 重写 `AesUtil`，删除手写 JCE 代码；手机号脱敏改用 Hutool `DesensitizedUtil` | 编译通过 |
| 单元测试 | spring-boot-starter-test（parent 全局引入） | 条件评估器单测 8 个（min 含/max 不含/类型过滤/层级分支） | **8/8 通过** |

**本批未执行（原因）**：
- Flyway：5 库迁移格式需重写（去 DROP/USE/GRANT）+ 存量库 baseline，风险高，单独立项
- mzt-biz-log 审计 / MP 数据权限 / EasyExcel 导出 / @Scheduled / Redisson：分别在 Phase 2/3 有业务落点后再引入

### 验收动作（必需）
1. 执行增量 SQL：`mysql --default-character-set=utf8mb4 -u unified_dev -p < docs/sql/07_phase15_upgrade.sql`
2. 重启后端（IDEA 重新 Run）
3. 实测：
   - 账号流：hrstaff/hrstaff123 提交"注册" → hrmanager 审批通过 → 新账号可登录（账号+123）
   - 请假矩阵：prodworker 请假 → prodmanager → hrstaff；prodmanager 请假 → hrmanager → boss
   - 自审跳过：boss 提交请假 → 自动通过（记录 skip）
   - 上推：报销链中财务专员上推 → 财务经理（按等级推进而非直达终态）
