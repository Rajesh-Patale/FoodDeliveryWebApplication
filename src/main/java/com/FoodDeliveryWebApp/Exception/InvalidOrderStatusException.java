package com.FoodDeliveryWebApp.Exception;

public class InvalidOrderStatusException extends RuntimeException {
    public InvalidOrderStatusException(String message) {

        super(message);
    }
}