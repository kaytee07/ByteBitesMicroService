package com.bytebites.authservice.dto;
import java.util.Map;

public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private long timeStamp;
    private Map<String, String> errors;

    public static ErrorResponse of(int status, String error, String message){
        ErrorResponse response = new ErrorResponse();
        response.status = status;
        response.error = error;
        response.message = message;
        response.timeStamp = System.currentTimeMillis();
        return  response;
    }

    public static ErrorResponse of(int status, String error, Map<String, String> errors){
        ErrorResponse response = new ErrorResponse();
        response.status = status;
        response.error = error;
        response.errors = errors;
        response.timeStamp = System.currentTimeMillis();
        return  response;
    }

}
