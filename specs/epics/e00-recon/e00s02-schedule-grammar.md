# Story e00s02: Грамматика schedule + стабильность id

**type:** feat
**risk:** P1
**context:** domain
**Context:** Разведка без токена — стартовать первой.
Снимает Q3 (база W), Q4 (якорь чётности),
Q5/Q8 (стабильность lesson/group id), Q6/Q7
(subnum/ctype). Результат — фикстуры и заметка,
на которых стоит ядро e02s01. Дискавери-деталь:
`timeslot = (W,HH:MM:SS,HH:MM:SS,P)`,
`P ∈ {full,upper,lower}`, слоты из `/time/list`
(документации API нет — только LIVE-пробы).

## Requirements

#### ADDED: Фикстуры schedule-API
`grade/list`, `group/forGrade/*`, минимум два
`schedule/group/{id}`, `time/list`, `week`
в `specs/fixtures/schedule/` сырьём.

#### ADDED: Заметка грамматики
W-база (0/1 = понедельник?), источник якоря
чётности для произвольной даты, смысл
subnum/ctype/uberid, стабильность id —
что подтверждено, что гипотеза.

#### ADDED: Первый снимок для R4
Датированный снимок `schedule/group` и
`forGrade` как база сравнения во времени.

## Steps

1. Снять и положить фикстуры группы 116+
→ verify: `test -f specs/fixtures/schedule/group-116.json`
2. Написать grammar-note.md с выводами Q3–Q8
→ verify: `test -f specs/fixtures/schedule/grammar-note.md`
3. Положить датированный R4-снимок
→ verify: `ls specs/fixtures/schedule/snap-*`

## Verification Script

1. Фикстуры открываются, даты снятия видны.
2. `grammar-note.md` явно размечает факт
против гипотезы по каждому Q.
3. e02s01 может стартовать: W-база и чётность
известны или помечены допущением.

## Out of scope

- Код парсера (e02s01), выбор группы в UI.

## Risks

- W-база не бьётся с реальными парами →
e02s01 стартует с допущением, проверка позже.
- id групп плывут регулярно → B5-резолв
по human-ключу обязателен (уже в e02s02).
