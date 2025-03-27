package com.BankingSystem.CutomerrService.service;


import com.BankingSystem.CutomerrService.dto.LoginRequest;
import com.BankingSystem.CutomerrService.dto.UserRequest;
import com.BankingSystem.CutomerrService.dto.UserResponse;
import com.BankingSystem.CutomerrService.entity.AuthenticationResponse;
import com.BankingSystem.CutomerrService.entity.Role;
import com.BankingSystem.CutomerrService.entity.User;
import com.BankingSystem.CutomerrService.exception.UserExistException;
import com.BankingSystem.CutomerrService.exception.UserNotFoundException;
import com.BankingSystem.CutomerrService.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::convertToDTO).toList();
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id ));
        return convertToDTO(user);
    }

    public UserResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));
        return convertToDTO(user);
    }

    public AuthenticationResponse createUser(UserRequest userRequest) {
        if (userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
            throw new UserExistException("Username already exists");
        }

        User user = convertToEntity(userRequest);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setRole(Optional.ofNullable(userRequest.getRole()).orElse(Role.USER));

        user = userRepository.save(user);
        String token = jwtService.generateToken(user);

        return new AuthenticationResponse(token);
    }

    public AuthenticationResponse loginUser(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Invalid username or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UserNotFoundException("Invalid username or password");
        }

        String token = jwtService.generateToken(user);
        return new AuthenticationResponse(token);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest updatedUser) {
        User user = userRepository.findById(id).map(existingUser -> {
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setEmail(updatedUser.getEmail());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }
            return userRepository.save(existingUser);
        }).orElseThrow(() -> new UserNotFoundException("User not found"));
        return convertToDTO(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    private UserResponse convertToDTO(User user) {
        UserResponse userResponse = new UserResponse();
        BeanUtils.copyProperties(user, userResponse);
        return userResponse;
    }

    private User convertToEntity(UserRequest userRequest) {
        User user = new User();
        BeanUtils.copyProperties(userRequest, user);
        return user;
    }

    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }
}
