CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    provider VARCHAR(32) NOT NULL CHECK (provider IN ('GOOGLE')),
    provider_subject VARCHAR(255) NOT NULL,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    profile_image_url VARCHAR(1024),
    status VARCHAR(32) NOT NULL CHECK (status IN ('ACTIVE', 'BANNED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_app_user_provider_subject UNIQUE (provider, provider_subject),
    CONSTRAINT uk_app_user_email UNIQUE (email),
    CONSTRAINT uk_app_user_username UNIQUE (username)
);

CREATE TABLE role (
    id UUID PRIMARY KEY,
    name VARCHAR(32) NOT NULL CHECK (name IN ('ROLE_USER', 'ROLE_AUTHOR', 'ROLE_ADMIN')),
    CONSTRAINT uk_role_name UNIQUE (name)
);

CREATE TABLE user_role (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL,
    assigned_by UUID,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

CREATE INDEX idx_app_user_email ON app_user(email);
CREATE INDEX idx_app_user_status ON app_user(status);
CREATE INDEX idx_user_role_role_id ON user_role(role_id);
CREATE INDEX idx_user_role_user_id ON user_role(user_id);
