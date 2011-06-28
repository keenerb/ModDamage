package com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.Damage.Conditional;


import java.util.List;

import com.KoryuObihiro.bukkit.ModDamage.Backend.DamageEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.DamageCalculation;

public class DamageLessThanEquals extends ConditionalDamageCalculation
{
	final int value;
	public DamageLessThanEquals(boolean inverted, int value, List<DamageCalculation> calculations)
	{ 
		this.inverted = inverted;
		this.value = value;
		this.calculations = calculations;
	}
	@Override
	protected boolean condition(DamageEventInfo eventInfo){ return eventInfo.eventDamage <= value;}
}
