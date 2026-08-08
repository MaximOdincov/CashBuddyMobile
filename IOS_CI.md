# 🍎 Сборка iOS через GitHub Actions

Так как твой мак на Intel (x86_64), а Compose Multiplatform 1.11+ требует Apple Silicon для iOS,
мы собираем iOS-приложение **в облаке** (GitHub Actions, Apple Silicon runner), а затем
скачиваем готовый `.app` и запускаем локально в Simulator.app.

---

## Шаг 1. Запушить проект на GitHub

```bash
cd /Users/maxim/tech/CashBuddy
git init
git add .
git commit -m "CashBuddy KMP app"
git branch -M main
git remote add origin https://github.com/<твой-логин>/cashBuddy.git
git push -u origin main
```

> Workflow автоматически запустится при пуше (если менялись shared/ или iosApp/).

## Шаг 2. Запустить workflow вручную (или дождаться авто-запуска)

1. Открой репозиторий на GitHub → вкладка **Actions**
2. Слева выбери **iOS Build (Simulator)**
3. Кнопка **Run workflow** → Run

Сборка идёт ~20–30 минут (первый раз дольше — качаются зависимости).

## Шаг 3. Скачать артефакт

После успешного завершения:
1. Кликни на прошедший run
2. Внизу раздел **Artifacts** → **CashBuddy-iOS-Simulator**
3. Скачается `CashBuddy-iOS-Simulator.app.zip`

Распакуй — получишь `CashBuddy.app`.

## Шаг 4. Запустить в Simulator.app на твоём Intel-маке

Приложение собрано под **arm64** (Apple Silicon runner), а твой симулятор на Intel = **x86_64**.
Нужно включить Rosetta для симулятора (это работает на Intel через CoreSimulator translation):

```bash
# 1. Запустить симулятор
open -a Simulator

# 2. Загрузить .app в запущенный симулятор
xcrun simctl install booted /путь/к/CashBuddy.app

# 3. Запустить приложение
xcrun simctl launch booted com.smartbudget.cashbuddy
```

Если `simctl install` ругается на архитектуру — значит CoreSimulator не трансилирует arm64.
Тогда единственный способ — запустить на Apple Silicon маке или в облаке через браузер.

### Альтернатива: запустить в облаке через браузер (browserstack / appetize)

Если локально не идёт, можно загрузить `.app` в:
- **appetize.io** — эмулятор iOS в браузере (есть бесплатный тариф)
- **BrowserStack** — реальные устройства в облаке (платно)

---

## Что внутри workflow (для понимания)

`.github/workflows/ios-build.yml`:
1. Runner `macos-14` (Apple Silicon M1)
2. Ставит JDK 17 + кеширует Kotlin/Native
3. `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` — собирает Shared.framework
4. `xcodebuild ... -sdk iphonesimulator` — собирает iOS `.app`
5. Зипует и загружает как artifact

## Локальный запуск при наличии Mac на Apple Silicon

Если у команды есть Mac на M1/M2/M3 — всё проще, без облака:

```bash
cd CashBuddy
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
open iosApp/iosApp.xcodeproj
# В Xcode: выбрать симулятор → Run
```
