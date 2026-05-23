package main.java.com.codertg2.environment;

import java.util.Map;

public class EnvironmentConfig {
    private long timeout;
    private Map<String, String> envVars;
    private String dir;

    public EnvironmentConfig(long timeout, Map<String, String> envVars, String dir) {
        this.timeout = timeout;
        this.envVars = envVars;
        this.dir = dir;
    }

    public long getTimeout() {
        return timeout;
    }

    public Map<String, String> getEnvVars() {
        return envVars;
    }

    public String getDir() {
        return dir;
    }

}
