package dev.nautchkafe.mojang.rest;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;

import java.util.concurrent.CompletableFuture;

final class AuthRestClient {

    private final AsyncHttpClient httpClient;

    HttpClientWrapper() {
        httpClient = Dsl.asyncHttpClient();
    }

    public CompletableFuture<Response> get(final String url) {
        return CompletableFuture.supplyAsync(() -> {
            return Try.of(() -> httpClient.prepareGet(url).execute().get())
                    .onFailure(e -> System.err.println("> HTTP request failed: " + e.getMessage()))
                    .getOrElseThrow(() -> new RuntimeException(">   Failed to execute HTTP request for URL: " + url));
        });
    }

    public void close() {
        httpClient.close();
    }
}