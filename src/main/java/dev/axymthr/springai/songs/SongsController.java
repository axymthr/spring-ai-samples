package dev.axymthr.springai.songs;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/songs")
public class SongsController {
    private final ChatClient chatClient;

    public SongsController(ChatClient.Builder chatClient) {
        this.chatClient = chatClient.build();
    }

    // http :8080/songs/stringprompt/topSong
    @GetMapping("/stringprompt/topSong")
    public String topSong() {
        String stringPrompt =
                "What was the Billboard number one year-end top 100 single for 1984?";
        return chatClient.prompt().user(stringPrompt).call().content();
    }
// http :8080/songs/parameter/topsong/1999
    @GetMapping("/parameter/topsong/{year}")
    public String parameterTopSong(@PathVariable("year") int year) {
        String stringPrompt =
                "What was the Billboard number one year-end top 100 single for {year}?";
        PromptTemplate template = new PromptTemplate(stringPrompt);
        template.add("year", year);
        return chatClient.prompt().user(template.render()).call().content();
    }

    @GetMapping("/objectreturn/topsong/{year}")
    public TopSong objectReturnTopSong(@PathVariable("year") int year) {
        BeanOutputConverter<TopSong> beanOutputConverter = new BeanOutputConverter<>(TopSong.class);
        String format = beanOutputConverter.getFormat();
        String stringPrompt =
                """
                What was the Billboard number one year-end top 100 single for {year}?
                {format}
                """;
        PromptTemplate template = new PromptTemplate(stringPrompt);
        template.add("year", year);
        template.add("format", format);
        System.out.println("PARSER FORMAT: " + format);

        Prompt prompt = template.create();
        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
        Usage usage = response.getMetadata().getUsage();
        System.out.println("Usage: " + usage.getPromptTokens()
        + " " + usage.getGenerationTokens() + " " + usage.getTotalTokens());
        Generation generation = response.getResult();
        TopSong topSong = beanOutputConverter.convert(generation.getOutput().getContent());
        return topSong;
    }
}
