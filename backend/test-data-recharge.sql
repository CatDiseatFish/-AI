-- 充值套餐测试数据
-- 插入充值套餐（按常见的充值档位）

INSERT INTO recharge_products (name, points, price_cents, enabled, sort_order, created_at, updated_at)
VALUES
    ('体验套餐', 100, 1000, 1, 1, NOW(), NOW()),      -- 10元 = 100积分
    ('入门套餐', 500, 4500, 1, 2, NOW(), NOW()),      -- 45元 = 500积分 (9折)
    ('进阶套餐', 1000, 8000, 1, 3, NOW(), NOW()),     -- 80元 = 1000积分 (8折)
    ('高级套餐', 3000, 21000, 1, 4, NOW(), NOW()),    -- 210元 = 3000积分 (7折)
    ('尊享套餐', 5000, 30000, 1, 5, NOW(), NOW()),    -- 300元 = 5000积分 (6折)
    ('至尊套餐', 10000, 50000, 1, 6, NOW(), NOW());   -- 500元 = 10000积分 (5折)

-- 兑换规则测试数据
-- centsPerPoint = 10 表示 10分（0.1元）= 1积分，即 1元 = 10积分

INSERT INTO recharge_exchange_rules (name, currency, cents_per_point, min_amount_cents, max_amount_cents, enabled, created_at, updated_at)
VALUES
    ('标准兑换规则', 'CNY', 10, 1000, 100000, 1, NOW(), NOW());  -- 1元 = 10积分，最小充值10元，最大充值1000元
