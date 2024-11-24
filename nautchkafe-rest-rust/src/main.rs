#[tokio::main]
async fn main() {
    let http_client = AuthRestClient::new();
    let rate_limiter = AuthMojangRateLimiter::new(5);
    let auth_cache = AuthMapper::new();

    let auth_validator = AuthValidator::new(http_client, rate_limiter, auth_cache);

    match auth_validator.authenticate("noyzys".to_string()).await {
        Ok(profile) => println!("> Authenticated user: {}", profile.username),
        Err(e) => println!("> Authentication failed: {}", e),
    }
}