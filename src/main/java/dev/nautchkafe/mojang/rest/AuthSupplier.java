package dev.nautchkafe.mojang.rest;

@FunctionalInterface
interface SupplierWithException<TYPE> {
    
    TYPE get() throws Exception;
}