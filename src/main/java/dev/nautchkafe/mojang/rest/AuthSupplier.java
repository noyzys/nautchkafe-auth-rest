package dev.nautchkafe.mojang.rest;

@FunctionalInterface
interface SupplierWithException<TYPPE> {
    
    TYPE get() throws Exception;
}