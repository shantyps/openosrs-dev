package net.runelite.rs.api;

import net.runelite.api.PacketWriter;
import net.runelite.api.ServerPacket;
import net.runelite.mapping.Import;

public interface RSPacketWriter extends PacketWriter {
    @Import("serverPacket")
    @Override
    ServerPacket getServerPacket();
}
