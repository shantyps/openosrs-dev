import net.runelite.mapping.Export;
import net.runelite.mapping.Implements;
import net.runelite.mapping.ObfuscatedName;

@ObfuscatedName("fe")
@Implements("PlayerEquipmentItem")
public class PlayerEquipmentItem {
	@ObfuscatedName("p")
	@Export("recolorTo")
	public short[] recolorTo;
	@ObfuscatedName("m")
	@Export("retextureTo")
	public short[] retextureTo;

	PlayerEquipmentItem(int var1) {
		ItemComposition var2 = UserComparator6.ItemDefinition_get(var1); // L: 12
		if (var2.method3560()) { // L: 13
			this.recolorTo = new short[var2.recolorTo.length]; // L: 14
			System.arraycopy(var2.recolorTo, 0, this.recolorTo, 0, this.recolorTo.length); // L: 15
		}

		if (var2.method3572()) { // L: 17
			this.retextureTo = new short[var2.retextureTo.length]; // L: 18
			System.arraycopy(var2.retextureTo, 0, this.retextureTo, 0, this.retextureTo.length); // L: 19
		}

	} // L: 21
}
