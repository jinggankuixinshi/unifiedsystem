# UnifiedSystem 企业管理系统 — 架构设计文档 v1.0

## 一、系统架构总览

```
┌─────────────────────────────────────────────────────────┐
│                 Electron 桌面客户端 (Vue 3 + TypeScript)     │
│  ┌──────────────────────────────────────────────────┐   │
│  │   生产模块 │ 物流模块 │ 销售模块 │ 财务模块 │ 系统管理  │   │
│  └──────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────┤
│              HTTP REST API + WebSocket 通知              │
├─────────────────────────────────────────────────────────┤
│              Spring Boot 3.3.x 后端服务                    │
│  ┌──────────┬──────────┬──────────┬──────────┐          │
│  │ 生产服务  │ 物流服务  │ 销售服务  │ 财务服务  │ 系统服务 │          │
│  └──────────┴──────────┴──────────┴──────────┘          │
│  ┌──────────────────────────────────────────────┐       │
│  │  审批工作流引擎 │ 消息通知 │ 报表引擎(JimuReport)  │       │
│  └──────────────────────────────────────────────┘       │
│  ┌──────────────────────────────────────────────┐       │
│  │  Spring Security + JWT + Caffeine 令牌管理     │       │
│  └──────────────────────────────────────────────┘       │
├─────────────────────────────────────────────────────────┤
│           MySQL 8.x + Redis (5 库多数据源)                │
│  ┌──────────┬──────────┬──────────┬──────────┬────────┐ │
│  │ system_db│ prod_db  │ logis_db │ sales_db │ fin_db │ │
│  │ (核心)   │ (生产)    │ (物流)    │ (销售)    │ (财务)  │ │
│  └──────────┴──────────┴──────────┴──────────┴────────┘ │
└─────────────────────────────────────────────────────────┘
```

| 决策点 | 选型 |
|---|---|
| 部署方式 | 纯内网，Electron 桌面客户端访问 |
| 并发能力 | 支撑 10,000 并发 |
| 多数据源 | Baomidou dynamic-datasource，5 库 |
| 认证 | Spring Security + JWT (jjwt) |
| 令牌管理 | Caffeine 本地缓存 |
| 通知 | WebSocket 实时推送 + 站内消息中心（已读/未读留痕） |
| 报表 | JimuReport 嵌入式报表引擎 |
| UI | Ant Design Vue 4.x |
| 缓存 | Redis（分布式锁、序列号生成、审批锁） |

---

## 二、数据库架构设计（5 库）

### 2.0 数据库账户策略（三账户模型）

**MySQL 账户：**

```
unified_dev (开发账户)          unified_ops (运维账户)       unified_app (应用账户)
  ALL PRIVILEGES                SELECT,INSERT,UPDATE          SELECT,INSERT,UPDATE
  建表 / 改表 / 删表             数据修正 / 手动查询           Spring Boot 运行时连接
  最多 32 连接                  最多 8 连接                   最多 16 连接
       ↓                              ↓                            ↓
┌─────────────────────────────────────────────────────────────────────┐
│  system_db  │  prod_db  │  logis_db  │  sales_db  │  fin_db       │
└─────────────────────────────────────────────────────────────────────┘
```

| 账户 | 权限 | 密码来源 | 连接上限 | 用途 |
|---|---|---|---|---|
| `unified_dev`@`localhost` | `ALL PRIVILEGES` | `DB_DEV_PWD` | 32 | 建库建表、DDL 变更 |
| `unified_ops`@`localhost` | `SELECT, INSERT, UPDATE` | `DB_OPS_PWD` | 8 | 运维数据修正、手动查询 |
| `unified_app`@`localhost` | `SELECT, INSERT, UPDATE` | `DB_APP_PWD` | 16 | Spring Boot 运行时连接 |

**Redis 账户**（Redis 7.x ACL，不需要建库建表）：

```
redis_admin (管理员)         redis_ops (运维)            redis_app (应用)
  +@all 全部命令              +@read +@write               +@read +@write
  配置修改 / ACL 管理         手动清缓存 / 查数据           Spring Boot 运行时连接
  无连接限制                  禁 flushdb/config/shutdown   禁 flushdb/config/keys/scan
```

