package controller;

import dto.UserDto;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserDto userDto) {
        log.info("Регистрация нового пользователя: {}", userDto);
        return new ResponseEntity<>(registrationService.registerUser(userDto), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        log.info("Получен пользователь по ID: {}", id);
        return new ResponseEntity<>(registrationService.getUser(id), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Удален пользователь с ID: {}", id);
        registrationService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}