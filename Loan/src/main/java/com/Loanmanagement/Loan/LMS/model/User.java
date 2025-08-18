package com.Loanmanagement.Loan.LMS.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ safer for DB auto-increment
    private Integer id;

    private String firstname;
    private String lastname;

    @Column(unique = true, nullable = false) // ✅ email must be unique
    private String email;

    private String password;
    private String phone;

    private boolean enabled = true; // ✅ default active

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // ✅ role must always exist
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private BankAccount bankAccount;

    @OneToMany(mappedBy = "user")
    private List<Loan> loans;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ✅ FIX: Spring expects "ROLE_" prefix for hasRole()
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email; // ✅ email is used as username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // ✅ adjust if you want expiration handling
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // ✅ adjust if you want lock handling
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // ✅ adjust if you want credential expiration
    }

    @Override
    public boolean isEnabled() {
        return enabled; // ✅ respects your enabled flag
    }
}
