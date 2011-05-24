package com.KoryuObihiro.bukkit.ModDamage.Handling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;
import com.KoryuObihiro.bukkit.ModDamage.ModDamage;



public class WorldHandler
{
//// MEMBERS ////
	public ModDamage plugin;
	public Logger log; 
	private World world;
	
	public boolean globalsLoaded = false;
	public boolean groupsLoaded = false;
	public boolean mobHealthLoaded = false;
	public boolean scanLoaded = false;
	
	final private DamageCalculator damageCalc;
	final private HealthCalculator healthCalc;
	final private ConfigurationNode offensiveNode;
	final private ConfigurationNode defensiveNode;
	final private ConfigurationNode mobHealthNode;
	final private ConfigurationNode scanNode;
	
	//generic and 
	final private HashMap<DamageElement, List<String>> offensiveRoutines = new HashMap<DamageElement, List<String>>();
	final private HashMap<DamageElement, List<String>> defensiveRoutines = new HashMap<DamageElement, List<String>>();
	final private HashMap<DamageElement, String> mobHealthSettings = new HashMap<DamageElement, String>();
	
	//specific-item HashMap
	final private HashMap<Material, List<String>> itemOffensiveRoutines = new HashMap<Material, List<String>>();
	final private HashMap<Material, List<String>> itemDefensiveRoutines = new HashMap<Material, List<String>>();
	
	final private HashMap<ArmorSet, List<String>> armorOffensiveRoutines = new HashMap<ArmorSet, List<String>>();
	final private HashMap<ArmorSet, List<String>> armorDefensiveRoutines = new HashMap<ArmorSet, List<String>>();
	
	//Handlers
	final private HashMap<String, GroupHandler> groupHandlers = new HashMap<String, GroupHandler>();
	
	//Scan items
	final private List<Material> globalScanItems = new ArrayList<Material>();
	
//// CONSTRUCTOR ////
	public WorldHandler(ModDamage plugin, World world, ConfigurationNode offensiveNode, ConfigurationNode defensiveNode, ConfigurationNode mobHealthNode, ConfigurationNode scanNode, DamageCalculator damageCalc, HealthCalculator healthCalc) 
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
		
		reload();
	}

	public boolean reload()
	{ 
		//load Offensive configuration
		globalsLoaded = loadGlobalRoutines();
		if(globalsLoaded && ModDamage.consoleDebugging_normal) 
			log.info("[" + plugin.getDescription().getName() + "] Global configuration for world \"" 
				+ world.getName() + "\" initialized!");
		else if(ModDamage.consoleDebugging_verbose)
			log.warning("[" + plugin.getDescription().getName() + "] Global configuration for world \"" 
				+ world.getName() + "\" could not load.");
		
		//load Defensive configuration
		groupsLoaded = loadGroupHandlers(true);
		//load Scan item configuration
		scanLoaded = loadScanItems();
		if(groupsLoaded && ModDamage.consoleDebugging_normal) 
			if(!groupHandlers.isEmpty()) 
				log.info("[" + plugin.getDescription().getName() + "] Group configuration(s) for world \""
				+ world.getName() + "\" initialized!");
		else if(ModDamage.consoleDebugging_verbose) 
			log.warning("[" + plugin.getDescription().getName() + "] Group configuration for world \""
				+ world.getName() + "\" could not load.");

		//load MobHealth configuration
		mobHealthLoaded = loadMobHealth();
		
		if(globalsLoaded || groupsLoaded || scanLoaded || mobHealthLoaded)
			return true;
		return false;
	}
	
	
//// CLASS-SPECIFIC FUNCTIONS ////

///////////////////// OFFENSIVE/DEFENSIVE ///////////////////////
	
