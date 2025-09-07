# Облачное хранилище

Дипломный проект (Netology) — REST-сервис для авторизованной работы с файлами.\
Сервис позволяет пользователю:

- загружать файлы,
- скачивать файлы,
- удалять файлы,
- переименовывать файлы,
- получать список своих файлов.

Фронтенд готов и подключается к бэкенду без изменений.

---

## Стек

- Java 21
- Spring Boot 3
- Spring Data JPA + Hibernate
- PostgreSQL
- Testcontainers (интеграционные тесты)
- JUnit 5 + Mockito
- Docker, Docker Compose
- Maven

---

## Запуск проекта

### 1. Клонируем репозиторий

```bash
git clone https://github.com/<your-username>/cloudstorage.git
cd cloudstorage
```

### 2. Сборка проекта

```bash
./mvnw clean package -DskipTests
```

### 3. Запуск через Docker Compose

```bash
docker-compose up --build
```

Контейнеры:

- `app` — backend (Spring Boot)
- `db` — PostgreSQL

Сервис будет доступен по адресу, указанному в `application.yml`. Если порт и контекст не менялись, то по умолчанию это [http://localhost:8080/cloud](http://localhost:8080/cloud).

---

## Конфигурация

`application.yml` (prod):

```yaml
server:
  port: 8080
  servlet:
    context-path: /cloud

spring:
  datasource:
    url: jdbc:postgresql://db:5432/cloudstorage
    username: user
    password: password
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: create
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher

app:
  cors:
    allowed-origins: "http://localhost:8080,http://localhost:8081"
```

---

## Фронтенд

Используем готовый фронтенд:\
[Netology Diploma Frontend](https://github.com/netology-code/jd-homeworks/tree/master/diploma/netology-diplom-frontend)

⚙️ Настройка:

```env
VUE_APP_BASE_URL=http://localhost:8080/cloud
```

---

## REST API

Все запросы должны содержать заголовок `auth-token`.

| Метод  | Путь                       | Описание                      |
| ------ | -------------------------- | ----------------------------- |
| POST   | /cloud/login               | Авторизация, получение токена |
| POST   | /cloud/upload/{filename}   | Загрузка файла                |
| GET    | /cloud/download/{filename} | Скачивание файла              |
| PUT    | /cloud/file                | Переименование файла          |
| DELETE | /cloud/file/{filename}     | Удаление файла                |
| GET    | /cloud/list                | Список файлов пользователя    |

---

## Тестирование

### Unit-тесты

### Интеграционные тесты

Используется Testcontainers для автоматического поднятия PostgreSQL.

---

## Автор

Роман Коньшин (@romarekonshin)\
Netology, дипломная работа «Облачное хранилище» (2025)

