ALTER TABLE wallet_transaction
MODIFY COLUMN initiator_id BIGINT NOT NULL;

ALTER TABLE wallet_transaction
MODIFY COLUMN currency VARCHAR(3) NOT NULL;

ALTER TABLE wallet_transaction
MODIFY COLUMN is_reversed BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE wallet_transaction
MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE wallet_transaction
ADD CONSTRAINT chk_transaction_different_wallets
CHECK (
    from_wallet_id IS NULL
    OR to_wallet_id IS NULL
    OR from_wallet_id <> to_wallet_id
);