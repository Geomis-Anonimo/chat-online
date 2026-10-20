CREATE INDEX idx_support_conversation_admin_assigned_at
    ON support_conversation (admin_id, assigned_at);