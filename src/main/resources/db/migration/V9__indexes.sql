CREATE INDEX ix_user_account_status ON user_account (status);
CREATE INDEX ix_user_account_created_at ON user_account (created_at DESC);
CREATE INDEX ix_password_history_user ON password_history (user_id, created_at DESC);
CREATE INDEX ix_login_attempt_email ON login_attempt (email, attempted_at DESC);

CREATE INDEX ix_refresh_token_user ON refresh_token (user_id) WHERE revoked_at IS NULL;
CREATE INDEX ix_api_key_org ON api_key (org_id) WHERE revoked_at IS NULL;
CREATE INDEX ix_membership_org ON membership (org_id);
CREATE INDEX ix_invitation_email ON invitation (email) WHERE accepted_at IS NULL;

CREATE INDEX ix_client_device_org ON client_device (org_id, status);
CREATE INDEX ix_agent_session_user ON agent_session (user_id, created_at DESC);
CREATE INDEX ix_agent_tool_call_msg ON agent_tool_call (message_id);
CREATE INDEX ix_model_usage_org_day ON model_usage (org_id, recorded_at DESC);
CREATE INDEX ix_audit_log_actor ON audit_log (actor_id, created_at DESC);
