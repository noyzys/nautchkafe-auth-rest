use crate::auth_client::{AuthRestClient, ApiProvider};
use crate::rate_limiter::AuthMojangRateLimiter;
use crate::models::AuthUserProfile;
use crate::auth_cache::AuthMapper;
use futures::TryFutureExt;

pub struct AuthValidator {
    http_client: AuthRestClient,
    rate_limiter: AuthMojangRateLimiter,
    auth_cache: AuthMapper,
}

impl AuthValidator {
    
    pub fn new(http_client: AuthRestClient, rate_limiter: AuthMojangRateLimiter, auth_cache: AuthMapper) -> Self {
        AuthValidator {
            http_client,
            rate_limiter,
            auth_cache,
        }
    }

    pub async fn authenticate(&self, username: String) -> Result<AuthUserProfile, String> {
        self.auth_cache
            .get(&username)
            .await
            .or_else(|| self.fetch_user_profile(username).await)
    }

    fn fetch_user_profile(&self, username: String) -> Result<AuthUserProfile, String> {
        let url = format!("https://api.mojang.com/users/profiles/minecraft/{}", username);
        let response = self.http_client.get(&url).await?;
        
        self.http_client
            .parse_response(&response, ApiProvider::Mojang)
            .or_else(|_| self.fallback_to_ashcon(username))
    }

    fn fallback_to_ashcon(&self, username: String) -> Result<AuthUserProfile, String> {
        let url = format!("https://api.ashcon.app/mojang/v2/uuid/{}", username);
        let response = self.http_client.get(&url).await?;
        
        self.http_client
            .parse_response(&response, ApiProvider::Ashcon)
            .map_err(|e| format!("Error from Ashcon API: {}", e))
    }
}
