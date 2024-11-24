package dev.nautchkafe.mojang.rest;

import dev.nautchkafe.mojang.rest.AuthUserProfile;
import io.vavr.control.Either;

import java.util.concurrent.CompletableFuture;

interface AuthCoordinator {

    CompletableFuture<Either<String, AuthUserProfile>> authenticate(final String username);
}