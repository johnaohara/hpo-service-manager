package org.jboss.perf.api;

public class NewExperimentResult {

    enum NewExperimentStatus {
        SUCCESS,
        FAILURE
    }

    public static NewExperimentResult failure() {
        return new NewExperimentResult(NewExperimentStatus.FAILURE);
    }
    public static NewExperimentResult failure(String msg) {
        return new NewExperimentResult(NewExperimentStatus.FAILURE, msg);
    }

    static NewExperimentResult success(){
        return new NewExperimentResult(NewExperimentStatus.SUCCESS);
    }

    public NewExperimentResult(NewExperimentStatus status) {
        this(status, "");
    }
    public NewExperimentResult(NewExperimentStatus status, String msg) {
        this.status = status;
        this.message = msg;
    }

    public NewExperimentStatus status;
    public String message;

}
