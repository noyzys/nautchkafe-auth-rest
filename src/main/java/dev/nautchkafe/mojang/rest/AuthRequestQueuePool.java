package dev.nautchkafe.mojang.rest;

import dev.nautchkafe.mojang.rest.AuthService;
import dev.nautchkafe.mojang.rest.AuthUserProfile;
import io.vavr.control.Either;
import io.vavr.collection.Queue;

import java.util.concurrent.CompletableFuture;

final class AuthRequestQueuePool implements AuthRequestQueue {

    private final Queue<CompletableFuture<Response>> requestQueue = Queue.empty();

    @Override
    public CompletableFuture<Response> enqueueRequest(final AuthSupplier<CompletableFuture<Response>> request) {
        final CompletableFuture<Response> futureRequest = new CompletableFuture<>();

        requestQueue = requestQueue.append(futureRequest);
        processQueue();

        return futureRequest;
    }

    private void processQueue() {
        Try.run(() -> {
            if (requestQueue.size() > 0) {
                final CompletableFuture<Response> queuedRequest = requestQueue.head();
                requestQueue = requestQueue.tail();

                request.get()
                        .thenAccept(response -> queuedRequest.complete(response))
                        .exceptionally(ex -> {
                            queuedRequest.completeExceptionally(ex);
                });
            }
        }).onFailure(ex -> {
            System.err.println("> Error processing request: " + ex.getMessage());
        });
    }
}