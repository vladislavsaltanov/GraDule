# Story e02s01: Ядро расписания (pure Kotlin)

**type:** feat
**risk:** P0
**context:** domain
**Context:** Чистое ядро без Android: парсер
`timeslot`, матчинг звонков по времени начала,
модель `ScheduleEntry` (SERVER|LOCAL;
WEEKLY FULL|UPPER|LOWER + ONE_OFF с датой;
`info` как есть), функция «записи на дату»
с чётностью. Даты вычисляются при чтении,
не хранятся. Заметки/напоминания — на вхождение
(запись+дата). Ждёт e00s02 (W-база, якорь).
Slopcheck: внешних пакетов нет — stdlib [OK].
Reason for Depth: ядро отдельно от адаптеров,
чтобы тестировать на фикстурах без эмулятора.

## Requirements

#### ADDED: Парсер timeslot
`(W,HH:MM:SS,HH:MM:SS,P)`; `minutes` опущен
при нуле → 0; `P` full/upper/lower; тесты
на фикстуре 116: несовпадения — ошибкой.

#### ADDED: Записи на дату
День недели + чётность → подходящие WEEKLY
и ONE_OFF; подгруппы не дублируются в слот;
`info` доставляется рядом со временем.

#### ADDED: Правила reconcile серверного набора
Замена набора SERVER в одной транзакции;
LOCAL/слои пользователя не затрагиваются;
ключ переноса заметок: стабильный id (Q5)
или запасной (день, начало, чётность,
предмет, subnum).

## Steps

1. Парсер + матчинг слотов, тесты на 116
→ verify: `./gradlew :app:testDebugUnitTest`
2. entries-on-date + reconcile-правила, тесты
→ verify: `./gradlew :app:testDebugUnitTest`

## Verification Script

1. Тесты зелёные на фикстуре 116 и образце
пользователя: full/upper/lower, 6 подгрупп
ИнЯз, строки с info.
2. Разовая запись выбирается только своей датой.

## Out of scope

- Сеть, Room, UI (e02s02); оверрайды (e04s02).

## Risks

- W-база/Q4 не закрыты → допущение в коде
с `ponytail:`-комментом и тестом-якорем.
- `REPLACE`+CASCADE (§8 п.2): только явный
upsert по полям, REPLACE запрещён.
