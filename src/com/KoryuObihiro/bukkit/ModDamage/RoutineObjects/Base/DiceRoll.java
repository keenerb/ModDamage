package com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Base;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.KoryuObihiro.bukkit.ModDamage.Backend.TargetEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.Backend.Matching.DynamicInteger;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Routine;

public class DiceRoll extends Chanceroutine 
{
	protected final DynamicInteger rollValue;
	protected final boolean isAdditive;
	protected DiceRoll(String configString)
	{
		super(configString);
		this.rollValue = null;
		this.isAdditive = false;
	}
	protected DiceRoll(String configString, DynamicInteger rollValue) 
	{
		super(configString);
		this.rollValue = rollValue;
		this.isAdditive = true;
	}

	@Override
	public void run(TargetEventInfo eventInfo)
	{
		if(isAdditive)
			eventInfo.eventValue = Math.abs(random.nextInt()%(eventInfo.eventValue + 1));
		else eventInfo.eventValue += Math.abs(random.nextInt()%(rollValue.getValue(eventInfo) + 1));
	}
	
	public static void register()
	{
		Routine.registerBase(DiceRoll.class, Pattern.compile("roll(\\." + DynamicInteger.dynamicPart + ")?", Pattern.CASE_INSENSITIVE));
	}
	
	public static DiceRoll getNew(Matcher matcher)
	{ 
		if(matcher != null)
		{
			if(!matcher.group(1).equalsIgnoreCase(""))
			{
				DynamicInteger match1 = DynamicInteger.getNew(matcher.group(2));
				if(match1 != null)
					return new DiceRoll(matcher.group(), match1);
			}
			else return new DiceRoll(matcher.group());
		}
		return null;
	}
}