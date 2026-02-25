package com.enterprise.codeplatform.service;

import com.enterprise.codeplatform.dto.AuthRequest;
import com.enterprise.codeplatform.dto.AuthResponse;
import com.enterprise.codeplatform.dto.RegistrationRequest;
import com.enterprise.codeplatform.entity.User;
import com.enterprise.codeplatform.repository.UserRepository;
import com.enterprise.codeplatform.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtils jwtUtils;
        private final AuthenticationManager authenticationManager;

        public AuthResponse register(RegistrationRequest request) {
                if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                        throw new RuntimeException("Email already exists");
                }

                var user = User.builder()
                                .name(request.getName())
                                .email(request.getEmail())
                                .username(request.getUsername())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(request.getRole() == null ? User.Role.DEVELOPER : request.getRole())
                                .build();
                userRepository.save(user);
                var jwtToken = jwtUtils.generateToken(user);
                return AuthResponse.builder()
                                .token(jwtToken)
                                .username(user.getDisplayUsername())
                                .role(user.getRole())
                                .build();
        }

        public AuthResponse login(AuthRequest request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
                var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
                var jwtToken = jwtUtils.generateToken(user);
                return AuthResponse.builder()
                                .token(jwtToken)
                                .username(user.getDisplayUsername())
                                .role(user.getRole())
                                .build();
        }
}
