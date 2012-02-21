package com.ModDamage.Routines;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ModDamage.ModDamage;
import com.ModDamage.PluginConfiguration.OutputPreset;
import com.ModDamage.Backend.IntRef;
import com.ModDamage.Backend.Matching.DynamicInteger;
import com.ModDamage.EventInfo.DataRef;
import com.ModDamage.EventInfo.EventData;
import com.ModDamage.EventInfo.EventInfo;

public class Multiplication extends ValueChangeRoutine 
{
	public Multiplication(String configString, DataRef<IntRef> defaultRef, ValueChangeType changeType, DynamicInteger value)
	{ 
		super(configString, defaultRef, changeType, value);
	}
	@Override
	public int getValue(EventData data){ return defaultRef.get(data).value * number.getValue(data); }
	
	public static void register()
	{
		ValueChangeRoutine.registerRoutine(Pattern.compile("(?:(?:mult(?:iply)?)\\.|\\*)(.+)", Pattern.CASE_INSENSITIVE), new RoutineBuilder());
	}

	protected static class RoutineBuilder extends ValueChangeRoutine.ValueBuilder
	{
		@Override
		public Multiplication getNew(Matcher matcher, ValueChangeType changeType, EventInfo info)
		{ 
			DynamicInteger match = DynamicInteger.getNew(matcher.group(1), info);
			DataRef<IntRef> defaultRef = info.get(IntRef.class, "-default");
			if(match != null && defaultRef != null)
			{
				ModDamage.addToLogRecord(OutputPreset.INFO, "Multiply" + changeType.getStringAppend() + ": " + matcher.group(1));
				return new Multiplication(matcher.group(), defaultRef, changeType, match);
			}
			return null;
		}
	}
}