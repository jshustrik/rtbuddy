# RouteBuddy

Веб-платформа путешествий с авторскими маршрутами.

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

## Карты

Приложение использует API Яндекс.Карт.
Интерактивные карты подключаются через JavaScript API, а печатная PDF-выгрузка строит статичные изображения карт через Static API.

Ключ по умолчанию уже задан для локального Docker-запуска:

```text
***********************
```

Для замены ключа:

```bash
YANDEX_MAPS_API_KEY=your-key ./scripts/docker-up.sh
```

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
| `routesview-service` | HTML-интерфейс, proxy API, PDF-печать |

## Проверка

```bash
./scripts/test-all.sh
./scripts/smoke-load.sh
```

`test-all.sh` сам запускает тесты в Docker, если на машине нет локального Java. `docker-up.sh` проверяет именно `http://localhost:8080/`, то есть тот же адрес, который открывает пользователь.