| 账户 | 权限 | 密码来源 | 用途 |
|---|---|---|---|
| `redis_admin` | `+@all` | `REDIS_ADMIN_PWD` | ACL 管理、配置修改 |
| `redis_ops` | `+@hash +@list +@string +@set +@sortedset`，禁 `flushdb/config/shutdown` | `REDIS_OPS_PWD` | 运维清缓存、查数据 |
| `redis_app` | `+@hash +@string +@set`，禁 `flushdb/config/keys/scan/monitor` | `REDIS_APP_PWD` | Spring Boot 运行时连接 |

**MySQL + Redis 关键设计原则：**
- `ops` 和 `app` **不授予 DELETE / flushdb / config** —— MySQL 走 `@TableLogic` 逻辑删除，Redis 禁破坏性命令
- 密码全部走环境变量，**禁止**硬编码在 yml/properties/xml 中
- 全部账户锁定 `localhost`，禁止远程直连
- `root` 仅保留给 DBA 紧急维护

**初始化顺序：**
1. `mysql --default-character-set=utf8mb4 -u root -p < docs/sql/00_create_users.sql`（创建 MySQL 三账户）
2. `redis-cli < docs/redis/01_acl_setup.txt`（创建 Redis 三账户）
3. `mysql --default-character-set=utf8mb4 -u unified_dev -p < docs/sql/0*_unified_*.sql`（建表 + 授权）
4. 启动 Spring Boot（使用 `unified_app` + `redis_app` 连接）

### 2.1 unified_system_db（核心库）

| 表名 | 说明 |
|---|---|
| `sys_user` | 用户表（登录账号、密码、关联员工信息） |
| `sys_department` | 部门表（树形结构，预留 CRUD） |
| `sys_role` | 角色表（预留 CRUD） |
| `sys_user_role` | 用户-角色关联 |
| `sys_resource` | 资源权限表（菜单/按钮/API） |
| `sys_role_resource` | 角色-资源关联 |
| `sys_resource_dept` | 资源-部门可见性（部门级数据权限） |
| `sys_dict_type` | 字典类型 |
| `sys_dict_data` | 字典数据 |
| `sys_message` | 站内消息（标题、内容、发送人、接收人、已读/未读、时间） |
| `sys_audit_log` | 操作审计日志 |
| `sys_operate_log` | 登录/操作日志 |
| `sys_sequence` | 业务单号序列（入库单号、调拨单号、合同编号等） |

| 表名 | 说明 |
|---|---|
| `wf_template` | 审批流程模板（生产采购/销售报单/调拨/报销/请假/加班） |
| `wf_node_template` | 审批节点模板（审批层级、审批人类型、金额条件、跳转逻辑） |
| `wf_instance` | 审批流程实例（关联业务单据，当前节点，状态） |
| `wf_record` | 审批记录（每级审批人、操作、意见、时间） |

| 表名 | 说明 |
|---|---|
| `att_record` | 考勤记录（用户ID、登录时间、登出时间、考勤状态） |
| `att_leave` | 请假记录（类型、起止时间、时长、审批状态） |
| `att_overtime` | 加班记录（起止时间、时长、审批状态） |
| `att_schedule` | 考勤基准规则（标准上班时间、下班时间、弹性配置） |

**核心库合计：约 20 张表**

---

### 2.2 unified_production_db（生产库）

| 表名 | 说明 |
|---|---|
| `prod_product` | 产成品（编码、名称、规格、单位、基准售价、状态） |
| `prod_bom` | 物料清单（产品ID、版本号、生效日期） |
| `prod_bom_item` | BOM 明细（原料ID、用量、单位） |
| `prod_material` | 原材料/物料（编码、名称、规格、单位、安全库存） |
| `prod_production_plan` | 生产计划（计划编号、产品ID、计划数量、计划日期、状态） |
| `prod_production_schedule` | 排期表（计划ID、产线、班组/人员、开始时间、结束时间） |
| `prod_work_order` | 工单（排期ID、开工/完工时间、计划产出、实际产出、状态） |
| `prod_purchase_request` | 采购申请单（申请编号、申请人、部门、总金额、审批状态） |
| `prod_purchase_request_item` | 采购申请明细（物料ID、数量、单价、金额） |

| 表名 | 说明 |
|---|---|
| `prod_warehouse` | 生产仓库存（物料/产品ID、批次号、生产日期、数量、库位） |
| `prod_warehouse_log` | 生产仓出入库流水（单号、类型(入库/出库)、关联业务单号、数量、批次） |
| `prod_quality_check` | 质检记录（类型(原料入库/生产入库)、AQL标准、抽样数、合格数、不合格数、结果） |
| `prod_inbound` | 入库单（单号、类型(采购/生产)、供应商、质检单ID、状态） |
| `prod_inbound_item` | 入库单明细 |
| `prod_outbound` | 出库单（单号、类型(生产领料/调拨出库)、状态） |
| `prod_outbound_item` | 出库单明细 |

