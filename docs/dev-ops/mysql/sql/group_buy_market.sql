CREATE DATABASE IF NOT EXISTS group_buy_market CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE group_buy_market;

CREATE TABLE IF NOT EXISTS group_buy_discount (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    discount_id VARCHAR(8) NOT NULL,
    discount_name VARCHAR(64) NOT NULL,
    discount_desc VARCHAR(256) NOT NULL,
    discount_type TINYINT NOT NULL DEFAULT 0 COMMENT '0基础优惠、1人群优惠',
    market_plan VARCHAR(4) NOT NULL DEFAULT 'ZJ',
    market_expr VARCHAR(32) NOT NULL,
    tag_id VARCHAR(64) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_discount_id (discount_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS group_buy_activity (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    activity_id BIGINT NOT NULL,
    activity_name VARCHAR(128) NOT NULL,
    discount_id VARCHAR(8) NOT NULL,
    group_type TINYINT NOT NULL DEFAULT 0 COMMENT '0自动成团、1达成目标拼团',
    take_limit_count INT NOT NULL DEFAULT 1,
    target INT NOT NULL DEFAULT 1,
    valid_time INT NOT NULL DEFAULT 15 COMMENT '拼团时长（分钟）',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0创建、1生效、2过期、3废弃',
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    tag_id VARCHAR(64) DEFAULT NULL,
    tag_scope VARCHAR(4) DEFAULT NULL COMMENT '1可见限制、2参与限制',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_activity_id (activity_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sku (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    source VARCHAR(8) NOT NULL,
    channel VARCHAR(8) NOT NULL,
    goods_id VARCHAR(12) NOT NULL,
    goods_name VARCHAR(128) NOT NULL,
    original_price DECIMAL(12, 2) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_sku_scope (source, channel, goods_id),
    CHECK (original_price >= 0.01)
) ENGINE=InnoDB DEFAULT COLLATE=utf8mb4_0900_bin;

CREATE TABLE IF NOT EXISTS sc_sku_activity (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    source VARCHAR(8) NOT NULL,
    channel VARCHAR(8) NOT NULL,
    goods_id VARCHAR(12) NOT NULL,
    activity_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_activity_scope (source, channel, goods_id),
    CONSTRAINT fk_sc_activity FOREIGN KEY (activity_id) REFERENCES group_buy_activity (activity_id)
) ENGINE=InnoDB DEFAULT COLLATE=utf8mb4_0900_bin;

CREATE TABLE IF NOT EXISTS crowd_tags (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tag_id VARCHAR(64) NOT NULL,
    tag_name VARCHAR(128) NOT NULL,
    tag_desc VARCHAR(256) DEFAULT NULL,
    statistics INT UNSIGNED NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT COLLATE=utf8mb4_0900_bin;

CREATE TABLE IF NOT EXISTS crowd_tags_detail (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tag_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(128) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_tag_user (tag_id, user_id),
    CONSTRAINT fk_detail_tag FOREIGN KEY (tag_id) REFERENCES crowd_tags (tag_id)
) ENGINE=InnoDB DEFAULT COLLATE=utf8mb4_0900_bin;

CREATE TABLE IF NOT EXISTS crowd_tags_job (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tag_id VARCHAR(64) NOT NULL,
    batch_id VARCHAR(64) NOT NULL,
    tag_type TINYINT NOT NULL COMMENT '2手工导入名单',
    tag_rule VARCHAR(4096) NOT NULL COMMENT '手工批次的逗号分隔用户ID',
    stat_start_time DATETIME DEFAULT NULL,
    stat_end_time DATETIME DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0初始、1计划、2重置、3完成',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_tag_batch (tag_id, batch_id),
    CONSTRAINT fk_job_tag FOREIGN KEY (tag_id) REFERENCES crowd_tags (tag_id)
) ENGINE=InnoDB DEFAULT COLLATE=utf8mb4_0900_bin;

CREATE TABLE IF NOT EXISTS group_buy_order (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    team_id VARCHAR(8) NOT NULL COMMENT '拼单组队ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    source VARCHAR(8) NOT NULL COMMENT '渠道',
    channel VARCHAR(8) NOT NULL COMMENT '来源',
    original_price DECIMAL(8, 2) NOT NULL COMMENT '原始价格',
    deduction_price DECIMAL(8, 2) NOT NULL COMMENT '折扣金额',
    pay_price DECIMAL(8, 2) NOT NULL COMMENT '支付价格',
    target_count INT NOT NULL COMMENT '目标数量',
    complete_count INT NOT NULL COMMENT '完成数量',
    lock_count INT NOT NULL COMMENT '锁单数量',
    status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态（0-拼单中、1-完成、2-失败、3-完成-含退单）',
    valid_start_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '拼团开始时间',
    valid_end_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '拼团结束时间',
    notify_type VARCHAR(8) NOT NULL DEFAULT 'HTTP' COMMENT '回调类型（HTTP、MQ）',
    notify_url VARCHAR(512) DEFAULT NULL COMMENT '回调地址（HTTP 回调不可为空）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uq_team_id (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='拼团组队订单表';

CREATE TABLE IF NOT EXISTS group_buy_order_list (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    team_id VARCHAR(8) NOT NULL COMMENT '拼单组队ID',
    order_id VARCHAR(12) NOT NULL COMMENT '订单ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    start_time DATETIME NOT NULL COMMENT '活动开始时间',
    end_time DATETIME NOT NULL COMMENT '活动结束时间',
    goods_id VARCHAR(16) NOT NULL COMMENT '商品ID',
    source VARCHAR(8) NOT NULL COMMENT '渠道',
    channel VARCHAR(8) NOT NULL COMMENT '来源',
    original_price DECIMAL(8, 2) NOT NULL COMMENT '原始价格',
    deduction_price DECIMAL(8, 2) NOT NULL COMMENT '折扣金额',
    pay_price DECIMAL(8, 2) NOT NULL DEFAULT 0.00 COMMENT '支付金额',
    status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态（0初始锁定、1消费完成、2用户退单）',
    out_trade_no VARCHAR(12) NOT NULL COMMENT '外部交易单号-确保外部调用唯一幂等',
    out_trade_time DATETIME DEFAULT NULL COMMENT '外部交易时间',
    biz_id VARCHAR(64) NOT NULL COMMENT '业务唯一ID（activityId_userId_次数）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uq_order_id (order_id),
    UNIQUE KEY uq_biz_id (biz_id),
    KEY idx_user_id_activity_id (user_id, activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户拼单明细表';

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
