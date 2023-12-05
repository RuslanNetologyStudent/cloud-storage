package service;

import model.FileBody;
import entity.FileEntity;
import entity.UserEntity;
import security.JWTToken;
import java.io.IOException;
import repository.FileRepository;
import lombok.extern.slf4j.Slf4j;
import java.security.MessageDigest;
import java.io.BufferedInputStream;
import exception.IncorrectDataEntry;
import lombok.RequiredArgsConstructor;
import exception.FileNotFoundException;
import java.security.DigestInputStream;
import org.springframework.stereotype.Service;
import java.security.NoSuchAlgorithmException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor

public class FileService {

    private FileRepository fileRepository;

    private JWTToken jwtToken;
    Long userID = jwtToken.getAuthenticatedUser().getId();
    Long userId = jwtToken.getAuthenticatedUser().getId();

    @Transactional
    public void addFile(MultipartFile file, String fileName) {

        log.info("Поиск файла в базе данных по имени файла {} и идентификатору {}", fileName, userID);
        fileRepository.findFileByUserEntityIdAndFileName(userID, fileName).ifPresent(s -> {
            log.info("Файл не найден по имени файла {} и Id {}", fileName, userID);
            throw new IncorrectDataEntry("Файл с таким именем: { " + fileName + " } уже существует", userID);
        });

        String hash = null;
        byte[] fileData = null;
        try {
            hash = checksum(file);
            fileData = file.getBytes();
            log.info("Получен массив байтов файла и сгенерирован хэш файла:{}", hash);
        }
        catch (NoSuchAlgorithmException | IOException e) {
        }

        log.info("Проверка наличия файла в базе данных по Id пользователя:{} и хэшу:{}", userID, hash);
        if (fileRepository.findFileByUserEntityIdAndHash(userID, hash).isPresent()) {
            log.info("Файл с хэшом: {} у пользователя с Id: {} существует", hash, userID);
            return;
        }
        log.info("Файл в базе данных по Id пользователя:{} и хэшу:{} не существует", userID, hash);

        FileEntity createdFile = FileEntity.builder()
                .fileName(fileName)
                .fileType(file.getContentType())
                .fileData(fileData)
                .hash(hash)
                .size(file.getSize())
                .userEntity(UserEntity.builder().id(userID).build())
                .build();

        log.info("Файл создан и сохранен в базе данных. Имя файла: {}, хэш: {}, Id пользователя: {}", fileName, hash, userID);
        fileRepository.save(createdFile);
    }

    private String checksum(MultipartFile file) throws NoSuchAlgorithmException, IOException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        try (var fis = file.getInputStream();
             var bis = new BufferedInputStream(fis);
             var dis = new DigestInputStream(bis, md)) {

            while (dis.read() != -1) ;
            md = dis.getMessageDigest();
        }

        var result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        log.info("Контрольная значение успешно сгенерирована для файла: {}", file.getName());
        return result.toString();
    }


    public void renameFile(String fileName, FileBody fileBody) {

        log.info("Поиск файла для переименования в базе данных по имени файла: {} и Id пользователя: {}", fileName, userID);
        FileEntity fileToRename = fileRepository.findFileByUserEntityIdAndFileName(userID, fileName)
                .orElseThrow(() -> new FileNotFoundException("Файл с именем: { " + fileName + " } не найден", userID));

        log.info("Проверка нового имени файла в базе данных по имени файла: {} и Id пользователя: {}", fileBody.getFileName(), userID);
        fileRepository.findFileByUserEntityIdAndFileName(userID, fileBody.getFileName()).ifPresent(s -> {
            log.info("Файл не найден в базе данных по имени файла: {} и Id пользователя: {}", fileName, userID);
            throw new IncorrectDataEntry("Файл с таким именем: { " + fileName + " } уже существует", userID);
        });

        log.info("Переименование файла в базе данных. Старое имя файла: {}, новое имя файла: {}, Id пользователя: {}",
                fileName, fileToRename, userID);
        fileToRename.setFileName(fileBody.getFileName());
        fileRepository.save(fileToRename);
    }

    public void deleteFile(String fileName) {

        log.info("Поиск файла для удаления в базе данных по имени файла: {} и Id пользователя: {}", fileName, userID);
        FileEntity fileFromStorage = fileRepository.findFileByUserEntityIdAndFileName(userID, fileName).orElseThrow(() -> new FileNotFoundException(
                "Файл с именем: { " + fileName + " } не найден", userID));

        log.info("Удаление файла из базы данных по имени файла: {} и Id пользователя: {}", fileFromStorage, userID);
        fileRepository.deleteById(fileFromStorage.getId());
    }
}