// global
	public boolean loadGlobalRoutines()
	{
		String progressString = "UNKNOWN";
		try
		{
			//clear everything first
			clearRoutines();
			
			boolean loadedSomething = false;
			ConfigurationNode offensiveGlobalNode = (offensiveNode != null)?offensiveNode.getNode("global"):null;
			ConfigurationNode defensiveGlobalNode = (defensiveNode != null)?defensiveNode.getNode("global"):null;
	//load "global" node routines
			if(offensiveGlobalNode != null)
			{
				progressString = "generic damage types in Offensive";
				if(loadGenericRoutines(true))
					loadedSomething = true;
				else if(ModDamage.consoleDebugging_verbose)
					log.warning("Could not load " + progressString);
				
				progressString = "Offensive item routines";
				if(loadItemRoutines(true))
					loadedSomething = true;
				else if(ModDamage.consoleDebugging_verbose)
					log.warning("Could not load " + progressString);
			}
			if(defensiveGlobalNode != null)
			{
				progressString = "generic damage types in Defensive";
				if(loadGenericRoutines(false))
					loadedSomething = true;
				else if(ModDamage.consoleDebugging_verbose)
					log.warning("Could not load " + progressString);

				progressString = "Defensive item routines";
				if(loadItemRoutines(false))
					loadedSomething = true;
				else if(ModDamage.consoleDebugging_verbose)
					log.warning("Could not load " + progressString);
			}
			
			return loadedSomething;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.severe("[" + plugin.getDescription().getName() 
					+ "] Invalid global configuration for world \"" + world.getName()
					+ "\" - failed while loading " + progressString + ".");
			return false;
		}
	}

	public boolean loadGenericRoutines(boolean isOffensive)
	{
		List<String>damageCategories = DamageElement.getGenericTypeStrings();
		ConfigurationNode genericNode = (isOffensive?offensiveNode:defensiveNode).getNode("global").getNode("generic");
		for(String damageCategory : damageCategories)
		{
			if(genericNode != null)
			{
				List<String> calcStrings = genericNode.getStringList(damageCategory, null);
				DamageElement element = DamageElement.matchDamageElement(damageCategory);
				if(calcStrings != null)
				{
					if(ModDamage.consoleDebugging_verbose) log.info("{Found global generic " + (isOffensive?"Offensive":"Defensive") + " " 
							+ damageCategory + " node for world \"" + world.getName() + "\"}");
					if(!calcStrings.equals(null)) //!calcStrings.equals(null)
					{
						damageCalc.checkCommandStrings(calcStrings, damageCategory, isOffensive);
						if(calcStrings.size() > 0)
						{
							if(!(isOffensive?offensiveRoutines:defensiveRoutines).containsKey(element))
							{
								(isOffensive?offensiveRoutines:defensiveRoutines).put(element, calcStrings);
								if(ModDamage.consoleDebugging_normal) log.info("-" + world.getName() + ":" 
										+ (isOffensive?"Offensive":"Defensive") + ":Generic:" + damageCategory 
										+ calcStrings.toString());//debugging
							}
							else if(ModDamage.consoleDebugging_normal)
							{
								log.warning("Repetitive generic "  + damageCategory + " in " + (isOffensive?"Offensive":"Defensive") + " - ignoring");
								continue;
							}
						}
						else if(ModDamage.consoleDebugging_verbose)
							log.warning("No instructions found for generic " + damageCategory + " node - is this on purpose?");
					}
				}
				else if(ModDamage.consoleDebugging_verbose) log.info("Global generic " + element.getReference() 
						+ " node for" + (isOffensive?"Offensive":"Defensive") + " not found.");
			}
			ConfigurationNode relevantNode = ((isOffensive?offensiveNode:defensiveNode).getNode("global").getNode(damageCategory));
			if(relevantNode != null)
			{
				if(ModDamage.consoleDebugging_verbose) log.info("{Found global specific " + (isOffensive?"Offensive":"Defensive") + " " 
						+ damageCategory + " node for world \"" + world.getName() + "\"}");
				
				if(DamageElement.matchDamageElement(damageCategory).hasSubConfiguration())
					for(DamageElement damageElement : DamageElement.getElementsOf(damageCategory))
					{
						String elementReference = damageElement.getReference();
						//check for leaf-node buff strings
						List<String> calcStrings = relevantNode.getStringList(elementReference, null);
						if(!calcStrings.equals(null)) //!calcStrings.equals(null)
						{
							damageCalc.checkCommandStrings(calcStrings, elementReference, isOffensive);
							if(calcStrings.size() > 0)
							{
								if(!(isOffensive?offensiveRoutines:defensiveRoutines).containsKey(damageElement))
								{
									(isOffensive?offensiveRoutines:defensiveRoutines).put(damageElement, calcStrings);
									if(ModDamage.consoleDebugging_normal) log.info("-" + world.getName() + ":" 
											+ (isOffensive?"Offensive":"Defensive") + ":" + elementReference 
											+ calcStrings.toString());//debugging
								}
								else if(ModDamage.consoleDebugging_normal)
								{
									log.warning("Repetitive "  + elementReference + " in " + (isOffensive?"Offensive":"Defensive") + " - ignoring");
									continue;
								}
							}
							else if(ModDamage.consoleDebugging_verbose)
								log.warning("No instructions found for " + elementReference + " node - is this on purpose?");
						}
						else if(ModDamage.consoleDebugging_verbose) log.info("Global " + damageElement.getReference() 
								+ " node for" + (isOffensive?"Offensive":"Defensive") + " not found.");
					}
			}
		}
		return true;
	}

	public boolean loadItemRoutines(boolean isOffensive){ return loadItemRoutines(isOffensive, false);}
	public boolean loadItemRoutines(boolean isOffensive, boolean force)
	{
		ConfigurationNode itemNode = (isOffensive
											?offensiveNode.getNode("global").getNode("item")
											:defensiveNode.getNode("global").getNode("item"));
		if(itemNode != null)	
		{
			List<String> itemList = (isOffensive?offensiveNode:defensiveNode).getNode("global").getKeys("item");
			List<String> calcStrings = null;
			for(Material material : Material.values())	
			{
				if(itemList.contains(material.name())) //TODO Mess with casing here?
					calcStrings = itemNode.getStringList(material.name(), null);
				if(calcStrings != null)
				{
					damageCalc.checkCommandStrings(calcStrings, material.name(), isOffensive);
					if(calcStrings.size() > 0)
					{
						if(ModDamage.consoleDebugging_normal)
							log.info("-" + world.getName() + ":" + (isOffensive?"Offensive":"Defensive") + ":" 
								+ material.name() + "(" + material.getId() + ") "+ calcStrings.toString());//debugging
						if(!(isOffensive?itemOffensiveRoutines:itemDefensiveRoutines).containsKey(material))
							(isOffensive?itemOffensiveRoutines:itemDefensiveRoutines).put(material, calcStrings);
						else if(ModDamage.consoleDebugging_normal) log.warning("[" + plugin.getDescription().getName() + "] Repetitive " 
								+ material.name() + "(" + material.getId() + ") definition in " + (isOffensive?"Offensive":"Defensive") + " item globals - ignoring");
					}
					else if(ModDamage.consoleDebugging_verbose)
					{
						log.warning("No instructions found for global " + material.name() + "(" + material.getId()
							+ ") item node in " + (isOffensive?"Offensive":"Defensive") + " - is this on purpose?");
					}
					return true;
				}
			}
		}
		return false;
	}

	public boolean loadGroupHandlers(boolean force)
	{
		//get all of the groups in configuration
		List<String> groups = new ArrayList<String>();
		{
			groups.addAll((offensiveNode != null && offensiveNode.getKeys("groups") != null)?offensiveNode.getKeys("groups"):new ArrayList<String>());
			groups.addAll((defensiveNode != null && defensiveNode.getKeys("groups") != null)?defensiveNode.getKeys("groups"):new ArrayList<String>());
			groups.addAll((scanNode != null && scanNode.getKeys("groups") != null)?scanNode.getKeys("groups"):new ArrayList<String>());
		}
		//load groups with offensive and defensive settings first
		if(!groups.isEmpty())
			for(String group : groups)
			{	
				if(groupHandlers.containsKey(group) && force)
				{
					groupHandlers.remove(group);
					if(ModDamage.consoleDebugging_normal)
						ModDamage.log.info("{Forcing reload of group " + group + "...}");
				}
				if(!groupHandlers.containsKey(group))
					groupHandlers.put(group, new GroupHandler(this, group, 
									((offensiveNode != null && offensiveNode.getNode("groups") != null)?offensiveNode.getNode("groups").getNode(group):null),
									((defensiveNode != null && defensiveNode.getNode("groups") != null)?defensiveNode.getNode("groups").getNode(group):null), 
									((scanNode != null && scanNode.getNode("groups") != null)?scanNode.getNode("groups"):null), 
									damageCalc));
			}
		return true;
	}
	
