package org.phuchoang2005.ecommerce.api;

import java.time.LocalDateTime;

public class ApiErrorResponse {
    private final String timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    public ApiErrorResponse(int status, String error, String message, String path){
        this.message = message;
        this.error = error;
        this.timestamp = LocalDateTime.now().toString();
        this.status = status;
        this.path = path;
    }
    public int getStatus(){return this.status;}
    public String getMessage(){return this.message;}
}
