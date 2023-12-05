package controller;

import dto.FileDto;
import entity.FileEntity;
import model.FileBody;
import java.util.List;
import java.util.stream.Collectors;
import repository.FileRepository;
import security.JWTToken;
import service.FileService;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import exception.FileNotFoundException;
import javax.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import javax.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private FileRepository fileRepository;
    private JWTToken jwtToken;
    Long userID = jwtToken.getAuthenticatedUser().getId();
    Long userId = jwtToken.getAuthenticatedUser().getId();

    @PostMapping("/file")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    public ResponseEntity<Void> addFile(@NotNull @RequestParam("file") MultipartFile file, @RequestParam("filename") String fileName) {
        if (file.isEmpty()) {
            log.info("Не выбран файл для загрузки: {}", file.isEmpty());
            throw new FileNotFoundException("Файл не выбран", 0);
        }
        log.info("Файл для загрузки на сервер: {}", fileName);
        fileService.addFile(file, fileName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/file")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    public FileDto getFile(@RequestParam("filename") String fileName) {

        log.info("Поиск файла в базе данных по имени файла: {} и Id пользователя: {}", fileName, userID);
        FileEntity file = fileRepository.findFileByUserEntityIdAndFileName(userID, fileName).orElseThrow(() -> new FileNotFoundException(
                "Файл с именем: { " + fileName + " } не найден", userID));

        log.info("Загрузка файла: {} из базы данных. Id пользователя: {}", fileName, userID);
        return FileDto.builder()
                .fileName(file.getFileName())
                .fileType(file.getFileType())
                .fileData(file.getFileData())
                .build();
    }

    @PutMapping("/file")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    public ResponseEntity<Void> renameFile(@RequestParam("filename") String fileName, @Valid @RequestBody FileBody body) {
        log.info("Запрос файла для переименования: {}", fileName);
        fileService.renameFile(fileName, body);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/file")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    public ResponseEntity<Void> deleteFile(@RequestParam("filename") String fileName) {
        log.info("Запрос файла для удаления: {}", fileName);
        fileService.deleteFile(fileName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    public List<FileDto> getAllFiles(@RequestParam @Min(1) int limit) {

        log.info("Поиск всех файлов в базе данных по Id пользователя: {} и лимиту вывода: {}", userId, limit);
        List<FileEntity> listFiles = fileRepository.findFilesByUserIdWithLimit(userId, limit);

        log.info("Все файлы в базе данных по Id пользователя: {} и лимиту вывода: {} найдены | Список файлов: {}", userId, limit, listFiles);
        return listFiles.stream()
                .map(file -> FileDto.builder()
                        .fileName(file.getFileName())
                        .size(file.getSize())
                        .build()).collect(Collectors.toList());
    }
}