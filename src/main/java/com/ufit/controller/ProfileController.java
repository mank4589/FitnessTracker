package com.ufit.controller;

import com.ufit.model.UserProfile;
import com.ufit.service.UserProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/profiles")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"}, allowCredentials = "false")
public class ProfileController {

    private final UserProfileService profileService;

    public ProfileController(UserProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Create a new user profile
     */
    @PostMapping
    public ResponseEntity<UserProfile> createProfile(@RequestBody UserProfile profile) {
        try {
            UserProfile created = profileService.createProfile(profile);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all profiles
     */
    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllProfiles() {
        List<UserProfile> profiles = profileService.getAllProfiles();
        return ResponseEntity.ok(profiles);
    }

    /**
     * Get profile by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfile> getProfileById(@PathVariable Long id) {
        Optional<UserProfile> profile = profileService.findById(id);
        return profile.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get profile by name
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<UserProfile> getProfileByName(@PathVariable String name) {
        Optional<UserProfile> profile = profileService.findByName(name);
        return profile.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update a profile
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserProfile> updateProfile(@PathVariable Long id, @RequestBody UserProfile profile) {
        Optional<UserProfile> existing = profileService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        profile.setId(id);
        UserProfile updated = profileService.updateProfile(profile);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a profile
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        Optional<UserProfile> existing = profileService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        profileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Link latest health snapshot to profile
     */
    @PostMapping("/{id}/link-health")
    public ResponseEntity<Void> linkHealthSnapshot(@PathVariable Long id) {
        profileService.linkLatestHealthSnapshot(id);
        return ResponseEntity.ok().build();
    }
}

