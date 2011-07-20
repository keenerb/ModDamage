package com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.CalculatedEffect;

import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.RoutineUtility;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Routine;

public class PlayerSetItem extends PlayerCalculatedEffectRoutine
{
	protected final Material material;
	public PlayerSetItem(boolean forAttacker, Material material, List<Routine> routines)
	{
		super(forAttacker, routines);
		this.material = material;
	}
	
	@Override
	protected void applyEffect(Player affectedObject, int input) 
	{
		affectedObject.setItemInHand(new ItemStack(material, input));
	}
	
	public static void register(RoutineUtility routineUtility)
	{
		routineUtility.registerBase(PlayerSetItem.class, Pattern.compile(RoutineUtility.entityPart + "effect\\.addItem" + RoutineUtility.materialRegex + "\\.([0-9]+)", Pattern.CASE_INSENSITIVE));
	}
}
