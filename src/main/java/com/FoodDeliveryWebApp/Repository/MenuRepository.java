package com.FoodDeliveryWebApp.Repository;

import com.FoodDeliveryWebApp.Entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    //search restaurant by using item name
    @Query("SELECT m FROM Menu m WHERE LOWER(REPLACE(m.itemName, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:itemName, ' ', ''), '%'))")
    List<Menu> findByItemName(String itemName);

    //search, by using item name and restaurant id
    @Query("SELECT m FROM Menu m WHERE m.itemName = :itemName AND m.restaurant.id = :restaurantId")
    Optional<Menu> findByRestaurantIdAndItemName(@Param("restaurantId") Long restaurantId, @Param("itemName") String itemName);

    @Query("SELECT m FROM Menu m JOIN m.restaurant r WHERE r.restaurantName = :restaurantName")
    List<Menu> findByRestaurantName(@Param("restaurantName") String restaurantName);

    //List<Menu> findByRestaurantId(@Param("restaurant_id") Long restaurantId);
}
