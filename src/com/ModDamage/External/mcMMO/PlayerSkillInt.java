package com.ModDamage.External.mcMMO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.ModDamage.ModDamage;
import com.ModDamage.PluginConfiguration.OutputPreset;
import com.ModDamage.StringMatcher;
import com.ModDamage.Utils;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.ExternalPluginManager;
import com.ModDamage.EventInfo.DataRef;
import com.ModDamage.EventInfo.EventData;
import com.ModDamage.EventInfo.EventInfo;
import com.ModDamage.Expressions.IntegerExp;
import com.ModDamage.Matchables.EntityType;
import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.SkillType;

public class PlayerSkillInt extends IntegerExp
{
	public static void register()
	{
		IntegerExp.register(
				Pattern.compile("([a-z]+)_SKILL(|"+Utils.joinBy("|", SkillProperty.values())+")_(\\w+)", Pattern.CASE_INSENSITIVE),
				new DynamicIntegerBuilder()
				{
					@Override
					public IntegerExp getNewFromFront(Matcher matcher, StringMatcher sm, EventInfo info)
					{
						String name = matcher.group(1).toLowerCase();
						DataRef<Entity> entityRef = info.get(Entity.class, name);
						DataRef<EntityType> entityElementRef = info.get(EntityType.class, name);
						if (entityRef == null || entityElementRef == null) return null;
						String skillProp = matcher.group(2).toUpperCase();
						String skillType = matcher.group(3).toUpperCase();
						
						if (skillProp == "") skillProp = "LEVEL";
						
						try
						{
							return sm.acceptIf(new PlayerSkillInt(
									entityRef, entityElementRef,
									SkillProperty.valueOf(skillProp),
									SkillType.valueOf(skillType)));
						}
						catch (IllegalArgumentException e) {
							// SkillType.valueOf failed to find a match
							ModDamage.addToLogRecord(OutputPreset.FAILURE, "Unknown skill type \""+skillType+"\", valid values are: "+Utils.joinBy(", ", SkillType.values()));
						}
						catch (NoClassDefFoundError e) {
							if (ExternalPluginManager.getMcMMOPlugin() == null)
								ModDamage.addToLogRecord(OutputPreset.FAILURE, "You need mcMMO to use the skill variables.");
							else
								ModDamage.addToLogRecord(OutputPreset.FAILURE, "McMMO has changed. Please notify the ModDamage developers.");
						}
						return null;
					}
				});
	}
	
	enum SkillProperty
	{
		LEVEL {
				@Override
				int getProperty(Player player, SkillType skillType)
				{
					return ExperienceAPI.getLevel(player, skillType);
				}
			},
		XP {
				@Override
				int getProperty(Player player, SkillType skillType)
				{
					return ExperienceAPI.getXP(player, skillType);
				}
			},
		XPNEEDED {
				@Override
				int getProperty(Player player, SkillType skillType)
				{
					return ExperienceAPI.getXPToNextLevel(player, skillType);
				}
			};
		
		abstract int getProperty(Player player, SkillType skillType);
	}

	protected final DataRef<Entity> entityRef;
	protected final DataRef<EntityType> entityElementRef;
	protected final SkillProperty skillProperty;
	protected final SkillType skillType;
	
	PlayerSkillInt(DataRef<Entity> entityRef, DataRef<EntityType> entityElementRef, SkillProperty skillProperty, SkillType skillType)
	{
		this.entityRef = entityRef;
		this.entityElementRef = entityElementRef;
		this.skillProperty = skillProperty;
		this.skillType = skillType;
	}
	
	@Override
	protected int myGetValue(EventData data) throws BailException
	{
		if(!entityElementRef.get(data).matches(EntityType.PLAYER))
			return 0;
	
		try
		{
			return skillProperty.getProperty((Player)entityRef.get(data), skillType);
		}
		catch (Exception e)
		{
			ModDamage.addToLogRecord(OutputPreset.FAILURE, "mcMMO threw an exception: "+e);
			return 0;
		}
	}
	
	@Override
	public String toString()
	{
		return entityRef + "_skill"+skillProperty+"_" + skillType.name().toLowerCase();
	}
}
