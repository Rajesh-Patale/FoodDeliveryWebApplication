package com.FoodDeliveryWebApp.Entity;

import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDateTime;
 
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @OneToOne
    @JoinColumn(name = "order_id")
    private Orders order;
 
    private LocalDateTime invoiceDate;
}