    Дипломная работа «Облачное хранилище»

    Описание проекта:

    Задача — разработать REST-сервис. Сервис должен предоставить REST-интерфейс для загрузки файлов и вывода списка 
    уже загруженных файлов пользователя.

    Все запросы к сервису должны быть авторизованы. Заранее подготовленное веб-приложение (FRONT) должно подключаться к 
    разработанному сервису без доработок, а также использовать функционал FRONT для авторизации, загрузки и вывода 
    списка файлов пользователя.

    Требования к приложению:

    1. Сервис должен предоставлять REST-интерфейс для интеграции с FRONT.
    2. Сервис должен реализовывать все методы, описанные в yaml-файле:
    - вывод списка файлов;
    - добавление файла;
    - удаление файла;
    - авторизация.
    3. Все настройки должны вычитываться из файла настроек (yml).
    4. Информация о пользователях сервиса (логины для авторизации) и данные должны храниться в базе данных
    (на выбор студента).

    Требования к реализации:

    1. Приложение разработано с использованием Spring Boot.
    2. Использован сборщик пакетов gradle/maven.
    3. Для запуска используется docker, docker-compose.
    4. Код размещён на Github.
    5. Код покрыт unit-тестами с использованием mockito.
    6. Добавлены интеграционные тесты с использованием testcontainers.

    Шаги реализации:

    1. Изучите протокол получения и отправки сообщений между FRONT и BACKEND.
    2. Нарисуйте схему приложений.
    3. Опишите архитектуру приложения, где хранятся настройки и большие файлы, структуры таблиц/коллекций базы данных.
    4. Создайте репозиторий проекта на Github.
    5. Напишите приложение с использованием Spring Boot.
    6. Протестируйте приложение с помощью curl/postman.
    7. Протестируйте приложение с FRONT.
    8. Напишите README.md к проекту.
    9. Отправьте на проверку.

    Описание и запуск FRONT:

    1. Установите nodejs (версия не ниже 19.7.0) на компьютер, следуя инструкции.
    2. Скачайте FRONT (JavaScript).
    3. Перейдите в папку FRONT приложения и все команды для запуска выполняйте из неё.
    4. Следуя описанию README.md FRONT проекта, запустите nodejs-приложение (npm install, npm run serve).
    5. Далее нужно задать url для вызова своего backend-сервиса.
    В файле .env FRONT (находится в корне проекта) приложения нужно изменить url до backend, например:
    VUE_APP_BASE_URL=http://localhost:8080.
    Нужно указать корневой url вашего backend, к нему frontend будет добавлять все пути согласно спецификации
    Для VUE_APP_BASE_URL=http://localhost:8080 при выполнении логина frontend вызовет http://localhost:8080/login
    Запустите FRONT снова: npm run serve.
    Изменённый url сохранится для следующих запусков.
    6. По умолчанию FRONT запускается на порту 8080 и доступен по url в браузере http://localhost:8080.
    Если порт 8080 занят, FRONT займёт следующий доступный (8081). После выполнения npm run serve в терминале вы 
    увидите, на каком порту он запустился.

    Авторизация приложения:
    FRONT-приложение использует header auth-token, в котором отправляет токен (ключ-строка) для идентификации 
    пользователя на BACKEND. Для получения токена нужно пройти авторизацию на BACKEND и отправить на метод 
    /login логин и пароль. В случае успешной проверки в ответ BACKEND должен вернуть json-объект с полем auth-token и 
    значением токена. Все дальейшие запросы с FRONTEND, кроме метода /login, отправляются с этим header. Для выхода 
    из приложения нужно вызвать метод BACKEND /logout, который удалит/деактивирует токен. Последующие запросы с этим 
    токеном будут не авторизованы и вернут код 401.

    Обратите внимание, что название самого параметра (как и всех параметров в спецификации), его регистр имеют значение.
    Важно, чтобы ваш backend возвращал токен в поле auth-token – если поле будет называться authToken или authtoken, 
    фронт не сможет найти токен и дальше логина процесс не пройдёт.