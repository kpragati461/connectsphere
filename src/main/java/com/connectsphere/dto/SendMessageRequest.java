package com.connectsphere.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private String content;
    private Long sharedPostId;
}