///////////////////// MOBHEALTH ///////////////////////	
	public boolean loadMobHealth()
	{
		if(mobHealthNode != null) 
		{
			if(ModDamage.consoleDebugging_verbose) log.info("{Found MobHealth node for world \"" + world.getName() + "\"}");
			List<DamageElement> creatureTypes = new ArrayList<DamageElement>();
			creatureTypes.addAll(DamageElement.getElementsOf("animal"));
			creatureTypes.addAll(DamageElement.getElementsOf("mob"));
			//load Mob health settings
			for(DamageElement creatureType : creatureTypes)
			{
			//check for leaf-node health strings
				String calcString = (String) mobHealthNode.getProperty(creatureType.getReference());
				if(calcString != null)
				{
					if(!healthCalc.checkCommandString(calcString))
						log.severe("Invalid command string \"" + calcString + "\" in MobHealth " + creatureType.getReference() 
								+ " definition - refer to config for proper calculation node");
				//display debug message to acknowledge that the settings have been validated
					if(ModDamage.consoleDebugging_normal) log.info("-" + world.getName() + ":MobHealth:" + creatureType.getReference() 
							+ " [" + calcString.toString() + "]");
				//check that this type of mob hasn't already been loaded
					if(!mobHealthSettings.containsKey(creatureType))
						mobHealthSettings.put(creatureType, calcString);
					else if(ModDamage.consoleDebugging_normal) log.warning("Repetitive " + creatureType.getReference() 
							+ " definition - ignoring");
				}
				else if(ModDamage.consoleDebugging_verbose)
					log.warning("No instructions found for " + creatureType.getReference() + " - is this on purpose?");
			}
			return true;
		}
		return false;
	}
	
	public boolean setHealth(Entity entity)
	{
		//determine creature type
		DamageElement creatureType = DamageElement.matchEntityType(entity);
		if(creatureType != null)
		{
			if(mobHealthSettings.containsKey(creatureType))
				((LivingEntity)entity).setHealth(healthCalc.parseCommand(mobHealthSettings.get(creatureType)));
			return true;
		}
		return false;
	}	

