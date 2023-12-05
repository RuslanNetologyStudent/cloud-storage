package service;

import enums.Role;
import dto.UserDto;
import utils.MapperUtils;
import entity.UserEntity;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import repository.UserRepository;
import exception.IncorrectDataEntry;
import lombok.RequiredArgsConstructor;
import exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final MapperUtils mapperUtils;
    private final PasswordEncoder passwordEncoder;

    public UserDto registerUser(UserDto userDto) {
        UserEntity userEntity = mapperUtils.toUserEntity(userDto);

        log.info("Проверка доступности логина: {}", userEntity);
        userRepository.findUserByLogin(userEntity.getLogin()).ifPresent(s -> {
            log.info("Пользователь с таким логином: {} уже существует", userEntity.getLogin());
            throw new IncorrectDataEntry("Пользователь с таким логином: { " + userEntity.getLogin() +
                    " } уже зарегистрирован", userEntity.getId());
        });
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        userEntity.setRoles(Collections.singleton(Role.ROLE_USER));
        userEntity.setRole(Role.ROLE_USER.getAuthority());
        log.info("Зарегистрирован новый пользователь: {}", userEntity);
        return mapperUtils.toUserDto(userRepository.save(userEntity));
    }

    public UserDto getUser(Long id) {
        log.info("Поиск пользователя в базе данных по Id: {}", id);
        UserEntity userFound = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Пользователь не найден", id));
        log.info("Пользователь в базе данных найден. ID: {}, логин: {}", id, userFound);
        return mapperUtils.toUserDto(userFound);
    }

    public void deleteUser(Long id) {
        log.info("Поиск пользователя в базе данных по Id: {}", id);
        UserEntity userFound = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Пользователь не найден", id));
        log.info("Пользователь в базе данных найден. ID: {}, логин: {}", id, userFound);
        log.info("Пользователь удален. ID: {}, логин: {}", id, userFound);
        userRepository.deleteById(id);
    }
}