**生产库合计：约 16 张表**

---

### 2.3 unified_logistics_db（物流库）

| 表名 | 说明 |
|---|---|
| `log_warehouse` | 物流仓库存（产品ID、批次号、生产日期、数量、库位、入库日期） |
| `log_warehouse_log` | 物流仓出入库流水 |
| `log_transfer` | 调拨单（单号、调出仓、调入仓、类型(常规/样品/报废/返修/赠送)、总价值、审批状态） |
| `log_transfer_item` | 调拨明细 |
| `log_transfer_sign` | 调拨签收记录（签收人、签收时间、电子签名/确认） |
| `log_picking` | 拣货单（关联发货单、仓库、操作员、状态） |
| `log_picking_item` | 拣货明细（产品ID、批次、数量、库位） |
| `log_shipping` | 发货单/四联销货单（单号、销售单ID、发货方式、物流信息、打印状态） |
| `log_shipping_item` | 发货明细 |
| `log_quality_check` | 物流仓质检记录（入库质检(生产→物流仓)、AQL标准、结果） |
| `log_inbound` | 物流仓入库单 |
| `log_inbound_item` | 入库明细 |
| `log_outbound` | 物流仓出库单（发货出库） |
| `log_outbound_item` | 出库明细 |

**物流库合计：约 14 张表**

---

### 2.4 unified_sales_db（销售库）

| 表名 | 说明 |
|---|---|
| `sal_customer` | 客户（编码、名称、联系人、电话、地址、状态(正常/禁用/存档)） |
| `sal_product_price` | 产品价格历史（产品ID、售价、生效日期、来源） |
| `sal_sales_order` | 销售报单（单号、客户ID、销售员、总金额、付款方式、交货日期、质保条款、合同附件(原始)、价格异常等级、审批状态） |
| `sal_sales_order_item` | 销售报单明细（产品ID、规格、数量、单价、总价、价格偏离度标记） |
| `sal_sales_order_attachment` | 报单附件（原始合同附件、补充材料） |
| `sal_contract` | 合同（合同编号、销售单ID、合同内容JSON、电子章ID、e签宝合同ID、PDF路径、签署状态） |
| `sal_contract_sign_log` | 合同签署日志（e签宝回调记录、签署人、时间） |
| `sal_after_sale` | 售后服务记录 |
| `sal_price_anomaly_config` | 价格异常阈值配置（轻度/中度/重度百分比阈值） |

**销售库合计：约 9 张表**

---

### 2.5 unified_finance_db（财务库）

| 表名 | 说明 |
|---|---|
| `fin_account` | 账户（银行账户、现金账户、余额） |
| `fin_account_subject` | 会计科目表（编码、名称、类型(资产/负债/权益/收入/费用)、级次） |
| `fin_voucher` | 记账凭证（凭证号、日期、摘要、制单人、审核人、状态） |
| `fin_voucher_entry` | 凭证分录（科目ID、借方金额、贷方金额、摘要） |
| `fin_receivable` | 应收账款（客户ID、销售单ID、金额、已收、余额、账期、到期日） |
| `fin_receivable_log` | 收款记录 |
| `fin_payable` | 应付账款（供应商ID、采购单ID、金额、已付、余额、账期、到期日） |
| `fin_payable_log` | 付款记录 |
| `fin_expense` | 费用报销单（单号、申请人、部门、报销类型(差旅/办公/招待/其他)、总金额、预算对比、审批状态） |
| `fin_expense_item` | 报销明细 |
| `fin_budget` | 预算（部门ID、科目、年度、预算金额、已执行金额） |
| `fin_budget_log` | 预算执行记录 |
| `fin_salary` | 工资单（员工ID、月份、基本工资、绩效奖金、加班费、扣款、社保、公积金、实发、状态） |
| `fin_salary_item` | 工资项目明细 |
| `fin_salary_template` | 工资计算模板（工资项目、计算公式） |
| `fin_social_insurance` | 社保公积金配置（基数、比例） |
| `fin_daily_settlement` | 资金日报 |
| `fin_supplier` | 供应商（编码、名称、联系人、银行账户） |

**财务库合计：约 18 张表**

---

### 汇总

