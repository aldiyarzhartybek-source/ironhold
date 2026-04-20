# IronHold

IronHold — прототип tower defense на Java + LibGDX (Stage 0 baseline).

## Текущий статус (Stage 0)

На текущем этапе реализованы базовые вещи:

- desktop/core структура проекта на Gradle
- базовые экраны: `Loading -> Menu -> Game`
- UI меню через Scene2D (`Stage`, `Skin`, `Label`, `TextButton`)
- централизованная загрузка ассетов через `AssetService`
- загрузка базовых JSON-конфигов (`enemies`, `towers`, `waves`, `economy`)
- каркас EventBus и GameFacade
- доменные модели-заглушки (Enemy, Tower, BuildSlot, WaveDefinition, EconomyState)

## Технологии

- Java 11+
- LibGDX
- Gradle 8.x

## Структура проекта

- `core` — игровая логика, экраны, фасады, модели
- `lwjgl3` — desktop launcher (LWJGL3)
- `assets` — игровые ресурсы (конфиги, карты, и т.д.)

## Как запустить (Desktop)

### Требования

- Установленная JDK 11+
- Gradle wrapper (`./gradlew`) уже в репозитории

### Запуск

```bash
./gradlew :lwjgl3:run
```

## Базовый smoke-check

1. Игра стартует без падений.
2. После loading открывается menu.
3. Кнопка **Start** открывает `GameScreen`.
4. Кнопка **Exit** закрывает приложение.
5. На `GameScreen` отображаются placeholder-данные (в т.ч. доменные счетчики).

## Платформы

Текущий фокус Stage 0:
- macOS
- Windows

## Ограничения текущего этапа

- Механики tower defense в полном виде еще не реализованы.
- Большая часть систем пока в формате каркаса/заглушек для следующего этапа.

## Дальше

README будет уточняться по мере реализации следующих задач:
- расширение систем уровня
- события/состояния
- боевые и экономические механики
- контент и баланс
