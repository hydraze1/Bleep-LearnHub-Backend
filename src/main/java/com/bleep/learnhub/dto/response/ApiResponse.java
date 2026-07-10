package com.bleep.learnhub.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private T data;
    private String message;
    private String error;
    private String errorCode;

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .data(data)
                .message(message)
                .error("200")
                .errorCode(null)
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .data(null)
                .message(message)
                .error("200")
                .errorCode(null)
                .build();
    }

    public static <T> ApiResponse<T> failure(String message, String error, String errorCode) {
        return ApiResponse.<T>builder()
                .data(null)
                .message(message)
                .error(error)
                .errorCode(errorCode)
                .build();
    }

    public static <T> ApiResponse<T> failure(String message, String error) {
        return ApiResponse.<T>builder()
                .data(null)
                .message(message)
                .error(error)
                .errorCode(null)
                .build();
    }
}
