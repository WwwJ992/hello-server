package com.stu.helloserver.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequestDTO {
    @NotBlank(message = "sessionId 不能为空")
    private String sessionId;

    @NotBlank(message = "message 不能为空")
    private String message;
}