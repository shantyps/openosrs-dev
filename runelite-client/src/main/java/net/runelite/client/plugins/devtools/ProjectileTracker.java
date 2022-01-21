package net.runelite.client.plugins.devtools;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ProjectileMoved;

import javax.inject.Inject;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kris | 22/10/2021
 */
@Slf4j
public class ProjectileTracker {

    private final Set<Projectile> inspectedProjectiles = Collections.synchronizedSet(new HashSet<>());
    private final Map<EarlyProjectileInfo, List<DynamicProjectileInfo>> trackedProjectiles = Collections.synchronizedMap(new HashMap<>());
    private final Map<EarlyProjectileInfo, Integer> identifiedProjectiles = Collections.synchronizedMap(new HashMap<>());
    private final Client client;

    @Inject
    public ProjectileTracker(Client client) {
        this.client = client;
    }

    void clearCache() {
        inspectedProjectiles.clear();
        trackedProjectiles.clear();
        identifiedProjectiles.clear();
    }

    void submitProjectileMoved(Client client, ProjectileMoved event, boolean rsFormat, QuadConsumer<EarlyProjectileInfo, DynamicProjectileInfo, String,
                               String> consumer, boolean translateCoordsInInstance) {
        Projectile projectile = event.getProjectile();
        /* No need to run the calculations for every tick that the projectile moves - just the first, on spawn, is enough. */
        if (!inspectedProjectiles.add(projectile)) return;
        /* If target idx isn't 0 but actor is null, we cannot accurately measure the projectile's info so discard it. */
        final int targetIdx = event.getProjectile().getRsInteracting();
        final Actor target = projectile.getInteracting();
        if (targetIdx != 0 && target == null) {
            log.info("Ambiguous target size - unable to determine source size which is necessary for real distance calculations.");
            return;
        }
        final LocalPoint destinationPoint = target != null ? target.getLocalLocation() : event.getPosition();
        final int localSrcPointX = projectile.getX1();
        final int localSrcPointY = projectile.getY1();
        final LocalPoint sourcePoint = new LocalPoint(localSrcPointX, localSrcPointY);
        final int tileHeight = Perspective.getTileHeight(client, sourcePoint, projectile.getFloor());
        final int currentCycle = client.getGameCycle();
        final int startCycle = projectile.getStartCycle() - currentCycle;
        final int endCycle = projectile.getEndCycle() - currentCycle;

        final WorldPoint sourceWorldPoint = WorldPoint.fromLocal(client, sourcePoint);
        final WorldPoint destWorldPoint = target != null ? target.getWorldLocation() : WorldPoint.fromLocal(client, event.getPosition());
        final List<Player> sourcePlayers =
                client.getPlayers().stream().filter(player -> player.getWorldLocation().distanceTo(sourceWorldPoint) == 0).collect(Collectors.toList());
        final List<NPC> sourceNpcs =
                client.getNpcs().stream().filter(npc -> npc.getWorldLocation().distanceTo(sourceWorldPoint) == 0).collect(Collectors.toList());

        final long largeNpcCount = sourceNpcs.stream().filter(npc -> npc.getComposition().getSize() != 1).count();
        if (largeNpcCount > 0 && (!sourcePlayers.isEmpty() || sourceNpcs.size() != largeNpcCount)) {
            log.info("Ambiguous source size - unable to determine source size which is necessary for real distance calculations.");
            return;
        }

        final int id = projectile.getId();
        final int startHeight = -((projectile.getHeight() - tileHeight) / 4);
        final int endHeight = projectile.getEndHeight() / 4;
        final int angle = projectile.getSlope();
        final int distOffset = projectile.getStartHeight();

        final WorldArea sourceArea = !sourceNpcs.isEmpty() ? sourceNpcs.get(0).getWorldArea() : new WorldArea(sourceWorldPoint, 1, 1);
        final WorldArea destinationArea = target != null ? target.getWorldArea() : new WorldArea(destWorldPoint, 1, 1);
        final int distance = sourceArea.distanceTo2D(destinationArea);
        final int tileToTileDistance = new WorldArea(sourceWorldPoint, 1, 1).distanceTo2D(new WorldArea(destWorldPoint, 1, 1));

        final EarlyProjectileInfo earlyProjectile = new EarlyProjectileInfo(id, startHeight, endHeight, angle, distOffset, startCycle);
        final DynamicProjectileInfo dynamicInfo = new DynamicProjectileInfo(sourcePoint, destinationPoint, endCycle - startCycle, distance);


        final String sourcePointString = formatLocation(sourceWorldPoint.getX(), sourceWorldPoint.getY(), sourceWorldPoint.getPlane(), rsFormat, translateCoordsInInstance);
        final String destPointString = formatLocation(destWorldPoint.getX(), destWorldPoint.getY(), destWorldPoint.getPlane(), rsFormat, translateCoordsInInstance);
        final String targetString = target instanceof Player ? ("Player(" + (target.getName() + " - " + destPointString + ")"))
                : target instanceof NPC ? ("Npc(" + (target.getName() + " - " + destPointString + ")"))
                : ("Unknown(" + destPointString + ")");
        final String excessInfo =
                " - tile to tile distance: " + tileToTileDistance + ", source to destination distance: " + distance + ", flight time: " + (endCycle - startCycle);
        final String projectileText = getProjectileText(earlyProjectile, dynamicInfo) + excessInfo;
        if (sourcePlayers.size() == 1 && sourceNpcs.isEmpty()) {
            consumer.accept(earlyProjectile, dynamicInfo,
                    "Player(" + sourcePlayers.get(0).getName() + " - " + sourcePointString + ") -> " + targetString,
                    projectileText);
        } else if (sourceNpcs.size() == 1 && sourcePlayers.isEmpty()) {
            consumer.accept(earlyProjectile, dynamicInfo, "Npc(" + sourceNpcs.get(0).getName() + " - " + sourcePointString + ") -> " + targetString,
                    projectileText);
        } else {
            consumer.accept(earlyProjectile, dynamicInfo, "Unknown(" + sourcePointString + ") -> " + targetString, projectileText);
        }
    }

