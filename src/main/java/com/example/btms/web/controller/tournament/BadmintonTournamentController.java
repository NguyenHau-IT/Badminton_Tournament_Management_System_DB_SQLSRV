package com.example.btms.web.controller.tournament;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BadmintonTournamentController {
    @GetMapping("/badmintonTournament/badmintonTournament")
    public String showTournamentPage() {
        // Trả về template: src/main/resources/templates/badmintonTournament/badmintonTournament.html
        return "badmintonTournament/badmintonTournament";
    }
}
