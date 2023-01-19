package fr.redstonneur1256.omega.messages.code;

public class ExecutionRequest {

    public int executionId;
    public String environment;
    public String code;

    public ExecutionRequest() {
    }

    public ExecutionRequest(int executionId, String environment, String code) {
        this.executionId = executionId;
        this.environment = environment;
        this.code = code;
    }

}
