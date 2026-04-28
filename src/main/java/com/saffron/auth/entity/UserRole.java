package com.saffron.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "user_role")
@IdClass(UserRole.UserRoleId.class)
@Getter
@Setter
public class UserRole {

    @Id
    @Column(name = "userId", length = 50)
    private String userId;

    @Id
    @Column(name = "roleCode", length = 50)
    private String roleCode;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class UserRoleId implements Serializable {
        private String userId;
        private String roleCode;
    }
}
