-- =============================================================================
-- Phase 1 增量升级脚本（存量库）
-- 内容：部门负责人ID、工作流分支模型、模板种子重写、销售低价理由
--
-- 说明：本脚本内容已全量合入 01/04 初始化脚本（新库初始化无需本脚本）；
--       仅用于 Phase 0 之前部署的旧库原地升级。
-- 执行：mysql --default-character-set=utf8mb4 -u unified_dev -p < 06_phase1_upgrade.sql
-- 注意：ALTER 部分仅需执行一次；重复执行会因列已存在报错，可忽略
--       若后续继续执行 07，节点种子会被 07 全量重写（含 node_level），无需担心重复
-- =============================================================================

SET NAMES utf8mb4;

-- =============================================================================
-- 1. 部门表：负责人用户ID（DEPT_MANAGER 审批解析）
-- =============================================================================
USE unified_system_db;

ALTER TABLE sys_department ADD COLUMN leader_id BIGINT COMMENT '负责人用户ID（DEPT_MANAGER 审批解析用）' AFTER leader;

UPDATE sys_department SET leader_id = 2  WHERE dept_code = 'root';
UPDATE sys_department SET leader_id = 3  WHERE dept_code = 'production';
UPDATE sys_department SET leader_id = 7  WHERE dept_code = 'logistics';
UPDATE sys_department SET leader_id = 9  WHERE dept_code = 'sales';
UPDATE sys_department SET leader_id = 11 WHERE dept_code = 'finance';

-- =============================================================================
-- 2. 工作流：分支字段
-- =============================================================================
ALTER TABLE wf_node_template ADD COLUMN branch_no INT NOT NULL DEFAULT 1 COMMENT '分支编号（同模板下按条件互斥，一条链一个分支）' AFTER template_id;
ALTER TABLE wf_instance ADD COLUMN branch_no INT DEFAULT 1 COMMENT '锁定的分支编号（启动时按业务指标匹配）' AFTER template_id;
ALTER TABLE wf_instance ADD COLUMN applicant_id BIGINT COMMENT '发起人用户ID' AFTER business_id;
ALTER TABLE wf_instance ADD COLUMN delegate_user_id BIGINT COMMENT '当前节点委托审批人ID' AFTER applicant_id;
ALTER TABLE wf_instance ADD COLUMN delegate_node_order INT COMMENT '委托生效的节点顺序（推进后清空）' AFTER delegate_user_id;

-- =============================================================================
-- 3. 重写审批节点种子（6 个模板）
-- =============================================================================
DELETE FROM wf_node_template WHERE template_id BETWEEN 1 AND 6;

-- 采购审批 (template_id=1, 按金额分级；区间 min 含 max 不含)
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(1, 1, 1, 'ROLE', 10, 'AMOUNT_RANGE', '{"max":5000}', '财务专员审批');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(1, 2, 1, 'ROLE', 10, 'AMOUNT_RANGE', '{"min":5000,"max":50000}', '财务专员审批'),
(1, 2, 2, 'ROLE', 11, 'NONE', '{}', '财务经理审批');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(1, 3, 1, 'ROLE', 10, 'AMOUNT_RANGE', '{"min":50000,"max":200000}', '财务专员审批'),
(1, 3, 2, 'ROLE', 11, 'NONE', '{}', '财务经理审批');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(1, 4, 1, 'ROLE', 10, 'AMOUNT_RANGE', '{"min":200000}', '财务专员审批'),
(1, 4, 2, 'ROLE', 11, 'NONE', '{}', '财务经理前置审批'),
(1, 4, 3, 'ROLE', 2, 'NONE', '{}', '老板终审');

-- 销售报单审批 (template_id=2, 按价格比 ratio=成交价/均价 分级)
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(2, 1, 1, 'ROLE', 10, 'PERCENTAGE', '{"min":0.85}', '财务专员审批');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(2, 2, 1, 'ROLE', 10, 'PERCENTAGE', '{"min":0.70,"max":0.85}', '财务专员审批(轻异常打标)');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(2, 3, 1, 'ROLE', 10, 'PERCENTAGE', '{"min":0.50,"max":0.70}', '财务专员审批(低价理由)');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(2, 4, 1, 'ROLE', 10, 'PERCENTAGE', '{"max":0.50}', '财务专员审批'),
(2, 4, 2, 'ROLE', 2, 'NONE', '{}', '老板终审');

-- 调拨审批 (template_id=3；常规按金额分支，特殊类型按类型分支)
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(3, 1, 1, 'ROLE', 7, 'AMOUNT_RANGE', '{"max":50000,"types":["normal"]}', '调入仓主管审核'),
(3, 1, 2, 'ROLE', 7, 'NONE', '{}', '调出仓主管审批');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(3, 2, 1, 'ROLE', 7, 'AMOUNT_RANGE', '{"min":50000,"types":["normal"]}', '调入仓主管审核'),
(3, 2, 2, 'ROLE', 7, 'NONE', '{}', '调出仓主管审批'),
(3, 2, 3, 'ROLE', 11, 'NONE', '{}', '财务复核');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(3, 3, 1, 'ROLE', 7, 'TYPE', '{"types":["sample","scrap","repair","gift"]}', '仓主管审批'),
(3, 3, 2, 'ROLE', 11, 'NONE', '{}', '财务复核'),
(3, 3, 3, 'ROLE', 2, 'NONE', '{}', '老板终审');

-- 费用报销审批 (template_id=4, 按金额分级)
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(4, 1, 1, 'DEPT_MANAGER', NULL, 'AMOUNT_RANGE', '{"max":5000}', '部门经理审批'),
(4, 1, 2, 'ROLE', 10, 'NONE', '{}', '财务专员审批'),
(4, 1, 3, 'ROLE', 12, 'NONE', '{}', '出纳打款');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(4, 2, 1, 'DEPT_MANAGER', NULL, 'AMOUNT_RANGE', '{"min":5000,"max":20000}', '部门经理审批'),
(4, 2, 2, 'ROLE', 10, 'NONE', '{}', '财务专员审批'),
(4, 2, 3, 'ROLE', 11, 'NONE', '{}', '财务经理审批'),
(4, 2, 4, 'ROLE', 12, 'NONE', '{}', '出纳打款');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(4, 3, 1, 'DEPT_MANAGER', NULL, 'AMOUNT_RANGE', '{"min":20000}', '部门经理审批'),
(4, 3, 2, 'ROLE', 10, 'NONE', '{}', '财务专员审批'),
(4, 3, 3, 'ROLE', 11, 'NONE', '{}', '财务经理审批'),
(4, 3, 4, 'ROLE', 2, 'NONE', '{}', '老板审批'),
(4, 3, 5, 'ROLE', 12, 'NONE', '{}', '出纳打款');

-- 请假 / 加班审批 (template_id=5,6)
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(5, 1, 1, 'DEPT_MANAGER', NULL, 'NONE', '{}', '部门经理审批');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(6, 1, 1, 'DEPT_MANAGER', NULL, 'NONE', '{}', '部门经理审批');

-- =============================================================================
-- 4. 销售报单：低价特批理由
-- =============================================================================
USE unified_sales_db;

ALTER TABLE sal_sales_order ADD COLUMN low_price_reason VARCHAR(500) DEFAULT '' COMMENT '低价特批理由（中度异常必填）' AFTER price_anomaly_level;

-- 验证
SELECT 'wf_node_template' AS t, COUNT(*) AS cnt FROM unified_system_db.wf_node_template
UNION ALL
SELECT 'sys_department.leader_id', COUNT(*) FROM unified_system_db.sys_department WHERE leader_id IS NOT NULL;
