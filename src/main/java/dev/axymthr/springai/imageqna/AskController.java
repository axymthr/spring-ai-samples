package dev.axymthr.springai.imageqna;

import dev.axymthr.springai.evaluator.Answer;
import dev.axymthr.springai.evaluator.Question;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("askImage")
public class AskController {
    @Value("classpath:/data/BurgerBattle-rules.pdf")
    Resource imageResource;

    private final ChatClient chatClient;

    public AskController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping("/ask-image")
    public Answer ask(@RequestBody Question question) {
        return chatClient
                .prompt()
                .user(userSpec -> userSpec
                        .text(question.question())
                        .media(MimeTypeUtils.IMAGE_PNG, imageResource))
                 .call()
                .entity(Answer.class);
    }
}

