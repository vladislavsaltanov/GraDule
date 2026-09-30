# Story e07s01: Виджет «пары сегодня»

**type:** feat
**risk:** P1
**context:** domain
**Context:** Виджет берёт тем же запросом
«записи на дату» из локальной БД, не из сети.
Фреймворк на реализации: Jetpack Glance
(на Compose runtime, с app-Compose напрямую
не interoperate — дока) против классики.
Делать после стабильного чтения (e02s02).
Multiple interpretations: Glance vs classic —
решает исполнитель, критерий: меньше кода
при теме из темы.md.

## Requirements

#### ADDED: Виджет сегодня
Пары текущей даты из БД; тап ведёт в экран дня.

## Steps

1. Провайдер данных из БД + превью
→ verify: `./gradlew :app:testDebugUnitTest`
2. Виджет на экране, обновление за день
→ verify: `./gradlew :app:assembleDebug`

## Verification Script

1. Добавить виджет — пары совпадают с экраном.
2. Авиарежим — виджет из кэша.

## Out of scope

- Конфигурируемые размеры, несколько виджетов.

## Risks

- Glance beta-шероховатости → fallback
на классику без смены данных.
