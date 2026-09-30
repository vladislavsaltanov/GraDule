# Story e03s01: Семестры, дисциплины, детали

**type:** feat
**risk:** P0
**context:** domain
**Context:** Баллы офлайн: семестры (Map,
текущий = максимальный id), дисциплины
с серверным `Rate` (B4 — источник значения),
`hiddenByUser` вне обновляемых колонок синка,
`rate == null` — прочерк, детальный экран
(подмодули, `ExtraRate`, сверка мелким).
Ждёт e00s01 (R2). Slopcheck: Retrofit [OK],
Room [OK], версии пинить в каталоге
при реализации (плейсхолдеры не копировать).

## Requirements

#### ADDED: Список семестров и дисциплин
Фильтр `wasRemovedByServer=0 AND hiddenByUser=0`;
скрытие пользователя переживает синк.

#### ADDED: Детальный экран
Подмодули + `ExtraRate`; сумма рядом
с серверным Rate мелким текстом, без алертов;
текстовые значения (физра) — текстом.

## Steps

1. Semester/Discipline + reconcile-контракт
→ verify: `./gradlew :app:testDebugUnitTest`
2. Детальный экран + сверка
→ verify: `./gradlew :app:testDebugUnitTest`

## Verification Script

1. Авиарежим — список и детали из кэша.
2. Скрыть дисциплину, обновить — скрыта.
3. Дисциплина без оценки — прочерк, не 0.

## Out of scope

- Лента (e03s02), журнал посещаемости.

## Risks

- Q2 опровергнет сумму → всё равно показывать
серверное число (решение B4).
- `deleteBySemester` + upsert стирает
`hiddenByUser` — запретить такой reconcile.
