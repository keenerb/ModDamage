package com.ModDamage.External.mcMMO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import com.ModDamage.ModDamage;
import com.ModDamage.PluginConfiguration.OutputPreset;
import com.ModDamage.StringMatcher;
import com.ModDamage.Utils;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.ExternalPluginManager;
import com.ModDamage.EventInfo.DataProvider;
import com.ModDamage.EventInfo.EventData;
import com.ModDamage.EventInfo.EventInfo;
import com.ModDamage.EventInfo.IDataProvider;
import com.ModDamage.Expressions.IntegerExp;
import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.SkillType;

public class PlayerSkillInt extends IntegerExp<Player>
{
	public static void register()
	{
		DataProvider.register(Integer.class, Player.class, 
				Pattern.compile("_SKILL(|"+Utils.joinBy("|", SkillProperty.values())+")_(\\w+)", Pattern.CASE_INSENSITIVE),
				new IDataParser<Integer>()
				{
					@Override
					public IDataProvider<Integer> parse(EventInfo info, IDataProvider<?> playerDP, Matcher m, StringMatcher sm)
					{
						String skillProp = m.group(1).toUpperCase();
						String skillType = m.group(2).toUpperCase();
						
						if (skillProp == "") skillProp = "LEVEL";
						
						try
						{
							return sm.acceptIf(new PlayerSkillInt(
									playerDP,
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

	protected final SkillProperty skillProperty;
	protected final SkillType skillType;
	
	PlayerSkillInt(IDataProvider<?> playerDP, SkillProperty skillProperty, SkillType skillType)
	{
		super(Player.class, playerDP);
		this.skillProperty = skillProperty;
		this.skillType = skillType;
	}
	
	@Override
	public Integer myGet(Player player, EventData data) throws BailException
	{
		try
		{
			return skillProperty.getProperty(player, skillType);
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
		return startDP + "_skill"+skillProperty+"_" + skillType.name().toLowerCase();
	}
}