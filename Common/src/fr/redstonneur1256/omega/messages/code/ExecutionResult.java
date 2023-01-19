package fr.redstonneur1256.omega.messages.code;

public class ExecutionResult {

    public int executionId;
    public boolean failure;
    public String text;

    public ExecutionResult() {
    }

    public ExecutionResult(int executionId, boolean failure, String text) {
        this.executionId = executionId;
        this.failure = failure;
        this.text = text;
    }

}
