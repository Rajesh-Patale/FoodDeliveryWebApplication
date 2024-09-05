package com.FoodDeliveryWebApp.ServiceI;

import com.FoodDeliveryWebApp.Entity.Invoice;
import com.FoodDeliveryWebApp.Entity.Orders;
 
public interface InvoiceService {
    Invoice generateInvoice(Orders order);
 
    Invoice getInvoiceById(Long invoiceId);
}