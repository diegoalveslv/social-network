package com.company.SocialNetwork.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomErrorResponse {
    private int status;
    private List<String> messages;
}