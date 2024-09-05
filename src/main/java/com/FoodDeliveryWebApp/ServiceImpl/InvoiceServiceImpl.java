package com.FoodDeliveryWebApp.ServiceImpl;

import com.FoodDeliveryWebApp.Entity.Invoice;
import com.FoodDeliveryWebApp.Entity.Orders;
import com.FoodDeliveryWebApp.Repository.InvoiceRepository;
import com.FoodDeliveryWebApp.ServiceI.InvoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
 
@Service
public class InvoiceServiceImpl implements InvoiceService {
 
    private static final Logger logger = LoggerFactory.getLogger(InvoiceServiceImpl.class);
 
    @Autowired
    private InvoiceRepository invoiceRepository;
 
    @Override
    public Invoice generateInvoice(Orders order) {
        // Ensure that the order has the necessary details
        if (order.getOrderItems().isEmpty()) {
            throw new RuntimeException("Order has no items. Cannot generate an invoice.");
        }
        // Create a new invoice object
        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setInvoiceDate(LocalDateTime.now());
 
        // Save the invoice to the repository
        return invoiceRepository.save(invoice);
    }
 
 
    @Override
    public Invoice getInvoiceById(Long invoiceId) {
        try {
            Optional<Invoice> invoice = invoiceRepository.findById(invoiceId);
            if (invoice.isPresent()) {
                return invoice.get();
            } else {
                logger.warn("Invoice with ID {} not found", invoiceId);
                return null;
            }
        } catch (Exception e) {
            logger.error("An error occurred while retrieving the invoice with ID {}: {}", invoiceId, e.getMessage());
            throw new RuntimeException("An error occurred while retrieving the invoice", e);
        }
    }
}