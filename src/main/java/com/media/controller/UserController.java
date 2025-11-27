package com.media.controller;

import com.media.dto.UserProfileDto;
import com.media.dto.UserSummaryDto;
import com.media.entity.User;
import com.media.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepo;

    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> me(Authentication auth) {
        User u = userRepo.findByUsername(auth.getName()).orElseThrow();
        UserProfileDto dto = new UserProfileDto(u.getId(), u.getUsername(), u.getEmail());
        return ResponseEntity.ok().body(dto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSummaryDto>> search(@RequestParam String q) {
        String needle = q == null ? "" : q.toLowerCase();
        List<UserSummaryDto> out = new ArrayList<>();
        for (User u : userRepo.findAll()) {
            String uname = u.getUsername() == null ? "" : u.getUsername().toLowerCase();
            if (uname.contains(needle)) {
                out.add(new UserSummaryDto(u.getId(), u.getUsername()));
            }
        }
        return ResponseEntity.ok().body(out);
    }
}
