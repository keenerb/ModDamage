package com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.Damage.Conditional.World;

import java.util.List;

import com.KoryuObihiro.bukkit.ModDamage.Backend.DamageEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.DamageCalculation;

public class WorldTime extends WorldConditionalDamageCalculation 
{
	private boolean checkInverse;
	private long beginningTime;
	private long endTime;
	public WorldTime(boolean inverted, int beginningTime, int endTime, List<DamageCalculation> calculations)
	{
		this.inverted = inverted;
		this.calculations = calculations;
		this.beginningTime = beginningTime;
		this.endTime = endTime;
		checkInverse = beginningTime > endTime;
	}
	@Override
	public boolean condition(DamageEventInfo eventInfo)
	{
		return(checkInverse
				?(eventInfo.world.getTime() > beginningTime && eventInfo.world.getTime() < endTime)
				:(!(eventInfo.world.getTime() > beginningTime) || !(eventInfo.world.getTime() < endTime)));
	}
}
