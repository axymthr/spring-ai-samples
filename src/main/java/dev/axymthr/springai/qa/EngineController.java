package dev.axymthr.springai.qa;

import dev.axymthr.springai.spring.engine.EngineResponse;
import dev.axymthr.springai.spring.engine.QueryEngine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/qa/engine")
public class EngineController {

	private final QueryEngine queryEngine;

	public EngineController(QueryEngine queryEngine) {
		this.queryEngine = queryEngine;
	}

	@GetMapping
	public Map<String, Object> query(
			@RequestParam(value = "question", defaultValue = "What is the purpose of Carina?") String question) {
		EngineResponse engineResponse = this.queryEngine.call(question);
		Map<String, Object> response = new HashMap<>();
		response.put("question", question);
		response.put("answer", engineResponse.getChatResponse().getResult().getOutput().getContent());
		return response;
	}

}
