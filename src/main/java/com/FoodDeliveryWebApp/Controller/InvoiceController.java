package com.FoodDeliveryWebApp.Controller;
 
import com.FoodDeliveryWebApp.Entity.Invoice;

import com.FoodDeliveryWebApp.Entity.Orders;

import com.FoodDeliveryWebApp.ServiceI.InvoiceService;

import com.FoodDeliveryWebApp.ServiceI.OrdersService;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
 
@RestController

@RequestMapping("/api/invoices")

@CrossOrigin("*")

public class InvoiceController {
 
    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);
 
    @Autowired

    private InvoiceService invoiceService;
 
    @Autowired

    private OrdersService ordersService;
 
    @PostMapping("/invoice/generate/{orderId}")

    public ResponseEntity<?> generateInvoice(@PathVariable Long orderId) {

        try {

            // Retrieve the order using the provided orderId

            Orders order = ordersService.getOrderById(orderId);

            if (order == null) {

                logger.warn("Order with ID {} not found for invoice generation", orderId);

                return ResponseEntity.status(HttpStatus.NOT_FOUND)

                        .body("Order not found with ID: " + orderId);

            }

            // Generate the invoice for the retrieved order

            Invoice invoice = invoiceService.generateInvoice(order);
 
            logger.info("Invoice successfully generated for Order ID {}", orderId);

            return ResponseEntity.ok(invoice);
 
        } catch (Exception e) {

            logger.error("An error occurred while generating the invoice for Order ID {}: {}", orderId, e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body("An error occurred while generating the invoice: " + e.getMessage());

        }

    }
 
    @GetMapping("/invoice/getInvoiceById/{invoiceId}")

    public ResponseEntity<?> getInvoice(@PathVariable Long invoiceId) {

        try {

            Invoice invoice = invoiceService.getInvoiceById(invoiceId);

            if (invoice == null) {

                logger.warn("Invoice with ID {} not found", invoiceId);

                return ResponseEntity.status(HttpStatus.NOT_FOUND)

                        .body("Invoice not found with ID: " + invoiceId);

            }
 
            logger.info("Invoice with ID {} retrieved successfully", invoiceId);

            return ResponseEntity.ok(invoice);
 
        } catch (Exception e) {

            logger.error("An error occurred while retrieving the invoice with ID {}: {}", invoiceId, e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body("An error occurred while retrieving the invoice: " + e.getMessage());

        }

    }
 
}
 