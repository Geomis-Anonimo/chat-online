CREATE INDEX idx_message_conversation_sent_date
    ON message (support_conversation_id, sent_date);

CREATE INDEX idx_support_conversation_admin_status
    ON support_conversation (admin_id, status);