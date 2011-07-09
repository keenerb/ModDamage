package com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.Nestable.Conditional;

import java.util.List;

import org.bukkit.Material;

import com.KoryuObihiro.bukkit.ModDamage.Backend.DamageEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.Backend.SpawnEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.ModDamageCalculation;

public class EntityWielding extends EntityConditionalCalculation<Material> 
{
	public EntityWielding(boolean inverted, boolean forAttacker, Material material, List<ModDamageCalculation> calculations)
	{  
		super(inverted, forAttacker, material, calculations);
	}
	@Override
	public Material getRelevantInfo(DamageEventInfo eventInfo){ return (forAttacker?eventInfo.materialInHand_attacker:eventInfo.materialInHand_target);}
	@Override
	public Material getRelevantInfo(SpawnEventInfo eventInfo){ return null;}
	
	
}
