package com.FoodDeliveryWebApp.ServiceI;

import com.FoodDeliveryWebApp.Entity.Menu;
import com.FoodDeliveryWebApp.Exception.MenuNotFoundException;
import com.FoodDeliveryWebApp.Exception.RestaurantNotFoundException;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface MenuService {

    Menu saveMenu(Menu menu, List<MultipartFile> imageFiles);

    List<Menu> getAllMenus();

    void deleteMenuByIdAndName(Long menuId);

    Menu updateMenuByRestaurantIdAndItemName(Long restaurantId, String itemName, Menu menu, List<MultipartFile> imageFiles) throws IOException;

    Menu getMenuById(Long menuId);

    Menu updateMenu(Long menuId, Menu menu) throws MenuNotFoundException;

    List<Menu> getItemNamesByRestaurantName(String restaurantName) throws RestaurantNotFoundException;

}
