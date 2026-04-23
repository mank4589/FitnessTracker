package com.ufit.controller;

import com.ufit.model.UserProfile;
import com.ufit.service.UserProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final UserProfileService profileService;

    public ProfileController(UserProfileService profileService) {
        this.profileService = profileService;
    }

    // ═══════════════ AUTH ENDPOINTS ═══════════════

    /**
     * Register a new user (username + password + profile data)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        try {
            String username = (String) body.get("username");
            String password = (String) body.get("password");

            UserProfile profile = new UserProfile();
            profile.setUsername(username);
            profile.setName((String) body.get("name"));
            
            if (body.containsKey("age")) profile.setAge(((Number) body.get("age")).intValue());
            if (body.containsKey("gender")) profile.setGender((String) body.get("gender"));
            if (body.containsKey("heightCm")) profile.setHeightCm(((Number) body.get("heightCm")).doubleValue());
            if (body.containsKey("weightKg")) profile.setWeightKg(((Number) body.get("weightKg")).doubleValue());
            if (body.containsKey("activityLevel")) profile.setActivityLevel((String) body.get("activityLevel"));
            if (body.containsKey("fitnessGoal")) profile.setFitnessGoal((String) body.get("fitnessGoal"));
            if (body.containsKey("dietType")) profile.setDietType((String) body.get("dietType"));

            UserProfile created = profileService.register(profile, password);
            // Clear password hash before returning
            created.setPassword(null);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    /**
     * Login with username + password
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<UserProfile> profileOpt = profileService.login(username, password);
        if (profileOpt.isPresent()) {
            UserProfile profile = profileOpt.get();
            profile.setPassword(null); // Don't send hash to frontend
            return ResponseEntity.ok(profile);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", "Invalid username or password"));
    }

    // ═══════════════ PROFILE CRUD ═══════════════

    /**
     * Create a new user profile (legacy)
     */
    @PostMapping
    public ResponseEntity<UserProfile> createProfile(@RequestBody UserProfile profile) {
        try {
            UserProfile created = profileService.createProfile(profile);
            created.setPassword(null);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all profiles (names only — no passwords)
     */
    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllProfiles() {
        List<UserProfile> profiles = profileService.getAllProfiles();
        // Clear password hashes
        profiles.forEach(p -> p.setPassword(null));
        return ResponseEntity.ok(profiles);
    }

    /**
     * Get profile by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfile> getProfileById(@PathVariable Long id) {
        Optional<UserProfile> profile = profileService.findById(id);
        profile.ifPresent(p -> p.setPassword(null));
        return profile.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get profile by name
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<UserProfile> getProfileByName(@PathVariable String name) {
        Optional<UserProfile> profile = profileService.findByName(name);
        profile.ifPresent(p -> p.setPassword(null));
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
        updated.setPassword(null);
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
