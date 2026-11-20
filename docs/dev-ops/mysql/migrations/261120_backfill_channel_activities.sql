-- 适用于仍有 source、channel、goods_id 的旧活动表。先执行 sql/group_buy_market.sql 建表。
USE group_buy_market;

-- 列出冲突投放。每个组合仅能自动迁移一条，冲突的组合由运营确认后手动写入。
SELECT source, channel, goods_id, COUNT(*) AS activity_count
FROM group_buy_activity
GROUP BY source, channel, goods_id
HAVING COUNT(*) > 1;

INSERT INTO sc_sku_activity (source, channel, goods_id, activity_id)
SELECT source, channel, goods_id, MIN(activity_id)
FROM group_buy_activity
GROUP BY source, channel, goods_id
HAVING COUNT(*) = 1
ON DUPLICATE KEY UPDATE sc_sku_activity.activity_id = sc_sku_activity.activity_id;

-- 列出仍需从商品目录补齐的渠道 SKU。原活动表没有原价或商品名。
SELECT activity.source, activity.channel, activity.goods_id
FROM sc_sku_activity activity
LEFT JOIN sku
    ON sku.source = activity.source AND sku.channel = activity.channel AND sku.goods_id = activity.goods_id
WHERE sku.id IS NULL;

-- 根据唯一的标签明细重新计算历史统计。
UPDATE crowd_tags tags
SET statistics = (SELECT COUNT(*) FROM crowd_tags_detail detail WHERE detail.tag_id = tags.tag_id);
