import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try (Server server = new Server(9999, 64)) {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


