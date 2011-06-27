package com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.Damage;


import com.KoryuObihiro.bukkit.ModDamage.Backend.DamageEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.DamageCalculation;

public class DivisionAddition extends DamageCalculation 
{
	private int divideValue;
	public DivisionAddition(int value){ divideValue = (value != 0?value:1);}
	@Override
	public void calculate(DamageEventInfo eventInfo){ eventInfo.eventDamage += eventInfo.eventDamage/divideValue;}
}