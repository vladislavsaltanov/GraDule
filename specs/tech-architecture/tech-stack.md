# Project Context — GraDule

Неофициальное приложение студентов ЮФУ: расписание + баллы БРС.

## Stack
- Kotlin 2.2.10, AGP 9.4.0 (uncommitted bump ex 9.2.1), Gradle wrapper 9.6.0 (uncommitted ex 9.4.1)
- compileSdk 36 (minor 1), minSdk 31, targetSdk 36, Java 11 sourceCompat, daemon toolchain 21 (foojay)
- Jetpack Compose BOM 2026.02.01 (Context7 latest: 2026.08.00 — upgrade candidate) + Material3, activity-compose 1.13.0, lifecycle-runtime-ktx 2.10.0, core-ktx 1.19.0
- Single-module (`:app`), version catalog `gradle/libs.versions.toml`

## Architecture
- Pattern: template single-Activity Compose (MainActivity → Scaffold → Greeting). No layers yet.
- Entry: `MainActivity.onCreate` + `setContent { GraDuleTheme { Scaffold } }`
- Theme: `ui/theme/` (Color/Theme/Type), dynamic color on API 31+

## Conventions (observed)
- No CLAUDE.md / CONVENTIONS.md / specs/ (created this baseline 2026-09-30)
- Tests: junit4 unit (`app/src/test`), espresso+compose-ui-test androidTest — both template-only
- Issues: GitHub issues via `gh` (`docs/agents/issue-tracker.md`); domain: single-context, no CONTEXT.md yet

## Signals
- `local.properties` was tracked — untracked 2026-09-30, now ignored
- Dirty tree: AGP/wrapper bumps + `.idea/vcs.xml` staged — decide commit vs revert before branching
- Baseline 2026-09-30: `:app:testDebugUnitTest` GREEN after compileSdk 36.1→37 (core-ktx 1.19.0 demands API 37+)
- No networking/persistence deps yet — first real epic will define data layer (BРС/расписание API)
- Next: `elaborate-spec` (first feature) → `scope-work` → `slice-tasks` → `plan-work`
