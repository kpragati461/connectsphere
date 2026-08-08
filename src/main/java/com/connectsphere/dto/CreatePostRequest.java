package com.connectsphere.dto;

import lombok.Data;

@Data
public class CreatePostRequest {
    private String content;
    private String mediaUrl;
}