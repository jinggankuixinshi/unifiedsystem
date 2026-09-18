-- =============================================================================
-- Phase 1.5 增量升级脚本（存量库）：角色等级 + 人事部门 + 请假矩阵 + 账号操作审批
--
-- 说明：本脚本内容已全量合入 01_unified_system_db.sql（新库初始化无需本脚本）；
--       仅用于已部署的旧库原地升级。
-- 前置：已执行 06_phase1_upgrade.sql（branch_no/applicant_id 等已就绪）
-- 执行：mysql --default-character-set=utf8mb4 -u unified_dev -p < 07_phase15_upgrade.sql
-- 注意：ALTER/固定ID种子仅可执行一次；重复执行会因列/索引/主键已存在报错，可忽略
-- =============================================================================

SET NAMES utf8mb4;
USE unified_system_db;

-- =============================================================================
-- 1. 角色等级（审批层级）：总经理100 / 人事高管80 / 部门高管60 / 专员30 / 员工20
-- =============================================================================
ALTER TABLE sys_role ADD COLUMN level INT NOT NULL DEFAULT 0 COMMENT '角色等级（审批按层级推进：总经理100/人事高管80/部门高管60/专员30/员工20）' AFTER description;

UPDATE sys_role SET level = 90  WHERE role_code = 'admin';
UPDATE sys_role SET level = 100 WHERE role_code = 'boss';
UPDATE sys_role SET level = 60  WHERE role_code = 'prod_manager';
UPDATE sys_role SET level = 20  WHERE role_code = 'prod_worker';
UPDATE sys_role SET level = 30  WHERE role_code = 'warehouse_keeper';
UPDATE sys_role SET level = 30  WHERE role_code = 'logistics_operator';
UPDATE sys_role SET level = 60  WHERE role_code = 'logistics_manager';
UPDATE sys_role SET level = 20  WHERE role_code = 'salesperson';
UPDATE sys_role SET level = 60  WHERE role_code = 'sales_manager';
UPDATE sys_role SET level = 30  WHERE role_code = 'finance_staff';
UPDATE sys_role SET level = 60  WHERE role_code = 'finance_manager';
UPDATE sys_role SET level = 30  WHERE role_code = 'cashier';

-- =============================================================================
-- 2. 人事部门建制：角色 13/14、部门 6、用户 13/14、关联与资源
-- =============================================================================
INSERT INTO sys_role (id, role_name, role_code, description, level, sort_order) VALUES
(13, '人事经理', 'hr_manager', '人事管理（人事高管）', 80, 12),
(14, '人事专员', 'hr_staff', '账号操作与人事事务', 30, 13);

INSERT INTO sys_department (id, dept_name, dept_code, parent_id, ancestors, sort_order, leader, leader_id, status)
VALUES (6, '人事部', 'hr', 1, '0,1', 5, '人事经理', 13, 1);

