package io.unlogged.pg_rag;

/*
  Original code example courtesy https://www.unlogged.io/post/all-you-need-to-know-about-spring-ai
  Had to be changed to compile with the later Spring AI library
 */

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AthleteController {

    private final ChatModel chatModel;
    private final String promptTemplate;

    public AthleteController(ChatModel chatModel, @Value("${app.promptTemplate}") String promptTemplate) {
        this.chatModel = chatModel;
        this.promptTemplate = promptTemplate;
    }

    @GetMapping("/topAthlete")
    public String topAthlete(@RequestParam("subject") String subject) {
        PromptTemplate pt = new PromptTemplate(promptTemplate);
        String renderedPrompt = pt.render(Map.of("subject", subject));
        return chatModel.call(renderedPrompt);
    }
}
