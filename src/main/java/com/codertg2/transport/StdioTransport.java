package main.java.com.codertg2.transport;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class StdioTransport {
    private BufferedReader reader;
    private PrintWriter writer;

    public StdioTransport() {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.writer = new PrintWriter(System.out);
    }

    public String readLine() {
        try {
            return reader.readLine();
        } catch (Exception e) {
            System.err.println("Error reading line: " + e.getMessage());
            return null;
        }
    }

    public void writeLine(String json) {
        writer.println(json);
        writer.flush();
    }

    public void log(String msg) {
        System.err.println(msg);
    }

    public void close() {
        try {
            reader.close();
            writer.close();
        } catch (Exception e) {
            System.err.println("Error closing transport: " + e.getMessage());
        }
    }

}
