package main.java.com.codertg2.environment;

public class ExecutionResult {
    private String output;
    private String error;
    private int exitCode;
    private boolean isError;

    public ExecutionResult(String output, String error, int exitCode) {
        this.output = output;
        this.error = error;
        this.exitCode = exitCode;
        this.isError = exitCode != 0;
    }

    public String getOutput() {
        return output;
    }

    public String getError() {
        return error;
    }

    public int getExitCode() {
        return exitCode;
    }

    public boolean isError() {
        return isError;
    }

}
