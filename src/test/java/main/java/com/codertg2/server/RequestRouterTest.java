package main.java.com.codertg2.server;

import main.java.com.codertg2.environment.Environment;
import main.java.com.codertg2.environment.ExecutionResult;
import main.java.com.codertg2.environment.Function;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RequestRouter — verifies that each JSON-RPC method is routed
 * correctly and produces valid JSON responses.
 */
public class RequestRouterTest {

    private RequestRouter router;
    private ObjectMapper mapper;

    // A fake environment that returns a canned response for any code execution
    private static class FakeEnvironment implements Environment {
        @Override
        public ExecutionResult execute(String code) {
            if (code.equals("fail()")) {
                return new ExecutionResult(null, "something broke", 1);
            }
            return new ExecutionResult("result from: " + code, null, 0);
        }

        @Override
        public List<Function> getFunctions() {
            return List.of(
                    new Function("getWeather", "Gets the weather for a city",
                            List.of(new Function.Parameter("city", "string", "The city name")),
                            "string"),
                    new Function("add", "Adds two numbers",
                            List.of(
                                    new Function.Parameter("a", "number", "First number"),
                                    new Function.Parameter("b", "number", "Second number")),
                            "number"));
        }

        @Override
        public void setup() {
        }

        @Override
        public void teardown() {
        }
    }

    @BeforeEach
    void setUp() {
        router = new RequestRouter(new FakeEnvironment(), "test-server", "0.1.0");
        mapper = new ObjectMapper();
    }

    // ── initialize ──────────────────────────────────────────────

    @Test
    void testInitializeReturnsServerInfo() throws Exception {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":\"1\",\"method\":\"initialize\"}";
        String response = router.route(request);

        JsonNode node = mapper.readTree(response);
        assertEquals("2.0", node.get("jsonrpc").asText());
        assertEquals("1", node.get("id").asText());
        assertEquals("test-server", node.get("result").get("name").asText());
        assertEquals("0.1.0", node.get("result").get("version").asText());
        assertFalse(node.get("isError").asBoolean());
    }

    // ── ping ────────────────────────────────────────────────────

    @Test
    void testPingReturnsEmptyResult() throws Exception {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":\"2\",\"method\":\"ping\"}";
        String response = router.route(request);

        JsonNode node = mapper.readTree(response);
        assertEquals("2.0", node.get("jsonrpc").asText());
        assertEquals("2", node.get("id").asText());
        assertTrue(node.get("result").isEmpty());
    }

    // ── tools/list ──────────────────────────────────────────────

    @Test
    void testToolsListReturnsAllFunctions() throws Exception {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":\"3\",\"method\":\"tools/list\"}";
        String response = router.route(request);

        JsonNode node = mapper.readTree(response);
        assertEquals("2.0", node.get("jsonrpc").asText());
        assertEquals("3", node.get("id").asText());

        JsonNode tools = node.get("result").get("tools");
        assertNotNull(tools);
        assertTrue(tools.isArray());
        assertEquals(2, tools.size());

        // First tool: getWeather
        JsonNode weather = tools.get(0);
        assertEquals("getWeather", weather.get("name").asText());
        assertEquals("Gets the weather for a city", weather.get("description").asText());

        JsonNode weatherProps = weather.get("inputSchema").get("properties");
        assertNotNull(weatherProps.get("city"));
        assertEquals("string", weatherProps.get("city").get("type").asText());

        // Second tool: add
        JsonNode add = tools.get(1);
        assertEquals("add", add.get("name").asText());
        JsonNode addProps = add.get("inputSchema").get("properties");
        assertNotNull(addProps.get("a"));
        assertNotNull(addProps.get("b"));
    }

    // ── code execution ──────────────────────────────────────────

    @Test
    void testCodeExecutionSuccess() throws Exception {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":\"4\",\"code\":\"getWeather('Austin')\"}";
        String response = router.route(request);

        JsonNode node = mapper.readTree(response);
        assertEquals("2.0", node.get("jsonrpc").asText());
        assertEquals("4", node.get("id").asText());
        assertEquals("result from: getWeather('Austin')", node.get("result").get("content").get("text").asText());
        assertFalse(node.get("result").get("isError").asBoolean());
    }

    @Test
    void testCodeExecutionFailure() throws Exception {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":\"5\",\"code\":\"fail()\"}";
        String response = router.route(request);

        JsonNode node = mapper.readTree(response);
        assertEquals("5", node.get("id").asText());
        assertTrue(node.get("result").get("isError").asBoolean());
    }

    // ── error cases ─────────────────────────────────────────────

    @Test
    void testUnknownMethodReturnsError() throws Exception {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":\"6\",\"method\":\"unknown/thing\"}";
        String response = router.route(request);

        JsonNode node = mapper.readTree(response);
        assertEquals("6", node.get("id").asText());
        assertEquals(-32601, node.get("error").get("code").asInt());
    }

    @Test
    void testNoMethodNoCodeReturnsError() throws Exception {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":\"7\"}";
        String response = router.route(request);

        JsonNode node = mapper.readTree(response);
        assertEquals("7", node.get("id").asText());
        assertEquals(-32600, node.get("error").get("code").asInt());
    }

    @Test
    void testInvalidJsonReturnsParseError() throws Exception {
        String request = "this is not json";
        String response = router.route(request);

        JsonNode node = mapper.readTree(response);
        assertEquals(-32700, node.get("error").get("code").asInt());
    }
}