| 数据库 | 表数 | 核心职责 |
|---|---|---|
| `unified_system_db` | ~20 | 用户/部门/角色/权限/消息/审计/工作流引擎/考勤 |
| `unified_production_db` | ~16 | 产品/BOM/计划/排期/工单/采购申请/生产仓/质检 |
| `unified_logistics_db` | ~14 | 物流仓/调拨/拣货/发货/物流质检 |
| `unified_sales_db` | ~9 | 客户/销售报单/价格预警/合同/e签宝 |
| `unified_finance_db` | ~18 | 会计科目/凭证/应收/应付/报销/预算/工资/资金 |
| **合计** | **~77** | |

---

## 三、审批工作流引擎设计

### 3.1 通用审批模型

```
wf_template (模板)
  ├── wf_node_template (节点定义, 1:N)
  │     ├── node_order: 审批顺序
  │     ├── approver_type: DEPT_MANAGER | ROLE | SPECIFIC_USER | POSITION
  │     ├── condition_type: NONE | AMOUNT_RANGE | PERCENTAGE
  │     └── condition_config: JSON (金额区间/比例阈值)
  │
wf_instance (实例)
  ├── business_type + business_id → 关联业务单据
  ├── current_node_order → 当前流转位置
  │
wf_record (审批记录)
  ├── approver_action: APPROVE | REJECT | PUSH_UP | DELEGATE
  └── action_time + comment
```

### 3.2 各业务审批链

#### 采购审批

```
生产人员提交采购申请
  ↓
条件判断（按金额）：
  小额（如 <5000）    → 财务专员 → [通过]
  中额（5000-50000）  → 财务专员 → 财务经理 → [通过]
  大额（50000-200000）→ 财务专员 → 财务经理 → [通过]
  特大额（>200000）   → 财务专员 → 财务经理(前置) → 老板(终审) → [通过]
  
     ← 任意级可驳回，驳回退回申请人
     ← 上级拥有越级审批权限
```

#### 销售报单审批

```
销售员提交报单（含合同附件）
  ↓
价格异常检测（3个月同产品加权平均价）：
  正常价格（≥85%）      → 财务专员 → [通过]
  轻度异常（<85%）       → 系统打标签，财务专员 → [通过/驳回]
  中度异常（<70%）       → 系统强制【低价特批理由】必填 → 财务专员 → [通过/驳回]
  重度异常（<50%）       → 财务专员 → 老板终审 → [通过/驳回]
  
     ← 财务发现异常低价但不到阈值，可人工上推老板
     ← 全部通过后 → 自动生成合同 + 电子章
```

#### 调拨审批

```
常规调拨：
  申请人制单 → 调入仓主管审核(仓位/品类) → 分价值流转：
    小额低价值 → 调出仓主管审批 → [通过]
    中大批量/高价值 → 财务复核 → [通过]
    超大批量/贵重/跨公司 → 仓管→仓主管→财务→老板终审 → [通过]

特殊调拨（样品/报废/返修/赠送）：
  → 制单必须备注特殊用途
  → 赠送类：仓主管（普通）→ 财务复核（大批量）→ 老板（大额）
  → 报废类：+ 质检审核 → 财务确认资产损失

审批完成后 → 实物交接 → 调入仓签收 → 归档 → 库存台账同步更新
```

#### 费用报销审批

```
员工提交报销 → 部门经理审批 → 财务专员：
  按金额分级：
    小额    → 财务专员审批 → 出纳打款
    中额    → 财务专员 → 财务经理 → 出纳打款
    大额    → 财务专员 → 财务经理 → 老板 → 出纳打款

报销类型：差旅 / 办公 / 招待 / 其他
需做预算对比展示（不限制，仅对比参考）
```

#### 请假 / 加班审批

```
员工提交请假/加班申请 → 部门经理审批 → [通过]
（预留 HR/行政多级审批扩展）
```

---

## 四、核心业务流程设计

### 4.1 生产入库流程

```
工人填生产完工单 → 一次质检(工人,不记系统) → 二次质检(仓管,AQL抽样)
  ├── 合格 → 入生产仓(生成入库单号, FIFO批次)
  └── 不合格 → 退回生产
```

### 4.2 采购入库流程

```
采购原料到货 → 仓管质检(AQL抽样) 
  ├── 合格 → 生成入库单号 → 打印入库标签 → 入生产仓(批次管理)
  └── 不合格 → 退回供应商
```

### 4.3 发货出库流程

