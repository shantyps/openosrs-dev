import net.runelite.mapping.Implements;
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;
import net.runelite.mapping.Export;
@ObfuscatedName("gl")
@Implements("MoveSpeed")
public enum MoveSpeed implements MouseWheel {

	@ObfuscatedName("o")
	@ObfuscatedSignature(descriptor = "Lgl;")
	field2201(((byte) (-1))),
	@ObfuscatedName("q")
	@ObfuscatedSignature(descriptor = "Lgl;")
	field2195(((byte) (0))),
	@ObfuscatedName("f")
	@ObfuscatedSignature(descriptor = "Lgl;")
	field2194(((byte) (1))),
	@ObfuscatedName("u")
	@ObfuscatedSignature(descriptor = "Lgl;")
	field2197(((byte) (2)));
	@ObfuscatedName("sv")
	@ObfuscatedSignature(descriptor = "Lat;")
	@Export("pcmPlayer0")
	static PcmPlayer pcmPlayer0;

	@ObfuscatedName("i")
	@ObfuscatedSignature(descriptor = "Llc;")
	@Export("Widget_spritesArchive")
	public static AbstractArchive Widget_spritesArchive;

	@ObfuscatedName("c")
	@Export("speed")
	public byte speed;

	MoveSpeed(byte var3) {
		this.speed = var3;
	}

	@ObfuscatedName("o")
	@ObfuscatedSignature(descriptor = "(I)I", garbageValue = "1790946346")
	@Export("rsOrdinal")
	public int rsOrdinal() {
		return this.speed;
	}

	@ObfuscatedName("w")
	@ObfuscatedSignature(descriptor = "(IB)Z", garbageValue = "68")
	public static boolean method3904(int var0) {
		return var0 >= 0 && var0 < 112 ? KeyHandler.KeyHandler_pressedKeys[var0] : false;
	}
}