package com.KoryuObihiro.bukkit.ModDamage.Backend.Aliasing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import com.KoryuObihiro.bukkit.ModDamage.ModDamage;
import com.KoryuObihiro.bukkit.ModDamage.PluginConfiguration.LoadState;
import com.KoryuObihiro.bukkit.ModDamage.PluginConfiguration.OutputPreset;
import com.KoryuObihiro.bukkit.ModDamage.Backend.ModDamageElement;

public class TypeNameAliaser extends Aliaser<ModDamageElement, List<String>> 
{
	protected HashMap<ModDamageElement, List<String>> thisMap = new HashMap<ModDamageElement, List<String>>();
	
	private static TypeNameAliaser staticInstance = new TypeNameAliaser();
	public static TypeNameAliaser getStaticInstance(){ return staticInstance;}
	
	private static final Random random = new Random();

	TypeNameAliaser()
	{
		super(AliasManager.TypeName.name());
		for(ModDamageElement element : ModDamageElement.values())
			thisMap.put(element, new ArrayList<String>());
	}
	
	@Override
	public void load(LinkedHashMap<String, Object> rawAliases)
	{
		this.loadState = LoadState.NOT_LOADED;
		
		boolean failFlag = false;
		ModDamageElement element = null;
		for(Entry<String, Object> entry : rawAliases.entrySet())
		{
			this.loadState = LoadState.SUCCESS;
			element = ModDamageElement.matchElement(entry.getKey());
			if(element != null)
			{
				if(entry.getValue() instanceof String)
				{
					String name = (String)entry.getValue();
					thisMap.get(element).add(name);
					ModDamage.addToLogRecord(OutputPreset.INFO_VERBOSE, "Aliasing type " + element.name() +  " as \"" + name + "\"");
					continue;
				}
				else if(entry.getValue() instanceof List)
				{
					ModDamage.addToLogRecord(OutputPreset.INFO, "Aliases multiple strings for type " + element.name());
					ModDamage.changeIndentation(true);
					for(Object object : (List<?>)entry.getValue())
						if(object instanceof String)
						{
							String name = (String)entry.getValue();
							thisMap.get(element).add(name);
							ModDamage.addToLogRecord(OutputPreset.INFO_VERBOSE, "Adding item \"" + name + "\"");
						}
					ModDamage.changeIndentation(false);
				}
				failFlag = true;
				ModDamage.addToLogRecord(OutputPreset.FAILURE, "Invalid content in alias for type " + element.name() + ": " + entry.getValue().toString());
			}
			else ModDamage.addToLogRecord(OutputPreset.WARNING_STRONG, "Unknown type \"" + entry.getKey() + "\"");//Only a warning because some users may preempt updated mob types
		}
		if(failFlag) loadState = LoadState.FAILURE;
	}

	@Override
	public void clear()
	{
		for(List<String> names : thisMap.values())
			names.clear();
	}
	
	public String toString(ModDamageElement element)
	{
		List<String> names = thisMap.get(element);
		return names.isEmpty()?names.get(random.nextInt(names.size())):element.name();
	}
	
	@Deprecated
	public List<String> matchAlias(String string){ return null;}
	@Deprecated
	public boolean completeAlias(String key, Object nestedContent){ return false;}
	@Deprecated
	protected ModDamageElement matchNonAlias(String key){ return null;}
	@Deprecated
	protected String getObjectName(ModDamageElement object){ return null;}
	@Deprecated
	protected List<String> getNewStorageClass(ModDamageElement value){ return null;}
}