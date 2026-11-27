USE group_buy_market;

-- 1. 补齐 group_buy_order 后续业务字段（拼团有效期 + 成团回调配置）
ALTER TABLE group_buy_order
    MODIFY COLUMN status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态（0-拼单中、1-完成、2-失败、3-完成-含退单）',
    ADD COLUMN valid_start_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '拼团开始时间' AFTER status,
    ADD COLUMN valid_end_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '拼团结束时间' AFTER valid_start_time,
    ADD COLUMN notify_type VARCHAR(8) NOT NULL DEFAULT 'HTTP' COMMENT '回调类型（HTTP、MQ）' AFTER valid_end_time,
    ADD COLUMN notify_url VARCHAR(512) DEFAULT NULL COMMENT '回调地址（HTTP 回调不可为空）' AFTER notify_type;

-- 回填历史测试数据的有效时间窗（默认创建时间 + 15 分钟）
UPDATE group_buy_order
SET valid_start_time = create_time,
    valid_end_time = DATE_ADD(create_time, INTERVAL 15 MINUTE);

-- 2. 补齐 group_buy_order_list 字段（实付金额 + 外部支付时间 + 防刷业务唯一ID + 查询索引）
ALTER TABLE group_buy_order_list
    MODIFY COLUMN status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态（0初始锁定、1消费完成、2用户退单）',
    ADD COLUMN pay_price DECIMAL(8, 2) NOT NULL DEFAULT 0.00 COMMENT '支付金额' AFTER deduction_price,
    ADD COLUMN out_trade_time DATETIME DEFAULT NULL COMMENT '外部交易时间' AFTER out_trade_no,
    ADD COLUMN biz_id VARCHAR(64) NOT NULL DEFAULT '' COMMENT '业务唯一ID（activityId_userId_次数）' AFTER out_trade_time;

-- 为历史测试记录回填唯一的 biz_id 和 pay_price，避免加唯一索引冲突
UPDATE group_buy_order_list
SET pay_price = deduction_price,
    biz_id = CONCAT(activity_id, '_', user_id, '_legacy_', id)
WHERE biz_id = '';

ALTER TABLE group_buy_order_list
    ADD UNIQUE KEY uq_biz_id (biz_id),
    ADD KEY idx_user_id_activity_id (user_id, activity_id);

-- 3. 新建回调通知任务表（本地消息表，用于成团/退单后的 HTTP 或 MQ 通知及定时重试补偿）
CREATE TABLE IF NOT EXISTS notify_task (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    team_id VARCHAR(8) NOT NULL COMMENT '拼单组队ID',
    notify_category VARCHAR(64) DEFAULT NULL COMMENT '回调种类（trade_settlement / trade_unpaid2refund 等）',
    notify_type VARCHAR(8) NOT NULL DEFAULT 'HTTP' COMMENT '回调类型（HTTP、MQ）',
    notify_mq VARCHAR(32) DEFAULT NULL COMMENT '回调消息主题',
    notify_url VARCHAR(128) DEFAULT NULL COMMENT '回调接口地址',
    notify_count INT NOT NULL DEFAULT 0 COMMENT '回调次数',
    notify_status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '回调状态（0初始、1完成、2重试、3失败）',
    parameter_json VARCHAR(256) NOT NULL COMMENT '回调参数JSON',
    uuid VARCHAR(128) NOT NULL COMMENT '唯一标识',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uq_uuid (uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='回调通知任务表';
