# Story e05s01: Схема v1 + сервер обмена

**type:** feat
**risk:** P0
**context:** domain
**Context:** Сервер не развёрнут — стори включает
его: Flask на `gradule.madebykowie.ru`,
`POST /api/export` → `{exportId, url}`,
`GET /s/{exportId}`. Схема v1: шаблон WEEKLY
с `FULL` + разовые ONE_OFF с датой, weekday
в базе из e00s02. Защита: `exportId` случайный
(`token_urlsafe(16)`, не счётчик), TTL 6 мес
от создания, лимит 5/10 мин (IP + заголовок).
Slopcheck: Flask [OK], flask-limiter [OK].
Без токена БРС в экспорте.

## Requirements

#### ADDED: Схема экспорта v1
Шаблон + разовые, FULL, версия схемы;
сериализация/парсинг с тестами.

#### ADDED: Сервер
Случайный id, TTL, лимиты, понятный ответ
на устаревшую ссылку.

## Steps

1. Схема v1 + round-trip тесты
→ verify: `./gradlew :app:testDebugUnitTest`
2. Flask-сервис + его тесты
→ verify: `test -f server/app.py`

## Verification Script

1. Экспорт → импорт на чистом устройстве:
расписание совпало.
2. Перебор id — чужие не находятся.
3. Просроченная ссылка — понятный ответ.

## Out of scope

- UI (e05s02), обновления снапшота
(импорт — срез, связи нет).

## Risks

- Лимит только по клиентскому заголовку
подделывается → IP как основа.
- Токен БРС никогда не покидает устройство.
