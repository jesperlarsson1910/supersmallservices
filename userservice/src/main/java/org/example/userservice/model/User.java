package org.example.userservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;

    public User() {}

    public User(String username, String email, String name) {
        this.username = username;
        this.email    = email;
        this.name     = name;
    }

    public Long getId()            { return id; }
    public String getUsername()    { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail()       { return email; }
    public void setEmail(String email)       { this.email = email; }
    public String getName()        { return name; }
    public void setName(String name)         { this.name = name; }
}