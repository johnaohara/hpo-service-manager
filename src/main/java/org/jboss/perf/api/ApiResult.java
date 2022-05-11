package org.jboss.perf.api;

public class ApiResult {

    enum NewExperimentStatus {
        SUCCESS,
        FAILURE
    }

    public static ApiResult failure() {
        return new ApiResult(NewExperimentStatus.FAILURE);
    }
    public static ApiResult failure(String msg) {
        return new ApiResult(NewExperimentStatus.FAILURE, msg);
    }

    public static ApiResult success(){
        return new ApiResult(NewExperimentStatus.SUCCESS);
    }

    public ApiResult(NewExperimentStatus status) {
        this(status, "");
    }
    public ApiResult(NewExperimentStatus status, String msg) {
        this.status = status;
        this.message = msg;
    }

    public NewExperimentStatus status;
    public String message;

}
