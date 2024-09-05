package com.FoodDeliveryWebApp.Controller;

import com.FoodDeliveryWebApp.Entity.Orders;
import com.FoodDeliveryWebApp.Exception.InvalidOrderCancellationException;
import com.FoodDeliveryWebApp.Exception.InvalidOrderStatusException;
import com.FoodDeliveryWebApp.Exception.OrdersNotFoundException;
import com.FoodDeliveryWebApp.ServiceI.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin("*")
public class OrdersController {

    private final Logger logger = LoggerFactory.getLogger(OrdersController.class);

    @Autowired
    private OrdersService orderService;

    @PostMapping("/order/create")
    public ResponseEntity<?> createOrder(
            @RequestParam Long userId,
            @RequestParam Long restaurantId,
            @RequestParam List<Long> menuIds,
            @RequestParam List<Integer> quantities) {
        try {
            // Validate input parameters
            if (menuIds.size() != quantities.size()) {
                logger.warn("Mismatch between menuIds and quantities size");
                return ResponseEntity.badRequest().body("The size of menuIds and quantities must match.");
            }
            // Create the order
            Orders order = orderService.createOrder(userId, restaurantId, menuIds, quantities);

            logger.info("Order successfully created with ID {}", order.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(order);

        } catch (Exception e) {
            logger.error("An error occurred while creating the order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while creating the order: " + e.getMessage());
        }
    }

    @GetMapping("/byOrderId/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable Long orderId) {
        try {
            Orders order = orderService.getOrder(orderId);

            if (order == null) {
                logger.warn("Order with ID {} not found", orderId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Order not found with ID: " + orderId);
            }

            logger.info("Order with ID {} retrieved successfully", orderId);
            return ResponseEntity.ok(order);

        } catch (Exception e) {
            logger.error("An error occurred while retrieving the order with ID {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving the order: " + e.getMessage());
        }
    }


    @GetMapping("/order/byUserId/{userId}")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable("userId") Long userId) {
        try {
            List<Orders> orders = orderService.getOrdersByUserId(userId);

            if (orders == null || orders.isEmpty()) {
                logger.warn("No orders found for user with ID {}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No orders found for user with ID: " + userId);
            }

            logger.info("Successfully retrieved orders for user with ID {}", userId);
            return ResponseEntity.ok(orders);

        } catch (Exception e) {
            logger.error("An error occurred while finding orders for user with ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving orders: " + e.getMessage());
        }
    }

    @PutMapping("/order/updateStatus/{orderId}/{paymentSuccess}")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @PathVariable boolean paymentSuccess) {
        try {
            // Update the order status based on payment success or failure
            Orders updatedOrder = orderService.updateOrderStatus(orderId, paymentSuccess);

            // Return the updated order
            return ResponseEntity.ok(updatedOrder);

        } catch (OrdersNotFoundException e) {
            // Handle order not found scenario
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (InvalidOrderStatusException e) {
            // Handle invalid status update scenario
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (Exception e) {
            // Handle general errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating the order status: " + e.getMessage());
        }
    }


    @PutMapping("/order/cancel/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        try {
            // Attempt to cancel the order
            Orders cancelledOrder = orderService.cancelOrder(orderId);

            // Return the updated order
            return ResponseEntity.ok(cancelledOrder);

        } catch (OrdersNotFoundException e) {
            // Handle order not found scenario
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (InvalidOrderCancellationException e) {
            // Handle invalid cancellation scenario
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (InvalidOrderStatusException e) {
            // Handle invalid status scenario
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (Exception e) {
            // Handle general errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while cancelling the order: " + e.getMessage());
        }
    }

}