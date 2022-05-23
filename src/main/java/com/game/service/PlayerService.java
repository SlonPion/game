package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Service
public class PlayerService {
    @Autowired
    private PlayerRepository playerRepository;

    private List<Player> createList(Map<String, String> map){
        String inputName = map.get("name");
        String inputTitle = map.get("title");
        Date inputBirthdayAfter = null;
        Date inputBirthdayBefore = null;
        Integer inputExperienceMin = null;
        Integer inputExperienceMax = null;
        Integer inputLevelMin = null;
        Integer inputLevelMax = null;
        Race inputRace = null;
        Profession inputProfession = null;
        Boolean inlineRadioOptions = null;

        if(map.get("after")!=null){
            inputBirthdayAfter = new Date(Long.parseLong(map.get("after")));
        }
        if(map.get("before")!=null){
            inputBirthdayBefore = new Date(Long.parseLong(map.get("before")));
        }
        if(map.get("minExperience")!=null){
            inputExperienceMin = Integer.parseInt(map.get("minExperience"));
        }
        if(map.get("maxExperience")!=null){
            inputExperienceMax = Integer.parseInt(map.get("maxExperience"));
        }
        if(map.get("minLevel")!=null){
            inputLevelMin = Integer.parseInt(map.get("minLevel"));
        }
        if(map.get("maxLevel")!=null){
            inputLevelMax = Integer.parseInt(map.get("maxLevel"));
        }
        if(map.get("race")!=null){
            inputRace = Race.valueOf(map.get("race"));
        }
        if(map.get("profession")!=null){
            inputProfession = Profession.valueOf(map.get("profession"));
        }
        if(map.get("banned")!=null){
            inlineRadioOptions = Boolean.parseBoolean(map.get("banned"));
        }
        List<Player> list = new ArrayList<Player>();
        List<Player> resultList = new ArrayList<>();
        Iterable<Player> iterable = playerRepository.findAll();
        iterable.forEach(list::add);
        for (int i = 0; i < list.size(); i++) {
            if ((inputName == null || list.get(i).getName().contains(inputName)) &&
                    (inputTitle == null || list.get(i).getTitle().contains(inputTitle)) &&
                    (inputBirthdayAfter == null || list.get(i).getBirthday().after(inputBirthdayAfter)) &&
                    (inputBirthdayBefore == null || list.get(i).getBirthday().before(inputBirthdayBefore)) &&
                    (inputExperienceMin == null || list.get(i).getExperience() > inputExperienceMin) &&
                    (inputExperienceMax == null || list.get(i).getExperience() < inputExperienceMax) &&
                    (inputLevelMin == null || list.get(i).getLevel() > inputLevelMin) &&
                    (inputLevelMax == null || list.get(i).getLevel() < inputLevelMax) &&
                    (inputRace == null || list.get(i).getRace().equals(inputRace)) &&
                    (inputProfession == null || list.get(i).getProfession().equals(inputProfession)) &&
                    (inlineRadioOptions == null || list.get(i).getBanned().equals(inlineRadioOptions))
            )
                resultList.add(list.get(i));
        }

        return resultList;
    }

    private boolean isEmptyPlayer(Player player) {
        return player.getName() == null &&
                player.getTitle() == null &&
                player.getRace() == null &&
                player.getProfession() == null &&
                player.getExperience() == null &&
                player.getLevel() == null &&
                player.getUntilNextLevel() == null &&
                player.getBirthday() == null &&
                player.getBanned() == null;
    }

    @Transactional
    public List<Player> getAll(Map<String, String> map){
        int pageNumber;
        int pageSize;
        PlayerOrder playerOrder;
        if(map.get("pageNumber")==null){
            pageNumber = 0;
        } else{
        pageNumber = Integer.parseInt(map.get("pageNumber"));
        }
        if(map.get("pageSize")==null){
            pageSize = 3;
        }else{
            pageSize = Integer.parseInt(map.get("pageSize"));
        }
        if(map.get("order")==null){
            playerOrder = PlayerOrder.ID;
        }else{
            playerOrder = PlayerOrder.valueOf(map.get("order"));
        }

        List<Player> list = createList(map);
        List<Player> resultList = new ArrayList<>();

        try {
            for(int i = pageNumber*pageSize; i<(pageNumber+1)*pageSize;i++){
                resultList.add(list.get(i));
            }
        }catch (IndexOutOfBoundsException e){}
        resultList.sort(new Comparator<Player>() {
            @Override
            public int compare(Player o1, Player o2) {
                switch(playerOrder) {
                    case NAME: return o1.getName().compareTo(o2.getName());
                    case EXPERIENCE: return o1.getExperience().compareTo(o2.getExperience());
                    case BIRTHDAY: return o1.getBirthday().compareTo(o2.getBirthday());
                    case LEVEL: return o1.getLevel().compareTo(o2.getLevel());
                    default: return o1.getId().compareTo(o2.getId());
                }
            }
        });

        return resultList;
    }

    @Transactional
    public Integer getCount(Map<String, String> map){
        return createList(map).size();
    }

    @Transactional
    public ResponseEntity<Player> getPlayerById(Long id){
        if(id<1){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Player());
        }
        Optional<Player> optionalPlayer = playerRepository.findById(id);
        if(optionalPlayer.isPresent()){
            return ResponseEntity.ok(optionalPlayer.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Player());
        }
    }

    @Transactional
    public Player createPlayer(Player player) {
        if (player == null) return null;
        player.computeLevelAndUntilNextLevel();

        if(!player.getName().isEmpty() &&
                player.getBirthday().getTime() > 0 &&
                player.getTitle().length() <= 30 &&
                player.getExperience() <= 10000000) {

            playerRepository.save(player);
            Optional<Player> savedPlayer = playerRepository.findById(player.getId());
            return savedPlayer.orElseGet(Player::new);
        }
        else return null;
    }

    @Transactional
    public ResponseEntity<Long> deleteById(Long id) {
        if (id < 1)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(id);
        if (playerRepository.existsById(id)) {
            playerRepository.deleteById(id);
            return ResponseEntity.ok(id);
        }
        else return ResponseEntity.status(HttpStatus.NOT_FOUND).body(id);
    }

    @Transactional
    public ResponseEntity<Player> update(Long id, Player player) {
        if (id < 1)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(player);
        if (!playerRepository.existsById(id))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(player);
        if (isEmptyPlayer(player)) {
            Optional<Player> saved = playerRepository.findById(id);
            if (saved.isPresent())
                return ResponseEntity.ok(saved.get());
        }

        if (player.getExperience() != null && (player.getExperience() < 0 || player.getExperience() > 10000000))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(player);
        if (player.getBirthday() != null && (player.getBirthday().getTime() < 0 || player.getBirthday().getTime() > new Date().getTime()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(player);
        Optional<Player> saved = playerRepository.findById(id);
        if (saved.isPresent()) {
            Player savedPlayer = saved.get();
            if (player.getName() != null)
                savedPlayer.setName(player.getName());
            if (player.getTitle() != null)
                savedPlayer.setTitle(player.getTitle());
            if (player.getRace() != null)
                savedPlayer.setRace(player.getRace());
            if (player.getProfession() != null)
                savedPlayer.setProfession(player.getProfession());
            if (player.getBirthday() != null)
                savedPlayer.setBirthday(player.getBirthday());
            if (player.getExperience() != null)
                savedPlayer.setExperience(player.getExperience());
            if (player.getBanned() != null)
                savedPlayer.setBanned(player.getBanned());
            savedPlayer.computeLevelAndUntilNextLevel();
            return ResponseEntity.ok(savedPlayer);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(player);
    }
}
