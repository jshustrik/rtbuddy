# RouteBuddy

Веб-платформа путешествий с авторскими маршрутами.

## Адрес проекта

Публичный адрес: https://routebuddy.ru

## Запуск через Docker

Из корня проекта:

```bash
./scripts/docker-up.sh
```

После запуска открыть:

```text
http://localhost:8080
```

Gateway перенаправит на каталог маршрутов.

Сборка не требует локальных Java/Gradle: нужны только Docker и интернет. Первый запуск скачивает базовые Docker-образы, один Gradle wrapper `8.14.3` и Maven-зависимости. Повторные сборки используют Docker/Gradle cache и идут существенно быстрее.

## Деплой

Проект развёрнут на виртуальной машине в Yandex Cloud. На сервере установлен Docker Compose, все сервисы запускаются как контейнеры. Локальный запуск использует HTTP-конфиг `deploy/nginx.conf`, чтобы проект стартовал без сертификатов. Продакшен-запуск использует SSL-конфиг:

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

Внешний трафик принимает контейнер `gateway` на Nginx, дальше запросы проксируются во внутренние сервисы: авторизация, профиль, маршруты, отзывы, экспорт и HTML-интерфейс. SSL-сертификат выпущен через Let's Encrypt и хранится в volume `certbot`.

## Переменные окружения

При первом запуске `./scripts/docker-up.sh` создаёт локальный файл `.env` и генерирует служебные значения:

- `POSTGRES_PASSWORD`;
- `JWT_SECRET`;
- `INTERNAL_SERVICE_TOKEN`;
- `JWT_EXPIRATION_MS`.

Файл `.env` не коммитится. Для карт нужно указать ключ API Яндекс.Карт:

```bash
YANDEX_MAPS_API_KEY=your-key ./scripts/docker-up.sh
```

Интерактивные карты подключаются через JavaScript API, а печатная PDF-выгрузка строит статичные изображения карт через Static API.

## Основные страницы

| Страница | URL |
| --- | --- |
| Регистрация | `http://localhost:8080/register` |
| Вход | `http://localhost:8080/login` |
| Профиль | `http://localhost:8080/profile` |
| Все маршруты | `http://localhost:8080/routes` |
| Мои маршруты | `http://localhost:8080/routes?my=true` |
| Создание маршрута | `http://localhost:8080/routes/create` |
| Конструктор из дней | `http://localhost:8080/constructor` |
| PDF-выгрузка | `http://localhost:8080/export` |
| Страница дня | `http://localhost:8080/routes/{routeId}/days/{dayId}` |

## Тестовый аккаунт

```text
Логин: traveler
Пароль: traveler123
```

## Сервисы

| Сервис | Назначение |
| --- | --- |
| `gateway` | единая точка входа `localhost:8080` |
| `auth-service` | регистрация, вход, JWT |
| `usrsys-service` | профиль, аватар |
| `make-service` | маршруты, дни, точки |
| `review-service` | отзывы и оценки 1-10 |
| `export-service` | PDF-файлы, данные для выгрузки и статичные карты Яндекса |
| `routesview-service` | HTML-интерфейс, proxy API |

## Проверка

```bash
./scripts/test-all.sh
./scripts/smoke-load.sh
```

`test-all.sh` сам запускает тесты в Docker, если на машине нет локального Java. `docker-up.sh` проверяет именно `http://localhost:8080/`, то есть тот же адрес, который открывает пользователь.
