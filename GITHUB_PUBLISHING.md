# Публикация Auralis Player на GitHub

## Карточка репозитория

- **Repository name:** `AuralisPlayer`
- **Description:** `Offline Android music player with a Liquid Glass interface, background playback and a 10-point equalizer.`
- **Website:** оставить пустым, пока нет официального сайта.
- **Topics:** `android`, `kotlin`, `jetpack-compose`, `music-player`, `offline`, `audio-player`, `media3`, `equalizer`, `liquid-glass`.
- **README:** `README.md` (русский); английская версия — `README.en.md`.

## Создать репозиторий

1. Открой GitHub и нажми **New repository**.
2. Введи имя `AuralisPlayer`.
3. Выбери `Public`, если хочешь открыть исходный код всем, или `Private`, если пока не готов публиковать его.
4. Не отмечай создание README, `.gitignore` или License на GitHub — всё это уже есть в проекте.
5. Создай пустой репозиторий.

## Загрузить исходники через Git

Открой PowerShell в корневой папке `AuralisPlayer` — там, где лежат `settings.gradle.kts` и `app`:

```powershell
git init
git add .
git commit -m "Initial commit: Auralis Player 1.2.0"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/AuralisPlayer.git
git push -u origin main
```

Замени `YOUR-USERNAME` на свой GitHub username. Если Git запросит авторизацию, используй официальный способ входа Git Credential Manager / GitHub CLI; не вставляй токен в исходники.

## Аватарка и превью

- Аватар проекта: `assets/auralis-avatar.png` — квадратная картинка с минималистичной фиолетовой стеклянной нотой. Используй её в README, профиле GitHub или аватарке GitHub Organization.
- Social Preview репозитория: `assets/auralis-social-preview.png`. После создания репозитория открой **Settings → General → Social preview → Edit** и загрузи этот файл.
- У GitHub обычный репозиторий не имеет отдельной круглой аватарки как пользовательский аккаунт. Для самого репозитория используют README и Social Preview; аватар можно назначить аккаунту или организации.

## Перед тем как сделать репозиторий публичным

- [ ] Проверь, что в истории Git нет паролей, ключей подписи и `local.properties`.
- [ ] Проведи Gradle Sync в Android Studio.
- [ ] Собери `assembleDebug` и проверь запуск на устройстве.
- [ ] Проверь разрешения аудиобиблиотеки и уведомлений.
- [ ] Проверь фоновое воспроизведение, блокировку экрана и гарнитуру.
- [ ] Проверь эквалайзер на физическом устройстве.
- [ ] Убедись, что имеешь право публиковать весь включённый код, графику и другие материалы.
- [ ] Прочитай `LICENSE`: сейчас в репозитории выбрана MIT License, разрешающая широкое повторное использование кода при сохранении уведомления об авторских правах и текста лицензии.

## После публикации

Добавь Topics из карточки репозитория, установи Social Preview и при необходимости включи вкладку **Issues**. Для следующей версии обнови `versionName` / `versionCode` в `app/build.gradle.kts` и добавь запись в `CHANGELOG.md`.
