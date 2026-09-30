# Story e07s02: PDF недели

**type:** feat
**risk:** P1
**context:** domain
**Context:** Экспорт расписания в PDF объёмом
в неделю (решение gradule-pdf-week): дни,
слоты, `info`, подгруппа пользователя.
Источник — те же «записи на дату», что экран
и виджет. Верхняя/нижняя недели — явным
заголовком.

## Requirements

#### ADDED: PDF недели
Одна неделя на документ; шаринг системным
sheet; название с датами и чётностью.

## Steps

1. Рендер недели в PDF + тест на фикстуре
→ verify: `./gradlew :app:testDebugUnitTest`
2. Кнопка + шаринг
→ verify: `./gradlew :app:assembleDebug`

## Verification Script

1. Сгенерировать PDF — все дни, info на месте.
2. Поделиться в мессенджер — открывается.

## Out of scope

- PDF семестра/дня, печать.

## Risks

- Кириллица в шрифтах PDF — проверить
на реальном файле.
