CREATE INDEX idx_support_conversation_status_created_at
    ON support_conversation (status, created_at);