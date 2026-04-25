# RouteBuddy — платформа для создания и просмотра маршрутов

Дипломный проект. Микросервисная система для просмотра, конструирования и экспорта туристических маршрутов.

---

## Необходимые программы

| Программа | Версия | Где скачать |
|-----------|--------|-------------|
| **IntelliJ IDEA** | Community или Ultimate | https://www.jetbrains.com/idea/download |
| **JDK (Java)** | 17 | https://adoptium.net или через IDEA |
| **Git** | любая | https://git-scm.com/downloads |

> **macOS:** JDK можно установить через Homebrew: `brew install openjdk@17`
>
> **Windows:** скачать JDK 17 с сайта Adoptium, установить, прописать `JAVA_HOME` в переменные среды.

---

## Шаг 1 — Скачать репозиторий

```bash
git clone git@github.com:jshustrik/rtbuddy.git
cd rtbuddy
```

Или через HTTPS (если нет SSH-ключа):

```bash
git clone https://github.com/jshustrik/rtbuddy.git
cd rtbuddy
```

---

## Шаг 2 — Открыть в IntelliJ IDEA

1. Запустить IntelliJ IDEA
2. **File → Open** → выбрать папку `rtbuddy`
3. IDEA обнаружит проект Gradle и предложит импортировать — нажать **OK / Trust Project**
4. Дождаться синхронизации Gradle (прогресс-бар внизу)

---

## Шаг 3 — Запустить демо-сервис

Запуск через терминал (из корня репозитория):

```bash
cd routesview-service
../gradlew bootRun
```

Или в IntelliJ IDEA:
- Открыть `routesview-service/src/main/kotlin/.../RoutesviewServiceApplication.kt`
- Нажать зелёную кнопку ▶ рядом с функцией `main`

> **Если Java не найдена** (ошибка `Unable to locate Java Runtime`):
>
> macOS:
> ```bash
> export JAVA_HOME=$(brew --prefix openjdk@17)
> export PATH="$JAVA_HOME/bin:$PATH"
> ```
>
> Затем повторить `../gradlew bootRun`.

---

## Шаг 4 — Открыть в браузере

После успешного запуска (в консоли появится `Started RoutesviewServiceApplication`):

| Страница | URL |
|----------|-----|
| **Маршрут + отзывы** | http://localhost:8083/route |
| **Конструктор маршрута** | http://localhost:8083/constructor |
| **Предпросмотр и PDF** | http://localhost:8083/export |

---

## Как пользоваться демо

### Маршрут (`/route`)
- Просматривайте дни маршрута «Неделя в Санкт-Петербурге» с точками
- Оставляйте отзывы — они сохраняются в браузере (localStorage)
- Удаляйте отзывы кнопкой ×

### Конструктор (`/constructor`)
- Раскройте любой день из трёх маршрутов (СПб, Золотое кольцо, Байкал)
- Нажмите **«Добавить»** — день попадёт в «Мой маршрут» справа
- Перетасуйте дни стрелками ↑ ↓, удалите ненужные
- Нажмите **«Сохранить маршрут»** — маршрут фиксируется
- Нажмите **«Выгрузить маршрут (PDF)»** — откроется окно предпросмотра со встроенными картами

### Предпросмотр PDF (`/export`)
- Показывает маршрут из конструктора с картами Leaflet + OSM
- Дождитесь загрузки карт (кнопка «Загрузка карт...» → «Сохранить как PDF»)
- Нажмите кнопку → выберите **«Сохранить как PDF»** в диалоге печати

---

## Технологии

- **Kotlin** + **Spring Boot 4** (routesview-service)
- **Thymeleaf** — серверный рендеринг HTML-шаблонов
- **Bootstrap 5** + **Font Awesome 6** — UI
- **Leaflet.js** + **OpenStreetMap** — интерактивные карты (без API-ключа)
- **H2** — встроенная база данных (для демо, настройка не нужна)
- **localStorage** — хранение маршрута и отзывов на стороне браузера

---

## Структура проекта

```
rtbuddy/
├── routesview-service/          ← основной демо-сервис (порт 8083)
│   ├── src/main/kotlin/...
│   │   ├── controller/
│   │   │   ├── ConstructorController.kt   → /constructor
│   │   │   ├── RoutePageController.kt     → /route
│   │   │   └── ExportController.kt        → /export
│   │   └── config/
│   │       └── RoutesViewServiceSecurityConfig.kt
│   └── src/main/resources/
│       ├── application.yml
│       └── templates/
│           ├── constructor.html   ← конструктор маршрута
│           ├── route-demo.html    ← страница маршрута с отзывами
│           └── export-demo.html   ← предпросмотр и печать PDF
├── auth-service/
├── review-service/
└── ...
```

---

## Часто задаваемые вопросы

**Карты не загружаются?**
Нужно подключение к интернету — карты загружаются с серверов OpenStreetMap.

**Отзывы пропали после перезагрузки?**
Очищен localStorage браузера (режим инкогнито или ручная очистка).

**Ошибка при запуске — «port 8083 already in use»?**
Убить процесс: `lsof -ti:8083 | xargs kill -9` (macOS/Linux).

**Нужна ли MongoDB?**
Нет — для демо-сервиса MongoDB не используется. Все данные либо в памяти (H2), либо в localStorage браузера.
