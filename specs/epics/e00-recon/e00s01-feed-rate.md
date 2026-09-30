# Story e00s01: R1+R2 — лента БРС и Rate

**type:** feat
**risk:** P2
**context:** domain
**Context:** Разведка без кода приложения.
Фикстуры снимают Q1 (формат `/api/v0/events`)
и Q2 (серверный `Rate` против суммы).
Без них блокируются парсер ленты (e03s02)
и решение B4. Ждёт токен пользователя.

## Requirements

#### ADDED: Сырой ответ ленты как фикстура
Полное тело ответа `GET /api/v0/events`
с реальным токеном + response headers
(Content-Type) в `specs/fixtures/brs/`.
Токен в фикстуре заменить на `TOKEN`.

#### ADDED: Заметка решения B4
Сверка `Rate` из `/student/` против суммы
подмодулей + `ExtraRate` на 3–4 дисциплинах
(включая бонусную/замороженную).
Вывод: показывать серверное число.

## Steps

1. Запросить ленту, сохранить тело+заголовки
→ verify: `test -f specs/fixtures/brs/events.raw`
2. Снять `/student/` и `/discipline/subject`,
сверить Rate, записать вывод
→ verify: `test -f specs/fixtures/brs/rate-note.md`

## Verification Script

1. Открыть `events.raw` — тело непустое,
токен заменён.
2. Прочитать `rate-note.md` — явный вывод
по Q1/Q2, решение B4 подтверждено/скорректировано.

## Out of scope

- Парсер ленты (e03s02), UI, Room.

## Risks

- Токен не дадут → стори висит; e03s01 идёт
по серверному Rate как гипотезе.
- Формат XML → Gson-план e03 мёртв,
конвертер выбирается по факту.
