package net.runelite.mixins;

import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.mixins.Copy;
import net.runelite.api.mixins.Inject;
import net.runelite.api.mixins.Mixin;
import net.runelite.api.mixins.Replace;
import net.runelite.rs.api.RSWorldMap;
import net.runelite.rs.api.RSWorldMapManager;

@Mixin(RSWorldMap.class)
public abstract class RSWorldMapMixin implements RSWorldMap
{
	@Inject
	public int zoomSpeed;

	@Override
	@Inject
	public Point getWorldMapPosition()
	{
		RSWorldMapManager worldMapManager = getWorldMapManager();
		int worldX = getWorldMapX() + worldMapManager.getSurfaceOffsetX();
		int worldY = getWorldMapY() + worldMapManager.getSurfaceOffsetY();
		return new Point(worldX, worldY);
	}

	@Inject
	public void setWorldMapPositionTarget(WorldPoint worldPoint)
	{
		setWorldMapPositionTarget(worldPoint.getX(), worldPoint.getY());
	}

	@Copy("smoothZoom")
	@Replace("smoothZoom")
	public void copy$smoothZoom() {
		final float target = getWorldMapZoomTarget();
		final float current = getWorldMapZoom();
		if (current < target) {
			final float value = zoomSpeed == 0 ? target : (Math.min(target, current + (current / zoomSpeed)));
			setWorldMapZoom(value);
		} else if (current > target) {
			final float value = zoomSpeed == 0 ? target : (Math.max(target, current - (current / zoomSpeed)));
			setWorldMapZoom(value);
		}
	}

	@Override
	@Inject
	public void setWorldMapZoomSpeed(int zoomSpeed) {
		this.zoomSpeed = zoomSpeed;
	}

}