///////////////////// SCAN ///////////////////////
	private boolean loadScanItems() 
	{
		if(scanNode != null) 
		{
			if(ModDamage.consoleDebugging_verbose) log.info("{Found global Scan node for world \"" + world.getName() + "\"}");
			List<String> itemList = scanNode.getStringList("global", null);
			if(!itemList.equals(null))
			{
				//search for keyword-defined sets of materials
				for(String keyword : plugin.scanKeywords.keySet())
					if(itemList.contains(keyword)) //TODO Mess with casing here?
						if(!globalScanItems.contains(keyword)) 
							for(Material material : plugin.scanKeywords.get(keyword))
							{
								globalScanItems.add(material);
								if(ModDamage.consoleDebugging_normal)
									ModDamage.log.info("-" + world.getName() + ":Scan:" 
										+ material.name() + "(" + material.getId() + ") ");
							}	
				//searching for normally-defined materials
				for(Material material : Material.values())	
					if(itemList.contains(material.name()) || itemList.contains(Integer.toString(material.getId()))) //TODO Mess with casing here?
						if(!globalScanItems.contains(material)) 
						{
							globalScanItems.add(material);
							if(ModDamage.consoleDebugging_normal)
								ModDamage.log.info("-" + world.getName() + ":Scan:" 
									+ material.name() + "(" + material.getId() + ") ");
						}
				return true;
			}
		}
		return false;
	}

	public boolean canScan(Player player)
	{ 
		return (scanLoaded && canScan(player.getItemInHand().getType(), 
				ModDamage.Permissions.getGroup(player.getName(), player.getWorld().getName())));
	}
	public boolean canScan(Material itemType, String groupName)
	{ 
		if(groupName == null) groupName = "";
		return ((scanLoaded && globalScanItems.contains(itemType) 
				|| ((groupHandlers.get(groupName) != null)
						?groupHandlers.get(groupName).canScan(itemType)
						:false)));
	}
	
///////////////////// DAMAGE HANDLING ///////////////////////
	//TODO Possibly handle the event here, instead of the main plugin body?
