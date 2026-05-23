package main.java.com.codertg2.environment;

import java.util.List;

public interface Environment {
    public ExecutionResult execute(String code);

    public List<Function> getFunctions();

    public void setup();

    public void teardown();
}
