package main.java.com.codertg2.server;

import main.java.com.codertg2.environment.Environment;
import main.java.com.codertg2.environment.ExecutionResult;
import main.java.com.codertg2.environment.Function;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RequestRouter {
    private final Environment env;
    private final String name;
    private final String version;
    private final ObjectMapper mapper;

    public RequestRouter(Environment env, String name, String version) {
        this.env = env;
        this.name = name;
        this.version = version;
        this.mapper = new ObjectMapper();
    }

    private String initialize(JsonNode node) {
        return "{\"jsonrpc\":\"2.0\",\"id\":\"" + node.get("id").asText()
                + "\",\"result\":{\"name\":\"" + this.name
                + "\",\"version\":\"" + this.version + "\"},\"isError\":false}";
    }

    private String ping(JsonNode node) {
        return "{\"jsonrpc\":\"2.0\",\"id\":\"" + node.get("id").asText()
                + "\",\"result\":{},\"isError\":false}";
    }

    private String tools(JsonNode node) {
        StringBuilder ls = new StringBuilder();
        ls.append("{\"jsonrpc\":\"2.0\",\"id\":\"" + node.get("id").asText() + "\",\"result\":{\"tools\":[");
        for (int i = 0; i < env.getFunctions().size(); i++) {
            Function func = env.getFunctions().get(i);
            if (i > 0)
                ls.append(",");
            ls.append("{\"name\":\"" + func.getName() + "\",\"description\":\""
                    + func.getDescription() + "\",\"inputSchema\":{\"type\":\"object\",\"properties\":{");
            for (int j = 0; j < func.getInputSchema().size(); j++) {
                Function.Parameter param = func.getInputSchema().get(j);
                if (j > 0)
                    ls.append(",");
                ls.append("\"" + param.getName() + "\":{\"type\":\"" + param.getType()
                        + "\",\"description\":\"" + param.getDescription() + "\"}");
            }
            ls.append("}}}");
        }
        ls.append("]}}");
        return ls.toString();
    }

    private String executeCode(JsonNode node) {
        ExecutionResult res = env.execute(node.get("code").asText());
        return "{\"jsonrpc\":\"2.0\",\"id\":\"" + node.get("id").asText()
                + "\",\"result\":{\"content\":{\"type\":\"text\",\"text\":\"" + res.getOutput()
                + "\"},\"isError\":" + res.isError() + "}}";
    }

    public String route(String request) {
        try {
            JsonNode node = mapper.readTree(request);

            if (node.has("method")) {
                return switch (node.get("method").asText()) {
                    case "initialize" -> initialize(node);
                    case "ping" -> ping(node);
                    case "tools/list" -> tools(node);
                    default -> "{\"jsonrpc\":\"2.0\",\"id\":\"" + node.get("id").asText()
                            + "\",\"error\":{\"code\":-32601,\"message\":\"Unknown method\"}}";
                };
            } else if (node.has("code")) {
                return executeCode(node);
            } else {
                return "{\"jsonrpc\":\"2.0\",\"id\":\"" + node.get("id").asText()
                        + "\",\"error\":{\"code\":-32600,\"message\":\"Unknown message\"}}";
            }
        } catch (Exception e) {
            return "{\"jsonrpc\":\"2.0\",\"id\":null,\"error\":{\"code\":-32700,\"message\":\"Parse error: "
                    + e.getMessage() + "\"}}";
        }
    }
}
