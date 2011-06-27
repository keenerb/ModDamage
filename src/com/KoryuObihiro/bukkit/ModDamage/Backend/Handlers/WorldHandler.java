package com.KoryuObihiro.bukkit.ModDamage.Backend.Handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.KoryuObihiro.bukkit.ModDamage.ModDamage;
import com.KoryuObihiro.bukkit.ModDamage.Backend.ArmorSet;
import com.KoryuObihiro.bukkit.ModDamage.Backend.DamageElement;
import com.KoryuObihiro.bukkit.ModDamage.Backend.DamageEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.Backend.SpawnEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.DamageCalculation;
import com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.DamageCalculationAllocator;
import com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.SpawnCalculation;
import com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.SpawnCalculationAllocator;
import com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.Spawning.Set;
import com.KoryuObihiro.bukkit.ModDamage.CalculationObjects.Spawning.Conditional.ConditionalSpawnCalculation;



public class WorldHandler extends Handler
{
//// MEMBERS ////
	private World world;
	
	private boolean groupsLoaded = false;
	private boolean scanLoaded = false;
	private boolean mobHealthLoaded = false;
	private boolean isLoaded = false;
	//private List<String> configStrings = new ArrayList<String>();
	//private int configPages = 0;
	
	//nodes for config loading
	final private DamageCalculationAllocator damageCalc;
	final private SpawnCalculationAllocator healthCalc;
	final private ConfigurationNode offensiveNode;
	final private ConfigurationNode defensiveNode;
	final private ConfigurationNode mobHealthNode;
	final private ConfigurationNode scanNode;
	private List<String> configStrings = new ArrayList<String>();
	private int configPages = 0;
	
	//O/D routines
	final private HashMap<DamageElement, List<DamageCalculation>> offensiveRoutines = new HashMap<DamageElement, List<DamageCalculation>>();
	final private HashMap<DamageElement, List<DamageCalculation>> defensiveRoutines = new HashMap<DamageElement, List<DamageCalculation>>();
	final private HashMap<Material, List<DamageCalculation>> meleeOffensiveRoutines = new HashMap<Material, List<DamageCalculation>>();
	final private HashMap<Material, List<DamageCalculation>> meleeDefensiveRoutines = new HashMap<Material, List<DamageCalculation>>();
	final private HashMap<String, List<DamageCalculation>> armorOffensiveRoutines = new HashMap<String, List<DamageCalculation>>();
	final private HashMap<String, List<DamageCalculation>> armorDefensiveRoutines = new HashMap<String, List<DamageCalculation>>();
	//other MD config
	final private HashMap<DamageElement, List<ConditionalSpawnCalculation>> mobSpawnRoutines = new HashMap<DamageElement, List<ConditionalSpawnCalculation>>();
	
	//Handlers
	public final HashMap<String, GroupHandler> groupHandlers = new HashMap<String, GroupHandler>();
	
	
//// CONSTRUCTOR ////
	public WorldHandler(ModDamage plugin, World world, ConfigurationNode offensiveNode, ConfigurationNode defensiveNode, ConfigurationNode mobHealthNode, ConfigurationNode scanNode, DamageCalculationAllocator damageCalc, SpawnCalculationAllocator healthCalc) 
	{
		this.world = world;
		this.plugin = plugin;
		this.log = ModDamage.log;
		this.offensiveNode = offensiveNode;
		this.defensiveNode = defensiveNode;
		this.mobHealthNode = mobHealthNode;
		this.scanNode = scanNode;
		this.damageCalc = damageCalc;
		this.healthCalc = healthCalc;
		
		reload(true);
	}

//// CONFIG LOADING ////
	public void reload(boolean printToConsole)
	{ 
		//clear everything first
		clear();
		
		//load Offensive configuration
		routinesLoaded = loadDamageRoutines();

		//load MobHealth configuration
		mobHealthLoaded = loadMobHealth();

		//load Scan item configuration
		scanLoaded = loadScanItems();

		if(loadedSomething() && ModDamage.consoleDebugging_normal) 
			log.info("[" + plugin.getDescription().getName() + "] Global configuration for world \"" 
				+ world.getName() + "\" initialized!");
		else if(ModDamage.consoleDebugging_verbose)
			log.warning("[" + plugin.getDescription().getName() + "] Global configuration for world \"" 
				+ world.getName() + "\" could not load.");
		
		isLoaded = (routinesLoaded || mobHealthLoaded || scanLoaded || groupsLoaded);
	}
	
