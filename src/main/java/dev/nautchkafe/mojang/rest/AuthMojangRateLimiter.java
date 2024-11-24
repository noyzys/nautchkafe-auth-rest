package dev.nautchkafe.mojang.rest;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

final class AuthMojangRateLimiter implements AuthRateLimiter {

    private final int maxRequestsPerSecond;
    private final Duration retryBackoffDuration;

    AuthMojangRateLimiter(final int maxRequestsPerSecond, final Duration retryBackoffDuration) {
    	this.maxRequestsPerSecond = maxRequestsPerSecond;
        this.retryBackoffDuration = retryBackoffDuration;
    }

    @Override
    public <TYPE> CompletableFuture<Either<String, TYPE>> executeWithRateLimit(final Supplier<CompletableFuture<Either<String, TYPE>>> requestSupplier) {
        return Try.of(this::isRateLimitExceeded)
                  .getOrElse(false) 
                  .flatMap(exceeded -> exceeded ? retryWithBackoff(requestSupplier, 1) : requestSupplier.get());
    }

    private boolean isRateLimitExceeded() {
        final Instant now = Instant.now();
        final boolean exceeded = Duration.between(getLastRequestTime(), now).getSeconds() <= 1;
        return exceeded;
    }

    private Instant getLastRequestTime() {
        return Instant.now();
    }

    private <TYPE> CompletableFuture<Either<String, TYPE>> retryWithBackoff(final Supplier<CompletableFuture<Either<String, TYPE>>> requestSupplier, 
    		final int attemptCount) {
        final long backoffTime = retryBackoffDuration.toMillis() * attemptCount;
        return CompletableFuture.supplyAsync(() -> {
            Try.run(() -> TimeUnit.MILLISECONDS.sleep(backoffTime))
                .onFailure(e -> e.printStackTrace());
            
            return requestSupplier.get();
        }).thenCompose(response -> response);
    }
}