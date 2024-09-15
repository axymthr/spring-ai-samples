package dev.axymthr.springai.summarizer;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SummaryController {
    private final ChatClient chatClient;

    @Value("classpath:/prompts/summarizer.st")
    private Resource summarizeTemplate;

    public SummaryController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
// http -f POST :8080/summarize file@'./The_European_Union_Artificial_Intelligence_Act_1708436995.pdf'
    @PostMapping(path = "/summarize", produces = "text/plain")
    public String summarize(@RequestParam("file") MultipartFile file) {
        Resource resource = file.getResource();
        List<Document> documents = new TikaDocumentReader(resource).get();
        String docText = documents.stream()
                .map(Document::getContent)
                 .collect(Collectors.joining("\n\n"));
        return chatClient.prompt()
                .system(systemSpec -> systemSpec
                        .text(summarizeTemplate)
                        .param("document", docText))
                .call()
                .content();
    }
}
