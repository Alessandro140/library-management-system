package com.example.library.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Where;

/**
 * Represents a user in the library system.
 * This entity is mapped to the 'users' table in the database.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Where(clause = "deleted=false")
@Table(name = "users")
public class User extends Auditable {

    /**
     * The unique identifier of the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The username of the user.
     */
    @Column(nullable = false, length = 255)
    private String username;

    /**
     * The password of the user.
     */
    @Column(nullable = false, length = 255)
    private String password;

    /**
     * The role of the user (e.g., ADMIN, MEMBER).
     */
    @Column(nullable = false)  @Enumerated(EnumType.STRING)
    private UserRole role;

    /**
     * The email of the user. Must be unique.
     */
    @Column(nullable = false, length = 255, unique = true)
    private String email;

    /**
     * The loans made by the user.
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private final Set<Loan> loans = new HashSet<>();

    /**
    * Indicates whether the User is deleted (soft delete).
    */
    private Boolean deleted = false;

}
