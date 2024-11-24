package dev.nautchkafe.mojang.rest;

import dev.nautchkafe.mojang.rest.AuthRequestApiConstants;
import dev.nautchkafe.mojang.rest.AuthRestClient;
import dev.nautchkafe.mojang.rest.AuthUserProfile;
import dev.nautchkafe.mojang.rest.AuthRequestQueuePool;
import io.vavr.control.Either;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import org.asynchttpclient.Response;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

final class AuthValidator implements AuthCoordinator {

    private final AuthRestClient httpClient;
    private final AuthRequestQueuePool requestQueue;
    private final AuthRateLimiter rateLimiter;

    AuthValidator(final AuthRestClient httpClient, final AuthRequestQueuePool requestQueue, 
        final AuthRateLimiter rateLimiter) {
        this.httpClient = httpClient;
        this.requestQueue = requestQueue;
    }

    @Override
    public CompletableFuture<Either<String, AuthUserProfile>> authenticate(final String username) {
        return validateUsername(username)
                .map(validName -> fetchFromApi(String.format(AuthRequestApiConstants.MOJANG_API_URL, validName), this::parseMojangResponse)
                        .thenCompose(result -> result.isRight() 
                                ? CompletableFuture.completedFuture(result)
                                : fetchFromApi(String.format(AuthRequestApiConstants.ASHCON_API_URL, validName), this::parseAshconResponse)))
                .getOrElseGet(error -> CompletableFuture.completedFuture(Either.left(error)));
    }

    private Validation<String, String> validateUsername(final String username) {
        return Validation
                .combine(validateNotNull(username), validateUsernamePattern(username))
                .ap((validName, patternMatch) -> validName);
    }

    private Validation<String, String> validateNotNull(final String username) {
        return username == null
                ? Validation.invalid("Username cannot be null")
                : Validation.valid(username);
    }

    private Validation<String, String> validateUsernamePattern(final String username) {
        return username.matches("^[a-zA-Z0-9_]{3,16}$")
                ? Validation.valid(username)
                : Validation.invalid("Username must match the pattern: ^[a-zA-Z0-9_]{3,16}$");
    }

    private CompletableFuture<Either<String, AuthUserProfile>> fetchFromApi(
            final String url,
            final Function<Response, Either<String, AuthUserProfile>> responseParser) {
        return requestQueue.enqueueRequest(() -> httpClient.get(url))
                .thenApply(Try::of)
                .thenApply(responseTry -> responseTry
                        .map(Either::<String, Response>right)
                        .getOrElseGet(t -> Either.left("HTTP error: " + t.getMessage())))
                .thenApply(responseEither -> responseEither.flatMap(responseParser));
    }

    private CompletableFuture<Either<String, AuthUserProfile>> fetchFromApi(final String url,
            final Function<Response, Either<String, AuthUserProfile>> responseParser) {
        return rateLimiter.executeWithRateLimit(() -> requestQueue.enqueueRequest(() -> httpClient.get(url))
            .thenApply(Try::of)
            .thenApply(responseTry -> responseTry
                .map(Either::right)
                .getOrElseGet(t -> Either.left("HTTP error: " + t.getMessage()))))
        .thenApply(responseEither -> responseEither.flatMap(responseParser));
    }

    private Either<String, AuthUserProfile> parseMojangResponse(final Response response) {
        return parseApiResponse(response, body -> {
            Either<String, String> uuidResult = extractJsonValue(body, "id");
            Either<String, String> usernameResult = extractJsonValue(body, "name");

            if (uuidResult.isLeft() || usernameResult.isLeft()) {
                return Either.left("Error parsing Mojang response: " + uuidResult.getLeft() + ", " + usernameResult.getLeft());
            }

            return Either.right(new AuthUserProfile(UUID.fromString(uuidResult.get()), usernameResult.get()));
        });
    }

    private Either<String, AuthUserProfile> parseAshconResponse(final Response response) {
        return parseApiResponse(response, body -> {
            final Either<String, String> uuidResult = extractJsonValue(body, "uuid");
            final Either<String, String> usernameResult = extractJsonValue(body, "username");

            if (uuidResult.isLeft() || usernameResult.isLeft()) {
                return Either.left("Error parsing Ashcon response: " + uuidResult.getLeft() + ", " + usernameResult.getLeft());
            }

            return Either.right(new AuthUserProfile(UUID.fromString(uuidResult.get()), usernameResult.get()));
        });
    }

    private Either<String, AuthUserProfile> parseApiResponse(final Response response, 
        final Function<String, Either<String, AuthUserProfile>> parser) {
        if (response.getStatusCode() != 200) {
            return Either.left("API response invalid: " + response.getStatusCode());
        }

        final String body = response.getResponseBody();
        return parser.apply(body);
    }

    // split -> Jackson databind
    private Either<String, String> extractJsonValue(final String json, final String key) {
        return Either.try(() -> json.split("\"" + key + "\":\"")[1].split("\"")[0])
                    .mapLeft(e -> "Error extracting key '" + key + "' from JSON: " + e.getMessage());
    }
}