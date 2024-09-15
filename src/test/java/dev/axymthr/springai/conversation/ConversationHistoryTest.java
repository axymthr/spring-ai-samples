package dev.axymthr.springai.conversation;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ConversationHistoryTest.Conf.class)
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
@Testcontainers
public class ConversationHistoryTest {

    private static final Logger logger = LoggerFactory.getLogger(ConversationHistoryTest.class);

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    protected ChatModel chatModel;

    @Autowired
    protected EmbeddingModel embeddingModel;

    @Autowired
    protected VectorStore vectorStore;

    @Test
    void functionCallTest() {

        ChatMemory chatMemory = new InMemoryChatMemory();

        // @formatter:off
        var chatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        You are an assistant project manager expert at managing many resources and schedules.
                        Adopt the user's tone to make them feel comfortable with you. If they are playful and silly, so are you. If they are professional and matter-of-fact, so are you.
                        Keep your responses short and direct because people need your help in a hurry, but for complex tasks, think out loud by writing each step.
                        For questions about long documents, pull the most relevant quote from the document and consider whether it answers the user's question or whether it lacks sufficient detail.
                        Today is {current_date}. This message was sent by {user_name} at exactly {message_creation_time}.
                        Available projects are: {available_projects}. The project name is its natural identifier.
                        When calling functions, always use the exact name of the project as provided here. For example, a user's request may reference `project a`, `12345`, or simply `A`,
						but if `Project A (12345)` is on the list of available projects, then function calls should be made with `Project A (12345)`. However, if the user's request references
						a significantly different project name like `project b`, `54333`, or simply `B`, then the request should be rejected.
						""")
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory))
                .defaultAdvisors(new VectorStoreChatMemoryAdvisor(vectorStore))
                .build();
        // @formatter:on

        for (int i = 0; i < 5; i++) {
            // @formatter:off
            String response = chatClient.prompt()
                    .system(sp -> sp.params(Map.of(
                            "current_date", LocalDate.now().toString(),
                            "message_creation_time", LocalDateTime.now(),
                            "user_name", "Alice",
                            "available_projects", List.of("Project A (12345)", "Project B (54333)")
                    )))
                    .user(u -> u.text("What functions are available? List the function names."))
                    .functions("getCurrentWeather", "clockInFunction", "clockOutFunction", "findAllProjectNamesFunction", "updateProjectFunction")
                    .advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, "696")
                            .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                    .call()
                    .content();
            // @formatter:on

            logger.info("Response: {}", response);

            assertThat(response).contains("getCurrentWeather", "clockInFunction", "clockOutFunction",
                    "findAllProjectNamesFunction", "updateProjectFunction");

        }
    }

    @Test
    void streamFunctionCallTest() {

        ChatMemory chatMemory = new InMemoryChatMemory();

        // @formatter:off
        var chatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        You are an assistant project manager expert at managing many resources and schedules.
                        Adopt the user's tone to make them feel comfortable with you. If they are playful and silly, so are you. If they are professional and matter-of-fact, so are you.
                        Keep your responses short and direct because people need your help in a hurry, but for complex tasks, think out loud by writing each step.
                        For questions about long documents, pull the most relevant quote from the document and consider whether it answers the user's question or whether it lacks sufficient detail.
                        Today is {current_date}. This message was sent by {user_name} at exactly {message_creation_time}.
                        Available projects are: {available_projects}. The project name is its natural identifier.
                        When calling functions, always use the exact name of the project as provided here. For example, a user's request may reference `project a`, `12345`, or simply `A`,
						but if `Project A (12345)` is on the list of available projects, then function calls should be made with `Project A (12345)`. However, if the user's request references
						a significantly different project name like `project b`, `54333`, or simply `B`, then the request should be rejected.
						""")
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory))
                .defaultAdvisors(new VectorStoreChatMemoryAdvisor(vectorStore))
                .build();
        // @formatter:on

        for (int i = 0; i < 5; i++) {
            // @formatter:off
            String response = chatClient.prompt()
                    .system(sp -> sp.params(Map.of(
                            "current_date", LocalDate.now().toString(),
                            "message_creation_time", LocalDateTime.now(),
                            "user_name", "Alice",
                            "available_projects", List.of("Project A (12345)", "Project B (54333)")
                    )))
                    .user(u -> u.text("What functions are available? List the function names."))
                    .functions("getCurrentWeather", "clockInFunction", "clockOutFunction", "findAllProjectNamesFunction", "updateProjectFunction")
                    .advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, "696")
                            .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                    .stream()
                    .content().collectList().block().stream().collect(Collectors.joining());
            // @formatter:on

            logger.info("Response: {}", response);

            assertThat(response).contains("getCurrentWeather", "clockInFunction", "clockOutFunction",
                    "findAllProjectNamesFunction", "updateProjectFunction");
        }
    }

    @Configuration
    @EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
    public static class Conf {

        record Request(String text) {
        }

        record Response(String text) {
        }

        @Bean
        @Description("Clock in")
        public Function<Request, Response> clockInFunction() {
            return request -> new Response("clockInFunction " + request.text());
        }

        @Bean
        @Description("Clock out")
        public Function<Request, Response> clockOutFunction() {
            return request -> new Response("clockOutFunction " + request.text());
        }

        @Bean
        @Description("Find all project names")
        public Function<Request, Response> findAllProjectNamesFunction() {
            return request -> new Response("findAllProjectNamesFunction " + request.text());
        }

        @Bean
        @Description("Update project")
        public Function<Request, Response> updateProjectFunction() {
            return request -> new Response("updateProjectFunction " + request.text());
        }

        record Location(String location) {
        }

        @Bean
        @Description("Get the weather in location")
        public Function<Location, Response> getCurrentWeather() {
            return request -> new Response("Current temperature is 11C");
        }

        @Bean
        public OpenAiApi openAiApi() {
            String apiKey = System.getenv("OPENAI_API_KEY");
            if (!StringUtils.hasText(apiKey)) {
                throw new IllegalArgumentException(
                        "You must provide an API key.  Put it in an environment variable under the name OPENAI_API_KEY");
            }

            return new OpenAiApi(apiKey);
        }

        @Bean
        public OpenAiChatModel openAiChatModel(OpenAiApi api, FunctionCallbackContext functionCallbackContext) {
            OpenAiChatModel openAiChatModel = new OpenAiChatModel(api,
                    OpenAiChatOptions.builder().withModel(OpenAiApi.ChatModel.GPT_4_O_MINI).build(),
                    functionCallbackContext, RetryUtils.DEFAULT_RETRY_TEMPLATE);
            return openAiChatModel;
        }

        @Bean
        public OpenAiEmbeddingModel openAiEmbeddingModel(OpenAiApi api) {
            return new OpenAiEmbeddingModel(api);
        }

        @Bean
        @ConditionalOnMissingBean
        public FunctionCallbackContext springAiFunctionManager(ApplicationContext context) {
            FunctionCallbackContext manager = new FunctionCallbackContext();
            manager.setApplicationContext(context);
            return manager;
        }

        @Bean
        public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
            return new PgVectorStore(jdbcTemplate, embeddingModel, PgVectorStore.INVALID_EMBEDDING_DIMENSION,
                    PgVectorStore.PgDistanceType.COSINE_DISTANCE, true, PgVectorStore.PgIndexType.HNSW, true);
        }

        @Bean
        public JdbcTemplate myJdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        @Primary
        public DataSourceProperties dataSourceProperties() {
            var datasource = new DataSourceProperties();
            datasource.setUrl(String.format("jdbc:postgresql://%s:%d/%s", postgresContainer.getHost(),
                    postgresContainer.getMappedPort(5432), "postgres"));
            datasource.setUsername(postgresContainer.getUsername());
            datasource.setPassword(postgresContainer.getPassword());
            return datasource;
        }

        @Bean
        public HikariDataSource dataSource(DataSourceProperties dataSourceProperties) {
            return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        }

    }
}