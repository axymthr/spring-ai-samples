package dev.axymthr.springai.ollama;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ChatController {
    private final ChatClient chatClient;

    ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/question")
    String question(@RequestParam(value = "question", defaultValue = "Why do I have to go to bed at 8 PM?") String question) {
        return chatClient.prompt()
                .user(question)
                .system("You are a mom, make sure that your answers are kid-friendly, use small words, and respond like you are talking to a 2nd grade child.")
                .call()
                .content();
    }

    @GetMapping("/joke")
    String joke() {
        return chatClient.prompt()
                .user("Please tell me a dad joke about computers")
                .call()
                .content();
    }
}