import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private String method;
    private String path;
    private String version;
    private Map<String, String> headers;
    private StringBuffer body;

    public Request(String method, String path, String version, Map<String, String> headers, StringBuffer body) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = headers;
        this.body = body;
    }

    public static Request parse(BufferedReader in) throws IOException {
        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            return null;
        }

        final var headers = new HashMap<String, String>();
        var header = in.readLine();
        while (header.length() > 0) {
            var index = header.indexOf(":");
            headers.put(header.substring(0, index), header.substring(index + 1));
            in.readLine();
        }

        final var body = new StringBuffer();
        String bodyLine = in.readLine();
        while (bodyLine != null) {
            body.append(bodyLine).append("\r\n");
            bodyLine = in.readLine();
        }

        return new Request(parts[0], parts[1], parts[2], headers, body);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public Map<String,String> getHeaders() {
        return headers;
    }

    public StringBuffer getBody() {
        return body;
    }
}
