package main.java.com.codertg2.server;

import main.java.com.codertg2.environment.Environment;
import main.java.com.codertg2.transport.StdioTransport;
import main.java.com.codertg2.server.RequestRouter;

public class McpServer {
    private final String name;
    private final String version;
    private final Environment environment;
    private StdioTransport transport;
    private RequestRouter router;

    private McpServer(Builder builder) {
        this.name = builder.name;
        this.version = builder.version;
        this.environment = builder.environment;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void start() {
        transport = new StdioTransport();
        router = new RequestRouter(this.environment, this.name, this.version);
        this.environment.setup();
        String line = transport.readLine();
        while (line != null) {
            transport.writeLine(router.route(line));
            line = transport.readLine();
        }
        stop();
    }

    public void stop() {
        this.environment.teardown();
        transport.close();
    }

    public static class Builder {
        private String name;
        private String version;
        private Environment environment;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder environment(Environment environment) {
            this.environment = environment;
            return this;
        }

        public McpServer build() {
            if (this.name == null) {
                throw new IllegalStateException("Name cannot be null");
            }
            return new McpServer(this);
        }
    }
}