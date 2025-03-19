package task;

import task.dto.Conversation;
import task.dto.Message;
import task.dto.Model;
import task.dto.Role;
import task.utils.Constant;

import java.util.Scanner;

public class ChatApp {
    public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    OpenAIClient openAIClient = new OpenAIClient(Model.GPT_4o_MINI, Constant.API_KEY, true);
    Conversation conversation = new Conversation();
        System.out.println("Input system prompt");
        System.out.println(">");
    String sysPrompt = scanner.nextLine();

        if (sysPrompt.isBlank()) {
        conversation.addMessage(new Message(Role.SYSTEM, Constant.DEFAULT_SYSTEM_PROMPT));
        System.out.printf("Has been used default SYS PROMPT %s %n", Constant.DEFAULT_SYSTEM_PROMPT);
    } else {
        conversation.addMessage(new Message(Role.SYSTEM, sysPrompt));
        System.out.printf(" SYS PROMPT %s %n", sysPrompt);
    }
        System.out.println();
        System.out.println("Please, type your question or question to quit");
        while (true) {
        System.out.println(">");
        String input = scanner.nextLine();
        if (input.equalsIgnoreCase("exit")) {
            System.out.println("Exiting.......");
            break;
        }
        conversation.addMessage(new Message(Role.USER, input));
        System.out.print("AI: ");
        try {
            Message message = openAIClient.postAndPrint(conversation.getMessages());
            conversation.addMessage(message);
            System.out.print(message.content());

        } catch (Exception e) {
            e.printStackTrace();
            scanner.close();
            throw new RuntimeException(e);
        }
        System.out.println();
    }
        scanner.close();
}
}
