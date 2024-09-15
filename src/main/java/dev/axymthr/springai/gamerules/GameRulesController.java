package dev.axymthr.springai.gamerules;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameRulesController {

    private final GameRagService gameRagService;

    public GameRulesController(GameRagService gameRagService) {
        this.gameRagService = gameRagService;
    }

    /*
    * Questions:
    * Does Burger Force field protect from Burgerpocalypse?
    * Does Burger Force field protect your burger from Burger Bomb?
    * What is the Picky Eater card?
    * What is the Burger Bomb card?
    * */

    @PostMapping(path = "/gamerules", produces = "text/plain")
    public String askAboutRules(@RequestBody RulesQuestion question) {
        return gameRagService.generateResponse(question.question());
    }
}
