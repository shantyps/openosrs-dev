import net.runelite.mapping.Export;
import net.runelite.mapping.Implements;
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;

@ObfuscatedName("gj")
@Implements("MoveSpeed")
public enum MoveSpeed implements MouseWheel {
	@ObfuscatedName("c")
	@ObfuscatedSignature(
		descriptor = "Lgj;"
	)
	field2119((byte)-1),
	@ObfuscatedName("b")
	@ObfuscatedSignature(
		descriptor = "Lgj;"
	)
	field2120((byte)0),
	@ObfuscatedName("p")
	@ObfuscatedSignature(
		descriptor = "Lgj;"
	)
	field2123((byte)1),
	@ObfuscatedName("m")
	@ObfuscatedSignature(
		descriptor = "Lgj;"
	)
	field2122((byte)2);

	@ObfuscatedName("ft")
	@Export("worldHost")
	static String worldHost;
	@ObfuscatedName("t")
	@Export("speed")
	public byte speed;

	MoveSpeed(byte var3) {
		this.speed = var3;
	}

	@ObfuscatedName("b")
	@ObfuscatedSignature(
		descriptor = "(B)I",
		garbageValue = "48"
	)
	@Export("rsOrdinal")
	public int rsOrdinal() {
		return this.speed;
	}

	@ObfuscatedName("w")
	@ObfuscatedSignature(
		descriptor = "(ZI)V",
		garbageValue = "964558054"
	)
	static void method3731(boolean var0) {
		byte var1 = 0;
		if (!AttackOption.method2356()) {
			var1 = 12;
		} else if (class295.client.method1154()) {
			var1 = 10;
		}

		PlayerType.method5521(var1);
		if (var0) {
			Login.Login_username = "";
			Login.Login_password = "";
			class148.field1651 = 0;
			BufferedSource.otp = "";
		}

		HealthBar.method2311();
		WorldMapID.method4805();
	}
}
