# Story e01s01: Тема + гибридная навигация

**type:** feat
**risk:** P0
**context:** infra
**Context:** Каркас, на котором стоят все экраны.
Гибрид: bottom-навигация 3 экранов (главная,
расписание, оценки) + drawer (семестр,
напоминания, обновить, импорт/экспорт).
Цвета — источник истины `docs/design/DESIGN.md`
(прототипы: `home-schedule.html`, `grades.html`,
`grade-changes.html`; синяя палитра дипсик — заглушка, не брать).
Zoom-out: модуль — `MainActivity` + `ui/theme`
шаблона; назначение — точка входа и тема;
вызывающих нет (лист); контракт — сборка
и светлая/тёмная тема.

## Requirements

#### ADDED: Тема из docs/design/DESIGN.md
Фон/карточка/текст/время/практика/лекция,
светлая + тёмная, dynamic color API 31+.

#### ADDED: Гибридная навигация
Bottom 3 + drawer действий; пустые экраны
открываются, тема переключается.

#### ADDED: Общий SyncUiState
`sealed interface SyncUiState<out T>`: Loading,
Empty легитимно / факультет не поддержан,
Content, StaleWithError, FailedNoCache.

## Steps

1. Перенести цвета, проверить обе темы
→ verify: `./gradlew :app:assembleDebug`
2. Bottom 3 + drawer + пустые экраны
→ verify: `./gradlew :app:assembleDebug`
3. SyncUiState + тексты пустых/ошибок
→ verify: `./gradlew :app:testDebugUnitTest`

## Verification Script

1. Собрать debug, открыть каждый экран.
2. Переключить тему — цвета из таблицы.
3. Пустой экран расписания чужого факультета —
текст «не работает», а не «тихо».

## Out of scope

- Данные, сеть, БД (e02/e03).

## Risks

- Drawer+bottom спорят за жесты → проверить
назад/свайп расписания руками.
