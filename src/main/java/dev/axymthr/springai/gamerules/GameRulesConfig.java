package dev.axymthr.springai.gamerules;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class GameRulesConfig {
    @Value("classpath:/data/bb-rules.txt")
    private Resource bbRules;

    @Bean
    @Qualifier("simpleVectorStore")
    VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return new SimpleVectorStore(embeddingModel);
    }

    @Bean
    ApplicationRunner loadRules(@Qualifier("simpleVectorStore") VectorStore simpleVectorStore) {
        return args -> {
            simpleVectorStore.add(new TextReader(bbRules).get());
        };
    }
}
