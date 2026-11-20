USE group_buy_market;

INSERT INTO group_buy_discount
    (discount_id, discount_name, discount_desc, discount_type, market_plan, market_expr, tag_id)
VALUES
    ('25120207', '测试优惠', '直减20元', 0, 'ZJ', '20', NULL),
    ('25120208', '渠道优惠', '名单用户8.5折', 1, 'ZK', '0.85', 'DEMO_TAG');

INSERT INTO group_buy_activity
    (activity_id, activity_name, discount_id, status, start_time, end_time, tag_id, tag_scope)
VALUES
    (100123, '测试活动', '25120207', 1, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 1 YEAR, NULL, NULL),
    (100124, '渠道活动', '25120208', 1, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 1 YEAR, NULL, NULL);

INSERT INTO sku (source, channel, goods_id, goods_name, original_price)
VALUES
    ('s01', 'c01', '9890001', '测试商品', 100.00),
    ('s01', 'c02', '9890001', '测试商品', 99.99);

INSERT INTO sc_sku_activity (source, channel, goods_id, activity_id)
VALUES
    ('s01', 'c01', '9890001', 100123),
    ('s01', 'c02', '9890001', 100124);

INSERT INTO crowd_tags (tag_id, tag_name, statistics)
VALUES ('DEMO_TAG', '测试名单', 1);

INSERT INTO crowd_tags_detail (tag_id, user_id)
VALUES ('DEMO_TAG', 'xiaofuge');

INSERT INTO crowd_tags_job (tag_id, batch_id, tag_type, tag_rule, status)
VALUES ('DEMO_TAG', '10001', 2, 'xiaofuge,liergou', 1);
