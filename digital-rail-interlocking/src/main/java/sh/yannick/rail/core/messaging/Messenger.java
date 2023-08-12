package sh.yannick.rail.core.messaging;

import java.io.IOException;
import java.util.Set;

public interface Messenger {
    Message send(Message message) throws IOException;

    Set<Message> send(Set<Message> messages) throws IOException;

    void disconnect() throws IOException;
}
