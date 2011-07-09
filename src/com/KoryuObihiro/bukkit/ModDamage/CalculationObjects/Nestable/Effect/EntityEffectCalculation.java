	package com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.Nestable.Effect;

import java.util.List;

import org.bukkit.entity.LivingEntity;

import com.KoryuObihiro.bukkit.ModDamage.Backend.DamageEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.Backend.SpawnEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.ModDamageCalculation;

abstract public class EntityEffectCalculation<InputType> extends EffectCalculation<LivingEntity, InputType>
{
	protected final boolean forAttacker;
	public EntityEffectCalculation(boolean forAttacker, InputType value)
	{
		super(value);
		this.forAttacker = forAttacker;
	}
	public EntityEffectCalculation(boolean forAttacker, List<ModDamageCalculation> calculations)
	{
		super(calculations);
		this.forAttacker = forAttacker;
	}

	@Override
	protected LivingEntity getAffectedObject(DamageEventInfo eventInfo){ return (forAttacker?eventInfo.entity_attacker:eventInfo.entity_target);}
	@Override
	protected LivingEntity getAffectedObject(SpawnEventInfo eventInfo){ return eventInfo.entity;}
	
}
