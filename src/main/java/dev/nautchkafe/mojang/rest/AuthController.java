package dev.nautchkafe.mojang.rest;

import dev.nautchkafe.mojang.rest.AuthUserProfile;
import dev.nautchkafe.mojang.rest.AuthService;
import io.vavr.control.Either;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/auth")
final class AuthController {

    private final AuthService authService;

    AuthController(final AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/player")
    public CompletableFuture<Either<String, PlayerProfile>> findPlayer(@RequestParam final String username) {
        return authService.authenticate(username);
    }
}