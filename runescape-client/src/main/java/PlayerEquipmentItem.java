import net.runelite.mapping.Export;
import net.runelite.mapping.Implements;
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;

@ObfuscatedName("ff")
@Implements("PlayerEquipmentItem")
public class PlayerEquipmentItem {
	@ObfuscatedName("h")
	@Export("recolorTo")
	public short[] recolorTo;
	@ObfuscatedName("g")
	@Export("retextureTo")
	public short[] retextureTo;

	public PlayerEquipmentItem(int var1) {
		ItemComposition var2 = class67.ItemDefinition_get(var1); // L: 12
		if (var2.method3763()) { // L: 13
			this.recolorTo = new short[var2.recolorTo.length]; // L: 14
			System.arraycopy(var2.recolorTo, 0, this.recolorTo, 0, this.recolorTo.length); // L: 15
		}

		if (var2.method3711()) { // L: 17
			this.retextureTo = new short[var2.retextureTo.length]; // L: 18
			System.arraycopy(var2.retextureTo, 0, this.retextureTo, 0, this.retextureTo.length); // L: 19
		}

	} // L: 21

	@ObfuscatedName("ih")
	@ObfuscatedSignature(
		descriptor = "(I)I",
		garbageValue = "-1915861434"
	)
	static final int method3324() {
		return Client.menuOptionsCount - 1; // L: 9232
	}
}
