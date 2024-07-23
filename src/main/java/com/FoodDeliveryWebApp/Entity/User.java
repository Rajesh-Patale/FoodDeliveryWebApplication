package com.FoodDeliveryWebApp.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String gender;

    @Column(unique = true)
    private String mobileNo;

    private String address;

    @Column(unique = true)
    private String username;

    private String password;

    private String confirmPassword;
//    @OneToMany
//    @JoinColumn(
//            name = "Transaction",referencedColumnName = "Id"
//    )
//
//    private TransactiobDetails transactiobDetails;
}