```
销售报单终审通过 + 合同签署完成 + 货款确认
  ↓
物流生成拣货单(FIFO自动分配批次) → 打印四联销货单
  ↓
物流司机按纸质单去仓储拿货(实物交接)
  ↓
四联分发：销售联 | 物流联 | 仓储联 | 客户联
  ↓
物流出库 → 库存扣减 → 应收记账
```

### 4.4 合同生成与签署流程

```
销售报单所有审批终审完成
  ↓
系统填充合同模板(销售单数据+模板) → 自动盖合法电子章
  ↓
需要对方签署 → 调用 e签宝 API 发起电子签署(预留)
  ↓
双方签署完成 → 合同归档 → 可导出 PDF
```

### 4.5 考勤与薪资计算流程

```
登录即打卡 → 系统记录登录/登出时间
  ↓
对照考勤规则计算状态(正常/迟到/早退/加班)
  ↓
月末汇总：考勤数据 + 基本工资 + 绩效奖金 + 社保公积金
  ↓
计算实发工资 → 生成工资单 → 审批 → 发放
```

---

## 五、报表体系设计

### 5.1 实时看板（Dashboard）

| 报表 | 内容 |
|---|---|
| 销售实时报表 | 当日销售额、订单明细、毛利、异常低价亏损订单、销售趋势 |
| 库存实时报表 | 各仓库存总值、调拨流水、库存周转预警、呆滞库存 |
| 应收应付实时 | 应收余额、账龄预警、待付采购款、到期款项提醒 |
| 资金实时报表 | 账户余额、当日收支、资金缺口预警 |
| 生产实时报表 | 当日投产、完工入库、在制品、材料领用消耗 |
| 单据审批报表 | 待审核采购/调拨/销售单、异常单据汇总 |

### 5.2 周期报表（月报/季报）

| 报表 | 内容 |
|---|---|
| 销售周期报表 | 周期销量、客户排行、产品毛利表、折扣汇总 |
| 生产周期报表 | 周期产值、产品单位成本、材料耗用、损耗统计 |
| 库存周期报表 | 周期出入库/调拨汇总、盘点差异、库存周转天数 |
| 应收账龄周期表 | 账龄分段、坏账预估 |
| 应付结算周期表 | 到期应付、结算进度 |
| 费用周期报表 | 各部门费用汇总、预算执行对比 |

### 5.3 法定财务报表（月/季/年）

| 报表 | 说明 |
|---|---|
| 利润表（损益表） | 收入 - 成本 - 费用 = 净利润 |
| 资产负债表 | 资产 = 负债 + 所有者权益 |
| 现金流量表 | 经营 + 投资 + 筹资活动现金流 |

> 报表引擎统一使用 **JimuReport** 嵌入，支持拖拽设计、动态数据源、导出 Excel/PDF。

---

## 六、技术栈定稿

| 层 | 技术 | 版本 |
|---|---|---|
| 后端框架 | Spring Boot | 3.3.x |
| Java | JDK | 17+ |
| ORM | MyBatis-Plus | 3.5.x |
| 数据库 | MySQL | 8.x |
| 缓存 | Redis | 7.x |
| 多数据源 | Baomidou dynamic-datasource | 4.x |
| 认证 | Spring Security + JWT (jjwt) | |
| 令牌管理 | Caffeine 本地缓存 | |
| WebSocket | Spring WebSocket | |
| 报表引擎 | JimuReport | 最新版 |
| Excel | EasyExcel | |
| 电子签 | e签宝 API（预留） | |
| 构建 | Maven | |
| 前端框架 | Vue 3 (Composition API) | 3.x |
| 构建工具 | Vite | 5.x |
| 语言 | TypeScript | |
| UI 组件库 | Ant Design Vue | 4.x |
| 路由 | Vue Router 4 | |
| 状态管理 | Pinia | |
| HTTP | Axios | |
| 图表 | ECharts / AntV | |
| 桌面壳 | Electron | 29+ |
| 打印 | Electron 原生打印 | |

---

## 七、项目模块结构

