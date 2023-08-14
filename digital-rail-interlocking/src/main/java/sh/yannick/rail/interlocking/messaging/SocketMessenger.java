package sh.yannick.rail.interlocking.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SocketMessenger implements Messenger, Closeable {
    private final SocketClient client;

    public SocketMessenger(@Value("{socket.server.ip}") String ip) throws IOException {
        client = new SocketClient();
        client.connect(ip, 6666);
    }

    @Override
    public Message send(Message message) throws IOException {
        String reply = client.send(new ObjectMapper().writeValueAsString(message));
        if (reply != null && !reply.isEmpty() && !reply.equals(".")) {
            return new ObjectMapper().readValue(reply, Message.class);
        }

        return null;
    }

    @Override
    public Set<Message> send(Set<Message> messages) throws IOException {
        Set<Message> replies = new HashSet<>();
        for (Message message : messages) {
            replies.add(send(message));
        }

        return replies.stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public void disconnect() throws IOException {
        client.send(".");
        client.disconnect();
    }

    @Override
    public void close() throws IOException {
        client.disconnect();
    }
}
