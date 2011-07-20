package com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional;

import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.World;
import org.bukkit.World.Environment;

import com.KoryuObihiro.bukkit.ModDamage.Backend.DamageEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.Backend.SpawnEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.ConditionalRoutine;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.RoutineUtility;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Routine;

public class WorldEnvironment extends WorldConditionalStatement 
{
	protected final Environment environment;
	public WorldEnvironment(boolean inverted, World world, Environment environment, List<Routine> routines)
	{
		super(inverted, world);
		this.environment = environment;
	}
	public WorldEnvironment(boolean inverted, Environment environment)
	{
		super(inverted);
		this.environment = environment;
	}
	@Override
	public boolean condition(DamageEventInfo eventInfo){ return (useEventWorld?eventInfo.world:world).getEnvironment().equals(environment);}
	@Override
	public boolean condition(SpawnEventInfo eventInfo){ return (useEventWorld?eventInfo.world:world).getEnvironment().equals(environment);}
	
	public static void register(RoutineUtility routineUtility)
	{
		ConditionalRoutine.registerStatement(routineUtility, WorldEnvironment.class, Pattern.compile("world\\.environment\\." + RoutineUtility.environmentRegex, Pattern.CASE_INSENSITIVE));
	}
}
