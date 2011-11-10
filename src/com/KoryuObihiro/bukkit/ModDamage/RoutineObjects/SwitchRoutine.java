package com.KoryuObihiro.bukkit.ModDamage.RoutineObjects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Entity;

import com.KoryuObihiro.bukkit.ModDamage.ModDamage;
import com.KoryuObihiro.bukkit.ModDamage.ModDamage.DebugSetting;
import com.KoryuObihiro.bukkit.ModDamage.ModDamage.LoadState;
import com.KoryuObihiro.bukkit.ModDamage.Backend.EntityReference;
import com.KoryuObihiro.bukkit.ModDamage.Backend.ModDamageElement;
import com.KoryuObihiro.bukkit.ModDamage.Backend.TargetEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.Backend.Aliasing.RoutineAliaser;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Permissions.PlayerGroupSwitch;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Switch.ArmorSetSwitch;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Switch.BiomeSwitch;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Switch.ConditionSwitch;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Switch.EntityTypeSwitch;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Switch.EnvironmentSwitch;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Switch.WieldSwitch;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Switch.WorldSwitch;

public abstract class SwitchRoutine<EventInfoClass, CaseInfoClass> extends NestedRoutine 
{
	private static HashMap<Pattern, SwitchBuilder> registeredSwitchRoutines = new HashMap<Pattern, SwitchBuilder>();
	
	protected final LinkedHashMap<CaseInfoClass, List<Routine>> switchStatements;
	public final boolean isLoaded;
	public final List<String> failedCases = new ArrayList<String>();
	
	//TODO Definitely not as efficient as it could be. Refactor?
	protected SwitchRoutine(String configString, LinkedHashMap<String, Object> rawSwitchStatements)
	{
		super(configString);
		this.switchStatements = new LinkedHashMap<CaseInfoClass, List<Routine>>();
		boolean caseFailed = false;
		for(String switchCase : rawSwitchStatements.keySet())
		{
			//get the case first, see if it refers to anything valid
			CaseInfoClass matchedCase = matchCase(switchCase);
			
			ModDamage.addToLogRecord(DebugSetting.CONSOLE, "", LoadState.SUCCESS);
			ModDamage.addToLogRecord(DebugSetting.NORMAL, " case: \"" + switchCase + "\"", LoadState.SUCCESS);

			//then grab the routines
			ModDamage.indentation++;
			LoadState[] stateMachine = { LoadState.SUCCESS };
			List<Routine> routines = RoutineAliaser.parse(rawSwitchStatements.get(switchCase), stateMachine);
			ModDamage.indentation--;
			switchStatements.put(matchedCase, stateMachine[0].equals(LoadState.FAILURE)?null:routines);
			
			if(stateMachine[0].equals(LoadState.SUCCESS))
				ModDamage.addToLogRecord(DebugSetting.VERBOSE, " End case \"" + switchCase + "\"", LoadState.SUCCESS);
			else ModDamage.addToLogRecord(DebugSetting.NORMAL, " Invalid content in case \"" + switchCase + "\"", LoadState.SUCCESS);
			ModDamage.addToLogRecord(DebugSetting.CONSOLE, "", LoadState.SUCCESS);
			//Check if the case is valid
			if(!caseIsSane(matchedCase))
				caseFailed = true;
		}
		isLoaded = !caseFailed;
	}
	
	protected boolean caseIsSane(CaseInfoClass someCase){ return someCase != null && switchStatements.get(someCase) != null;}
	
	@Override
	public void run(TargetEventInfo eventInfo) 
	{
		EventInfoClass info = getRelevantInfo(eventInfo);
		if(info != null)
			for(CaseInfoClass infoKey : switchStatements.keySet())
				if(compare(info, infoKey))
				{
					for(Routine routine : switchStatements.get(infoKey))
						routine.run(eventInfo);
					break;
				}
	}
	
	protected boolean compare(EventInfoClass info_event, CaseInfoClass info_case){ return info_event.equals(info_case);}
	
	abstract protected EventInfoClass getRelevantInfo(TargetEventInfo eventInfo);

	abstract protected CaseInfoClass matchCase(String switchCase);
	
	public static void register()
	{
		registeredSwitchRoutines.clear();
		NestedRoutine.registerRoutine(Pattern.compile("switch\\.(.*)", Pattern.CASE_INSENSITIVE), new RoutineBuilder());
		registeredSwitchRoutines.clear();
		ArmorSetSwitch.register();
		BiomeSwitch.register();
		ConditionSwitch.register();
		EntityTypeSwitch.register();
		EnvironmentSwitch.register();
		PlayerGroupSwitch.register();
		WieldSwitch.register();
		WorldSwitch.register();
	}

	protected static void registerSwitch(Pattern syntax, SwitchBuilder builder)
	{
		Routine.registerRoutine(registeredSwitchRoutines, syntax, builder);
	}
	
