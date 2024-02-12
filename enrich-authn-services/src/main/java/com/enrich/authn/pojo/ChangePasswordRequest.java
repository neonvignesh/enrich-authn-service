package com.enrich.authn.pojo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "User ID cannot be blank")
    private String user_id;

    @NotBlank(message = "Old password cannot be blank")
    private String old_password;

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    // Getters and setters...
}
