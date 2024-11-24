package dev.nautchkafe.mojang.rest;

import org.asynchttpclient.Response;

import java.util.concurrent.CompletableFuture;

interface AuthRequestQueue {

    CompletableFuture<Response> enqueueRequest(final AuthSupplier<CompletableFuture<Response>> request);
}