    private String formatLocation(final int x, final int y, final int z, boolean rsFormat, boolean translateCoordsInInstance) {
        LocalPoint localPoint = LocalPoint.fromWorld(client, x, y);
        final boolean isInInstance = x >= 6400;
        final WorldPoint baseMapPoint = localPoint == null ? null : WorldPoint.fromLocalInstance(client, localPoint, z);
        if (rsFormat) {
            if (isInInstance && baseMapPoint != null && translateCoordsInInstance) {
                final int msqx = baseMapPoint.getX() >> 6;
                final int msqz = baseMapPoint.getY() >> 6;
                final int tx = baseMapPoint.getX() & 0x3F;
                final int tz = baseMapPoint.getY() & 0x3F;
                return "level = " + z + ", msqx = " + msqx + ", msqz = " + msqz + ", tx = " + tx + ", tz = " + tz + ", instanced = true";
            } else {
                final int msqx = x >> 6;
                final int msqz = y >> 6;
                final int tx = x & 0x3F;
                final int tz = y & 0x3F;
                return "level = " + z + ", msqx = " + msqx + ", msqz = " + msqz + ", tx = " + tx + ", tz = " + tz;
            }
        } else {
            if (isInInstance && baseMapPoint != null && translateCoordsInInstance) {
                return "x = " + baseMapPoint.getX() + ", y = " + baseMapPoint.getY() + ", z = " + baseMapPoint.getPlane() + ", instanced = true";
            } else {
                return "x = " + x + ", y = " + y + ", z = " + z;
            }
        }
    }