//Player-targeted damage
//PvP
	public int calcAttackBuff(Player player_target, Player player_attacking, int eventDamage)
	{
		if(globalsLoaded)
		{
			String group_target = ModDamage.Permissions.getGroup(player_target.getWorld().getName(), player_target.getName());
			String group_attacking = ModDamage.Permissions.getGroup(player_target.getWorld().getName(), player_target.getName());
			ArmorSet armorSet_attacking = new ArmorSet(player_attacking);
			//apply global buff settings
			return runGlobalRoutines(DamageElement.GENERIC_HUMAN, true, eventDamage)
				+ runArmorRoutines(armorSet_attacking, true, eventDamage)
				+ runGlobalRoutines(DamageElement.matchItemType(player_attacking.getItemInHand().getType()), true, eventDamage)
				+ runItemRoutines(player_attacking.getItemInHand().getType(), true, eventDamage)
			//apply group settings
				+ (groupHandlers.containsKey(group_attacking)
						?groupHandlers.get(group_attacking).calcAttackBuff(group_target, player_attacking.getItemInHand().getType(), armorSet_attacking, eventDamage)
						:0);
		}
		return 0;
	}
	public int calcDefenseBuff(Player player_target, Player player_attacking, int eventDamage)
	{		
		if(globalsLoaded)
		{
			//get group strings
			String group_target = ModDamage.Permissions.getGroup(player_target.getWorld().getName(), player_target.getName());
			String group_attacking = ModDamage.Permissions.getGroup(player_attacking.getWorld().getName(), player_attacking.getName());
			ArmorSet armorSet_target = new ArmorSet(player_target);
			//apply global buff settings
			return runGlobalRoutines(DamageElement.GENERIC_HUMAN, false, eventDamage)
					+ runArmorRoutines(armorSet_target, false, eventDamage)
					+ runGlobalRoutines(DamageElement.matchItemType(player_attacking.getItemInHand().getType()), false, eventDamage)
					+ runItemRoutines(player_attacking.getItemInHand().getType(), false, eventDamage)
			//apply group buff settings
				+ (groupHandlers.containsKey(group_target)
						?groupHandlers.get(group_target).calcDefenseBuff(group_attacking, player_attacking.getItemInHand().getType(), armorSet_target, eventDamage)
						:0);
		}
		return 0;
	}
	//---
	
//handle NPvP
	public int calcAttackBuff(Player player_target, DamageElement damageType, int eventDamage)
	{
		//String group_target = plugin.Permissions.getGroup(player_target.getWorld().getName(), player_target.getName());
		if(globalsLoaded)
		{
			return runGlobalRoutines(damageType.getType(), true, eventDamage)
					+ runGlobalRoutines(damageType, true, eventDamage);
		}
		return 0;
	}
	public int calcDefenseBuff(Player player_target, DamageElement damageType, int eventDamage)
	{
		if(globalsLoaded)
		{
			String group_target = ModDamage.Permissions.getGroup(player_target.getWorld().getName(), player_target.getName());
			ArmorSet armorSet_target = new ArmorSet(player_target);
			return runGlobalRoutines(DamageElement.GENERIC_HUMAN, false, eventDamage)
					+ runArmorRoutines(armorSet_target, false, eventDamage)
					+ runGlobalRoutines(DamageElement.matchItemType(player_target.getItemInHand().getType()), false, eventDamage) //item type
					+ runItemRoutines(player_target.getItemInHand().getType(), false, eventDamage) //item specific
					+ (groupHandlers.containsKey(group_target) //group specific
							?groupHandlers.get(group_target).calcDefenseBuff(damageType, player_target.getItemInHand().getType(), armorSet_target, eventDamage)
							:0);
		}
		return 0;
	}
	//---
	
//Mob-targeted damage
//WORLD vs MOB
	public int calcAttackBuff(DamageElement mobType_target, DamageElement damageType, int eventDamage)
	{
		if(globalsLoaded)
		{
			//apply global buff settings
			return runGlobalRoutines(damageType.getType(), true, eventDamage)
					+ runGlobalRoutines(damageType, true, eventDamage);
		}
		return 0;
	}
	public int calcDefenseBuff(DamageElement mobType_target, DamageElement damageType, int eventDamage)
	{
		if(globalsLoaded)
		{
			return runGlobalRoutines(mobType_target.getType(), false, eventDamage)
					+ runGlobalRoutines(mobType_target, false, eventDamage);
		}
		return 0;
	}
	//---
