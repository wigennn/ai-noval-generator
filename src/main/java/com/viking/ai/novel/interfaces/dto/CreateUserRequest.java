package com.viking.ai.novel.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    
    private String password;
    
    @NotBlank(message = "手机号不能为空")
    private String phone;
    
    private String email;
}
