package net.runelite.rs.api;

import net.runelite.api.NPC;
import net.runelite.mapping.Import;

public interface RSNPC extends RSActor, NPC
{
	@Import("definition")
	@Override
	RSNPCComposition getComposition();

	@Import("nameChange")
	@Override
	String getNameOverride();

	@Import("definition")
	void setComposition(RSNPCComposition composition);

	@Override
	int getIndex();

	void setIndex(int id);
}
