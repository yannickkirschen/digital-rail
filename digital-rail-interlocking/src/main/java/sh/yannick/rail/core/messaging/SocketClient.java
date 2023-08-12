package sh.yannick.rail.core.messaging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void connect(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public String send(String message) throws IOException {
        out.println(message);
        return in.readLine();
    }

    public void disconnect() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
}