    String getProjectileText(EarlyProjectileInfo earlyProjectile, DynamicProjectileInfo dynamicInfo) {
        Integer identifiedProjectileDuration = identifiedProjectiles.get(earlyProjectile);
        if (identifiedProjectileDuration == null) {
            /* Check if this projectile already has information on it, if so, we can provide in-depth values for that projectile. */
            List<DynamicProjectileInfo> existingDynamicInfos = trackedProjectiles.computeIfAbsent(earlyProjectile, k -> new ArrayList<>());
            Optional<DynamicProjectileInfo> diffDistanceInfo =
                    existingDynamicInfos.stream().filter(dynamicProjectileInfo -> dynamicProjectileInfo.distance != dynamicInfo.distance).findFirst();

            existingDynamicInfos.add(dynamicInfo);

            if (diffDistanceInfo.isPresent()) {
                DynamicProjectileInfo existingDynamicInfo = diffDistanceInfo.get();
                final int distanceDiff = dynamicInfo.distance - existingDynamicInfo.distance;
                final int durationDiff = dynamicInfo.flightDuration - existingDynamicInfo.flightDuration;
                final int durationPerTileDistance = Math.abs(durationDiff / distanceDiff);
                identifiedProjectiles.put(earlyProjectile, durationPerTileDistance);
                /* Since identified projectile now tracks this, we no longer need to keep a hold of these projectiles. */
                trackedProjectiles.remove(earlyProjectile);
                return formatFullProjectileEntry(earlyProjectile, dynamicInfo, durationPerTileDistance);
            }
            return formatIncompleteProjectileEntry(earlyProjectile, dynamicInfo);
        }
        return formatFullProjectileEntry(earlyProjectile, dynamicInfo, identifiedProjectileDuration);
    }

    private String formatIncompleteProjectileEntry(EarlyProjectileInfo earlyProjectileInfo, DynamicProjectileInfo dynamicProjectileInfo) {
        return "IncompleteProjectile(id = " + earlyProjectileInfo.getId() + ", startHeight = " + earlyProjectileInfo.getStartHeight() + ", endHeight = " + earlyProjectileInfo.getEndHeight() + ", delay = " + earlyProjectileInfo.getDelay() + ", angle = " + earlyProjectileInfo.getAngle() + ", distOffset = " + earlyProjectileInfo.getDistanceOffset() + ") - Distance: " + dynamicProjectileInfo.getDistance() + ", flight duration: " + dynamicProjectileInfo.getFlightDuration();
    }

    private String formatFullProjectileEntry(EarlyProjectileInfo earlyProjectileInfo, DynamicProjectileInfo dynamicProjectileInfo, int stepMultiplier) {
        final int duration = dynamicProjectileInfo.getFlightDuration();
        final int distance = dynamicProjectileInfo.getDistance();
        final int lengthAdjustment = duration - (distance * stepMultiplier);
        return "Projectile(id = " + earlyProjectileInfo.getId() + ", startHeight = " + earlyProjectileInfo.getStartHeight() + ", endHeight = " + earlyProjectileInfo.getEndHeight() + ", delay = " + earlyProjectileInfo.getDelay() + ", angle = " + earlyProjectileInfo.getAngle() + ", lengthAdjustment = " + lengthAdjustment + ", distOffset = " + earlyProjectileInfo.getDistanceOffset() + ", stepMultiplier = " + stepMultiplier + ")";
    }

    @EqualsAndHashCode
    @Getter
    static class DynamicProjectileInfo {
        private final LocalPoint startPoint;
        private final LocalPoint endPoint;
        private final int flightDuration;
        private final int distance;

        DynamicProjectileInfo(LocalPoint startPoint, LocalPoint endPoint, int flightDuration, int distance) {
            this.startPoint = startPoint;
            this.endPoint = endPoint;
            this.flightDuration = flightDuration;
            this.distance = distance;
        }
    }

    @Data
    static class EarlyProjectileInfo {
        private final int id;
        private final int startHeight;
        private final int endHeight;
        private final int angle;
        private final int distanceOffset;
        private final int delay;
    }

    @FunctionalInterface
    public interface QuadConsumer<X, Y, T, U> {
        void accept(X x, Y y, T t, U u);
    }

}
