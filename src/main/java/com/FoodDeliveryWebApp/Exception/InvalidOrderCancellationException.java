package com.FoodDeliveryWebApp.Exception;

public class InvalidOrderCancellationException extends RuntimeException {
    public InvalidOrderCancellationException(String message) {

        super(message);
    }
}