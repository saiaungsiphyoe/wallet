CREATE TABLE wallet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appuser_id BIGINT NOT NULL,
    wallet_status VARCHAR(20) NOT NULL,
    balance DECIMAL(19, 4) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_wallet_appuser
        FOREIGN KEY (appuser_id)
        REFERENCES appuser(id),

    CONSTRAINT uk_appuser_currency
        UNIQUE (appuser_id, currency)
)