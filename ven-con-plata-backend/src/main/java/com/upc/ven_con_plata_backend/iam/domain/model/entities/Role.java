package com.upc.ven_con_plata_backend.iam.domain.model.entities;

import com.upc.ven_con_plata_backend.iam.domain.model.valueobjects.Roles;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Entity
public class Role {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(unique = true, length = 20)
    private Roles name;

    public Role() {}

    public Role(Roles name) {
        this.name = name;
    }

    public String getStringName() { return name.name(); }

    public static Role getDefaultRole() { return new Role(Roles.INVESTOR); }

    public static Role toRoleFromName(String name) {
        return new Role(Roles.valueOf(name));
    }

    public static List<Role> validateRoleSet(List<Role> roles) {
        if (roles == null || roles.isEmpty()) return List.of(getDefaultRole());
        return roles;
    }
}
