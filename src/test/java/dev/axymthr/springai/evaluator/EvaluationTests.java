package dev.axymthr.springai.evaluator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.RelevancyEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class EvaluationTests {
    @Autowired
    AskController askController;

    @Autowired
    ChatClient.Builder chatClientBuilder;

    @Test
    public void shouldReturnRelevantAnswer() {
        String userText = "Why is the sky blue?";
        Answer answer = askController.askQuestion(new Question(userText));
        RelevancyEvaluator evaluator = new RelevancyEvaluator(chatClientBuilder);
        EvaluationResponse evaluationResponse  = evaluator.evaluate(
                new EvaluationRequest(
                        userText,
                        List.of(),
                        answer.answer()));
        Assertions.assertThat(evaluationResponse.isPass())
                .withFailMessage("""
                        The answer "%s" is not relevant to the question "%s".
                        """, answer.answer(), userText)
                .isTrue();
    }


}
