package com.manager.dbee.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Security_Administrator {

    private String admin_No;           // char(6), PK
    private String login_id;           // varchar(50), Unique
    private String password_hash;      // char(64)
    private String name;              // varchar(50)
    private String email;             // varchar(320), nullable
    private String phone;             // varchar(30), nullable

    private Boolean can_read;          // tinyint(1), nullable → Boolean
    private Boolean can_update;        // tinyint(1), nullable → Boolean
    private Boolean can_delete;        // tinyint(1), nullable → Boolean

    private LocalDateTime created_at;       // datetime, nullable
    private LocalDateTime updated_at;       // datetime, nullable
    private LocalDateTime last_login_at;     // datetime, nullable

    private Boolean is_active;              // tinyint(1), nullable → Boolean

    private LocalDateTime first_fail_at;     // datetime, nullable
    private Integer login_fail_count;        // int unsigned, nullable
    private LocalDateTime locked_until;     // datetime, nullable
}
