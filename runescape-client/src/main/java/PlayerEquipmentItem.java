import net.runelite.mapping.Export;
import net.runelite.mapping.Implements;
import net.runelite.mapping.ObfuscatedName;

@ObfuscatedName("fy")
@Implements("PlayerEquipmentItem")
public class PlayerEquipmentItem {
	@ObfuscatedName("q")
	@Export("recolorTo")
	public short[] recolorTo;
	@ObfuscatedName("f")
	@Export("retextureTo")
	public short[] retextureTo;

	PlayerEquipmentItem(int var1) {
		ItemComposition var2 = EnumComposition.ItemDefinition_get(var1); // L: 12
		if (var2.method3737()) { // L: 13
			this.recolorTo = new short[var2.recolorTo.length]; // L: 14
			System.arraycopy(var2.recolorTo, 0, this.recolorTo, 0, this.recolorTo.length); // L: 15
		}

		if (var2.method3728()) { // L: 17
			this.retextureTo = new short[var2.retextureTo.length]; // L: 18
			System.arraycopy(var2.retextureTo, 0, this.retextureTo, 0, this.retextureTo.length); // L: 19
		}

	} // L: 21
}
