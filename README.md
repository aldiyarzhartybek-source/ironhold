# IronHold

**Tower Defense на Java 11 + LibGDX** — финальный проект курса «Шаблоны проектирования».

![Java](https://img.shields.io/badge/Java-11+-orange?style=flat-square&logo=openjdk)
![LibGDX](https://img.shields.io/badge/LibGDX-1.12-red?style=flat-square)
![Gradle](https://img.shields.io/badge/Gradle-8.x-blue?style=flat-square&logo=gradle)
![Patterns](https://img.shields.io/badge/GoF_Patterns-7+-purple?style=flat-square)

---

## О проекте

IronHold — геометрический tower defense с псевдо-2.5D визуалом в стиле минимализма.
Игрок расставляет башни на слотах, отбивает волны врагов и управляет экономикой золота.

Проект написан как **демонстрация паттернов GoF в production-контексте**: каждый
архитектурный приём решает конкретную проблему в игровом коде, а не добавлен «для галочки».

---

## Геймплей

- **5 уровней** с нарастающей сложностью
- **3 режима:** Classic (ручной старт волн), One Life(одна жизнь), Rush (авто-старт)
- Постройка, апгрейд и снос башен прямо в игре
- Таргетинг: Nearest / First / Strongest на выбор
- Win / Lose оверлей с Restart и Back to Menu

### Башни

| Иконка | Башня | Атака | Особенность |
|--------|-------|-------|-------------|
| ⬜ | **Dart** | Одиночный луч | Быстрый, точный |
| 🔷 | **Lightning** | Цепь до 3 врагов | Мгновенный урон |
| 🔴 | **Mortar** | AoE-взрыв | Медленный снаряд, splash-радиус |
| 🔺 | **Flame** | Конус огня | Близкое AoE, высокая частота |

### Враги

| Враг | HP | Скорость | Особенность |
|------|----|---------|-------------|
| **Grunt** | 150 | ×1.0 | Стандартный |
| **Runner** | 100 | ×1.45 | Быстрый |
| **Elite** | 250 | ×0.95 | Живучий |
| **Boss** | 1950 | ×0.72 | Финальная угроза волны |

---

## Паттерны проектирования

Центральная тема проекта. Каждый паттерн закрывает реальную архитектурную задачу.

### Strategy — атаки башен (`com.ironhold.combat`)

Вместо `if/else` по `towerId` — единый пайплайн, полиморфный вызов.

```java
// CombatRuntimeSystem — один цикл для всех 4 типов
tower.getAttackStrategy().fire(tower, target, state);
```

| Класс | Башня |
|-------|-------|
| `ProjectileAttackStrategy` | Dart — быстрый луч |
| `LightningAttackStrategy` | Lightning — цепь + мгновенный урон |
| `MortarAttackStrategy` | Mortar — снаряд + AoE при взрыве |
| `FlamethrowerAttackStrategy` | Flame — конус огня |

> **Связанный паттерн — Factory Method:** `AttackStrategyFactory.create(towerId)` —
> единственное место, где строковый id влияет на поведение. Новая башня = новый `case` + новый класс.

### Strategy (enum) — таргетинг (`TowerTargetingPriority`)

Enum с абстрактным методом `pickInRange()` — Strategy без отдельной иерархии классов
(Effective Java идиома). Отдельна от Strategy атак намеренно: два разных алгоритма,
две разные точки расширения.

### Template Method — игровой кадр (`LevelUpdateTemplate`)

Фиксированный порядок шагов; подкласс заполняет только конкретные шаги:

```
updateLevelState → publishWaveEvents → processSpawns → updateCombat → afterCombat
```

`final`-метод `updateFrame()` гарантирует, что порядок никогда не изменится случайно.

### Facade — `GameFacade`

Единственная точка входа для `GameScreen`, `StageHud`, `BuildSlotPopup` и других
UI-компонентов к 6 подсистемам (`BuildSystem`, `CombatRuntimeSystem`, `SpawnSystem`,
`WaveEventSystem`, `EconomySystem`, `EventBus`).

### Observer — `SimpleEventBus`

Слабая связанность: `BuildSystem` не знает о `StageHud`, но `StageHud` сам
подпишется на `TowerBuiltEvent`.

Реализованные события: `EnemyKilledEvent`, `EnemySpawnedEvent`, `TowerBuiltEvent`,
`WaveStartedEvent`, `WaveCompletedEvent`, `BuildPlacementFailedEvent`.

### Builder — `GameRuntimeView.Builder`

Иммутабельный снимок состояния игры, который рендер читает за кадр.
UI-слой никогда не обращается к мутабельному `GameRuntimeState` напрямую.

```java
GameRuntimeView view = GameRuntimeView.builder()
    .fromRuntime(state, eventTracker)
    .gold(state.getEconomy().getGold())
    .build();
```

---

## Архитектура

```
core/
├── assets/          — загрузка ресурсов
├── combat/          — AttackStrategy + Factory (GoF)
├── config/          — парсинг и валидация JSON-конфигов
├── core/            — экраны, HUD, popup'ы (UI-слой)
│   └── render/      — рендереры: башни, враги, снаряды, эффекты
├── events/          — EventBus (Observer, GoF)
├── game/            — оркестрация: GameFacade, CombatRuntimeSystem,
│   │                  SpawnSystem, BuildSystem (Template Method, Facade)
│   └── model/       — иммутабельные модели данных
├── level/           — состояние уровня, фазы волн
├── save/            — прогресс игрока
└── ui/              — GameTheme, UiLayer
```

**Принцип зависимостей:** каждый слой зависит только от слоёв ниже.
`render/` не знает о `combat/`, `combat/` не знает о `core/`.

---

## Визуальный стек

Рендер без текстур — только `ShapeRenderer` + `SpriteBatch`:

1. Backdrop — тёмно-синий фон
2. Path shadow — длинная диагональная тень (+22 px, −18 px)
3. Path wall — лавандово-серые «стены»
4. Path groove — тёмный канал дороги
5. Build slots — карманы башен
6. Enemies — геометрические фигуры с белым контуром и hit-flash при уроне
7. Towers — база + вращающаяся турель + пульсирующее ядро + отдача при выстреле
8. Projectiles — ориентированный луч с trail-эффектом из 4 призрачных копий
9. FX — конус огня, молния, взрыв мортиры, hit-эффекты
10. Bloom post-process (`FxBloomPipeline`) — только FX-слой, HUD остаётся чётким
11. HUD + Scene2D

---

## Быстрый старт

### Требования

- JDK 11+
- Gradle wrapper (`./gradlew`) — уже в репозитории

### Запуск

```bash
./gradlew :lwjgl3:run
```

### Сборка JAR

```bash
./gradlew :lwjgl3:jar
java -jar lwjgl3/build/libs/IronHold-desktop.jar
```

---

## Конфигурация

Все игровые параметры — JSON-файлы в `assets/config/`.
Движок валидирует значения при запуске и автоисправляет невалидные с warning в лог.

| Файл | Что настраивает |
|------|-----------------|
| `towers.json` | Стоимость, урон, дальность, скорострельность, таргетинг |
| `enemies.json` | HP, скорость, награда, визуальная форма |
| `waves.json` | Состав волн, интервал спавна |
| `economy.json` | Стартовое золото, штрафы |
| `levels/` | Пути на карте, слоты башен, привязка волн |

---

## Авторы

Жартыбек А.Е, Смаков Д.А 
