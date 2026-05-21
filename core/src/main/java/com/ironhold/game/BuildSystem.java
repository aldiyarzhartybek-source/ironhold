package com.ironhold.game;

import com.ironhold.events.BuildPlacementFailedEvent;
import com.ironhold.events.EventBus;
import com.ironhold.events.TowerBuiltEvent;
import com.ironhold.game.model.BuildSlot;
import com.ironhold.game.model.EconomyState;
import com.ironhold.game.model.PlacedTower;
import com.ironhold.game.model.Tower;
import com.ironhold.game.model.TowerTargetingPriority;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Handles tower placement flow and economy checks.
 */
public final class BuildSystem {
    private static final float BUILD_SLOT_CLICK_RADIUS = 28f;
    private static final float MIN_RUNTIME_TOWER_RANGE = 16f;
    private static final int MIN_RUNTIME_TOWER_DAMAGE = 1;
    private static final float MIN_RUNTIME_TOWER_FIRE_RATE_SEC = 0.1f;

    private final EventBus eventBus;
    private final EconomyState economy;
    private final Map<String, Tower> towersById;

    public BuildSystem(EventBus eventBus, EconomyState economy, Map<String, Tower> towersById) {
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus");
        this.economy = Objects.requireNonNull(economy, "economy");
        this.towersById = Objects.requireNonNull(towersById, "towersById");
    }

    public GameFacade.BuildPlacementResult tryPlaceTower(
        GameRuntimeState state,
        String towerId,
        float worldX,
        float worldY
    ) {
        Tower tower = towersById.get(towerId);
        if (tower == null) {
            return fail(towerId, worldX, worldY, GameFacade.BuildPlacementResult.TOWER_NOT_FOUND);
        }
        int slotIndex = findNearestBuildSlotIndex(state.getBuildSlots(), worldX, worldY, BUILD_SLOT_CLICK_RADIUS);
        if (slotIndex < 0) {
            return fail(towerId, worldX, worldY, GameFacade.BuildPlacementResult.SLOT_NOT_FOUND);
        }
        BuildSlot slot = state.getBuildSlots().get(slotIndex);
        if (slot.isOccupied()) {
            return fail(towerId, worldX, worldY, GameFacade.BuildPlacementResult.SLOT_OCCUPIED);
        }
        if (!economy.trySpend(tower.getCost())) {
            return fail(towerId, worldX, worldY, GameFacade.BuildPlacementResult.NOT_ENOUGH_GOLD);
        }
        state.getSessionStats().addGoldSpent(tower.getCost());
        state.getBuildSlots().set(slotIndex, slot.withTower(towerId));
        state.getPlacedTowers().add(new PlacedTower(
            slot.getSlotId(),
            tower.getId(),
            slot.getX(),
            slot.getY(),
            Math.max(MIN_RUNTIME_TOWER_RANGE, tower.getRange()),
            Math.max(MIN_RUNTIME_TOWER_DAMAGE, tower.getDamage()),
            Math.max(MIN_RUNTIME_TOWER_FIRE_RATE_SEC, tower.getFireRateSec()),
            TowerTargetingPriority.FIRST
        ));
        eventBus.publish(new TowerBuiltEvent(tower.getId(), slot.getSlotId(), tower.getCost()));
        return GameFacade.BuildPlacementResult.OK;
    }

    public GameFacade.BuildPlacementResult failNoTowers(float worldX, float worldY) {
        return fail(null, worldX, worldY, GameFacade.BuildPlacementResult.NO_TOWERS_AVAILABLE);
    }

    public boolean trySellTower(GameRuntimeState state, String slotId) {
        if (slotId == null || slotId.isBlank()) {
            return false;
        }
        int slotIndex = findBuildSlotIndexById(state.getBuildSlots(), slotId);
        if (slotIndex < 0) {
            return false;
        }
        BuildSlot slot = state.getBuildSlots().get(slotIndex);
        if (!slot.isOccupied()) {
            return false;
        }
        PlacedTower placed = findPlacedTowerBySlot(state.getPlacedTowers(), slotId);
        if (placed == null) {
            return false;
        }
        Tower tower = towersById.get(placed.getTowerId());
        if (tower == null) {
            return false;
        }
        int refund = Math.max(0, Math.round(tower.getCost() * economy.getSellRecoveryRate()));
        economy.addGold(refund);
        state.getBuildSlots().set(slotIndex, slot.cleared());
        state.getPlacedTowers().remove(placed);
        return true;
    }

    public int calculateSellRefund(String towerId) {
        Tower tower = towersById.get(towerId);
        if (tower == null) {
            return 0;
        }
        return Math.max(0, Math.round(tower.getCost() * economy.getSellRecoveryRate()));
    }

    private GameFacade.BuildPlacementResult fail(
        String towerId,
        float worldX,
        float worldY,
        GameFacade.BuildPlacementResult reason
    ) {
        eventBus.publish(new BuildPlacementFailedEvent(towerId, reason.name(), worldX, worldY));
        return reason;
    }

    private static PlacedTower findPlacedTowerBySlot(List<PlacedTower> placedTowers, String slotId) {
        for (PlacedTower placed : placedTowers) {
            if (slotId.equals(placed.getSlotId())) {
                return placed;
            }
        }
        return null;
    }

    private static int findBuildSlotIndexById(List<BuildSlot> buildSlots, String slotId) {
        for (int i = 0; i < buildSlots.size(); i++) {
            if (slotId.equals(buildSlots.get(i).getSlotId())) {
                return i;
            }
        }
        return -1;
    }

    private static int findNearestBuildSlotIndex(List<BuildSlot> buildSlots, float worldX, float worldY, float radius) {
        float bestDistanceSq = radius * radius;
        int bestIndex = -1;
        for (int i = 0; i < buildSlots.size(); i++) {
            BuildSlot slot = buildSlots.get(i);
            float dx = worldX - slot.getX();
            float dy = worldY - slot.getY();
            float distanceSq = dx * dx + dy * dy;
            if (distanceSq <= bestDistanceSq) {
                bestDistanceSq = distanceSq;
                bestIndex = i;
            }
        }
        return bestIndex;
    }
}
