package com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.CalculatedEffect;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.LivingEntity;

import com.KoryuObihiro.bukkit.ModDamage.ModDamage;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Routine;

public class EntityExplode extends EntityCalculatedEffectRoutine
{
	public EntityExplode(boolean forAttacker, List<Routine> routines){ super(forAttacker, routines);}
	
	@Override
	protected void applyEffect(LivingEntity affectedObject, int input) 
	{
		affectedObject.getWorld().createExplosion(affectedObject.getLocation(), input);
	}
	
	public static void register(ModDamage routineUtility)
	{
		ModDamage.registerEffect(EntityExplode.class, Pattern.compile(ModDamage.entityRegex + "effect\\.explode", Pattern.CASE_INSENSITIVE));
	}
	
	public static EntityExplode getNew(Matcher matcher, List<Routine> routines)
	{
		if(matcher != null && routines != null)
			return new EntityExplode(matcher.group(1).equalsIgnoreCase("attacker"), routines);
		return null;
	}
}
