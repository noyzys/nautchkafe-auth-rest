package dev.nautchkafe.mojang.rest;

final class AuthRequestApiConstants {

    public static final String MOJANG_API_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    public static final String ASHCON_API_URL = "https://api.ashcon.app/mojang/v2/uuid/%s";

    private AuthRequestApiConstants() {
        throw new AssertionError("> Cannot instantiate constants class");
    }
}