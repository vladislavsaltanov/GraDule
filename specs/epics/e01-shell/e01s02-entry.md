# Story e01s02: Разбор ссылки БРС, хранение, выход

**type:** feat
**risk:** P1
**context:** domain
**Context:** Вход без OAuth/WebView: пользователь
вставляет ссылку ленты
`events?token=&recordbook=&semester=`,
плюс Share Target (`ACTION_SEND`, `text/plain`).
Токен постоянный в query — не логировать URL
с токеном, не слать на свой сервер, не класть
в экспорт. Хранение: шифрование ключом
из Android Keystore перед DataStore
(`security-crypto` deprecated 1.1.0 —
«Use Android Keystore directly» — не брать).
Онбординг: расписание работает без входа.

## Requirements

#### ADDED: Парсер ссылки ленты
Три параметра, понятная ошибка на битой
ссылке; юнит-тесты на фиктивных токенах.

#### ADDED: Share Target как второй ввод
`intent-filter` на `ACTION_SEND`.

#### ADDED: Выход из аккаунта
Чистит токен и все данные (один аккаунт).

## Steps

1. Парсер ссылки + тесты
→ verify: `./gradlew :app:testDebugUnitTest`
2. Share Target + DataStore/Keystore + logout
→ verify: `./gradlew :app:testDebugUnitTest`

## Verification Script

1. Вставить битую ссылку — понятная ошибка.
2. Расшарить ссылку из браузера — подхватилась.
3. Выйти — данные и токен исчезли.

## Out of scope

- Синк баллов (e03), опрос (e06).

## Risks

- Keystore на старых API → fallback с явным
варнингом, не молча.
- Токен в логах Retrofit — логгер скрывает
параметр `token` (проверить в e03).