	@Override
	protected boolean loadGroupRoutines(boolean isOffensive)
	{
		boolean loadedSomething = false;
		//get all of the groups in configuration
		List<String> groups = new ArrayList<String>();
		{
			groups.addAll((offensiveNode != null && offensiveNode.getKeys("groups") != null)?offensiveNode.getKeys("groups"):new ArrayList<String>());
			groups.addAll((defensiveNode != null && defensiveNode.getKeys("groups") != null)?defensiveNode.getKeys("groups"):new ArrayList<String>());
			groups.addAll((scanNode != null && scanNode.getKeys("groups") != null)?scanNode.getKeys("groups"):new ArrayList<String>());
		}
		//load groups with offensive and defensive settings first
		if(!groups.isEmpty())
		{
			for(String group : groups)
			{	
				if(groupHandlers.containsKey(group))
				{
					if(ModDamage.consoleDebugging_normal)
						log.warning("Repetitive group definition found for group \"" + group + "\" found - ignoring.");
				}
				else 
				{
					GroupHandler groupHandler = new GroupHandler(plugin, this, log, group,
							((offensiveNode != null && offensiveNode.getNode("groups") != null)?offensiveNode.getNode("groups").getNode(group):null),
							((defensiveNode != null && defensiveNode.getNode("groups") != null)?defensiveNode.getNode("groups").getNode(group):null), 
							((scanNode != null && scanNode.getNode("groups") != null)?scanNode.getNode("groups"):null), 
							damageCalc);
							
					if(groupHandler.loadedSomething())
					{
						groupHandlers.put(group, groupHandler);
						loadedSomething = true;
					}
				}
			}
		}
		return loadedSomething;
	}
///////////////////// MOBHEALTH
	public boolean loadMobHealth()
	{
		boolean loadedSomething = false;
		if(mobHealthNode != null) 
		{
			if(ModDamage.consoleDebugging_verbose) log.info("{Found MobHealth node for world \"" + world.getName() + "\"}");
			List<DamageElement> creatureTypes = new ArrayList<DamageElement>();
			creatureTypes.addAll(DamageElement.getElementsOf("animal"));
			creatureTypes.addAll(DamageElement.getElementsOf("mob"));
			//load Mob health settings
			for(DamageElement creatureType : creatureTypes)
			{
			//check the node property for a default spawn calculation
				List<Object> calcStrings = mobHealthNode.getList(creatureType.getReference());
				//So, when a list of calculations are called, they're just ArrayList<Object>
				// Normal calcStrings are just strings,
				// conditionals are represented with a LinkedHashMap.
				if(calcStrings != null)
				{
					List<SpawnCalculation> calculations = healthCalc.parseList(calcStrings);
					if(!calculations.isEmpty())
					{
						if(!mobSpawnRoutines.containsKey(creatureType))
						{
							//mobSpawnDefaults.put(creatureType, calculation);
							String configString = "-MobHealth:" + world.getName() + ":" + creatureType.getReference() 
								+ " [" + calcStrings.toString() + "]";
							configStrings.add(configString);
							if(ModDamage.consoleDebugging_normal) log.info(configString);
							loadedSomething = true;
						}
						else if(ModDamage.consoleDebugging_normal) log.warning("Repetitive " + creatureType.getReference() 
								+ " definition - ignoring");
					}
					else  log.severe("Invalid command string \"" + calcStrings.toString() + "\" in MobHealth " + creatureType.getReference() 
							+ " definition - refer to config for proper calculation node");
					
				}
				else if(ModDamage.consoleDebugging_verbose)
					log.warning("No instructions found for " + creatureType.getReference() + " - is this on purpose?");
			}
		}
		return loadedSomething;
	}
	
