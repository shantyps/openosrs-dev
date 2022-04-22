import net.runelite.mapping.Export;
import net.runelite.mapping.Implements;
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;

@ObfuscatedName("gm")
@Implements("MoveSpeed")
public enum MoveSpeed implements MouseWheel {
	@ObfuscatedName("v")
	@ObfuscatedSignature(
		descriptor = "Lgm;"
	)
	field2186((byte)-1),
	@ObfuscatedName("c")
	@ObfuscatedSignature(
		descriptor = "Lgm;"
	)
	field2184((byte)0),
	@ObfuscatedName("i")
	@ObfuscatedSignature(
		descriptor = "Lgm;"
	)
	field2185((byte)1),
	@ObfuscatedName("f")
	@ObfuscatedSignature(
		descriptor = "Lgm;"
	)
	field2187((byte)2);

	@ObfuscatedName("ah")
	protected static String field2189;
	@ObfuscatedName("b")
	@Export("speed")
	public byte speed;

	MoveSpeed(byte var3) {
		this.speed = var3; // L: 14
	} // L: 15

	@ObfuscatedName("v")
	@ObfuscatedSignature(
		descriptor = "(B)I",
		garbageValue = "16"
	)
	@Export("rsOrdinal")
	public int rsOrdinal() {
		return this.speed; // L: 18
	}

	@ObfuscatedName("c")
	@ObfuscatedSignature(
		descriptor = "(Lpi;I)Ljava/lang/String;",
		garbageValue = "562183385"
	)
	public static String method3883(Buffer var0) {
		return WorldMapIcon_1.method3933(var0, 32767); // L: 60
	}

	@ObfuscatedName("l")
	@ObfuscatedSignature(
		descriptor = "(I)V",
		garbageValue = "1188814406"
	)
	public static void method3884() {
		KitDefinition.KitDefinition_cached.clear(); // L: 130
	} // L: 131
}
