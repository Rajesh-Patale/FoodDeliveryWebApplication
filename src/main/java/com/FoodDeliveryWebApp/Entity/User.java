package com.FoodDeliveryWebApp.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(exclude = "reviews")
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

    @Lob
    @Column(name = "profile_picture",columnDefinition = "LONGBLOB")
    @Basic(fetch = FetchType.LAZY)
    @Nullable
    private byte[] profilePicture;

    private boolean verified = false;

    //one user have many location
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Location> locations = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "user-reviews")
    private List<Review> reviews;

    @OneToOne
    @Transient
    private ForgotPasswordOtp forgotPasswordOtp;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Message> messages = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    @JsonManagedReference(value="user_subscription")
    private List<Subscription> subscription;

}
