-- 仅修复开发库中旧版试算样例的零长度活动区间。
USE group_buy_market;

UPDATE group_buy_activity AS activity
JOIN sc_sku_activity AS placement ON placement.activity_id = activity.activity_id
SET activity.start_time = CURRENT_TIMESTAMP - INTERVAL 1 DAY,
    activity.end_time = CURRENT_TIMESTAMP + INTERVAL 1 YEAR
WHERE activity.activity_id = 100123
  AND activity.discount_id = '25120208'
  AND activity.status = 1
  AND activity.start_time = '2024-12-07 10:19:40'
  AND activity.end_time = '2024-12-07 10:19:40'
  AND placement.source = 's01'
  AND placement.channel = 'c01'
  AND placement.goods_id = '9890001';
