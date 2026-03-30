package com.hkt.fooddelivery.service;

import com.hkt.fooddelivery.dto.UpdateProfileRequest;
import com.hkt.fooddelivery.dto.UserProfileResponse;
import com.hkt.fooddelivery.entity.User;
import com.hkt.fooddelivery.exception.ResourceNotFoundException;
import com.hkt.fooddelivery.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final StorageService storageService;

    public UserProfileResponse getMyProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));


        return new UserProfileResponse(
                user.getId(), user.getUsername(), user.getEmail(), user.getPhone(),
                user.getFirstName(), user.getLastName(), user.getAvatarUrl(),
                user.getRole(), user.isVerified()
        );
    }

    @Transactional
    public UserProfileResponse updateProfile(String username, UpdateProfileRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        if (req.avatarFile() != null && !req.avatarFile().isEmpty()) {
            if (user.getAvatarUrl() != null) {
                storageService.deleteByUrl(user.getAvatarUrl());
            }
            String newUrl = storageService.upload(req.avatarFile(), "avatars");
            user.setAvatarUrl(newUrl);
        }

        user.changeProfile(req.firstName(), req.lastName());
        user.changePhone(req.phone());


        userRepository.save(user);

        return new UserProfileResponse(
                user.getId(), user.getUsername(), user.getEmail(), user.getPhone(),
                user.getFirstName(), user.getLastName(), user.getAvatarUrl(),
                user.getRole(), user.isVerified()
        );
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}