package main.java.com.codertg2.environment;

import java.util.List;

public class Function {
    private String name;
    private String description;
    private List<Parameter> inputSchema;
    private String returnType;

    public Function(String name, String description, List<Parameter> inputSchema, String returnType) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
        this.returnType = returnType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Parameter> getInputSchema() {
        return inputSchema;
    }

    public String getReturnType() {
        return returnType;
    }

    public static class Parameter {
        private String name;
        private String type;
        private String description;

        public Parameter(String name, String type, String description) {
            this.name = name;
            this.type = type;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }
    }
}