	public boolean doSpawnCalculations(SpawnEventInfo eventInfo)
	{
		//determine creature type
		if(eventInfo.damageElement != null && mobSpawnRoutines.containsKey(eventInfo.damageElement))
		{
			for(SpawnCalculation calculation : mobSpawnRoutines.get(eventInfo.damageElement))
				calculation.calculate(eventInfo);
			return true;
		}
		return false;
	}	

///////////////////// SCAN
	private boolean loadScanItems() 
	{
		boolean loadedSomething = false;
		if(scanNode != null) 
		{
			if(ModDamage.consoleDebugging_verbose) log.info("{Found global Scan node for world \"" + world.getName() + "\"}");
			List<String> itemList = scanNode.getStringList("global", null);
			if(!itemList.equals(null))
			{
				for(String itemString : itemList)
				{
					if(ModDamage.itemAliases.containsKey(itemString.toLowerCase()))
						for(Material material : ModDamage.itemAliases.get(itemString.toLowerCase()))
						{
							scanItems.add(material);
							String configString = "-Scan:" + world.getName() + ":" + material.name() + "(" + material.getId() + ")";
							configStrings.add(configString);
							if(ModDamage.consoleDebugging_normal) log.info(configString);
						}
					else
					{
						Material material = Material.matchMaterial(itemString);
						if(material != null)
						{
							scanItems.add(material);
							String configString = "-Scan:" + world.getName() + ":" + material.name() + "(" + material.getId() + ") ";
							configStrings.add(configString);
							if(ModDamage.consoleDebugging_normal) log.info(configString);
							loadedSomething = true;
						}
						else if(ModDamage.consoleDebugging_verbose) log.warning("Invalid Scan item \"" + itemString + "\" found in world \"" 
							+ world.getName() + "\" globals - ignoring");
					}
				}
			}
		}
		return loadedSomething;
	}

	public boolean canScan(Player player)
	{ 
		boolean groupCanScan = false;
		for(String group : ModDamage.Permissions.getGroups(player.getWorld().getName(), player.getName()))
			if(canScan(player.getItemInHand().getType(), group))
			{
				groupCanScan = true;
				break;
			}
		return (scanLoaded && groupCanScan);
	}
	protected boolean canScan(Material itemType, String groupName)
	{ 
		if(groupName == null) groupName = "";
		return ((scanLoaded && scanItems.contains(itemType) 
				|| ((groupHandlers.get(groupName) != null)
						?groupHandlers.get(groupName).canScan(itemType)
						:false)));
	}

//// DAMAGE HANDLING ////
	public void doDamageCalculations(DamageEventInfo eventInfo) 
	{
		switch(eventInfo.eventType)
		{
///////////////////// Player vs. Player 
			case PLAYER_PLAYER:
				runRoutines(eventInfo, true);//attack buff
				runEquipmentRoutines(eventInfo, true);
				
				runRoutines(eventInfo, false);//defense buff
				runEquipmentRoutines(eventInfo, false);
				
			//calculate group buff
				try
				{
					for(String group_attacker : eventInfo.groups_attacker)
						if(groupHandlers.containsKey(group_attacker))
							for(String group_target : eventInfo.groups_target)
							{
								groupHandlers.get(group_attacker).doAttackCalculations(eventInfo);//attack buff
								groupHandlers.get(group_target).doDefenseCalculations(eventInfo);//defense buff		
							}
				}
				catch(Exception e)
				{
					if(eventInfo.groups_target == null)
						log.warning("[" + plugin.getDescription().getName() + "] No groups found for player \"" 
								+ eventInfo.name_target + "\" in world \"" + world.getName() + "\" - add this player to a group in Permissions!");
					else if(eventInfo.groups_attacker == null)
						log.warning("[" + plugin.getDescription().getName() + "] No groups found for player \"" 
								+ eventInfo.name_attacker + "\" in world \"" + world.getName() + "\" - add this player to a group in Permissions!");
				}
				
			return;

///////////////////// Player vs. Mob
			case PLAYER_MOB:
				runRoutines(eventInfo, true);//attack buff
				runEquipmentRoutines(eventInfo, true);
				
				runRoutines(eventInfo, false);//defense buff
			
			//group buff
				try
				{
					for(String group_attacking : eventInfo.groups_attacker)
						if(groupHandlers.containsKey(group_attacking))
							groupHandlers.get(group_attacking).doAttackCalculations(eventInfo);
				}
				catch(Exception e)
				{
					log.warning("[" + plugin.getDescription().getName() + "] No groups found for player \"" 
							+ eventInfo.name_attacker + "\" in world \"" + world.getName() + "\" - add this player to a group in Permissions!");
				}
			return;
				
///////////////////// Mob vs. Player
			case MOB_PLAYER:
				runRoutines(eventInfo, true);//attack buff
				
				runRoutines(eventInfo, false);//defense buff
				runEquipmentRoutines(eventInfo, false);
				
			//calculate group buff
				try
				{
					String[] groups_target = ModDamage.Permissions.getGroups(world.getName(), ((Player)eventInfo.entity_target).getName());
					for(String group_target : groups_target)
						if(groupHandlers.containsKey(group_target))
							groupHandlers.get(group_target).doDefenseCalculations(eventInfo);
				}
				catch(Exception e)
				{
					log.warning("[" + plugin.getDescription().getName() + "] No groups found for player \"" 
							+ eventInfo.name_target + "\" in world \"" + world.getName() + "\" - add this player to a group in Permissions!");
				}
			return;
			

///////////////////// Mob vs. Mob
			case MOB_MOB:
				runRoutines(eventInfo, true);//attack buff

				runRoutines(eventInfo, false);//defense buff
			return;
	
///////////////////// Nonliving vs. Player
			case NONLIVING_PLAYER:
				runRoutines(eventInfo, true);//attack buff
				
				runRoutines(eventInfo, false);//defense buff
				runEquipmentRoutines(eventInfo, false);
				
			//calculate group buff
				try
				{
					for(String group_target : eventInfo.groups_target)
						if(groupHandlers.containsKey(group_target))
							groupHandlers.get(group_target).doDefenseCalculations(eventInfo);
				}
				catch(Exception e)
				{
					log.warning("[" + plugin.getDescription().getName() + "] No groups found for player \"" 
						+ eventInfo.name_target + "\" in world \"" + world.getName() + "\" - add this player to a group in Permissions!");
				}
			return;

///////////////////// Nonliving vs. Mob
			case NONLIVING_MOB:
				runRoutines(eventInfo, true);//attack buff
				runRoutines(eventInfo, false);//defense buff
			return;
			
			default: return;
		}
	}
	
////HELPER FUNCTIONS////
	public World getWorld(){ return world;}
	
