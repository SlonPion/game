package com.game.controller;

import com.game.entity.Player;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rest")
public class MyController {
    @Autowired
    private PlayerService playerService;

    @GetMapping("/players")
    public List<Player>getAll(@RequestParam Map<String, String> map){
        return playerService.getAll(map);
    }

    @GetMapping("/players/count")
    public Integer getCount(@RequestParam Map<String, String> map){
        return playerService.getCount(map);
    }

    @GetMapping("/players/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable("id") Long id) {
        return playerService.getPlayerById(id);
    }

    @PostMapping("/players")
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        if (player.getExperience() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(player);
        Player created = playerService.createPlayer(player);
        if (created == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(created);
        else return ResponseEntity.ok(created);
    }

    @PostMapping("/players/{id}")
    public ResponseEntity<Player> update(@PathVariable("id") Long id, @RequestBody Player player) {
        return playerService.update(id, player);
    }

    @DeleteMapping("/players/{id}")
    public ResponseEntity<Long> delete(@PathVariable("id") Long id) {
        return playerService.deleteById(id);
    }

}
