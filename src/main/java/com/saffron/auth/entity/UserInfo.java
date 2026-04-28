package com.saffron.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_info")
@Getter
@Setter
public class UserInfo {

    @Id
    @Column(name = "userId", length = 50)
    private String userId;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "deptId", length = 50)
    private String deptId;

    @Column(name = "userName", length = 100)
    private String userName;

    @Column(name = "email", length = 100)
    private String email;
}
