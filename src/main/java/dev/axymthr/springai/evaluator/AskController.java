package dev.axymthr.springai.evaluator;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AskController {
    private final ChatClient chatClient;

    public AskController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping("/ask")
    public Answer askQuestion(@RequestBody Question question) {
        return chatClient
                .prompt()
                .user(question.question())
                 .call()
                .entity(Answer.class);
    }
}

