package com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.KoryuObihiro.bukkit.ModDamage.ModDamage;
import com.KoryuObihiro.bukkit.ModDamage.ModDamage.DebugSetting;
import com.KoryuObihiro.bukkit.ModDamage.ModDamage.LoadState;
import com.KoryuObihiro.bukkit.ModDamage.Backend.TargetEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.Backend.IntegerMatching.IntegerMatch;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Routine;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Nested.ConditionalRoutine;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Nested.ConditionalStatement;

public class Comparison extends ConditionalStatement
{	
	protected final IntegerMatch operand1;
	protected final IntegerMatch operand2;
	protected final ComparisonType comparisonType;
	protected Comparison(boolean inverted, IntegerMatch operand1, IntegerMatch operand2, ComparisonType comparisonType)
	{
		super(inverted);
		this.operand1 = operand1;
		this.operand2 = operand2;
		this.comparisonType = comparisonType;
	}

	@Override
	protected boolean condition(TargetEventInfo eventInfo) { return comparisonType.compare(operand1.getValue(eventInfo), operand2.getValue(eventInfo));}

	private enum ComparisonType
	{ 
		EQUALS, NOTEQUALS, LESSTHAN, LESSTHANEQUALS, GREATERTHAN, GREATERTHANEQUALS;
	
		public static ComparisonType match(String key)
		{
			for(ComparisonType type : ComparisonType.values())
				if(key.equalsIgnoreCase(type.name()))
					return type;
			ModDamage.addToLogRecord(DebugSetting.QUIET, "Invalid comparison \"" + key + "\"", LoadState.FAILURE);
			return null;
		}
		public boolean compare(int operand1, int operand2)
		{
			switch(this)
			{
				case EQUALS:			return operand1 == operand2;
				case NOTEQUALS:			return operand1 != operand2;
				case LESSTHAN:			return operand1 < operand2;
				case LESSTHANEQUALS:	return operand1 <= operand2;
				case GREATERTHAN:		return operand1 > operand2;
				case GREATERTHANEQUALS:	return operand1 >= operand2;
				default:				return false;
			}
		}
	}

	protected static String comparisonPart;
	static
	{
		comparisonPart = "(";
		for(ComparisonType comparisonType : ComparisonType.values())
			comparisonPart += comparisonType.name() + "|";
		comparisonPart = comparisonPart.substring(0, comparisonPart.length() - 1) + ")";
	}
	public static void register()
	{
		ConditionalRoutine.registerConditionalStatement(Comparison.class, Pattern.compile("(!?)(?:" + Routine.dynamicIntegerPart + "\\." + comparisonPart + "\\." + Routine.dynamicIntegerPart + ")?", Pattern.CASE_INSENSITIVE));
	}
	
	public static Comparison getNew(Matcher matcher)
	{
		if(matcher != null)
		{
			IntegerMatch match1 = IntegerMatch.getNew(matcher.group(2)), match2 = IntegerMatch.getNew(matcher.group(4));
			ComparisonType comparisonType = ComparisonType.match(matcher.group(3));
			if(comparisonType != null && match1 != null && match2 != null)
				return new Comparison(matcher.group(1).equalsIgnoreCase("!"), match1, match2, comparisonType);
		}
		return null;
	}
}