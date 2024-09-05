package com.FoodDeliveryWebApp.Repository;

import com.FoodDeliveryWebApp.Entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

}