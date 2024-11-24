use std::time::{Instant, Duration};
use tokio::time::sleep;
use std::sync::Arc;
use tokio::sync::Mutex;

pub struct AuthMojangRateLimiter {
    max_requests_per_second: u32,
    last_request_time: Arc<Mutex<Instant>>,
}

impl AuthMojangRateLimiter {

    pub fn new(max_requests_per_second: u32) -> Self {
        AuthMojangRateLimiter {
            max_requests_per_second,
            last_request_time: Arc::new(Mutex::new(Instant::now())),
        }
    }

    pub async fn execute_with_rate_limit<F, T>(&self, request_fn: F) -> Result<T, String> 
    where
        F: FnOnce() -> Result<T, String> + Send + 'static,
        T: Send + 'static,
    {
        let now = Instant::now();
        let mut last_request_time = self.last_request_time.lock().await;

        if now.duration_since(*last_request_time).as_secs() < 1 {
            let backoff_time = Duration::from_secs(1) - now.duration_since(*last_request_time);
            sleep(backoff_time).await;
        }

        *last_request_time = Instant::now();

        request_fn()
    }
}