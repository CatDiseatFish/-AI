-- 修改钱包默认积分为2000
ALTER TABLE wallets MODIFY COLUMN balance INT NOT NULL DEFAULT 2000 COMMENT '积分余额';

-- 更新现有余额为0的用户(如果需要的话,可选)
UPDATE wallets SET balance = 2000 WHERE balance = 0;
