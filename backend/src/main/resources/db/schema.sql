CREATE TABLE IF NOT EXISTS finance_period (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    year_value INT NOT NULL,
    month_value INT NOT NULL,
    quarter_value INT NOT NULL,
    period_label VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_finance_period_year_month (year_value, month_value)
);

CREATE TABLE IF NOT EXISTS finance_metric (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    period_id BIGINT NOT NULL,
    metric_code VARCHAR(64) NOT NULL,
    metric_name VARCHAR(128) NOT NULL,
    metric_category VARCHAR(64) NOT NULL,
    actual_value DECIMAL(18,2) NOT NULL,
    unit VARCHAR(16) NOT NULL DEFAULT '万元',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_finance_metric_period_code (period_id, metric_code),
    KEY idx_finance_metric_code (metric_code),
    CONSTRAINT fk_finance_metric_period
        FOREIGN KEY (period_id) REFERENCES finance_period (id)
);

CREATE TABLE IF NOT EXISTS finance_budget (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    period_id BIGINT NOT NULL,
    metric_code VARCHAR(64) NOT NULL,
    budget_value DECIMAL(18,2) NOT NULL,
    unit VARCHAR(16) NOT NULL DEFAULT '万元',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_finance_budget_period_code (period_id, metric_code),
    KEY idx_finance_budget_code (metric_code),
    CONSTRAINT fk_finance_budget_period
        FOREIGN KEY (period_id) REFERENCES finance_period (id)
);

CREATE TABLE IF NOT EXISTS business_line_metric (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    period_id BIGINT NOT NULL,
    business_line VARCHAR(128) NOT NULL,
    revenue DECIMAL(18,2) NOT NULL,
    cost DECIMAL(18,2) NOT NULL,
    gross_profit DECIMAL(18,2) NOT NULL,
    net_profit DECIMAL(18,2) NOT NULL,
    unit VARCHAR(16) NOT NULL DEFAULT '万元',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_business_line_metric_period_line (period_id, business_line),
    CONSTRAINT fk_business_line_metric_period
        FOREIGN KEY (period_id) REFERENCES finance_period (id)
);

-- AI conversation: a chat session that groups multiple Q&A turns.
CREATE TABLE IF NOT EXISTS ai_conversation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    period_id BIGINT NULL,
    favorited TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_ai_conversation_updated_at (updated_at),
    KEY idx_ai_conversation_favorited (favorited)
);

-- Risk alert event: a single triggered alert with workflow status.
CREATE TABLE IF NOT EXISTS risk_alert_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    period_id BIGINT NOT NULL,
    alert_type VARCHAR(64) NOT NULL,
    alert_level VARCHAR(32) NOT NULL,
    message VARCHAR(512) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    UNIQUE KEY uk_risk_alert_event_period_type_msg (period_id, alert_type, message),
    KEY idx_risk_alert_event_status (status),
    KEY idx_risk_alert_event_created_at (created_at),
    CONSTRAINT fk_risk_alert_event_period
        FOREIGN KEY (period_id) REFERENCES finance_period (id)
);

-- AI message: a single user or assistant turn inside a conversation.
CREATE TABLE IF NOT EXISTS ai_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(16) NOT NULL,
    content MEDIUMTEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_ai_message_conversation_id (conversation_id),
    CONSTRAINT fk_ai_message_conversation
        FOREIGN KEY (conversation_id) REFERENCES ai_conversation (id)
        ON DELETE CASCADE
);
