import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Closeable {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> handlers = new ConcurrentHashMap<>();
    private ExecutorService threadPool;
    private ServerSocket serverSocket;

    public Server(int port, int threadPoolSize) throws IOException {
        threadPool = Executors.newFixedThreadPool(threadPoolSize);
        serverSocket = new ServerSocket(port);
    }

    public void start() throws IOException {
        while (true) {
            Socket socket = serverSocket.accept();
            threadPool.execute(connect(socket));
        }
    }

    public Runnable connect(Socket socket) {
        return () -> {
            try (
                    socket;
                    final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final var out = new BufferedOutputStream(socket.getOutputStream());
            ) {
                final Request request = Request.parse(in);

                if (request == null) {
                    out.write((
                            "HTTP/1.1 400 BAD REQUEST\r\n" +
                                    "Content-Length: " + 0 + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.flush();
                    return;
                }

                var methodMap = handlers.get(request.getMethod());
                if (methodMap == null) {
                    out.write((
                            "HTTP/1.1 404 NOT FOUND\r\n" +
                                    "Content-Length: " + 0 + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.flush();
                    return;
                }

                var handler = methodMap.get(request.getPath());
                if (handler == null) {
                    out.write((
                            "HTTP/1.1 404 NOT FOUND\r\n" +
                                    "Content-Length: " + 0 + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.flush();
                    return;
                }

                handler.handle(request, out);

            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    public void addHandler(String method, String path, Handler handler) {
        var methodMap = handlers.get(method);
        if (methodMap == null) {
            methodMap = new ConcurrentHashMap<>();
            methodMap.put(path, handler);
            handlers.put(method, methodMap);
            return;
        }
        if (methodMap.get(path) == null) {
            methodMap.put(path, handler);
        }
    }

    @Override
    public void close() throws IOException {
        threadPool.shutdown();
        serverSocket.close();
    }
}
