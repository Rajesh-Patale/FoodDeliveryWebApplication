package com.FoodDeliveryWebApp.Exception;

public class MenuItemNotFoundException extends RuntimeException {
    public MenuItemNotFoundException(String message) {

        super(message);
    }
}