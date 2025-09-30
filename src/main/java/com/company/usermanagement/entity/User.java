package com.company.usermanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column
    @JsonIgnore
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column()
    private String name;

    @Column
    private boolean enabled;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles;

    //

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.password; // Restituisce la password salvata
    }

    @Override
    public String getUsername() {
        return this.username; // Restituisce lo username salvato
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Per ora lo lasciamo sempre a true
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Per ora lo lasciamo sempre a true
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Per ora lo lasciamo sempre a true
    }

    @Override
    public boolean isEnabled() {
        return this.enabled; // Restituisce il valore del nostro campo 'enabled'
    }

}