```
UnifiedSystem/
├── UnifiedSystemBackend/
│   ├── unified-common/              # 公共模块
│   │   ├── core/                    # 基础类（BaseController/Entity/Service）
│   │   ├── security/                # JWT、Spring Security 配置
│   │   ├── workflow/                # 审批工作流引擎
│   │   ├── message/                 # WebSocket 消息推送
│   │   ├── exception/               # 全局异常处理
│   │   └── util/                    # 工具类（AES加密、序列号生成）
│   ├── unified-system/              # 系统管理模块
│   │   ├── user/                    # 用户管理
│   │   ├── department/              # 部门管理
│   │   ├── role/                    # 角色管理
│   │   ├── resource/                # 权限资源管理
│   │   ├── message/                 # 站内消息
│   │   ├── dict/                    # 字典管理
│   │   ├── audit/                   # 审计日志
│   │   └── attendance/              # 考勤管理
│   ├── unified-production/          # 生产管理模块
│   │   ├── product/                 # 产品 & BOM
│   │   ├── plan/                    # 生产计划 & 排期
│   │   ├── order/                   # 工单管理
│   │   ├── purchase/                # 采购申请
│   │   ├── warehouse/               # 生产仓管理
│   │   └── quality/                 # 质检管理
│   ├── unified-logistics/           # 物流管理模块
│   │   ├── warehouse/               # 物流仓管理
│   │   ├── transfer/                # 调拨管理
│   │   ├── picking/                 # 拣货管理
│   │   ├── shipping/                # 发货管理
│   │   └── quality/                 # 物流质检
│   ├── unified-sales/               # 销售管理模块
│   │   ├── customer/                # 客户管理
│   │   ├── order/                   # 销售报单
│   │   ├── price/                   # 价格管理 & 异常检测
│   │   ├── contract/                # 合同管理 & e签宝
│   │   └── aftersale/               # 售后服务
│   ├── unified-finance/             # 财务管理模块
│   │   ├── account/                 # 账户 & 会计科目
│   │   ├── voucher/                 # 记账凭证
│   │   ├── receivable/              # 应收账款
│   │   ├── payable/                 # 应付账款
│   │   ├── expense/                 # 费用报销
│   │   ├── budget/                  # 预算管理
│   │   ├── salary/                  # 工资管理
│   │   └── report/                  # 财务报表
│   └── unified-report/              # 报表模块
│       └── JimuReport 集成配置
│
├── UnifiedSystemFrontend/
│   ├── src/
│   │   ├── views/                   # 页面视图
│   │   │   ├── system/              # 系统管理页面
│   │   │   ├── production/          # 生产管理页面
│   │   │   ├── logistics/           # 物流管理页面
│   │   │   ├── sales/               # 销售管理页面
│   │   │   ├── finance/             # 财务管理页面
│   │   │   └── dashboard/           # 实时看板
│   │   ├── components/              # 公共组件
│   │   ├── stores/                  # Pinia 状态管理
│   │   ├── router/                  # 路由配置
│   │   ├── api/                     # API 接口层
│   │   ├── utils/                   # 工具函数
│   │   └── electron/                # Electron 主进程（打印、本地存储）
│   └── electron-builder.json        # Electron 打包配置
│
└── docs/                            # 项目文档
```

---

## 八、关键注意事项

| 事项 | 说明 |
|---|---|
| **部门/角色 CRUD** | 现阶段预留，使用字典 + 初始化 SQL 预置 |
| **e签宝集成** | 仅预留接口（`EsignService`），不实现完整对接 |
| **外部考勤对接** | 预留 `AttendanceSyncService` 接口，当前为登录打卡 |
| **预算对比** | 仅做对比展示，不做强制控制 |
| **一次质检** | 不录入系统 |
| **AQL 标准** | 系统内配置 AQL 抽样表，仓管按标准执行 |
| **FIFO** | 出库时按生产日期/入库日期自动分配最早批次 |
| **四联销货单** | Electron 桌面端原生打印，模板可配置 |
| **电子章** | 合同生成时自动加盖（预设印章图片 + 后端合成） |
| **Redis 用途** | 分布式锁（并发审批/库存扣减）、序列号生成、审批流程锁 |

---

## 九、下一步

如果以上设计确认无误，可以开始按模块逐步实现。建议的实现顺序：

1. **统一基础设施** — 项目骨架、多数据源配置、JWT 认证、统一异常处理
1. **系统管理** — 用户/角色/权限/部门/消息/审计
1. **审批工作流引擎** — 通用引擎 + 各业务模板配置
1. **生产管理** — 产品→BOM→计划→排期→工单→采购申请→生产仓→质检
1. **物流管理** — 物流仓→调拨→拣货→发货
1. **销售管理** — 客户→报单→价格异常检测→合同
1. **财务管理** — 科目→凭证→应收应付→报销→预算→工资→法定报表
1. **实时看板** — Dashboard + 周期报表 + JimuReport 集成
1. **Electron 桌面端** — 打印、本地存储、自动更新
