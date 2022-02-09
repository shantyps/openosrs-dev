import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;

@ObfuscatedName("kb")
public final class class291 {
	@ObfuscatedName("fm")
	@ObfuscatedSignature(
		descriptor = "(B)V",
		garbageValue = "5"
	)
	static final void method5478() {
		SceneTilePaint.method4270(class213.field2598, FriendsChat.field4141, class139.field1624); // L: 3601
		class260.method5000(LoginPacket.field3062, Interpreter.field854); // L: 3602
		if (class213.field2598 == class414.cameraX && FriendsChat.field4141 == WorldMapDecoration.cameraY && class139.field1624 == GrandExchangeOfferOwnWorldComparator.cameraZ && LoginPacket.field3062 == class7.cameraPitch && class7.cameraYaw == Interpreter.field854) { // L: 3603
			Client.field755 = false; // L: 3604
			Client.isCameraLocked = false; // L: 3605
			class7.cameraLookAtX = 0; // L: 3606
			class21.cameraLookAtY = 0; // L: 3607
			class334.cameraLookAtHeight = 0; // L: 3608
			WorldMapSectionType.cameraLookAtSpeed = 0; // L: 3609
			WallDecoration.cameraLookAtAcceleration = 0; // L: 3610
			class4.cameraMoveToAcceleration = 0; // L: 3611
			SceneTilePaint.cameraMoveToSpeed = 0; // L: 3612
			Message.cameraMoveToX = 0; // L: 3613
			class12.cameraMoveToY = 0; // L: 3614
			class121.cameraMoveToHeight = 0; // L: 3615
		}

	} // L: 3618
}
