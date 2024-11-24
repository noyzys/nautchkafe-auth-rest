## Rust & Java Rest-Api 
* library designed to handle rate limiting for authentication requests, specifically for Mojang and similar services.

## Features:
* Rate Limiting: Ensures requests are spaced by a defined interval to avoid exceeding rate limits.
* Request Queue (Rq): The RequestQueue class is responsible for managing a queue of HTTP requests to be processed sequentially.
* Asynchronous Design: Utilizes async/await syntax us execution, making it highly efficient for concurrent network requests.
* Backoff Mechanism: Implements a retry strategy with backoff duration when the rate limit is exceeded.
* Concurrency Safe: mutex to manage shared state across multiple async tasks, ensuring safe concurrent access to the rate limiter state.
* Validations

### Java use case:
```java
public static void main(String[] args) throws ExecutionException, InterruptedException {
    AuthRestClient httpClient = new AuthRestClient(); // HTTP client to make requests
    AuthRequestQueuePool requestQueue = new AuthRequestQueuePool(); // Request queue pool to manage request queuing
    AuthRateLimiter rateLimiter = new AuthRateLimiter(5, Duration.ofSeconds(1)); // Rate limiter with a max of 5 requests per second

    AuthValidator authValidator = new AuthValidator(httpClient, requestQueue, rateLimiter);
    String username = "nautchkafe";

    CompletableFuture<Either<String, AuthUserProfile>> authenticationResult = authValidator.authenticate(username);

    authenticationResult.thenAccept(result -> result.peek(authUserProfile -> {
        System.out.println("Authentication successful!");
        System.out.println("Username: " + authUserProfile.name());
        System.out.println("UUID: " + authUserProfile.uuid());
    }).peekLeft(error -> {
        System.err.println("Authentication failed: " + error);
    })).join();
}
```

### Rust use case:
```rust
async fn main() {
    let http_client = AuthRestClient::new();
    let rate_limiter = AuthMojangRateLimiter::new(5);
    let auth_validator = AuthValidator::new(http_client, rate_limiter);

    // Attempt authentication
    match auth_validator.authenticate("sus".to_string()).await {
        Ok(Either::Right(auth_user_profile)) => {
            println!("Authentication successful!");
            println!("Username: {}", auth_user_profile.username);
            println!("UUID: {}", auth_user_profile.uuid);
  }
}
```

```
sout logger:
Rate limited request for user: sus
Authenticated user: sus
UUID: 9d5f1a90-c4f9-412b-9d93-4d2a5f7a3f62
```

### Velocity proxy support
```java
// import com.velocitypowered.api.proxy.Player, import com.velocitypowered.api.proxy.ProxyServer
public CompletableFuture<Either<String, AuthUserProfile>> authenticateWithRateLimit(Player player) {
    String username = player.getUsername();
    return isPremium(username).thenCompose(isPremium -> {
        return Option.of(isPremium).filter(Boolean::booleanValue)
            .map(_ -> {
                System.out.println("User is premium, skipping rate limit for: " + username);
                return authCoordinator.authenticate(username); 
            
            }).getOrElse(() -> {
                return rateLimiter.executeWithRateLimit(() -> authCoordinator.authenticate(username));
        });
    });
}

//event
void onPlayerJoin(Player player) {
    authenticateWithRateLimit(player).thenAccept(result -> result.peek(authUserProfile -> {
        System.out.println("Authentication successful for: " + authUserProfile.getUsername());
    }).peekLeft(error -> {
        System.err.println("Authentication failed for: " + error);
    }));
}

@Listener
public void onPlayerJoin(PlayerJoinEvent event) {
    join(event.getPlayer()); 
}
```

**If you are interested in exploring functional programming and its applications within this project visit the repository at [vavr-in-action](https://github.com/noyzys/bukkit-vavr-in-action), [fp-practice](https://github.com/noyzys/fp-practice).**

