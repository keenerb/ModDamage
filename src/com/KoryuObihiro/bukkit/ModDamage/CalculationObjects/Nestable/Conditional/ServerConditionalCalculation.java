package com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.Nestable.Conditional;

import java.util.List;

import org.bukkit.Server;

import com.KoryuObihiro.bukkit.ModDamage.ModDamage;
import com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.ModDamageCalculation;


public abstract class ServerConditionalCalculation<T> extends ConditionalCalculation
{
	final T value;
	public ServerConditionalCalculation(boolean inverted, T value, List<ModDamageCalculation> calculations) 
	{
		super(inverted, calculations);
		this.value = value;
	}

	Server server = ModDamage.server;
}
