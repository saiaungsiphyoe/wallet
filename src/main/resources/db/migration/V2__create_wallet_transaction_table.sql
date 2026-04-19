CREATE TABLE wallet_transaction(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    initiator_id BIGINT,
    from_wallet_id BIGINT,
    to_wallet_id BIGINT,
    transaction_type VARCHAR(20) NOT NULL,
    transaction_date DATE NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3),
    is_reversed BOOLEAN DEFAULT FALSE,
    reversed_transaction_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transaction_initiator
    FOREIGN KEY (initiator_id)
    REFERENCES appuser(id),

    CONSTRAINT fk_from_wallet
    FOREIGN KEY (from_wallet_id)
    REFERENCES wallet(id),

    CONSTRAINT fk_to_wallet
    FOREIGN KEY (to_wallet_id)
    REFERENCES wallet(id),

    CONSTRAINT fk_reversed_transaction
    FOREIGN KEY (reversed_transaction_id)
    REFERENCES wallet_transaction(id)
)