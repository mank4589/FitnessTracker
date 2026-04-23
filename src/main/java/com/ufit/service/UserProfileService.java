package com.ufit.service;

import com.ufit.model.UserProfile;
import com.ufit.model.ufit;
import com.ufit.repository.UserProfileRepository;
import com.ufit.repository.ufitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserProfileService {

    private final UserProfileRepository profileRepository;
    private final ufitRepository healthSnapshotRepository;

    public UserProfileService(UserProfileRepository profileRepository,
                             ufitRepository healthSnapshotRepository) {
        this.profileRepository = profileRepository;
        this.healthSnapshotRepository = healthSnapshotRepository;
    }

    // ═══════════════ AUTHENTICATION ═══════════════

    /**
     * Register a new user with username and password.
     */
    @Transactional
    public UserProfile register(UserProfile profile, String rawPassword) {
        if (profile.getUsername() == null || profile.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (profileRepository.existsByUsername(profile.getUsername().trim())) {
            throw new IllegalArgumentException("Username already taken");
        }

        profile.setUsername(profile.getUsername().trim());
        profile.setPassword(hashPassword(rawPassword));
        return profileRepository.save(profile);
    }

    /**
     * Login with username and password. Returns the profile if credentials match.
     */
    public Optional<UserProfile> login(String username, String password) {
        if (username == null || password == null) return Optional.empty();
        
        Optional<UserProfile> profileOpt = profileRepository.findByUsername(username.trim());
        if (profileOpt.isEmpty()) return Optional.empty();

        UserProfile profile = profileOpt.get();
        if (profile.getPassword() != null && profile.getPassword().equals(hashPassword(password))) {
            return Optional.of(profile);
        }
        return Optional.empty();
    }

    /**
     * Hash a password using SHA-256.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // ═══════════════ PROFILE CRUD ═══════════════

    /**
     * Create a new user profile (legacy — for backward compat, sets a default password)
     */
    @Transactional
    public UserProfile createProfile(UserProfile profile) {
        // If no password set, this is a legacy profile creation
        if (profile.getPassword() == null && profile.getUsername() != null) {
            profile.setPassword(hashPassword("default"));
        }
        return profileRepository.save(profile);
    }

    /**
     * Update an existing profile
     */
    @Transactional
    public UserProfile updateProfile(UserProfile profile) {
        // Don't overwrite password if not provided
        if (profile.getId() != null) {
            Optional<UserProfile> existing = profileRepository.findById(profile.getId());
            if (existing.isPresent()) {
                UserProfile ex = existing.get();
                if (profile.getPassword() == null || profile.getPassword().isEmpty()) {
                    profile.setPassword(ex.getPassword());
                }
                if (profile.getUsername() == null || profile.getUsername().isEmpty()) {
                    profile.setUsername(ex.getUsername());
                }
            }
        }
        return profileRepository.save(profile);
    }

    /**
     * Find profile by ID
     */
    public Optional<UserProfile> findById(Long id) {
        return profileRepository.findById(id);
    }

    /**
     * Find profile by name
     */
    public Optional<UserProfile> findByName(String name) {
        return profileRepository.findByName(name);
    }

    /**
     * Get all profiles
     */
    public List<UserProfile> getAllProfiles() {
        return profileRepository.findAll();
    }

    /**
     * Delete a profile
     */
    @Transactional
    public void deleteProfile(Long id) {
        profileRepository.deleteById(id);
    }

    /**
     * Link the latest health snapshot to user profile
     */
    @Transactional
    public void linkLatestHealthSnapshot(Long profileId) {
        Optional<UserProfile> profileOpt = profileRepository.findById(profileId);
        if (profileOpt.isEmpty()) {
            return;
        }

        UserProfile profile = profileOpt.get();
        
        // Find the most recent health snapshot (assuming there's a way to query by date)
        List<ufit> allSnapshots = healthSnapshotRepository.findAll();
        if (!allSnapshots.isEmpty()) {
            // Get the most recent one based on ID (assuming higher ID = more recent)
            ufit latestSnapshot = allSnapshots.stream()
                .max((s1, s2) -> Long.compare(s1.getId(), s2.getId()))
                .orElse(null);
            
            if (latestSnapshot != null) {
                profile.setLatestHealthSnapshot(latestSnapshot);
                profileRepository.save(profile);
            }
        }
    }

    /**
     * Check if profile name exists
     */
    public boolean existsByName(String name) {
        return profileRepository.existsByName(name);
    }
}
