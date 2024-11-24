package dev.nautchkafe.mojang.rest;

import java.util.UUID;

record AuthUserProfile(
    UUID uuid, 
    String username, 
    boolean premium
) {
}