package com.smartwallet.service;

import com.smartwallet.domain.WalletUser;
import com.smartwallet.dto.TokenResponse;
import com.smartwallet.dto.UserDto;
import com.smartwallet.dto.UserLoginRequest;
import com.smartwallet.dto.UserRegisterRequest;
import com.smartwallet.exception.ApiException;
import com.smartwallet.repository.WalletUserRepository;
import com.smartwallet.security.JwtService;
import com.smartwallet.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final WalletUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public UserDto register(UserRegisterRequest request) {
        if (userRepository.existsByPhone(request.phone())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Пользователь с таким телефоном уже зарегистрирован");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Пользователь с таким email уже зарегистрирован");
        }
        WalletUser user = WalletUser.builder()
                .phone(request.phone())
                .email(request.email())
                .name(request.name())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();
        user = userRepository.saveAndFlush(user);
        return DtoMapper.toUserDto(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(UserLoginRequest request) {
        WalletUser user = userRepository.findByPhone(request.phone())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Неверный телефон или пароль"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Неверный телефон или пароль");
        }
        String token = jwtService.createAccessToken(user.getPhone());
        return new TokenResponse(token, "bearer");
    }

    public UserDto profile(WalletUser user) {
        return DtoMapper.toUserDto(user);
    }
}
