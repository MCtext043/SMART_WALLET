# Smart Wallet (Spring Boot)

REST API SmartWallet для мобильного/веб-клиента: пользователи, карты с правилами кэшбэка, транзакции, подбор карты под категорию, подсказки и чат через внешний GigaChat-прокси. Исходная спецификация API — в репозитории [MCtext043/SmartWallet](https://github.com/MCtext043/SmartWallet).

## Ответ на вопрос «это реально?»


Да. Это ограниченный REST-сервис (CRUD пользовательских данных, несколько доменных правил, один внешний HTTP-вызов к GigaChat-прокси). Такой функционал **типично и предсказуемо переносится** на Spring Boot без «магических» платформенных ограничений.

Оговорки (честно):

| Тема | Комментарий |
|------|--------------|
| **Перенос пользователей из сторонней БД** | Хэши паролей в форматах других стеков не подставляются «как есть» без отдельного `PasswordEncoder` под тот же алгоритм. |
| **Побитовое совпадение всех текстов ошибок валидации** | Частично упрощено: при ошибках Bean Validation возвращается одна строка `detail`. |
| **Внешний GigaChat** | По-прежнему HTTP POST на настраиваемый URL (`assistant.gigachat.url`). В интеграционных тестах клиент замокан. |

## Стек технологий

| Слой | Технология |
|------|-------------|
| Рантайм | Java **21**, Spring Boot **3.4.x** |
| Web | Spring Web MVC |
| Безопасность | Spring Security **stateless JWT** (JJWT **0.12**, HS256), BCrypt для пароля |
| БД | PostgreSQL, Spring Data **JPA** (Hibernate), **Flyway** V1-схема |
| Контракт API | springdoc-openapi — UI по адресу **`/docs`** (OpenAPI JSON: **`/v3/api-docs`**) |
| Тесты | JUnit 5, Spring Boot Test, **Testcontainers** (PostgreSQL). Без установленного Docker тест контейнерного класса можно пропустить (`@Testcontainers(disabledWithoutDocker = true)`). |

## Архитектура

Приложение разбито на классические слои:

```text
┌──────────────────────────────────────────────┐
│  Controllers (HTTP, DTO snake_case через     │
│  Jackson PROPERTY_NAMING Strategy)           │
└─────────────┬────────────────────────────────┘
              │ использует Application services
┌─────────────▼────────────────────────────────┐
│  Services                                   │
│  • Auth • Cards • Transactions              │
│  • Cashback • Recommendations • Assistant    │
└─────────────┬────────────────────────────────┘
              │ доменная модель (JPA)
┌─────────────▼────────────────────────────────┐
│  Repositories / Entities / PostgreSQL       │
└──────────────────────────────────────────────┘

Отдельно:
• JwtAuthenticationFilter + JwtService
• GigachatClient (RestTemplate) — прокси к внешнему ассистенту
```

**Почему так:** доменная логика сосредоточена в сервисах, контроллеры только маппинг HTTP ↔ DTO.

## Полный функционал (соответствие эндпоинтам)

| Метод | Путь | Описание |
|-------|------|----------|
| `GET` | `/` | Информация о сервисе (`message`, `version`, `docs`) |
| `GET` | `/health` | `{"status":"healthy"}` |
| `POST` | `/auth/register` | Регистрация, уникальные `phone`, `email` |
| `POST` | `/auth/login` | JWT `access_token`, `token_type: bearer` |
| `GET` | `/auth/profile` | Профиль (Bearer) |
| `GET`/`POST` | `/cards`, `/cards/` | Список / создание карты |
| `GET` | `/cards/{id}` | Одна карта пользователя |
| `GET`/`POST` | `/transactions`, `/transactions/` | Список / создание транзакции с расчётом `cashback_earned` |
| `GET` | `/assistant/recommendations` | До **3** рекомендаций; если за последние сутки уже есть сохранённые — они возвращаются из БД |
| `POST` | `/assistant/chat` | Чат ассистента (контекст карт и последних 5 транзакций → GigaChat-прокси) |
| `GET` | `/cashback/best-card?category=…` | Лучшая карта по ставке для категории (fallback на `прочее`) |

Подробная интерактивная документация этого сервиса: **`http://localhost:8000/docs`**.

## Запуск локально

Требования: **JDK 21+**, **PostgreSQL**, **Maven** (или свой wrapper). Убедитесь, что БД создана, например `smartwallet`.

1. При необходимости скорректируйте `src/main/resources/application.yml` (`spring.datasource.*`, секрет JWT `jwt.secret`).
2. Сборка и запуск:
   ```bash
   mvn spring-boot:run
   ```
3. По умолчанию слушает порт **8000**.

Переменные окружения (альтернатива правке YAML) можно задать через `SPRING_APPLICATION_JSON` или стандартные `SPRING_DATASOURCE_*`, см. документацию Spring Boot.

### Важные параметры `application.yml`

| Ключ | Назначение |
|------|------------|
| `spring.datasource.*` | Подключение к PostgreSQL |
| `jwt.secret` | Подпись JWT (минимально длинный ключ в проде; для HS256 в коде ключ нормализуется через SHA-256-хэш) |
| `jwt.access-token-expire-minutes` | Время жизни access-токена |
| `assistant.gigachat.url` | URL метода отправки сообщения (по умолчанию `http://91.146.28.240:8041/api/gigachat/message`; см. Swagger внешнего сервиса: [Swagger UI на 8041](http://91.146.28.240:8041/swagger-ui/index.html#/)) |
| `assistant.gigachat.*-timeout-ms` | Таймауты HTTP-клиента |

## Docker Compose

Нужны **Docker Desktop** (или эквивалент) и свободный порт **8000** на хосте.

```bash
docker compose up --build -d
```

- API после старта: `http://localhost:8000` (Swagger этого приложения: `/docs`).
- PostgreSQL живёт только внутри сети compose; том с данными: `smartwallet-pg-data`.
- При необходимости задайте свой URL вызова GigaChat через переменную окружения (в `docker-compose` уже есть значение по умолчанию):

```bash
set ASSISTANT_GIGACHAT_URL=http://91.146.28.240:8041/api/gigachat/message
docker compose up -d api
```

Остановка: `docker compose down` (данные тома сохранятся до `docker compose down -v`).

Подсказка под Windows/PowerShell: для `curl.exe` надёжнее передавать JSON-тело из файла (`--data-binary @path\to\body.json`), чтобы не ломать кавычки на кириллице и спецсимволах в `--data`.


## Тесты

Интеграционный класс **`BackendHappyPathIT`** поднимает PostgreSQL через **Testcontainers** и прогоняет основной пользовательский сценарий; клиент к GigaChat в тестах **подменяется** моками. Нужен **запущенный Docker**.

```bash
mvn verify
```

Дополнительно прогоняются модульные тесты в `CoreBusinessLogicTest` — они не требуют Docker.

Без Docker контейнерный интеграционный тест может быть автоматически пропущен (см. `@Testcontainers`).

---

Репозиторий-источник: [https://github.com/MCtext043/SmartWallet](https://github.com/MCtext043/SmartWallet).
