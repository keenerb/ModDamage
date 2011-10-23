package com.KoryuObihiro.bukkit.ModDamage.RoutineObjects;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;

import com.KoryuObihiro.bukkit.ModDamage.ModDamage;
import com.KoryuObihiro.bukkit.ModDamage.ModDamage.DebugSetting;
import com.KoryuObihiro.bukkit.ModDamage.ModDamage.LoadState;
import com.KoryuObihiro.bukkit.ModDamage.Backend.TargetEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.Backend.Aliasing.RoutineAliaser;
import com.KoryuObihiro.bukkit.ModDamage.Backend.Matching.DynamicInteger;
import com.KoryuObihiro.bukkit.ModDamage.Backend.Matching.DynamicString;

public class Delay extends NestedRoutine
{	
	protected final DynamicInteger delay;
	protected final List<Routine> routines;
	protected static final Pattern delayPattern = Pattern.compile("delay\\." + DynamicString.dynamicPart, Pattern.CASE_INSENSITIVE);
	public Delay(String configString, DynamicInteger delayValue, List<Routine> routines)
	{
		super(configString);
		this.delay = delayValue;
		this.routines = routines;
	}
	@Override
	public void run(TargetEventInfo eventInfo)
	{ 
		Bukkit.getScheduler().scheduleAsyncDelayedTask(Bukkit.getPluginManager().getPlugin("ModDamage"), new DelayedRunnable(eventInfo.clone()), delay.getValue(eventInfo));
	}
		
	public static void register()
	{
		NestedRoutine.registerNested(Delay.class, delayPattern);
	}
	
	public static Delay getNew(String string, Object nestedContent)
	{
		if(string != null && nestedContent != null)
		{
			Matcher matcher = delayPattern.matcher(string);
			if(matcher.matches())
			{
				ModDamage.addToLogRecord(DebugSetting.CONSOLE, "", LoadState.SUCCESS);
				ModDamage.addToLogRecord(DebugSetting.NORMAL, "Delay: \"" + matcher.group() + "\"", LoadState.SUCCESS);

				ModDamage.indentation++;
				LoadState[] stateMachine = { LoadState.SUCCESS };
				List<Routine> routines = RoutineAliaser.parse(nestedContent, stateMachine);
				ModDamage.indentation--;
				if(!stateMachine[0].equals(LoadState.FAILURE))
				{
					DynamicInteger numberMatch = DynamicInteger.getNew(matcher.group(1));
					if(numberMatch != null)
					{
						ModDamage.addToLogRecord(DebugSetting.VERBOSE, "End Delay \"" + matcher.group() + "\"\n", LoadState.SUCCESS);
						return new Delay(matcher.group(), numberMatch, routines);
					}
					else ModDamage.addToLogRecord(DebugSetting.QUIET, "Invalid Delay \"" + matcher.group() + "\"", LoadState.FAILURE);
				}
			}
		}
		return null;
	}
	
	private class DelayedRunnable implements Runnable
	{
		private final TargetEventInfo eventInfo;
		private DelayedRunnable(TargetEventInfo eventInfo)
		{
			this.eventInfo = eventInfo;
		}
		
		@Override
		public void run()//Runnable
		{
			for(Routine routine : routines)
				routine.run(eventInfo);
		}
	}
}