	@Override
	public void clear()
	{
		super.clear();
		groupHandlers.clear();
	}

	public boolean loadedSomething(){ return isLoaded;}

//// COMMAND FUNCTIONS ////
	public boolean sendWorldConfig(Player player, int pageNumber)
	{

		//reloadConfig();
		//now send config information to the player (or console)
		if(player == null)
		{
			String printString = "Config for world \"" + world.getName() + "\":";
			for(String configString : configStrings)
				printString += "\n" + configString;
			log.info(printString);
			for(Handler groupHandler : groupHandlers.values())
			{
				groupHandler.sendGroupConfig(player, pageNumber);
			}
			return true;
		}
		else if(configPages > 0 && configPages >= pageNumber && pageNumber > 0)
		{
			player.sendMessage(plugin.ModDamageString(ChatColor.GOLD) + " World \"" + world.getName().toUpperCase() 
					+ "\" (" + pageNumber + "/" + configPages + ")");
			for(int i = (9 * (pageNumber - 1)); i < (configStrings.size() < (9 * pageNumber)
														?configStrings.size()
														:(9 * pageNumber)); i++)
				player.sendMessage(ChatColor.DARK_AQUA + configStrings.get(i));
			return true;
		}
		return false;
	}

	/* FIXME NAO
	public boolean reloadConfig()
	{
		//Get config information dynamically
		List<String> configStrings = new ArrayList<String>();
		configPages = 0;
		if(loadedSomething())
		{
			for(DamageElement damageElement : offensiveRoutines.keySet())
			{
				configStrings.add(ChatColor.DARK_AQUA + "-Offensive:" + world.getName() + ":" + (damageElement.hasSubConfiguration()?"generic":"") + );
			}
			if(groupsLoaded) 
			{
				configStrings.add(ChatColor.DARK_PURPLE + "Groups loaded:"); //TODO Customize colors later
				for(GroupHandler groupHandler : groupHandlers.values())
					configStrings.add(ChatColor.DARK_PURPLE + groupHandler.getGroupName());
			}
			configPages = configStrings.size()/9 + ((configStrings.size()%9 > 0)?1:0);
		}
		else
		{
			
		}
		

		List<String> configStrings = new ArrayList<String>();
		configPages = configStrings.size()/9 + ((configStrings.size()%9 > 0)?1:0);
	}
	
	
	public boolean add(String[] args, List<String> calcStrings)
	{
		if(args[2] == "global")
		{
			
		}
		else if(args[2] == "groups")
		{
			GroupHandler groupHandler = groupHandlers.get(plugin.getGroupMatch(world, args[3], false));
			if(groupHandler != null)
				groupHandler.add(args, calcStrings);
		}
		return false;
	}
	
	public boolean remove(String[] args)
	{
		if(args[2] == "global")
		{
			
		}
		else if(args[2] == "groups")
		{
			GroupHandler groupHandler = groupHandlers.get(plugin.getGroupMatch(world, args[3], false));
			if(groupHandler != null)
				groupHandler.remove(args);
		}
		return false;
	}
	*/
	
	
	//TODO (mebbe)
	//  Implement aliases?! :D
}

	