package com.ModDamage.Routines;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ModDamage.ModDamage;
import com.ModDamage.PluginConfiguration.OutputPreset;
import com.ModDamage.EventInfo.EventData;
import com.ModDamage.EventInfo.EventInfo;
import com.ModDamage.Routines.Nested.EntityItemAction;
import com.ModDamage.Routines.Nested.Message;

abstract public class Routine
{
	protected static final Pattern anyPattern = Pattern.compile(".*");
	//private static final RoutineBuilder builder = new RoutineBuilder();
	private static final Map<Pattern, RoutineBuilder> registeredBaseRoutines = new LinkedHashMap<Pattern, RoutineBuilder>();
	
	private final String configString;
	protected Routine(String configString){ this.configString = configString; }
	public final String getConfigString(){ return configString; }
	abstract public void run(final EventData data);
	
	public static void registerVanillaRoutines()
	{
		registeredBaseRoutines.clear();
		AliasedRoutine.register();
		Tag.registerRoutine();
		PlayEffectRoutine.register();
		Message.registerRoutine();
		EntityItemAction.registerRoutine();
		ValueChangeRoutine.register();
	}
	
	protected static void registerRoutine(Pattern pattern, RoutineBuilder builder)
	{
		registeredBaseRoutines.put(pattern, builder);
	}
	
	public static Routine getNew(String string, EventInfo info)
	{
		for(Entry<Pattern, RoutineBuilder> entry : registeredBaseRoutines.entrySet())
		{
			Matcher anotherMatcher = entry.getKey().matcher(string);
			if(anotherMatcher.matches())
			{
				Routine routine = entry.getValue().getNew(anotherMatcher, info);
				if(routine != null)
					return routine;
			}
		}
		ModDamage.addToLogRecord(OutputPreset.FAILURE, " No match found for base routine \"" + string + "\"");
		return null;
	}
	
	protected abstract static class RoutineBuilder
	{
		public abstract Routine getNew(Matcher matcher, EventInfo info);
	}
}