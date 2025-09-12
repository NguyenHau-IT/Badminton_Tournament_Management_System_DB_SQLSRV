package com.example.badmintoneventtechnology.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.badmintoneventtechnology.model.match.BadmintonMatch;
import com.example.badmintoneventtechnology.service.scoreboard.ScoreboardRemote;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/scoreboard")
@CrossOrigin(origins = "*")
public class ScoreboardController {

  private static final Logger log = LoggerFactory.getLogger(ScoreboardController.class);

  private final BadmintonMatch match = ScoreboardRemote.get().match();
  private final Object LOCK = ScoreboardRemote.get().lock();
  private final com.example.badmintoneventtechnology.util.log.Log appLog = ScoreboardRemote.get().log();

  // SSE clients
  private final List<SseEmitter> clients = new CopyOnWriteArrayList<>();
  private final ObjectMapper om = new ObjectMapper();

  public ScoreboardController() {
    // Phát snapshot khi match thay đổi
    match.addPropertyChangeListener(evt -> broadcastSnapshot());
  }

  /* ---------- helpers ---------- */
  private Map<String, Integer> view() {
    var s = match.snapshot();
    return Map.of("teamAScore", s.score[0], "teamBScore", s.score[1]);
  }

  private void broadcastSnapshot() {
    String payload;
    try {
      payload = om.writeValueAsString(match.snapshot());
    } catch (Exception e) {
      return;
    }
    for (SseEmitter c : clients) {
      try {
        c.send(SseEmitter.event().name("update").data(payload));
      } catch (Exception ex) {
        clients.remove(c);
        try {
          c.complete();
        } catch (Exception ignore) {
        }
      }
    }
  }

  /* ---------- GET ---------- */
  @GetMapping
  public ResponseEntity<Map<String, Integer>> getScoreboard() {
    synchronized (LOCK) {
      return ResponseEntity.ok(view());
    }
  }

  @GetMapping("/sync")
  public ResponseEntity<BadmintonMatch.Snapshot> getSnapshot() {
    synchronized (LOCK) {
      return ResponseEntity.ok(match.snapshot());
    }
  }

  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter stream() {
    SseEmitter em = new SseEmitter(0L); // no timeout
    clients.add(em);
    try {
      em.send(SseEmitter.event().name("init")
          .data(om.writeValueAsString(match.snapshot())));
    } catch (Exception ignore) {
    }
    em.onCompletion(() -> clients.remove(em));
    em.onTimeout(() -> clients.remove(em));
    em.onError(e -> clients.remove(em));
    return em;
  }

  /* ---------- ACTIONS ---------- */
  @PostMapping("/increaseA")
  public ResponseEntity<Map<String, Integer>> increaseA() {
    synchronized (LOCK) {
      match.pointTo(0);
      log.info("Increased score for Team A");
      try {
        appLog.plusA(match);
      } catch (Exception ignore) {
      }
      broadcastSnapshot();
      return ResponseEntity.ok(view());
    }
  }

  @PostMapping("/decreaseA")
  public ResponseEntity<Map<String, Integer>> decreaseA() {
    synchronized (LOCK) {
      match.pointDown(0, -1);
      log.info("Decreased score for Team A");
      try {
        appLog.minusA(match);
      } catch (Exception ignore) {
      }
      broadcastSnapshot();
      return ResponseEntity.ok(view());
    }
  }

  @PostMapping("/increaseB")
  public ResponseEntity<Map<String, Integer>> increaseB() {
    synchronized (LOCK) {
      match.pointTo(1);
      log.info("Increased score for Team B");
      try {
        appLog.plusB(match);
      } catch (Exception ignore) {
      }
      broadcastSnapshot();
      return ResponseEntity.ok(view());
    }
  }

  @PostMapping("/decreaseB")
  public ResponseEntity<Map<String, Integer>> decreaseB() {
    synchronized (LOCK) {
      match.pointDown(1, -1);
      log.info("Decreased score for Team B");
      try {
        appLog.minusB(match);
      } catch (Exception ignore) {
      }
      broadcastSnapshot();
      return ResponseEntity.ok(view());
    }
  }

  @PostMapping("/reset")
  public ResponseEntity<Map<String, Integer>> reset() {
    synchronized (LOCK) {
      match.resetAll();
      log.info("Reset scores to 0 - 0");
      try {
        appLog.logTs("Đặt lại điểm");
        appLog.logScore(match.snapshot());
      } catch (Exception ignore) {
      }
      broadcastSnapshot();
      return ResponseEntity.ok(view());
    }
  }

  @PostMapping("/next")
  public ResponseEntity<BadmintonMatch.Snapshot> nextGame() {
    synchronized (LOCK) {
      match.nextGame();
      try {
        appLog.nextGame(match);
      } catch (Exception ignore) {
      }
      broadcastSnapshot();
      return ResponseEntity.ok(match.snapshot());
    }
  }

  @PostMapping("/swap")
  public ResponseEntity<BadmintonMatch.Snapshot> swapEnds() {
    synchronized (LOCK) {
      match.swapEnds();
      try {
        appLog.swapEnds(match);
      } catch (Exception ignore) {
      }
      broadcastSnapshot();
      return ResponseEntity.ok(match.snapshot());
    }
  }

  @PostMapping("/change-server")
  public ResponseEntity<BadmintonMatch.Snapshot> changeServer() {
    synchronized (LOCK) {
      match.changeServer();
      try {
        appLog.logTs("Đổi giao cầu");
        appLog.logScore(match.snapshot());
      } catch (Exception ignore) {
      }
      broadcastSnapshot();
      return ResponseEntity.ok(match.snapshot());
    }
  }

  @PostMapping("/undo")
  public ResponseEntity<BadmintonMatch.Snapshot> undo() {
    synchronized (LOCK) {
      match.undo();
      try {
        appLog.undo(match);
      } catch (Exception ignore) {
      }
      broadcastSnapshot();
      return ResponseEntity.ok(match.snapshot());
    }
  }
}
