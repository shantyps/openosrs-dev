import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;
import net.runelite.mapping.Implements;
import net.runelite.mapping.Export;
@ObfuscatedName("fy")
@Implements("PlayerEquipmentItem")
public class PlayerEquipmentItem {
	@ObfuscatedName("f")
	@Export("recolorTo")
	public short[] recolorTo;

	@ObfuscatedName("u")
	@Export("retextureTo")
	public short[] retextureTo;

	public PlayerEquipmentItem(int var1) {
		ItemComposition var2 = class258.ItemDefinition_get(var1);
		if (var2.method3716()) {
			this.recolorTo = new short[var2.recolorTo.length];
			System.arraycopy(var2.recolorTo, 0, this.recolorTo, 0, this.recolorTo.length);
		}
		if (var2.method3731()) {
			this.retextureTo = new short[var2.retextureTo.length];
			System.arraycopy(var2.retextureTo, 0, this.retextureTo, 0, this.retextureTo.length);
		}
	}

	@ObfuscatedName("u")
	@ObfuscatedSignature(descriptor = "(CLlj;B)I", garbageValue = "37")
	@Export("lowercaseChar")
	static int lowercaseChar(char var0, Language var1) {
		int var2 = var0 << 4;
		if (Character.isUpperCase(var0) || Character.isTitleCase(var0)) {
			var0 = Character.toLowerCase(var0);
			var2 = (var0 << 4) + 1;
		}
		if (var0 == 241 && var1 == Language.Language_ES) {
			var2 = 1762;
		}
		return var2;
	}
}