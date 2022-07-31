/*
 * Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.mixins;

import com.google.common.collect.ImmutableSet;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.Set;
import net.runelite.api.Actor;
import net.runelite.api.Hitsplat;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.NpcID;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.SpritePixels;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.mixins.FieldHook;
import net.runelite.api.mixins.Inject;
import net.runelite.api.mixins.MethodHook;
import net.runelite.api.mixins.Mixin;
import net.runelite.api.mixins.Shadow;
import net.runelite.rs.api.RSActor;
import net.runelite.rs.api.RSClient;
import net.runelite.rs.api.RSHealthBar;
import net.runelite.rs.api.RSHealthBarDefinition;
import net.runelite.rs.api.RSHealthBarUpdate;
import net.runelite.rs.api.RSIterableNodeDeque;
import net.runelite.rs.api.RSNPC;
import net.runelite.rs.api.RSNode;

@Mixin(RSActor.class)
public abstract class RSActorMixin implements RSActor
{
	@Shadow("client")
	private static RSClient client;

	@Inject
	private static final Set<Integer> combatInfoFilter = ImmutableSet.of(0, 2, 16, 17, 18, 19, 20, 21, 22);

	@Inject
	private boolean dead;

	@Inject
	@Override
	public Actor getInteracting()
	{
		int index = getRSInteracting();
		if (index == -1 || index == 65535)
		{
			return null;
		}

		if (index < 32768)
		{
			NPC[] npcs = client.getCachedNPCs();
			return npcs[index];
		}

		index -= 32768;
		Player[] players = client.getCachedPlayers();
		return players[index];
	}

	@Inject
	@Override
	public int getHealthRatio()
	{
		RSIterableNodeDeque healthBars = getHealthBars();
		if (healthBars != null)
		{
			RSNode current = healthBars.getCurrent();
			RSNode next = current.getNext();
			if (next instanceof RSHealthBar)
			{
				RSHealthBar wrapper = (RSHealthBar) next;
				RSIterableNodeDeque updates = wrapper.getUpdates();

				RSNode currentUpdate = updates.getCurrent();
				RSNode nextUpdate = currentUpdate.getNext();
				if (nextUpdate instanceof RSHealthBarUpdate)
				{
					RSHealthBarUpdate update = (RSHealthBarUpdate) nextUpdate;
					return update.getHealthRatio();
				}
			}
		}
		return -1;
	}

	@Inject
	@Override
	public int getHealthScale()
	{
		RSIterableNodeDeque healthBars = getHealthBars();
		if (healthBars != null)
		{
			RSNode current = healthBars.getCurrent();
			RSNode next = current.getNext();
			if (next instanceof RSHealthBar)
			{
				RSHealthBar wrapper = (RSHealthBar) next;
				RSHealthBarDefinition definition = wrapper.getDefinition();
				return definition.getHealthScale();
			}
		}
		return -1;
	}

	@Inject
	@Override
	public WorldPoint getWorldLocation()
	{
		return WorldPoint.fromLocal(client,
			this.getPathX()[0] * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_TILE_SIZE / 2,
			this.getPathY()[0] * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_TILE_SIZE / 2,
			client.getPlane());
	}

	@Inject
	@Override
	public LocalPoint getLocalLocation()
	{
		return new LocalPoint(getX(), getY());
	}

	@Inject
	@Override
	public Polygon getCanvasTilePoly()
	{
		return Perspective.getCanvasTilePoly(client, getLocalLocation());
	}

	@Inject
	@Override
	public Point getCanvasTextLocation(Graphics2D graphics, String text, int zOffset)
	{
		return Perspective.getCanvasTextLocation(client, graphics, getLocalLocation(), text, zOffset);
	}

	@Inject
	@Override
	public Point getCanvasImageLocation(BufferedImage image, int zOffset)
	{
		return Perspective.getCanvasImageLocation(client, getLocalLocation(), image, zOffset);
	}

	@Inject
	@Override
	public Point getCanvasSpriteLocation(SpritePixels spritePixels, int zOffset)
	{
		return Perspective.getCanvasSpriteLocation(client, getLocalLocation(), spritePixels, zOffset);
	}

	@Inject
	@Override
	public Point getMinimapLocation()
	{
		return Perspective.localToMinimap(client, getLocalLocation());
	}

	@FieldHook("sequence")
	@Inject
	public void animationChanged(int idx)
	{
		if (this instanceof RSNPC)
		{
			int id = ((RSNPC) this).getId();

			if (id == NpcID.CORPOREAL_BEAST && this.getAnimation() == 1676)
			{
				setDead(true);
			}
		}

		AnimationChanged animationChange = new AnimationChanged();
		animationChange.setActor(this);
		client.getCallbacks().post(animationChange);
	}

	@FieldHook("currentSequenceFrameIndex")
	@Inject
	public void animationFrameIndexChanged(int idx)
	{
		AnimationFrameIndexChanged animationChange = new AnimationFrameIndexChanged();
		animationChange.setActor(this);
		client.getCallbacks().post(animationChange);
	}

	@FieldHook("spotAnimationStartCycle")
	@Inject
	public void spotAnimationChanged(int idx)
	{
		GraphicChanged graphicChanged = new GraphicChanged();
		graphicChanged.setActor(this);
		client.getCallbacks().post(graphicChanged);
	}

	@FieldHook("targetIndex")
	@Inject
	public void interactingChanged(int idx)
	{
		InteractingChanged interactingChanged = new InteractingChanged(this, getInteracting());
		client.getCallbacks().post(interactingChanged);
	}

	@FieldHook("facedDirection")
	@Inject
	public void facedDirectionChanged(int idx)
	{
		FacedDirectionChanged facedDirectionChanged = new FacedDirectionChanged(this, getFacedDirection(), instantTurn());
		client.getCallbacks().post(facedDirectionChanged);
	}

	@FieldHook("exactMoveDirection")
	@Inject
	public void exactMoveReceived(int idx)
	{
		ExactMoveEvent exactMoveEvent = new ExactMoveEvent(this, exactMoveDeltaX1(), exactMoveDeltaX2(), exactMoveDeltaY1(), exactMoveDeltaY2(),
				exactMoveArrive1Cycle(), exactMoveArrive2Cycle(), exactMoveDirection(), client.getGameCycle());
		client.getCallbacks().post(exactMoveEvent);
	}

	@FieldHook("recolourAmount")
	@Inject
	public void recolourReceived(int idx) {
		RecolourEvent event = new RecolourEvent(this, recolourStartCycle(), recolourEndCycle(), recolourHue(), recolourSaturation(), recolourLuminance(),
				recolourAmount(), client.getGameCycle());
		client.getCallbacks().post(event);
	}


	@FieldHook("combatLevelChange")
	@Inject
	public void combatLevelChange(int idx) {
		if (getCombatLevelOverride() == -1) return;
		CombatLevelChangeEvent event = new CombatLevelChangeEvent(this, getCombatLevel(), getCombatLevelOverride());
		client.getCallbacks().post(event);
	}

	@FieldHook("overheadText")
	@Inject
	public void overheadTextChanged(int idx)
	{
		String overheadText = getOverheadText();
		if (overheadText != null)
		{
			OverheadTextChanged overheadTextChanged = new OverheadTextChanged(this, overheadText);
			client.getCallbacks().post(overheadTextChanged);
		}
	}

	@FieldHook("showPublicPlayerChat")
	@Inject
	public void showPublicPlayerChatChanged(int idx) {
		client.getCallbacks().post(new ShowPublicPlayerChatChanged());
	}

	@Inject
	@Override
	public WorldArea getWorldArea()
	{
		int size = 1;
		if (this instanceof NPC)
		{
			NPCComposition composition = ((NPC) this).getComposition();
			if (composition != null && composition.getConfigs() != null)
			{
				composition = composition.transform();
			}
			if (composition != null)
			{
				size = composition.getSize();
			}
		}

		return new WorldArea(this.getWorldLocation(), size, size);
	}

	@Inject
	@Override
	public boolean isDead()
	{
		return dead;
	}

	@Inject
	@Override
	public void setDead(boolean dead)
	{
		this.dead = dead;
	}

	@Inject
	@MethodHook("addHealthBar")
	public void setCombatInfo(int combatInfoId, int gameCycle, int var3, int var4, int healthRatio, int health)
	{
		final HealthBarUpdated event = new HealthBarUpdated(this, healthRatio);
		client.getCallbacks().post(event);

		if (healthRatio == 0)
		{
			if (isDead())
			{
				return;
			}

			if (!combatInfoFilter.contains(combatInfoId))
			{
				return;
			}

			setDead(true);

			final ActorDeath actorDeath = new ActorDeath(this);
			client.getCallbacks().post(actorDeath);
		}
		else if (healthRatio > 0)
		{
			if (this instanceof RSNPC && ((RSNPC) this).getId() == NpcID.CORPOREAL_BEAST && isDead())
			{
				return;
			}

			setDead(false);
		}
	}

	/**
	 * Called after a hitsplat has been processed on an actor.
	 * Note that this event runs even if the hitsplat didn't show up,
	 * i.e. the actor already had 4 visible hitsplats.
	 *
	 * @param type      The hitsplat type (i.e. color)
	 * @param value     The value of the hitsplat (i.e. how high the hit was)
	 * @param var3      unknown
	 * @param var4      unknown
	 * @param gameCycle The gamecycle the hitsplat was applied on
	 * @param duration  The amount of gamecycles the hitsplat will last for
	 */
	@Inject
	@MethodHook(value = "addHitSplat", end = true)
	public void applyActorHitsplat(int type, int value, int var3, int var4, int gameCycle, int duration)
	{
		final Hitsplat hitsplat = new Hitsplat(Hitsplat.HitsplatType.fromInteger(type), value, gameCycle + duration);
		final HitsplatApplied event = new HitsplatApplied();
		event.setActor(this);
		event.setHitsplat(hitsplat);
		client.getCallbacks().post(event);
	}

	@Inject
	@Override
	public boolean isMoving()
	{
		return getPathLength() > 0;
	}
}
