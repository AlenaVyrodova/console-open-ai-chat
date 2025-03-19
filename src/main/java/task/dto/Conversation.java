package task.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Contains <b>id</b> (UUID) and list of <b>messages</b>
 */
public class Conversation {
    private final UUID id = UUID.randomUUID();
    private final List<Message> messages = new ArrayList<>();

    public List<Message> getMessages() {
        return messages;
    }

    public UUID getId() {
        return id;

    }

    public void addMessage(Message message) {
        messages.add(message);
    }
}
