package io.unlogged.pg_rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class VectorController {

    private final VectorStore vectorStore;

    @Autowired
    public VectorController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    // Endpoint to add documents to the vector store
    @GetMapping("/addDocuments")
    public void addDocuments() {
        List<Document> documents = List.of(
            new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
            new Document("The World is Big and Salvation Lurks Around the Corner"),
            new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2"))
        );
        vectorStore.add(documents);
    }

    // Modified endpoint to perform a similarity search and return a list of strings
    @GetMapping("/search")
    public List<String> searchDocuments(@RequestParam("query") String query) {
        // Using SearchRequest for a more detailed search configuration
        List<Document> results = vectorStore.similaritySearch(SearchRequest.query(query).withTopK(5));

        // Transform the document results into a List of Strings
        return results.stream()
                .map(doc -> "Id: " + doc.getId() + ", Content: " + doc.getContent())
                .collect(Collectors.toList());
    }
}
