package task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import task.dto.Message;
import task.dto.Model;
import task.dto.Role;
import task.utils.Constant;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Client for communication with Open AI API
 */
public class OpenAIClient {


    private final ObjectMapper mapper;
    private final HttpClient httpClient;
    private final Model model;
    private final String apiKey;
    private final boolean streamResponse;

    public OpenAIClient(Model model, String apiKey, boolean streamResponse) {
        this(model, apiKey, streamResponse, HttpClient.newHttpClient());
    }

    public OpenAIClient(Model model, String apiKey, boolean streamResponse, HttpClient httpClient) {
        this.mapper = new ObjectMapper();
        this.model = model;
        this.apiKey = checkApiKey(apiKey);
        this.streamResponse = streamResponse;
        this.httpClient = httpClient;
    }

    private String checkApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("apiKey cannot be null or empty");
        }
        return apiKey;
    }

    /**
     * Goes to Open AI API, prints chunks with AI response to the console and returns Message with AI message.
     *
     * @param messages message history
     * @return AI message
     */
    public Message postAndPrint(List<Message> messages) throws Exception {

        Map<String, Object> requestBody = Map.of(
                "model", this.model,
                "messages", messages,
                "stream", this.streamResponse,
                "n", 3

        );
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(Constant.OPEN_AI_API_URI)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build();
        StringBuilder aiResp = new StringBuilder();
        if (streamResponse) {
            postAndStreamToConsole(httpRequest, aiResp);
        }else {
            postRegularAndShowInConsole(httpRequest, aiResp);
        }
        return new Message(Role.AI, aiResp.toString());
    }

    public void postRegularAndShowInConsole(HttpRequest httpRequest, StringBuilder assistantResponse) {
        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode rootNode = mapper.readTree(response.body());
                if (rootNode != null) {
                    rootNode.get("choices");
                    JsonNode choices = rootNode.get("choices");
                    if (choices != null && choices.isArray() && !choices.isEmpty()) {
                        JsonNode message = choices.get(0).get("message");
                        if (message != null) {
                            String content = message.get("content").asText();
                            System.out.println(content);
                        }
                    }
                }
            } else {
                System.out.println(response.statusCode() + " " + response.body());

            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void postAndStreamToConsole(HttpRequest httpRequest, StringBuilder assistantResponse) {
        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> {
                    response.body().forEach(line -> {
                        if (line != null &&line.startsWith("data: ")) {
                            String chunk = line.substring(6);
                            if (!chunk.endsWith("[DONE]")) {
                                collectAndPrintContent(chunk, assistantResponse);
                            }
                        }
                    });
                }).join();
    }

    public void collectAndPrintContent(String data, StringBuilder assistantResponse) {

        try {
            JsonNode rootNode =   mapper.readTree(data);
            if(rootNode!=null){
                JsonNode choices = rootNode.get("choices");
                if(choices!=null && choices.isArray()&& !choices.isEmpty())
                {
                    JsonNode delta = choices.get(0).get("delta");
                    if(delta!=null){
                        JsonNode content = delta.get("content");
                        if(content!=null){
                            String cont =content.asText();
                            assistantResponse.append(cont);
                            System.out.print(cont);
                        }
                    }
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

}
