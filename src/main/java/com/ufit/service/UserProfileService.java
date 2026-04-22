package com.ufit.service;

import com.ufit.model.UserProfile;
import com.ufit.model.ufit;
import com.ufit.repository.UserProfileRepository;
import com.ufit.repository.ufitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    /**
     * Create a new user profile
     */
    @Transactional
    public UserProfile createProfile(UserProfile profile) {
        return profileRepository.save(profile);
    }

    /**
     * Update an existing profile
     */
    @Transactional
    public UserProfile updateProfile(UserProfile profile) {
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
