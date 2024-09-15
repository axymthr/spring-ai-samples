package dev.axymthr.springai;

import org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

//@EnableConfigurationProperties(WeatherConfigProperties.class)
@SpringBootApplication
public class AiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
    }

    @Component
    static class CarinaAiClient {
        private final VectorStore vectorStore;
        private final ChatClient chatClient;

        CarinaAiClient(VectorStore vectorStore, ChatClient chatClient) {
            this.vectorStore = vectorStore;
            this.chatClient = chatClient;
        }

        //TODO: Use ChatClient.Builder to add a default system prompt instead of the below block in every prompt

        String chat(String message) {
            var prompt = """
                    You're assisting with questions about services offered by Carina.
                    Carina is a two-sided healthcare marketplace focusing on home care aides (caregivers)
                    and their Medicaid in-home care clients (adults and children with developmental disabilities and low income elderly population).
                    Carina's mission is to build online tools to bring good jobs to care workers, so care workers can provide the
                    best possible care for those who need it.
                    
                    Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
                    If unsure, simply state that you don't know.
                    
                    DOCUMENTS:
                    {documents}
                    
                    """;
            var listOfSimilarDocs = this.vectorStore.similaritySearch(message);
            var docs = listOfSimilarDocs.stream()
                    .map(Document::getContent)
                    .collect(Collectors.joining(System.lineSeparator()));
            var systemMessage = new SystemPromptTemplate(prompt)
                    .createMessage(Map.of("documents", docs));
            var userMessage = new UserMessage(message);
            var promptList = new Prompt(List.of(systemMessage, userMessage));
            return this.chatClient.prompt(promptList).call().content();
        }

    }
//    @Bean
    ApplicationRunner demo(
            VectorStore vectorStore,
            @Value("file://${HOME}/Desktop/pdfs/medicaid-wa-faqs.pdf") Resource pdf,
            JdbcTemplate template,
            ChatClient chatClient,
            CarinaAiClient carinaAiClient) {
        return args -> {
//            setup(vectorStore, pdf, template);
            System.out.println(
                    carinaAiClient.chat("""
                            What should I know about the transition to Consumer direct care network washington?
                            """)
            );


        };
    }

    private static void setup(VectorStore vectorStore, Resource pdf, JdbcTemplate template) {
        template.update("delete from vector_store");
        var config = PdfDocumentReaderConfig
                .builder()
                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder()
                        .withNumberOfBottomTextLinesToDelete(3)
                        .build())
                .build();
        var pdfReader = new PagePdfDocumentReader(pdf, config);
        var textSplitter = new TokenTextSplitter();
        var docs = textSplitter.apply(pdfReader.get());
        vectorStore.accept(docs);
    }

    @Bean
    @Description("Get the weather in location")
    public Function<WeatherRequest, WeatherResponse> weatherFunction() {
        return new MockWeatherService();
    }

    public static class MockWeatherService implements Function<WeatherRequest, WeatherResponse> {

        @Override
        public AiApplication.WeatherResponse apply(AiApplication.WeatherRequest request) {
            double temperature = request.location().contains("Amsterdam") ? 20 : 25;
            return new AiApplication.WeatherResponse(temperature, request.unit);
        }
    }

//    @Bean
    CommandLineRunner runner(ChatClient.Builder chatClientBuilder) {
        return args -> {
            var chatClient = chatClientBuilder.build();

            var response = chatClient.prompt()
                    .user("What is the weather in Amsterdam and Paris?")
//                    .functions("weatherFunction") // reference by bean name.
                    .call()
                    .content();

            System.out.println(response);
        };
    }

    public record WeatherRequest(String location, String unit) {}

    public record WeatherResponse(double temp, String unit) {}

    @Bean
    ImageModel imageModel(@Value("${spring.ai.openai.api-key}") String apiKey) {
        return new OpenAiImageModel(new OpenAiImageApi(apiKey));
    }
}
