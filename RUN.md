# 📱 CashBuddy — запуск Android и сборка iOS

Базовая структура KMP + Compose Multiplatform готова, Android собирается (APK 14 МБ).
Ниже — как запустить приложение на Android и сбилдить под iOS через Xcode.

> Сервер должен быть запущен и доступен с устройства. Адрес задаётся в приложении: **Ещё → Настройки → URL API**.

---

## 1. Запуск под Android

### Вариант A — через Android Studio (рекомендуется)

1. Открой **Android Studio**
2. **File → Open** → выбери папку `/Users/maxim/tech/CashBuddy`
3. Дождись окончания Gradle Sync (внизу进度-бар, ~1–2 минуты)
4. Выбери устройство/эмулятор в тулбаре (сверху)
5. Нажми **▶ Run 'androidApp'** (зелёная стрелка)

Приложение установится и запустится. Если возникнет ошибка Gradle Sync — см. раздел «Траблшутинг» ниже.

### Вариант B — через терминал (сборка APK)

```bash
cd /Users/maxim/tech/CashBuddy
./gradlew :androidApp:assembleDebug
```

APK появится: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

Установить на подключённое устройство:
```bash
adb install androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

### Настройка подключения к серверу

По умолчанию приложение ходит на `http://10.0.2.2:8080` (это `localhost` твоего мака через эмулятор).

- **Эмулятор Android**: `10.0.2.2` → `localhost` хоста. Ничего менять не надо, если сервер на маке.
- **Реальное устройство**: введи IP твоего мака в局域网, например `http://192.168.1.42:8080`.
  Узнать IP: `System Settings → Wi-Fi → Details → IP address` или `ipconfig getifaddr en0`.

Сменить адрес: в приложении **Ещё → ⚙️ Настройки → URL API → Сохранить**.

### Вход

Демо-аккаунт уже на сервере: логин `demo`, пароль `demo1234`.
Или зарегистрируй новый через экран входа.

---

## 2. Сборка под iOS

### Требования
- **Xcode** (последняя версия, желательно 16+). Проверь: `xcodebuild -version`
- **CocoaPods НЕ нужен** — интеграция через встраиваемый фреймворк `Shared`.

### Шаг 1. Сгенерировать Xcode-проект и фреймворк

iOS-проект (`iosApp/`) уже создан Kotlin-плагином. Нужно только сбилдить Kotlin/Native фреймворк `Shared`:

```bash
cd /Users/maxim/tech/CashBuddy
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Это соберёт фреймворк для симулятора (Apple Silicon). Для реального устройства:
```bash
./gradlew :shared:linkDebugFrameworkIosArm64
```

### Шаг 2. Открыть в Xcode

```bash
open iosApp/iosApp.xcodeproj
```

Или через Finder: двойной клик по `iosApp/iosApp.xcodeproj`.

### Шаг 3. Настройка подписи (Signing)

1. В Xcode слева выбери проект **iosApp**
2. Вкладка **Signing & Capabilities**
3. **Team**: выбери твой Apple ID (добавь через *Add an Account...*, если нет — бесплатного достаточно для симулятора)
4. **Bundle Identifier**: сейчас `org.example.project.CashBuddy`. Поменяй на `com.smartbudget.CashBuddy` (или любой уникальный)
5. Если ошибка «Failed to register bundle identifier» — сделай его уникальнее, например `com.smartbudget.cashbuddy.2026`

### Шаг 4. Запуск на симуляторе

1. Сверху в Xcode выбери target: **iosApp** (не Shared)
2. Выбери симулятор: например **iPhone 16** (Simulator)
3. Нажми **▶ Run** (Cmd+R)

Приложение запустится в симуляторе. Сервер доступен на `http://localhost:8080` (для iOS-симулятора `localhost` работает напрямую).

### Шаг 5. Запуск на реальном устройстве (опционально)

1. Подключи iPhone по USB, доверяй компьютеру
2. В Xcode сверху выбери свой iPhone как target
3. Первая сборка на устройство потребует доверия разработчику:
   - На iPhone: **Settings → General → VPN & Device Management → твой Apple ID → Trust**
4. Нажми **▶ Run**

Адрес сервера на реальном iPhone — IP твоего мака в сети: `http://192.168.1.42:8080` (не localhost).

---

## 3. Что готово в приложении

| Экран | Функция |
|---|---|
| 🔐 **Логин** | вход/регистрация/по share-коду |
| 📊 **Бюджет** | список категорий с прогрессом, синхронизация банка, добавить транзакцию |
| 🤖 **Чат (AI)** | диалог с GLM-ассистентом, показ выполненных действий |
| ☰ **Ещё** | цели, уведомления, транзакции, настройки, выход |
| 🐷 **Цели** | список + пополнение |
| 🔔 **Уведомления** | лента |
| 📋 **Транзакции** | история |
| ⚙️ **Настройки** | тема (системная/светлая/тёмная), URL API |
| ➕ **Добавить транзакцию** | форма |

## Тема

Light/Dark/System — переключается в Настройках. По умолчанию «Системная».

---

## 4. Траблшутинг

### Gradle Sync failed в Android Studio
- **No JDK found**: задай JDK — *File → Settings → Build → Gradle → Gradle JDK* → выбери JDK 17 (Embedded JDK или Android Studio JDK)
- **Could not download**: проверь сеть/VPN. Зеркала: можно добавить в `settings.gradle.kts` альтернативные репозитории

### Could not find navigation-compose / ktor
Версии зафиксированы в `gradle/libs.versions.toml`. Если артефакт не находится — проверь актуальную версию на [Maven Central](https://central.sonatype.com/) и поправь в каталоге.

### iOS: «Shared framework not found»
```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```
Затем в Xcode: **Product → Clean Build Folder** (Cmd+Shift+K), потом Run.

### iOS: signing error
Для симулятора подпись не нужна — выбери target `iosApp` + симулятор. Для устройства нужен Apple ID (бесплатный аккаунт подходит для dev-сборки).

### Эмулятор Android не достучаться до сервера
- Убедись, что сервер слушает на `0.0.0.0` или `localhost`. `./gradlew bootRun` в server/ — ОК.
- Эмулятор: адрес `10.0.2.2` (не `localhost`!) — это алиас на хост.
- Реальное устройство: `http://<IP-мака>:8080`, проверь фаервол (System Settings → Network → Firewall).

### Ошибка `ConnectException` в логах приложения
Сервер не запущен или недоступен. Запусти:
```bash
cd /Users/maxim/tech/server
docker compose -f docker-compose.yml up -d   # БД
./gradlew bootRun                            # сервер
```

### Изменил код в shared, но iOS не подхватывает
Xcode кеширует фреймворк. После изменений в Kotlin:
```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```
И в Xcode: **Product → Clean Build Folder** (Cmd+Shift+K) → Run.
