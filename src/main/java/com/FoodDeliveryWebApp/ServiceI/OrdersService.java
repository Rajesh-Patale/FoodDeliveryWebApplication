package com.FoodDeliveryWebApp.ServiceI;

import com.FoodDeliveryWebApp.Entity.Orders;
import java.util.List;

public interface OrdersService {

    Orders createOrder(Long userId, Long restaurantId, List<Long> menuIds, List<Integer> quantities);
    Orders getOrder(Long orderId);
    List<Orders> getOrdersByUserId(Long userId);
    Orders getOrderById(Long orderId);
    Orders updateOrderStatus(Long orderId, boolean paymentSuccess);
    Orders cancelOrder(Long orderId);
}