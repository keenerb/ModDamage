package com.ModDamage.Routines;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ModDamage.ModDamage;
import com.ModDamage.PluginConfiguration.OutputPreset;
import com.ModDamage.Backend.Matching.DynamicInteger;
import com.ModDamage.EventInfo.DataRef;
import com.ModDamage.EventInfo.EventData;
import com.ModDamage.EventInfo.EventInfo;

public class ValueChangeRoutine extends Routine 
{
	private static final LinkedHashMap<Pattern, ValueBuilder> builders = new LinkedHashMap<Pattern, ValueBuilder>();
	protected enum ValueChangeType
	{
		Add
		{
			@Override
			int changeValue(int current, int value){ return current + value; }
		},
		Set
		{
			@Override
			int changeValue(int current, int value){ return value; }
		},
		Subtract
		{
			@Override
			int changeValue(int current, int value){ return current - value; }
		};
		abstract int changeValue(int current, int value);

		public String getStringAppend(){ return " (" + this.name().toLowerCase() + ")"; }

	}
	
	private final ValueChangeType changeType;
	protected final DynamicInteger number;
	protected final DataRef<Integer> defaultRef;
	protected ValueChangeRoutine(String configString, DataRef<Integer> defaultRef, ValueChangeType changeType, DynamicInteger number)
	{
		super(configString);
		this.defaultRef = defaultRef;
		this.changeType = changeType;
		this.number = number;
	}
	
	@Override
	public final void run(final EventData data){
		defaultRef.set(data, changeType.changeValue(
				defaultRef.get(data), getValue(data)));
	}
	
	protected int getValue(EventData data){ return number.getValue(data); }
	
	public static void register()
	{
		Routine.registerRoutine(Pattern.compile("(\\+|\\-|set\\.|add\\.|)(.+)", Pattern.CASE_INSENSITIVE), new RoutineBuilder());
		
		DiceRoll.register();
		Division.register();
		IntervalRange.register();
		LiteralRange.register();
		Multiplication.register();
	}
	
	private static final class RoutineBuilder extends Routine.RoutineBuilder
	{
		@Override
		public ValueChangeRoutine getNew(final Matcher matcher, EventInfo info)
		{
			ValueChangeType changeType = null;
			if(matcher.group(1).equalsIgnoreCase("-"))
				changeType = ValueChangeType.Subtract;
			else if(matcher.group(1).equalsIgnoreCase("+") || matcher.group(1).equalsIgnoreCase("add."))
				changeType = ValueChangeType.Add;
			else if(matcher.group(1).equalsIgnoreCase("") || matcher.group(1).equalsIgnoreCase("set."))
				changeType = ValueChangeType.Set;
			assert(changeType != null);
			
			for(Entry<Pattern, ValueBuilder> entry : builders.entrySet())
			{
				Matcher anotherMatcher = entry.getKey().matcher(matcher.group(2));
				if(anotherMatcher.matches())
					return entry.getValue().getNew(anotherMatcher, changeType, info);
			}
			DynamicInteger integer = DynamicInteger.getNew(matcher.group(2), info, false);
			DataRef<Integer> defaultRef = info.get(Integer.class, "-default");
			if(integer != null && defaultRef != null)
			{
				ModDamage.addToLogRecord(OutputPreset.INFO, changeType.name() + ": " + matcher.group(2));
				return new ValueChangeRoutine(matcher.group(), defaultRef, changeType, integer);
			}
			return null;
		}
	}
	
	public static void registerRoutine(Pattern pattern, ValueBuilder builder){ builders.put(pattern, builder); }
	public static abstract class ValueBuilder
	{
		public abstract ValueChangeRoutine getNew(final Matcher matcher, final ValueChangeType changeType, EventInfo info);
	}
}
