use serde::{Deserialize, Serialize};
use uuid::Uuid;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AuthUserProfile {
    pub uuid: Uuid,
    pub username: String,
    pub premium: bool,
}