INSERT INTO sys_user (id, username, password, real_name, dept_id, status) VALUES
(13, 'hrmanager', '$2a$10$.NnmlaBVmlPdGfNTHviGcOhzd.7JfNxtkgQ86mRXle3K3yrxPS2QG', '人事经理', 6, 1),
(14, 'hrstaff', '$2a$10$6hQ.o606TKoGQs7XYM/gbOKJJINKIKzjzzxWGHbT68d7fzPcrzPOi', '人事专员', 6, 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES (13, 13), (14, 14);

INSERT INTO sys_role_resource (role_id, resource_id) VALUES
(13, 1), (13, 11),
(14, 1), (14, 11);

-- =============================================================================
-- 3. 节点审批层级字段 + 全量重写审批节点种子（模板 1-7）
-- =============================================================================
ALTER TABLE wf_node_template ADD COLUMN node_level INT COMMENT '节点审批层级（上推判定用；空=按审批人解析）' AFTER condition_config;

DELETE FROM wf_node_template WHERE template_id BETWEEN 1 AND 7;

-- 采购 (template_id=1)
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(1, 1, 1, 'ROLE', 10, 'AMOUNT_RANGE', '{"max":5000}', 30, '财务专员审批');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(1, 2, 1, 'ROLE', 10, 'AMOUNT_RANGE', '{"min":5000,"max":50000}', 30, '财务专员审批'),
(1, 2, 2, 'ROLE', 11, 'NONE', '{}', 60, '财务经理审批');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(1, 3, 1, 'ROLE', 10, 'AMOUNT_RANGE', '{"min":50000,"max":200000}', 30, '财务专员审批'),
(1, 3, 2, 'ROLE', 11, 'NONE', '{}', 60, '财务经理审批');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(1, 4, 1, 'ROLE', 10, 'AMOUNT_RANGE', '{"min":200000}', 30, '财务专员审批'),
(1, 4, 2, 'ROLE', 11, 'NONE', '{}', 60, '财务经理前置审批'),
(1, 4, 3, 'ROLE', 2, 'NONE', '{}', 100, '老板终审');

-- 销售报单 (template_id=2)
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(2, 1, 1, 'ROLE', 10, 'PERCENTAGE', '{"min":0.85}', 30, '财务专员审批');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(2, 2, 1, 'ROLE', 10, 'PERCENTAGE', '{"min":0.70,"max":0.85}', 30, '财务专员审批(轻异常打标)');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(2, 3, 1, 'ROLE', 10, 'PERCENTAGE', '{"min":0.50,"max":0.70}', 30, '财务专员审批(低价理由)');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(2, 4, 1, 'ROLE', 10, 'PERCENTAGE', '{"max":0.50}', 30, '财务专员审批'),
(2, 4, 2, 'ROLE', 2, 'NONE', '{}', 100, '老板终审');

-- 调拨 (template_id=3)
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(3, 1, 1, 'ROLE', 7, 'AMOUNT_RANGE', '{"max":50000,"types":["normal"]}', 60, '调入仓主管审核'),
(3, 1, 2, 'ROLE', 7, 'NONE', '{}', 60, '调出仓主管审批');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(3, 2, 1, 'ROLE', 7, 'AMOUNT_RANGE', '{"min":50000,"types":["normal"]}', 60, '调入仓主管审核'),
(3, 2, 2, 'ROLE', 7, 'NONE', '{}', 60, '调出仓主管审批'),
(3, 2, 3, 'ROLE', 11, 'NONE', '{}', 60, '财务复核');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(3, 3, 1, 'ROLE', 7, 'TYPE', '{"types":["sample","scrap","repair","gift"]}', 60, '仓主管审批'),
(3, 3, 2, 'ROLE', 11, 'NONE', '{}', 60, '财务复核'),
(3, 3, 3, 'ROLE', 2, 'NONE', '{}', 100, '老板终审');

-- 费用报销 (template_id=4)
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(4, 1, 1, 'DEPT_MANAGER', NULL, 'AMOUNT_RANGE', '{"max":5000}', 60, '部门负责人审批'),
(4, 1, 2, 'ROLE', 10, 'NONE', '{}', 30, '财务专员审批'),
(4, 1, 3, 'ROLE', 12, 'NONE', '{}', 30, '出纳打款');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(4, 2, 1, 'DEPT_MANAGER', NULL, 'AMOUNT_RANGE', '{"min":5000,"max":20000}', 60, '部门负责人审批'),
(4, 2, 2, 'ROLE', 10, 'NONE', '{}', 30, '财务专员审批'),
(4, 2, 3, 'ROLE', 11, 'NONE', '{}', 60, '财务经理审批'),
(4, 2, 4, 'ROLE', 12, 'NONE', '{}', 30, '出纳打款');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(4, 3, 1, 'DEPT_MANAGER', NULL, 'AMOUNT_RANGE', '{"min":20000}', 60, '部门负责人审批'),
(4, 3, 2, 'ROLE', 10, 'NONE', '{}', 30, '财务专员审批'),
(4, 3, 3, 'ROLE', 11, 'NONE', '{}', 60, '财务经理审批'),
(4, 3, 4, 'ROLE', 2, 'NONE', '{}', 100, '老板审批'),
(4, 3, 5, 'ROLE', 12, 'NONE', '{}', 30, '出纳打款');

-- 请假 (template_id=5, 按申请人层级)
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(5, 1, 1, 'DEPT_TOP', NULL, 'APPLICANT_LEVEL', '{"max":60}', 60, '部门最高管审批'),
(5, 1, 2, 'ROLE', 14, 'NONE', '{}', 30, '人事专员审批');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(5, 2, 1, 'ROLE', 13, 'APPLICANT_LEVEL', '{"min":60,"max":80}', 80, '人事高管审批'),
(5, 2, 2, 'ROLE', 2, 'NONE', '{}', 100, '总经理审批');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(5, 3, 1, 'ROLE', 2, 'APPLICANT_LEVEL', '{"min":80}', 100, '总经理审批');

-- 加班 (template_id=6, 同请假矩阵)
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(6, 1, 1, 'DEPT_TOP', NULL, 'APPLICANT_LEVEL', '{"max":60}', 60, '部门最高管审批'),
(6, 1, 2, 'ROLE', 14, 'NONE', '{}', 30, '人事专员审批');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(6, 2, 1, 'ROLE', 13, 'APPLICANT_LEVEL', '{"min":60,"max":80}', 80, '人事高管审批'),
(6, 2, 2, 'ROLE', 2, 'NONE', '{}', 100, '总经理审批');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(6, 3, 1, 'ROLE', 2, 'APPLICANT_LEVEL', '{"min":80}', 100, '总经理审批');

-- 人事账号操作 (template_id=7)
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(7, 1, 1, 'ROLE', 13, 'APPLICANT_LEVEL', '{"max":80}', 80, '人事高管审批');
INSERT INTO wf_node_template (template_id, branch_no, node_order, approver_type, approver_id, condition_type, condition_config, node_level, node_name) VALUES
(7, 2, 1, 'ROLE', 2, 'APPLICANT_LEVEL', '{"min":80}', 100, '总经理审批');

ALTER TABLE wf_node_template ADD UNIQUE KEY uk_template_branch_order (template_id, branch_no, node_order);

-- =============================================================================
-- 4. 模板 7 + 描述更新
-- =============================================================================
INSERT INTO wf_template (id, template_name, business_type, description, version) VALUES
(7, '人事账号操作审批', 'hr_account_op', '账号注册/启用/禁用/删除审批，通过后自动生效', 1);

UPDATE wf_template SET description = '员工请假审批流程（按申请人层级：部门高管+人事 / 人事高管+总经理）' WHERE id = 5;
UPDATE wf_template SET description = '加班审批流程（同请假矩阵）' WHERE id = 6;

-- =============================================================================
-- 5. 人事账号操作单表
-- =============================================================================
CREATE TABLE IF NOT EXISTS hr_account_op (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    op_no VARCHAR(50) NOT NULL COMMENT '操作单号',
    op_type VARCHAR(20) NOT NULL COMMENT 'register/enable/disable/delete',
    target_user_id BIGINT COMMENT '目标用户ID（启用/禁用/删除）',
    payload TEXT COMMENT '注册信息JSON（username/realName/deptId/phone/email）',
    applicant_id BIGINT NOT NULL COMMENT '申请人用户ID',
    approval_status INT DEFAULT 0 COMMENT '审批状态 0待审 1通过 2驳回',
    executed TINYINT DEFAULT 0 COMMENT '执行状态 0未执行 1已执行 -1执行失败',
    exec_message VARCHAR(500) DEFAULT '' COMMENT '执行结果说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_op_no (op_no),
    INDEX idx_status (approval_status),
    INDEX idx_applicant (applicant_id)
) COMMENT '人事账号操作单表';

-- 验证
SELECT 'roles_with_level' AS t, COUNT(*) AS cnt FROM sys_role WHERE level > 0
UNION ALL SELECT 'hr_users', COUNT(*) FROM sys_user WHERE username IN ('hrmanager','hrstaff')
UNION ALL SELECT 'wf_nodes', COUNT(*) FROM unified_system_db.wf_node_template
UNION ALL SELECT 'hr_account_op_table', COUNT(*) FROM information_schema.tables WHERE table_schema='unified_system_db' AND table_name='hr_account_op';
