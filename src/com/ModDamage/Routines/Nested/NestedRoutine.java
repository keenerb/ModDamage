package com.ModDamage.Routines.Nested;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ModDamage.ModDamage;
import com.ModDamage.PluginConfiguration.OutputPreset;
import com.ModDamage.EventInfo.EventInfo;
import com.ModDamage.Routines.Routine;
import com.ModDamage.Routines.Tag;
import com.ModDamage.Routines.Nested.Parameterized.Delay;
import com.ModDamage.Routines.Nested.Parameterized.Knockback;

public abstract class NestedRoutine extends Routine
{
	private static LinkedHashMap<Pattern, RoutineBuilder> registeredNestedRoutines = new LinkedHashMap<Pattern, RoutineBuilder>();

	protected NestedRoutine(String configString){ super(configString); }
	
	public static void registerVanillaRoutines()
	{
		registeredNestedRoutines.clear();
		ConditionalRoutine.register();
		Tag.registerNested();
		Delay.register();
		Message.registerNested();
		Knockback.register();
		SwitchRoutine.register();
		EntitySpawn.register();
		EntityItemAction.registerNested();
		EntityHurt.register();
		EntityUnknownHurt.register();
		EntityHeal.register();
		EntityExplode.register();
		Nearby.register();
		ChangeProperty.register();
		McMMOChangeSkill.register();
	}
	
	protected static void registerRoutine(Pattern pattern, RoutineBuilder builder)
	{
		registeredNestedRoutines.put(pattern, builder);
	}
	
	public static NestedRoutine getNew(String string, Object nestedContent, EventInfo info)
	{
		for(Entry<Pattern, RoutineBuilder> entry : registeredNestedRoutines.entrySet())
		{
			Matcher matcher = entry.getKey().matcher(string);
			if(matcher.matches())
			{
				NestedRoutine routine = entry.getValue().getNew(matcher, nestedContent, info);
				if (routine != null)
					return routine;
			}
		}
		ModDamage.addToLogRecord(OutputPreset.FAILURE, " No match found for nested routine \"" + string + "\"");		
		return null;
	}
	
	protected static abstract class RoutineBuilder
	{
		public abstract NestedRoutine getNew(Matcher anyMatcher, Object nestedContent, EventInfo info);
	}
	
	public static void paddedLogRecord(OutputPreset preset, String message)
	{		
		ModDamage.addToLogRecord(OutputPreset.CONSOLE_ONLY, "");
		ModDamage.addToLogRecord(preset, message);
		ModDamage.addToLogRecord(OutputPreset.CONSOLE_ONLY, "");
	}
}