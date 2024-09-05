package com.FoodDeliveryWebApp.ServiceImpl;

import com.FoodDeliveryWebApp.Entity.*;
import com.FoodDeliveryWebApp.Exception.*;
import com.FoodDeliveryWebApp.Repository.*;
import com.FoodDeliveryWebApp.ServiceI.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrdersServiceImpl implements OrdersService {

    private final Logger logger = LoggerFactory.getLogger(OrdersServiceImpl.class);

    @Autowired
    private OrdersRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Override
    public Orders createOrder(Long userId, Long restaurantId, List<Long> menuIds, List<Integer> quantities) {
        try {
            logger.info("Creating order for user ID: {}, restaurant ID: {}", userId, restaurantId);
            // Retrieve and validate the user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("User with ID {} not found", userId);
                        return new UserNotFoundException("User not found with ID: " + userId);
                    });
            // Retrieve and validate the restaurant
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> {
                        logger.warn("Restaurant with ID {} not found", restaurantId);
                        return new RestaurantNotFoundException("Restaurant not found with ID: " + restaurantId);
                    });
            // Create the order object
            Orders order = new Orders();
            order.setUser(user);
            order.setRestaurant(restaurant);
            order.setDateAndTime(LocalDateTime.now());
            order.setOrderStatus("PENDING");

            double totalAmount = 0.0;
            logger.info("Total amount before delivery charge: {}", totalAmount);
            // Process each menu item and calculate the total amount
            for (int i = 0; i < menuIds.size(); i++) {
                Long menuId = menuIds.get(i);
                int quantity = quantities.get(i);

                // Retrieve and validate the menu item
                Menu menu = menuRepository.findById(menuId)
                        .orElseThrow(() -> {
                            logger.warn("Menu item with ID {} not found", menuId);
                            return new MenuItemNotFoundException("Menu item not found with ID: " + menuId);
                        });

                double itemTotalPrice = menu.getPrice() * quantity;
                totalAmount += itemTotalPrice;

                // Create and add the order item
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setMenu(menu);
                orderItem.setQuantity(quantity);
                orderItem.setItemTotalPrice(itemTotalPrice);

                order.getOrderItems().add(orderItem);
            }
            // Calculate additional charges
            double gst = totalAmount * 0.18; // 18% GST
            double deliveryCharge = 40.0; // Flat delivery charge
            double platformCharge = totalAmount * 0.05; // 5% platform fee
            double grandTotalPrice = totalAmount + gst + deliveryCharge + platformCharge;

            // Set the calculated amounts in the order
            order.setTotalAmount(totalAmount);
            order.setGst(gst);
            order.setDeliveryCharge(deliveryCharge);
            order.setPlatformCharge(platformCharge);
            order.setGrandTotalPrice(grandTotalPrice);

            Orders savedOrder = orderRepository.save(order);
            logger.info("Order created successfully with ID: {}", savedOrder.getId());

            return savedOrder;

        } catch (UserNotFoundException | RestaurantNotFoundException | MenuItemNotFoundException e) {
            logger.error("Order creation failed: {}", e.getMessage());
            throw new RuntimeException("Order creation failed: {}", e);

        } catch (Exception e) {
            logger.error("An unexpected error occurred during order creation: {}", e.getMessage());
            throw new RuntimeException("An error occurred while creating the order", e);
        }
    }


    @Override
    public Orders getOrder(Long orderId) {
        try {
            logger.info("Retrieving order with ID: {}", orderId);

            // Retrieve the order from the repository
            return orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        logger.warn("Order with ID {} not found ", orderId);
                        return new OrdersNotFoundException("Order not found with ID: " + orderId);
                    });

        } catch (OrdersNotFoundException e) {
            logger.error("Order not found exception: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            logger.error("An error occurred while retrieving the order with ID  {}: {}", orderId, e.getMessage());
            throw new RuntimeException("An error occurred while retrieving the order", e);
        }
    }


    @Override
    public List<Orders> getOrdersByUserId(Long userId) {
        try {
            logger.info("Retrieving orders for user with ID: {}", userId);

            // Fetch orders from the repository
            List<Orders> orders = orderRepository.findByUserId(userId);

            // Check if orders are found
            if (orders == null || orders.isEmpty()) {
                logger.warn("No orders found for user with ID {}", userId);
            } else {
                logger.info("Successfully retrieved {} orders for user with ID {}", orders.size(), userId);
            }

            return orders;

        } catch (Exception e) {
            logger.error("An error occurred while retrieving orders for user with ID {}: {}", userId, e.getMessage());
            throw new RuntimeException("An error occurred while retrieving orders", e);
        }
    }


    @Override
    public Orders getOrderById(Long orderId) {
        try {
            Optional<Orders> order = orderRepository.findById(orderId);
            if (order.isPresent()) {
                return order.get();
            } else {
                logger.warn("Order with ID {} not found", orderId);
                return null;
            }
        } catch (Exception e) {
            logger.error("An error occurred while retrieving the order with ID {}: {}", orderId, e.getMessage());
            // Optionally, you can rethrow the exception or handle it according to your needs
            throw new RuntimeException("An error occurred while retrieving the order", e);
        }
    }

    @Override
    public Orders updateOrderStatus(Long orderId, boolean paymentSuccess) {
        try {
            logger.info("Updating order status for order ID: {}", orderId);

            // Retrieve the order by ID
            Orders order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        logger.warn("Order with ID {} not found :", orderId);
                        return new OrdersNotFoundException("Order not found with ID: " + orderId);
                    });

            // Validate the current status
            if (!order.getOrderStatus().equals("PENDING")) {
                logger.warn("Order status update not allowed. Current status: {}", order.getOrderStatus());
                throw new InvalidOrderStatusException("Order status update not allowed from " + order.getOrderStatus());
            }

            // Update the order status based on payment success or failure
            if (paymentSuccess) {
                order.setOrderStatus("PAID");
                logger.info("Order ID: {} status updated to PAID", orderId);
            } else {
                order.setOrderStatus("FAILED");
                logger.info("Order ID: {} status updated to FAILED", orderId);
            }

            return orderRepository.save(order);

        } catch (OrdersNotFoundException | InvalidOrderStatusException e) {
            logger.error("Order status update failed: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            logger.error("An unexpected error occurred during order status update: {}", e.getMessage());
            throw new RuntimeException("An error occurred while updating the order status", e);
        }
    }


    @Override
    public Orders cancelOrder(Long orderId) {
        try {
            logger.info("Attempting to cancel order ID: {}", orderId);

            // Retrieve the order by ID
            Orders order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        logger.warn("Order with ID {} not found : ", orderId);
                        return new OrdersNotFoundException("Order not found with ID: " + orderId);
                    });

            // Check if the payment has failed
            if (order.getOrderStatus().equals("FAILED")) {
                order.setOrderStatus("CANCELLED");
                Orders cancelledOrder = orderRepository.save(order);
                logger.info("Order ID: {} has been cancelled due to payment failure", orderId);
                return cancelledOrder;
            }

            // Check if the order can be canceled within 2 minutes of being placed
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(order.getDateAndTime(), now);

            if (order.getOrderStatus().equals("PAID") && duration.toMinutes() <= 2) {
                order.setOrderStatus("CANCELLED");
                Orders cancelledOrder = orderRepository.save(order);
                logger.info("Order ID: {} has been cancelled within the allowed time frame", orderId);
                return cancelledOrder;
            } else if (order.getOrderStatus().equals("PAID")) {
                logger.warn("Cancellation time exceeded for order ID: {}", orderId);
                throw new InvalidOrderCancellationException("Order cannot be cancelled after 2 minutes of being placed");
            }

            // If the order is not in a state that can be cancelled
            logger.warn("Order ID: {} is not eligible for cancellation", orderId);
            throw new InvalidOrderStatusException("Order cannot be cancelled in its current status");

        } catch (OrdersNotFoundException | InvalidOrderCancellationException | InvalidOrderStatusException e) {
            logger.error("Order cancellation failed: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            logger.error("An unexpected error occurred during order cancellation: {}", e.getMessage());
            throw new RuntimeException("An error occurred while cancelling the order", e);
        }
    }

}