	protected final static class RoutineBuilder extends NestedRoutine.RoutineBuilder
	{
		@Override
		public SwitchRoutine<?, ?> getNew(Matcher switchMatcher, Object nestedContent)
		{
			if(switchMatcher != null && nestedContent != null)
			{
				if(switchMatcher.matches())
				{
					ModDamage.addToLogRecord(DebugSetting.CONSOLE, "", LoadState.SUCCESS);
					ModDamage.addToLogRecord(DebugSetting.NORMAL, "Switch: \"" + switchMatcher.group() + "\"", LoadState.SUCCESS);
					for(Pattern pattern : registeredSwitchRoutines.keySet())
					{
						Matcher matcher = pattern.matcher(switchMatcher.group(1));
						if(matcher.matches())
						{
							@SuppressWarnings("unchecked")
							LinkedHashMap<String, Object> switchCases = (nestedContent instanceof LinkedHashMap?(LinkedHashMap<String, Object>)nestedContent:null);
							if(switchCases != null)
							{
								SwitchRoutine<?, ?> routine = registeredSwitchRoutines.get(pattern).getNew(matcher, switchCases);
								if(routine != null)
								{
									if(routine.isLoaded)
									{
										ModDamage.addToLogRecord(DebugSetting.VERBOSE, "End Switch \"" + switchMatcher.group() + "\"", LoadState.SUCCESS);
										ModDamage.addToLogRecord(DebugSetting.CONSOLE, "", LoadState.SUCCESS);
										return routine;
									}
									else 
										for(String caseName : routine.failedCases)
											ModDamage.addToLogRecord(DebugSetting.QUIET, "Error: invalid case \"" + caseName + "\"", LoadState.FAILURE);
								}
							}
							else
							{
								ModDamage.addToLogRecord(DebugSetting.QUIET, "Error: unexpected nested content " + nestedContent.toString() + " in Switch routine \"" + switchMatcher + "\"", LoadState.FAILURE);
								ModDamage.addToLogRecord(DebugSetting.CONSOLE, "", LoadState.SUCCESS);
							}
							break;
						}
					}
				}
				ModDamage.addToLogRecord(DebugSetting.QUIET, "Error: invalid Switch \"" + switchMatcher.group() + "\"" + (ModDamage.getDebugSetting().equals(DebugSetting.VERBOSE)?"\n":""), LoadState.FAILURE);
			}
			return null;
		}
	}
	
	abstract protected static class SwitchBuilder
	{
		abstract protected SwitchRoutine<?, ?> getNew(Matcher matcher, LinkedHashMap<String, Object> switchStatements);
	}
	
	abstract public static class SingleValueSwitchRoutine<Type> extends SwitchRoutine<Type, Collection<Type>>
	{

		protected SingleValueSwitchRoutine(String configString, LinkedHashMap<String, Object> rawSwitchStatements)
		{
			super(configString, rawSwitchStatements);
		}

		@Override
		protected boolean compare(Type info_event, Collection<Type> info_case)
		{
			return info_case.contains(info_event);
		}
		
		@Override
		protected boolean caseIsSane(Collection<Type> someCase){ return super.caseIsSane(someCase) && !someCase.isEmpty();}
	}
	
	abstract public static class EntitySingleTraitSwitchRoutine<InfoType> extends SingleValueSwitchRoutine<InfoType>
	{
		protected final ModDamageElement necessaryElement;
		protected final EntityReference entityReference;
		protected EntitySingleTraitSwitchRoutine(String configString, LinkedHashMap<String, Object> switchStatements, ModDamageElement necessaryElement, EntityReference entityReference) 
		{
			super(configString, switchStatements);
			this.necessaryElement = necessaryElement;
			this.entityReference = entityReference;
		}
		
		protected Entity getRelevantEntity(TargetEventInfo eventInfo)
		{
			if(entityReference.getElement(eventInfo).matchesType(necessaryElement))
				return entityReference.getEntity(eventInfo);
			return null;
		}
	}
	
	abstract public static class EntityMultipleTraitSwitchRoutine<InfoType> extends SwitchRoutine<Collection<InfoType>, Collection<InfoType>>
	{
		protected final ModDamageElement necessaryElement;
		protected final EntityReference entityReference;
		protected EntityMultipleTraitSwitchRoutine(String configString, LinkedHashMap<String, Object> switchStatements, ModDamageElement necessaryElement, EntityReference entityReference) 
		{
			super(configString, switchStatements);
			this.necessaryElement = necessaryElement;
			this.entityReference = entityReference;
		}
		
		protected Entity getRelevantEntity(TargetEventInfo eventInfo)
		{
			if(entityReference.getElement(eventInfo).matchesType(necessaryElement))
				return entityReference.getEntity(eventInfo);
			return null;
		}
		
		@Override
		protected boolean compare(Collection<InfoType> info_event, Collection<InfoType> info_case)
		{
			for(Object object : info_event)
				if(info_case.contains(object))
					return true;
			return false;
		}
		
		@Override
		protected boolean caseIsSane(Collection<InfoType> someCase){ return super.caseIsSane(someCase) && !someCase.isEmpty();}
	}
}
