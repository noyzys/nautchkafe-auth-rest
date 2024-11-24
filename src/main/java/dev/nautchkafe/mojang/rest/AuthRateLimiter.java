package dev.nautchkafe.mojang.rest;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

interface AuthRateLimiter {

    <TYPE> CompletableFuture<Either<String, TYPE>> executeWithRateLimit(final Supplier<CompletableFuture<Either<String, TYPE>>> requestSupplier);
}