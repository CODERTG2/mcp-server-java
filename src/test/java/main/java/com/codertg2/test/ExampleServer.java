package main.java.com.codertg2.test;

import main.java.com.codertg2.environment.Environment;
import main.java.com.codertg2.environment.ExecutionResult;
import main.java.com.codertg2.environment.Function;
import main.java.com.codertg2.server.McpServer;

import java.util.List;

/**
 * Example MCP server built using the SDK.
 * 
 * This is what a developer using your package would write.
 * It defines a simple environment with two functions:
 *   - greet(name) → returns "Hello, {name}!"
 *   - add(a, b)   → returns the sum
 * 
 * Run it, then pipe JSON-RPC lines to stdin to test.
 */
public class ExampleServer {

    // ── Step 1: Define the environment ──────────────────────────
    static class MathAndGreetEnvironment implements Environment {

        @Override
        public ExecutionResult execute(String code) {
            try {
                // Simple function dispatch: parse "functionName(args)"
                String funcName = code.substring(0, code.indexOf('('));
                String argsStr = code.substring(code.indexOf('(') + 1, code.lastIndexOf(')'));

                return switch (funcName.trim()) {
                    case "greet" -> {
                        String name = argsStr.replace("'", "").replace("\"", "").trim();
                        yield new ExecutionResult("Hello, " + name + "!", null, 0);
                    }
                    case "add" -> {
                        String[] parts = argsStr.split(",");
                        double a = Double.parseDouble(parts[0].trim());
                        double b = Double.parseDouble(parts[1].trim());
                        yield new ExecutionResult(String.valueOf(a + b), null, 0);
                    }
                    default -> new ExecutionResult(null, "Unknown function: " + funcName, 1);
                };
            } catch (Exception e) {
                return new ExecutionResult(null, "Execution error: " + e.getMessage(), 1);
            }
        }

        @Override
        public List<Function> getFunctions() {
            return List.of(
                    new Function("greet", "Greets a person by name",
                            List.of(new Function.Parameter("name", "string", "The name to greet")),
                            "string"),
                    new Function("add", "Adds two numbers together",
                            List.of(
                                    new Function.Parameter("a", "number", "First number"),
                                    new Function.Parameter("b", "number", "Second number")),
                            "number"));
        }

        @Override
        public void setup() {
            System.err.println("[env] Environment set up.");
        }

        @Override
        public void teardown() {
            System.err.println("[env] Environment torn down.");
        }
    }

    // ── Step 2: Build and start the server ──────────────────────
    public static void main(String[] args) {
        McpServer server = McpServer.builder()
                .name("example-server")
                .version("0.1.0")
                .environment(new MathAndGreetEnvironment())
                .build();

        server.start();
    }
}
