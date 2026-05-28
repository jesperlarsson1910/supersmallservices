package org.example.inventoryservice.controller;

public enum ChaosScenario {
    NORMAL,
    FAIL_BEFORE_PUBLISH,
    DUPLICATE_MESSAGE,
    DATA_CORRUPTION,
    TRANSIENT_FAILURE
}