//PvNP	
	public int calcAttackBuff(DamageElement mobType_target, Player player_attacking, int eventDamage)
	{
		if(globalsLoaded)
		{
			String group_attacking = ModDamage.Permissions.getGroup(player_attacking.getWorld().getName(), player_attacking.getName());
			ArmorSet armorSet_attacking = new ArmorSet(player_attacking);
			return runGlobalRoutines(DamageElement.GENERIC_HUMAN, true, eventDamage) 
					+ runArmorRoutines(armorSet_attacking, true, eventDamage)
					+ runGlobalRoutines(DamageElement.matchItemType(player_attacking.getItemInHand().getType()), true, eventDamage)
					+ runItemRoutines(player_attacking.getItemInHand().getType(), true, eventDamage)
					+ (groupHandlers.containsKey(group_attacking)
							?groupHandlers.get(group_attacking).calcAttackBuff(mobType_target, player_attacking.getItemInHand().getType(), armorSet_attacking, eventDamage)
							:0);
		}
		return 0;
	}
	public int calcDefenseBuff(DamageElement mobType_target, Player player_attacking, int eventDamage)
	{
		if(globalsLoaded)
		{
			//String group_attacking = ModDamage.Permissions.getGroup(player_attacking.getWorld().getName(), player_attacking.getName());
			return runGlobalRoutines(mobType_target.getType(), false, eventDamage) 
					+ runGlobalRoutines(mobType_target, false, eventDamage);
		}
		return 0;
	}
	//---
	
//Routine handlers
	private int runGlobalRoutines(DamageElement damageType, boolean isOffensive, int eventDamage)
	{ return runGlobalRoutines(damageType, isOffensive, eventDamage, false);}
	private int runGlobalRoutines(DamageElement damageType, boolean isOffensive, int eventDamage, boolean printDebugging)
	{
		if(damageType != null && (isOffensive?offensiveRoutines:defensiveRoutines).containsKey(damageType))
			return damageCalc.parseCommands((isOffensive?offensiveRoutines:defensiveRoutines).get(damageType), eventDamage, isOffensive);
		return 0;
	}
	
	private int runItemRoutines(Material materialType, boolean isOffensive, int eventDamage)
	{
		if(materialType != null && (isOffensive?itemOffensiveRoutines:itemDefensiveRoutines).containsKey(materialType))
			return damageCalc.parseCommands((isOffensive?itemOffensiveRoutines:itemDefensiveRoutines).get(materialType), eventDamage, isOffensive);
		return 0;
	}
	
	private int runArmorRoutines(ArmorSet armorSet, boolean isOffensive, int eventDamage)
	{
		if(!armorSet.isEmpty() && (isOffensive?armorOffensiveRoutines:armorDefensiveRoutines).containsKey(armorSet))
			return damageCalc.parseCommands((isOffensive?armorOffensiveRoutines:armorDefensiveRoutines).get(armorSet), eventDamage, isOffensive);
		return 0;
	}
//----
	
///////////////////// HELPER FUNCTIONS ///////////////////////
	public World getWorld(){ return world;}
	
	private void clearRoutines() 
	{
		offensiveRoutines.clear();
		defensiveRoutines.clear();
		itemOffensiveRoutines.clear();
		itemDefensiveRoutines.clear();
		globalScanItems.clear();
		mobHealthSettings.clear();
	}

///////////////////// INGAME COMMANDS ///////////////////////
	public boolean sendWorldConfig(Player player, String configReference)
	{
		if(globalsLoaded)
		{
			if(player != null)
			{
				//send specified category stuff - perhaps format upon loading for performance? (TODO)
			}
			else //send to console
			{
				
			}
		}
		return false;
	}

	public boolean sendGroupConfig(Player player, String groupName, String configReference) 
	{
		if(groupHandlers.containsKey(groupName))
			return groupHandlers.get(groupName).sendGroupConfig(player, configReference);
		else return false;
	}
	

	public boolean group_isLoaded(String groupName){ return groupHandlers.containsKey(groupName);}
	public boolean attackType_isLoaded(DamageElement damageType){ return offensiveRoutines.containsKey(damageType);}
	public boolean defenseType_isLoaded(DamageElement damageType){ return defensiveRoutines.containsKey(damageType);}
	
	public boolean loadedSomething()
	{
		boolean groupsLoadedSomething = false;
		for(GroupHandler groupHandler : groupHandlers.values())
		{
			if(groupHandler.loadedSomething())
				groupsLoadedSomething = true;
			else groupHandlers.remove(groupHandler.getGroupName());
		}
		return (!offensiveRoutines.isEmpty() || !defensiveRoutines.isEmpty()
				|| !itemOffensiveRoutines.isEmpty() || !itemDefensiveRoutines.isEmpty()
				|| !globalScanItems.isEmpty() || !mobHealthSettings.isEmpty()
				|| groupsLoadedSomething);
	}
	
	public void clear()
	{
		clearRoutines();
		groupHandlers.clear();
	}
}

	