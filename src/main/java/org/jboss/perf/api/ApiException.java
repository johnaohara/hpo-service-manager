package org.jboss.perf.api;

public class ApiException extends RuntimeException {
    public ApiResult failure;
    public ApiException(ApiResult failure) {
        this.failure = failure;
    }
}
