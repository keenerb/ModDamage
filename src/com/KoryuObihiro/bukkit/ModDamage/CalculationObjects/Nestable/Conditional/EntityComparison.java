package com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.Nestable.Conditional;

import java.util.List;

import com.KoryuObihiro.bukkit.ModDamage.Backend.DamageEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.Backend.SpawnEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.ComparisonType;
import com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.ModDamageCalculation;

abstract public class EntityComparison extends EntityConditionalCalculation<Integer>
{
	final protected ComparisonType comparisonType;
	EntityComparison(boolean inverted, boolean forAttacker, int value, ComparisonType comparisonType, List<ModDamageCalculation> calculations)
	{ 
		super(inverted, forAttacker, value, calculations);
		this.comparisonType = comparisonType;
	}

	@Override
	protected boolean condition(DamageEventInfo eventInfo){ return ComparisonType.compare(comparisonType, getRelevantInfo(eventInfo), value);}
	@Override
	protected boolean condition(SpawnEventInfo eventInfo){ return ComparisonType.compare(comparisonType, getRelevantInfo(eventInfo), value);}
}
