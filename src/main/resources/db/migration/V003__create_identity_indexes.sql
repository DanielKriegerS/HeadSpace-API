CREATE INDEX IF NOT EXISTS idx_app_user_provider_subject ON app_user (provider, provider_subject);
CREATE INDEX IF NOT EXISTS idx_app_user_email ON app_user (email);
CREATE INDEX IF NOT EXISTS idx_app_user_status ON app_user (status);
CREATE INDEX IF NOT EXISTS idx_user_role_role_id ON user_role (role_id);
CREATE INDEX IF NOT EXISTS idx_user_role_user_id ON user_role (user_id);
