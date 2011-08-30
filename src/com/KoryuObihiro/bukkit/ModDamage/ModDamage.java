package com.KoryuObihiro.bukkit.ModDamage;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

import com.KoryuObihiro.bukkit.ModDamage.Backend.ArmorSet;
import com.KoryuObihiro.bukkit.ModDamage.Backend.ModDamageElement;
import com.KoryuObihiro.bukkit.ModDamage.Backend.Aliasing.Aliaser;
import com.KoryuObihiro.bukkit.ModDamage.Backend.Aliasing.ArmorAliaser;
import com.KoryuObihiro.bukkit.ModDamage.Backend.Aliasing.BiomeAliaser;
import com.KoryuObihiro.bukkit.ModDamage.Backend.Aliasing.ElementAliaser;
import com.KoryuObihiro.bukkit.ModDamage.Backend.Aliasing.GroupAliaser;
import com.KoryuObihiro.bukkit.ModDamage.Backend.Aliasing.ItemAliaser;
import com.KoryuObihiro.bukkit.ModDamage.Backend.Aliasing.MessageAliaser;
import com.KoryuObihiro.bukkit.ModDamage.Backend.Aliasing.RoutineAliaser;
import com.KoryuObihiro.bukkit.ModDamage.Backend.Aliasing.WorldAliaser;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.CalculationRoutine;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.ConditionalRoutine;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.ConditionalStatement;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Routine;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.SwitchRoutine;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Base.Addition;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Base.DiceRoll;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Base.DiceRollAddition;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Base.Division;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Base.DivisionAddition;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Base.IntervalRange;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Base.LiteralRange;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Base.Message;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Base.Multiplication;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Base.Set;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Calculation.EntityAddAirTicks;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Calculation.EntityAddFireTicks;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Calculation.EntityDropItem;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Calculation.EntityExplode;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Calculation.EntityHeal;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Calculation.EntityHurt;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Calculation.EntitySetAirTicks;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Calculation.EntitySetFireTicks;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Calculation.EntitySetHealth;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Calculation.EntitySpawn;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Calculation.PlayerAddItem;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Calculation.PlayerSetItem;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Calculation.SlimeSetSize;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Calculation.WorldTime;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.Binomial;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.EntityAirTicksComparison;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.EntityBiome;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.EntityCoordinateComparison;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.EntityDrowning;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.EntityExposedToSky;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.EntityFallComparison;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.EntityFalling;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.EntityFireTicksComparison;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.EntityHealthComparison;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.EntityLightComparison;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.EntityOnBlock;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.EntityOnFire;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.EntityTypeEvaluation;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.EntityUnderwater;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.EventHasRangedElement;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.EventRangedElementEvaluation;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.EventValueComparison;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.EventWorldEvaluation;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.PlayerGroupEvaluation;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.PlayerSleeping;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.PlayerSneaking;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.PlayerWearing;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.PlayerWearingOnly;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.PlayerWielding;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.ServerOnlineMode;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.ServerPlayerCountComparison;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.SlimeSizeComparison;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.WorldEnvironment;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional.WorldTimeComparison;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Switch.ArmorSetSwitch;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Switch.BiomeSwitch;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Switch.EntityTypeSwitch;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Switch.EnvironmentSwitch;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Switch.PlayerGroupSwitch;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Switch.PlayerWieldSwitch;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Switch.RangedElementSwitch;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Switch.WorldSwitch;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.platymuus.bukkit.permissions.PermissionInfo;
import com.platymuus.bukkit.permissions.PermissionsPlugin;

/**
 * "ModDamage" for Bukkit
 * 
 * @author Erich Gubler
 *
 */
public class ModDamage extends JavaPlugin
{
	//TODO 0.9.6 Command for autogen world/entitytype switches?
	//TODO 0.9.6 switch and comparison for wieldquantity
	//TODO 0.9.6 switch.conditional
	//TODO 0.9.6 switch and conditional for region
	// TODO 0.9.6 Make the Scan message possible.
	/*
	if(eventInfo.shouldScan)
	{
		int displayHealth = (eventInfo.entity_target).getHealth() - ((!(eventInfo.eventDamage < 0 && ModDamage.negative_Heal))?eventInfo.eventDamage:0);
		((Player)eventInfo.entity_attacker).sendMessage(ChatColor.DARK_PURPLE + eventInfo.element_target.getReference() 
				+ "(" + (eventInfo.name_target != null?eventInfo.name_target:("id " + eventInfo.entity_target.getEntityId()))
				+ "): " + Integer.toString((displayHealth < 0)?0:displayHealth));
	}
	*/
	// -Triggered effects...should be a special type of tag! :D Credit: ricochet1k
	// -AoE clearance, block search nearby for Material?
	
	// Ideas
	// -if.entityis.inRegion
	// -if.server.port.#port
	// -switch.spawnreason

	// -for.eventvalue iterations?
	// -foreach (probably want dynamic tags here)
	//    -region
	//    -item in inventory?
	//    -item in hand?
	//    -item by slot?
	//    -health tick?
	
	//--Yet-to-be-plausible:
	// -tag.$aliasName
	// -ability to clear non-static tag
	// -External: tag entities with an alias ($)
	// -External: check entity tags
	// -find a way to give players ownership of an explosion
	// -Deregister when Bukkit supports!
	
	// Ideas
	// -External calls to aliased sets of routines? But...EventInfo would be screwed up. :P

	
//Typical plugin stuff...for the most part. :P
	public static Server server;
	public final int oldestSupportedBuild = 1060;
	private final ModDamageEntityListener entityListener = new ModDamageEntityListener(this);
	public final static Logger log = Logger.getLogger("Minecraft");
	private static DebugSetting debugSetting = DebugSetting.NORMAL;
	public static enum DebugSetting
	{
		QUIET, NORMAL, CONSOLE, VERBOSE;
		public static DebugSetting matchSetting(String key)
		{
			for(DebugSetting setting : DebugSetting.values())
				if(key.equalsIgnoreCase(setting.name()))
						return setting;
				return null;
		}
		private boolean shouldOutput(DebugSetting setting)
		{
			if(setting.ordinal() <= this.ordinal())
				return true;
			return false;
		}
	}	
	private static Configuration config;
	private static String errorString_Permissions = ModDamageString(ChatColor.RED) + " You don't have access to that command.";

	private static int configPages = 0;
	private static List<String> configStrings_ingame = new ArrayList<String>();
	private static List<String> configStrings_console = new ArrayList<String>();
	private static int additionalConfigChecks = 0;
	
//External-plugin variables
	public static PermissionHandler Permissions = null;
	public static PermissionsPlugin permissionsBukkit = null;
	//private static elRegionsPlugin elRegions = null;
	public static boolean multigroupPermissions = true;
	public static boolean using_Permissions = false;
	public static boolean using_SuperPerms = false;//TODO 0.9.6 - DEPRECATE ME
	static boolean using_elRegions = false;
	
//General mechanics options
	static boolean negative_Heal;
	
//Predefined pattern strings
	public static final String statementPart = "(?:!?(?:[\\*\\w]+)(?:\\.[\\*\\w]+)*)";
	private static Pattern calculationPattern = Pattern.compile("((?:([\\*\\w]+)effect\\." + statementPart + ")|set)", Pattern.CASE_INSENSITIVE);//TODO 0.9.6 - Make a design decision here. Should Calculations only be "bleheffect"?
	private static Pattern conditionalPattern = Pattern.compile("(if|if_not)\\s+(" + statementPart + "(?:\\s+([\\*\\w]+)\\s+" + statementPart + ")*)", Pattern.CASE_INSENSITIVE);
	private static Pattern switchPattern = Pattern.compile("switch\\.(" + statementPart + ")", Pattern.CASE_INSENSITIVE);
	
	public static HashMap<Pattern, Method> registeredBaseRoutines = new HashMap<Pattern, Method>();

//LoadStates
	public enum LoadState
	{
		NOT_LOADED(ChatColor.GRAY + "NO  "), 
		FAILURE(ChatColor.RED + "FAIL"), 
		SUCCESS(ChatColor.GREEN + "YES ");
		
		private String string;
		private LoadState(String string){ this.string = string;}
		private String statusString(){ return string;}
		private static LoadState combineStates(LoadState...loadStates)
		{
			LoadState returnState = LoadState.NOT_LOADED;
			for(LoadState state : loadStates)
			{
				if(state.equals(LoadState.FAILURE))
					return LoadState.FAILURE;
				else if(state.equals(LoadState.SUCCESS))
					returnState = SUCCESS;
			}
			return returnState;
		}
	}

	//TODO All this repetitious crap has gotta be handled better.
//Routine objects
	static final List<Routine> damageRoutines = new ArrayList<Routine>();
	static final List<Routine> spawnRoutines = new ArrayList<Routine>();
	static final List<Routine> deathRoutines = new ArrayList<Routine>();
	static final List<Routine> foodRoutines = new ArrayList<Routine>();
	private static LoadState state_damageRoutines = LoadState.NOT_LOADED;
	private static LoadState state_spawnRoutines = LoadState.NOT_LOADED;
	private static LoadState state_deathRoutines = LoadState.NOT_LOADED;
	private static LoadState state_foodRoutines = LoadState.NOT_LOADED;
	private static LoadState state_routines = LoadState.NOT_LOADED;
	
//Alias objects
	private static ArmorAliaser armorAliaser = new ArmorAliaser();
	private static BiomeAliaser biomeAliaser = new BiomeAliaser();
	private static ElementAliaser elementAliaser = new ElementAliaser();
	private static GroupAliaser groupAliaser = new GroupAliaser();
	private static ItemAliaser itemAliaser = new ItemAliaser();
	private static MessageAliaser messageAliaser = new MessageAliaser();
	private static RoutineAliaser routineAliaser = new RoutineAliaser();
	private static WorldAliaser worldAliaser = new WorldAliaser();
	private static LoadState state_armorAliases = LoadState.NOT_LOADED;
	private static LoadState state_biomeAliases = LoadState.NOT_LOADED;
	private static LoadState state_elementAliases = LoadState.NOT_LOADED;
	private static LoadState state_itemAliases = LoadState.NOT_LOADED;
	private static LoadState state_groupAliases = LoadState.NOT_LOADED;
	private static LoadState state_messageAliases = LoadState.NOT_LOADED;
	private static LoadState state_routineAliases = LoadState.NOT_LOADED;
	private static LoadState state_worldAliases = LoadState.NOT_LOADED;
	private static LoadState state_aliases = LoadState.NOT_LOADED;
	
	private static LoadState state_plugin = LoadState.NOT_LOADED;
	public static boolean isEnabled = false;
	
////////////////////////// INITIALIZATION
	@Override
	public void onEnable() 
	{		
		ModDamage.server = getServer();
	//PERMISSIONS
		Plugin permissionsPlugin = getServer().getPluginManager().getPlugin("PermissionsBukkit");
		if (permissionsPlugin != null)
		{
			ModDamage.permissionsBukkit = (PermissionsPlugin)permissionsPlugin;
				using_Permissions = true;
				using_SuperPerms = true;
				log.info("[" + getDescription().getName() + "] " + this.getDescription().getVersion() + " enabled [PermissionsBukkit v" + permissionsPlugin.getDescription().getVersion() + " active]");
		}
		else
		{
			permissionsPlugin = getServer().getPluginManager().getPlugin("Permissions");
			if (permissionsPlugin != null)
			{
				using_Permissions = true;
				ModDamage.Permissions = ((Permissions)permissionsPlugin).getHandler();
				log.info("[" + getDescription().getName() + "] " + this.getDescription().getVersion() + " enabled [Permissions v" + permissionsPlugin.getDescription().getVersion() + " active]");
				
				//This is necessary for backwards-compatibility.
				multigroupPermissions = permissionsPlugin.getDescription().getVersion().startsWith("3.");
			}
			else log.info("[" + getDescription().getName() + "] " + this.getDescription().getVersion() + " enabled [Permissions not found]");
		}
		
	//TODO 0.9.6 ELREGIONS
		/*
		elRegions = (elRegionsPlugin) this.getServer().getPluginManager().getPlugin("elRegions");
		if (elRegions != null) 
		{
			using_elRegions = true;
		    log.info("[" + getDescription().getName() + "] Found elRegions v" + elRegions.getDescription().getVersion());
		}*/
		
	//Build check
		String string = getServer().getVersion();
		Matcher matcher = Pattern.compile(".*b([0-9]+)jnks.*", Pattern.CASE_INSENSITIVE).matcher(string);
		if(matcher.matches())
		{
			if(Integer.parseInt(matcher.group(1)) < oldestSupportedBuild)
				log.warning("Detected Bukkit build " + matcher.group(1) + " - builds " + oldestSupportedBuild + " and older are not supported with this version of ModDamage. Please update your current Bukkit installation.");
		}
		else log.severe("[" + getDescription().getName() + "] Either this is a nonstandard build, or the Bukkit builds system has changed. Either way, don't blame Koryu if stuff breaks.");
		
	//Event registration
		//register plugin-related stuff with the server's plugin manager
		server.getPluginManager().registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Event.Priority.Highest, this);
		server.getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Highest, this);
		server.getPluginManager().registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Highest, this);
		server.getPluginManager().registerEvent(Event.Type.ENTITY_REGAIN_HEALTH, entityListener, Event.Priority.Highest, this);
		
		config = this.getConfiguration();
		reload(true);
		isEnabled = true;
	}

	@Override
	public void onDisable(){ log.info("[" + getDescription().getName() + "] disabled.");}

////COMMAND PARSING ////
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		Player player = ((sender instanceof Player)?((Player)sender):null);
		boolean fromConsole = (player == null);
		
			if (label.equalsIgnoreCase("ModDamage") || label.equalsIgnoreCase("md"))
			{
				if (args.length == 0)
				{
					sendCommandUsage(player, false);
					return true;
				}
				else if(args.length >= 0)
				{
					if(args[0].equalsIgnoreCase("debug") || args[0].equalsIgnoreCase("d"))
						{
							if(fromConsole || hasPermission(player, "moddamage.debug"))
							{
								if(args.length == 1) toggleDebugging(player);
								else if(args.length == 2)
								{
									DebugSetting matchedSetting = DebugSetting.matchSetting(args[1]);
									if(matchedSetting != null)
										setDebugging(player, matchedSetting);
									else
									{
										sendCommandUsage(player, true);
										return true;
									}
									return true;
								}
							}
							else player.sendMessage(errorString_Permissions);
							return true;
						}
						else if(args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r"))
						{
							if(args.length == 1 || args.length == 2)
							{
								boolean reloadingAll = args.length == 2 && args[1].equalsIgnoreCase("all");
								if(fromConsole) reload(reloadingAll);
								else if(hasPermission(player, "moddamage.reload")) 
								{
									log.info("[" + getDescription().getName() + "] Reload initiated by user " + player.getName() + "...");
									reload(reloadingAll);
									switch(state_plugin)
									{
										case SUCCESS: 
											player.sendMessage(ModDamageString(ChatColor.GREEN) + " Reloaded!");
											break;
										case FAILURE: 
											player.sendMessage(ModDamageString(ChatColor.YELLOW) + " Reloaded with errors.");
											break;
										case NOT_LOADED: 
											player.sendMessage(ModDamageString(ChatColor.GRAY) + " No configuration loaded! Are any routines defined?");
											break;
									}
								}
								else player.sendMessage(errorString_Permissions);
							}
							return true;
						}
						else if(args[0].equalsIgnoreCase("enable"))
						{
							if(fromConsole || hasPermission(player, "moddamage.enable"))
								setPluginStatus(player, true);
							else player.sendMessage(errorString_Permissions);
							return true;
						}
						else if(args[0].equalsIgnoreCase("disable"))
						{
							if(fromConsole || hasPermission(player, "moddamage.disable"))
									setPluginStatus(player, false);
							else player.sendMessage(errorString_Permissions);
							return true;
						}
					if( isEnabled)
					{
						if(args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("c"))
						{
							//md check
							if(fromConsole)
							{
								sendConfig(null, 9001);
								log.info("[" + getDescription().getName() + "] Done.");
							}
							else if(args.length == 1)
							{
								if(hasPermission(player, "moddamage.check"))
									sendConfig(player, 0);
								else player.sendMessage(errorString_Permissions);
							}
							//md check int
							else if(args.length == 2)
							{
								try
								{
									sendConfig(player, Integer.parseInt(args[1]));
								} 
								catch(NumberFormatException e)
								{
									sendCommandUsage(player, true);
								}
							}
						}
						else
						{
							sendCommandUsage(player, true);
						}
					}
					else if(player == null)
						log.info("[" + getDescription().getName() + "] ModDamage must be enabled to use that command.");
					else player.sendMessage(ModDamageString(ChatColor.RED) + " ModDamage must be enabled to use that command.");
					return true;
				}
			}
		sendCommandUsage(player, true);
		return true;
	}
	
///// HELPER FUNCTIONS ////
	public static boolean hasPermission(Player player, String permission)
	{
		if (ModDamage.Permissions != null)
			return ModDamage.Permissions.has(player, permission);
		else if(ModDamage.permissionsBukkit != null)
		{
			PermissionInfo info =  ModDamage.permissionsBukkit.getPlayerInfo(player.getName());
			return info != null && info.getPermissions() != null && info.getPermissions().containsKey(permission);
		}
		else return player.isOp();
	}

	public static String ModDamageString(ChatColor color){ return color + "[" + ChatColor.DARK_RED + "Mod" + ChatColor.DARK_BLUE + "Damage" + color + "]";}

	
//// PLUGIN CONFIGURATION ////
	private void setPluginStatus(Player player, boolean sentEnable) 
	{
		if(sentEnable)
		{
			if(isEnabled)
			{
				if(player != null) player.sendMessage(ModDamageString(ChatColor.RED) + " Already enabled!");
				else log.info("[" + getDescription().getName() + "] Already enabled!");
			}
			else
			{
				isEnabled = true;
				log.info("[" + getDescription().getName() + "] Plugin enabled.");
				if(player != null) player.sendMessage(ModDamageString(ChatColor.GREEN) + " Plugin enabled.");
			}
		}
		else 
		{
			if(isEnabled)
			{
				isEnabled = false;
				log.info("[" + getDescription().getName() + "] Plugin disabled.");
				if(player != null) player.sendMessage(ModDamageString(ChatColor.GREEN) + " Plugin disabled.");
			}
			else
			{
				if(player != null) player.sendMessage(ModDamageString(ChatColor.RED) + " Already disabled!");
				else log.info("[" + getDescription().getName() + "] Already disabled!");
					
			}
		}
	}

	private void sendCommandUsage(Player player, boolean forError) 
	{
		if(player != null)
		{
			if(forError) player.sendMessage(ChatColor.RED + "Error: invalid command syntax.");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "ModDamage commands:");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "/moddamage | /md - bring up this help message");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "/md (check | c) - check configuration");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "/md (debug | d) [debugType] - change debug type");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "/md disable - disable ModDamage");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "/md enable - enable ModDamage");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "/md (reload | r) - reload configuration");
		}
		else
		{
			if(forError) log.info("Error: invalid command syntax.");
			log.info("ModDamage commands:\n" +
					"/moddamage | /md - bring up this help message\n" +
					"/md check - check configuration\n" +
					"/md debug [debugType] - change debugging type (quiet, normal, verbose)\n" +
					"/md disable - disable ModDamage\n" +
					"/md enable - enable ModDamage\n" +
					"/md reload - reload configuration");
		}
	}
	
/////////////////// MECHANICS CONFIGURATION 
	private void reload(boolean reloadingAll)
	{
		//clear and reregister MD routines, if true
		if(reloadingAll)
		{
			ModDamage.registeredBaseRoutines.clear();
			CalculationRoutine.registeredStatements.clear();
			ConditionalRoutine.registeredStatements.clear();
			SwitchRoutine.registeredStatements.clear();
			
		//Base Calculations
				Addition.register(this);
				DiceRoll.register(this);
				DiceRollAddition.register(this);
				Division.register(this);
				DivisionAddition.register(this);
				IntervalRange.register(this);
				LiteralRange.register(this);
				Multiplication.register(this);
				Set.register(this);
				Message.register(this);
		//Nestable Calculations
			//Conditionals
				Binomial.register(this);
				//Entity
				EntityAirTicksComparison.register(this);
				EntityBiome.register(this);
				EntityCoordinateComparison.register(this);
				EntityDrowning.register(this);
				EntityExposedToSky.register(this);
				EntityFallComparison.register(this);
				EntityFalling.register(this);
				EntityFireTicksComparison.register(this);
				EntityHealthComparison.register(this);
				EntityLightComparison.register(this);
				EntityOnBlock.register(this);
				EntityOnFire.register(this);
				EntityTypeEvaluation.register(this);
				EntityUnderwater.register(this);
				EventWorldEvaluation.register(this);
				PlayerGroupEvaluation.register(this);
				PlayerSleeping.register(this);
				PlayerSneaking.register(this);
				PlayerWearing.register(this);
				PlayerWearingOnly.register(this);
				PlayerWielding.register(this);
				SlimeSizeComparison.register(this);
				//World
				WorldTimeComparison.register(this);
				WorldEnvironment.register(this);
				//Server
				ServerOnlineMode.register(this);
				ServerPlayerCountComparison.register(this);
				//Event
				EventHasRangedElement.register(this);
				EventRangedElementEvaluation.register(this);
				EventValueComparison.register(this);
				EventWorldEvaluation.register(this);
			//Effects
				EntityAddAirTicks.register(this);
				EntityAddFireTicks.register(this);
				EntityDropItem.register(this);
				EntityExplode.register(this);
				EntityHeal.register(this);
				EntityHurt.register(this);
				EntitySetAirTicks.register(this);
				EntitySetFireTicks.register(this);
				EntitySetHealth.register(this);
				EntitySpawn.register(this);
				PlayerAddItem.register(this);
				PlayerSetItem.register(this);
				SlimeSetSize.register(this);
				WorldTime.register(this);
			//Switches
				ArmorSetSwitch.register(this);
				BiomeSwitch.register(this);
				EntityTypeSwitch.register(this);
				EnvironmentSwitch.register(this);
				PlayerGroupSwitch.register(this);
				PlayerWieldSwitch.register(this);
				RangedElementSwitch.register(this);
				WorldSwitch.register(this);
		}
		damageRoutines.clear();
		deathRoutines.clear();
		foodRoutines.clear();
		spawnRoutines.clear();
		armorAliaser.clear();
		biomeAliaser.clear();
		elementAliaser.clear();
		groupAliaser.clear();
		itemAliaser.clear();
		messageAliaser.clear();
		worldAliaser.clear();
		state_routines = state_damageRoutines = state_deathRoutines = state_foodRoutines = state_spawnRoutines = state_aliases = state_armorAliases = state_biomeAliases = state_elementAliases = state_groupAliases = state_itemAliases = state_messageAliases = state_worldAliases = LoadState.NOT_LOADED;
		configStrings_ingame.clear();
		configStrings_console.clear();
		
		try
		{
			config.load();
		}
		catch(Exception e)
		{
			//FIXME 0.9.6 - Any way to catch this without firing off the stacktrace? Request for Bukkit to not auto-load config.
			log.severe("Error in YAML configuration. Type /md check for more information.");
			e.printStackTrace();
			/*
			for(StackTraceElement element : e.getStackTrace())
			{
			    addToConfig(DebugSetting.QUIET, 0, element.toString(), LoadState.FAILURE);
			}
			*/
			state_plugin = LoadState.FAILURE;
		    addToConfig(DebugSetting.QUIET, 0, "Error in YAML configuration. See the console for more information.", LoadState.FAILURE);
		    return;
		}
		
	//get plugin config.yml...if it doesn't exist, create it.
		if(!(new File(this.getDataFolder(), "config.yml")).exists()) writeDefaults();
	//load debug settings
		String debugString = config.getString("debugging");
		if(debugString != null)
		{
			DebugSetting debugSetting = DebugSetting.matchSetting(debugString);
			switch(debugSetting)
			{
				case QUIET: 
					log.info("[" + getDescription().getName()+ "] \"Quiet\" mode active - suppressing debug messages and warnings.");
					break;
				case NORMAL: 
					log.info("[" + getDescription().getName()+ "] Debugging active.");
					break;
				case VERBOSE: 
					log.info("[" + getDescription().getName()+ "] Verbose debugging active.");
					break;
				default: 
					log.info("[" + getDescription().getName()+ "] Debug string not recognized - defaulting to \"normal\" settings.");
					debugSetting = DebugSetting.NORMAL;
					break;
			}
			ModDamage.debugSetting = debugSetting;
		}

	//Item aliasing
		for(String key : config.getKeys())
			if(key.equalsIgnoreCase("Aliases"))
			{
				ConfigurationNode aliasesNode = config.getNode(key);
				if(!aliasesNode.getKeys().isEmpty())
				{
					state_armorAliases = loadAliases(aliasesNode, "Armor", armorAliaser);
					state_biomeAliases = loadAliases(aliasesNode, "Biome", biomeAliaser);
					state_elementAliases = loadAliases(aliasesNode, "Element", elementAliaser);
					state_itemAliases = loadAliases(aliasesNode, "Item", itemAliaser);
					state_groupAliases = loadAliases(aliasesNode, "Group", groupAliaser);
					state_messageAliases = loadAliases(aliasesNode, "Message", messageAliaser);
					state_worldAliases = loadAliases(aliasesNode, "World", worldAliaser);
					state_aliases = LoadState.combineStates(state_armorAliases, state_elementAliases, state_groupAliases, state_itemAliases, state_messageAliases, state_worldAliases);
					switch(state_aliases)//XXX Could be a more dynamic way of using the same switch, but meh.
					{
						case NOT_LOADED:
							addToConfig(DebugSetting.VERBOSE,  0, "No aliases loaded! Are any aliases defined?", state_aliases);
							break;
						case FAILURE:
							addToConfig(DebugSetting.QUIET,  0, "One or more errors occured while loading aliases.", state_aliases);
							break;
						case SUCCESS:
							addToConfig(DebugSetting.VERBOSE,  0, "Aliases loaded!", state_aliases);
							break;
					}
					break;
				}
				else addToConfig(DebugSetting.VERBOSE,  0, "No Aliases node found.", LoadState.NOT_LOADED);
			}
		
	//routines
		state_damageRoutines = loadRoutines("Damage", damageRoutines);
		state_deathRoutines = loadRoutines("Death", deathRoutines);
		state_foodRoutines = loadRoutines("Food", foodRoutines);
		state_spawnRoutines = loadRoutines("Spawn", spawnRoutines);
		state_routines = LoadState.combineStates(state_damageRoutines, state_deathRoutines, state_foodRoutines, state_spawnRoutines);
		switch(state_aliases)
		{
			case NOT_LOADED:
				addToConfig(DebugSetting.VERBOSE,  0, "No routines loaded! Are any routines defined?", state_aliases);
				break;
			case FAILURE:
				addToConfig(DebugSetting.QUIET,  0, "One or more errors occured while loading routines.", state_aliases);
				break;
			case SUCCESS:
				addToConfig(DebugSetting.VERBOSE,  0, "Routines loaded!", state_aliases);
				break;
		}

		state_plugin = LoadState.combineStates(state_aliases, state_routines);
		
	//single-property config
		negative_Heal = config.getBoolean("negativeHeal", false);
		if(debugSetting.shouldOutput(negative_Heal?DebugSetting.NORMAL:DebugSetting.VERBOSE))
			log.info("[" + getDescription().getName()+ "] Negative-damage healing " + (negative_Heal?"en":"dis") + "abled.");
		
		config.load(); //Discard any changes made to the file by the above reads.
		
		String sendThis = null;
		switch(state_plugin)
		{
			case NOT_LOADED:
				sendThis = "No configuration loaded.";
				break;
			case FAILURE:
				sendThis = "Loaded configuration with one or more errors.";
				break;
			case SUCCESS:
				sendThis = "Finished loading configuration.";
				break;
		}
		log.info("[" + getDescription().getName() + "] " + sendThis);
	}

	private void writeDefaults() 
	{
		log.severe("[" + getDescription().getName() + "] No configuration file found! Writing a blank config...");
		config.setProperty("debugging", "normal");
		config.setProperty("Damage", null);
		config.setProperty("Death", null);
		config.setProperty("Food", null);
		config.setProperty("Spawn", null);
		
		String[][] toolAliases = { {"axe", "hoe", "pickaxe", "spade", "sword"}, {"WOOD_", "STONE_", "IRON_", "GOLD_", "DIAMOND_"}};
		for(String toolType : toolAliases[0])
		{
			List<String> combinations = new ArrayList<String>();
			for(String toolMaterial : toolAliases[1])
				combinations.add(toolMaterial + toolType.toUpperCase());
			config.setProperty("Aliases.Item." + toolType, combinations);
		}

		config.save();
		log.severe("[" + getDescription().getName() + "] Defaults written to config.yml!");
	}
	
	private LoadState loadRoutines(String loadType, List<Routine> routineList)
	{
		LoadState relevantState = LoadState.NOT_LOADED;

		List<Object> routineObjects = null;
			for(String key : config.getKeys())
				if(key.equalsIgnoreCase(loadType))
				{
					routineObjects =  config.getList(key);
					break;
				}
		if(routineObjects != null)
		{
			relevantState = LoadState.SUCCESS;
			addToConfig(DebugSetting.NORMAL, 0, loadType.toUpperCase() + " configuration:", LoadState.SUCCESS);
			LoadState[] stateMachine = {relevantState};//We use a single-cell array here because the enum is ASSIGNED later - this doesn't work if we want to operate by reference.
			List<Routine> calculations = parse(routineObjects, loadType, stateMachine);
			relevantState = stateMachine[0];
			
			if(!calculations.isEmpty() && !relevantState.equals(LoadState.FAILURE))
			{
				routineList.addAll(calculations);
				addToConfig(DebugSetting.NORMAL, 0, "End " + loadType.toUpperCase() + " configuration.", LoadState.SUCCESS);
			}
			else addToConfig(DebugSetting.QUIET, 0, "Error in " + loadType.toUpperCase() + " configuration.", LoadState.FAILURE);
		}
		return relevantState;
	}
	
	private LoadState loadAliases(ConfigurationNode aliasesNode, String loadType, Aliaser<?> aliaser)
	{
		//TODO 0.9.6 Add debugging in reload() to check if Aliases is present or not.
		LoadState relevantState = LoadState.NOT_LOADED;
		ConfigurationNode specificAliasesNode = null;
			for(String key : aliasesNode.getKeys())
				if(key.equalsIgnoreCase(loadType))
				{
					specificAliasesNode = aliasesNode.getNode(key);
					break;
				}
			if(specificAliasesNode != null)
			{
				if(!specificAliasesNode.getKeys().isEmpty())
				{
					relevantState = LoadState.SUCCESS;
					addToConfig(DebugSetting.VERBOSE, 0, aliaser.getName() + " aliases found, parsing...", LoadState.SUCCESS);
					for(String alias : specificAliasesNode.getKeys())
					{
						List<String> values = specificAliasesNode.getStringList(alias, new ArrayList<String>());
						if(values.isEmpty())
							addToConfig(DebugSetting.VERBOSE, 0, "Found empty " + loadType.toLowerCase() + " alias \"" + alias + "\", ignoring...", LoadState.NOT_LOADED);
						else if(!aliaser.addAlias(alias, values))
							relevantState = LoadState.FAILURE;
					}
				}
			}
			else addToConfig(DebugSetting.VERBOSE, 0, "No " + loadType + " aliases node found.", LoadState.NOT_LOADED);
			
		return relevantState;
	}
	
//// ROUTINE PARSING ////
	//Parse routine strings recursively
////ROUTINE PARSING ////
	//Parse routine strings recursively
	private List<Routine> parse(List<Object> routineStrings, String loadType, LoadState[] currentState){ return parse(routineStrings, loadType, 0, currentState);}
	@SuppressWarnings("unchecked")
	private List<Routine> parse(Object object, String loadType, int nestCount, LoadState[] resultingState)
	{
		LoadState currentState = LoadState.SUCCESS;
		List<Routine> routines = new ArrayList<Routine>();
		if(object != null)
		{
			if(object instanceof String)
			{
				Routine routine = null;
				for(Pattern pattern : registeredBaseRoutines.keySet())
				{
					Matcher matcher = pattern.matcher((String)object);
					if(matcher.matches())
					{
						try
						{
							routine = (Routine)registeredBaseRoutines.get(pattern).invoke(null, matcher);
							if(routine != null)
							{
								routines.add(routine);
								addToConfig(DebugSetting.NORMAL, nestCount, "Routine: \"" + (String)object + "\"", currentState);
							}
							else
							{
								//TODO: Catch what routine matched, if/when it failed.
								currentState = LoadState.FAILURE;
								addToConfig(DebugSetting.VERBOSE, 0, "Bad parameters for new " + registeredBaseRoutines.get(pattern).getClass().getSimpleName() + " \"" + (String)object + "\"", currentState);
							}
							break;
						}
						catch(Exception e){ e.printStackTrace();}
					}
				}
				if(routine == null)
				{
					currentState = LoadState.FAILURE;
					addToConfig(DebugSetting.QUIET, 0, "Couldn't match base routine string" + " \"" + (String)object + "\"", currentState);
				}
			}
			else if(object instanceof LinkedHashMap)
			{
				HashMap<String, Object> someHashMap = (HashMap<String, Object>)object;//A properly-formatted nested routine is a LinkedHashMap with only one key.
				if(someHashMap.keySet().size() == 1)
					for(String key : someHashMap.keySet())
					{
						Matcher conditionalMatcher = conditionalPattern.matcher(key);
						Matcher switchMatcher = switchPattern.matcher(key);
						Matcher effectMatcher = calculationPattern.matcher(key);
						if(conditionalMatcher.matches())
						{
							addToConfig(DebugSetting.CONSOLE, nestCount, "", LoadState.SUCCESS);
							addToConfig(DebugSetting.NORMAL, nestCount, "Conditional: \"" + key + "\"", LoadState.SUCCESS);
							ConditionalRoutine routine = ConditionalRoutine.getNew(conditionalMatcher, parse(someHashMap.get(key), loadType, nestCount + 1, resultingState));
							if(routine != null)
							{
								routines.add(routine);
								addToConfig(DebugSetting.VERBOSE, nestCount, "End Conditional \"" + key + "\"\n", currentState);
							}
							else
							{
								currentState = LoadState.FAILURE;
								addToConfig(DebugSetting.QUIET, 0, "Invalid Conditional"+ " \"" + key + "\"", currentState);
							}
						}
						else if(effectMatcher.matches())
						{
							addToConfig(DebugSetting.CONSOLE, nestCount, "", LoadState.SUCCESS);
							addToConfig(DebugSetting.NORMAL, nestCount, "CalculatedEffect: \"" + key + "\"", LoadState.SUCCESS);
							CalculationRoutine<?> routine = CalculationRoutine.getNew(effectMatcher, parse(someHashMap.get(key), loadType, nestCount + 1, resultingState));
							if(routine != null)
							{
								routines.add(routine);
								addToConfig(DebugSetting.VERBOSE, nestCount, "End CalculatedEffect \"" + key + "\"\n", currentState);
							}
							else
							{
								currentState = LoadState.FAILURE;
								addToConfig(DebugSetting.QUIET, 0, "Invalid CalculatedEffect \"" + key + "\"", currentState);
							}
						}
						else if(switchMatcher.matches())
						{					
							LinkedHashMap<String, Object> anotherHashMap = (someHashMap.get(key) instanceof LinkedHashMap?(LinkedHashMap<String, Object>)someHashMap.get(key):null);
							if(anotherHashMap != null)
							{
								addToConfig(DebugSetting.CONSOLE, nestCount, "", LoadState.SUCCESS);
								addToConfig(DebugSetting.NORMAL, nestCount, "Switch: \"" + key + "\"", LoadState.SUCCESS);
								LinkedHashMap<String, List<Routine>> routineHashMap = new LinkedHashMap<String, List<Routine>>();
								SwitchRoutine<?> routine = null;
								for(String anotherKey : anotherHashMap.keySet())
								{
									addToConfig(DebugSetting.CONSOLE, nestCount, "", LoadState.SUCCESS);
									addToConfig(DebugSetting.NORMAL, nestCount, " case: \"" + anotherKey + "\"", LoadState.SUCCESS);
									routineHashMap.put(anotherKey, parse(anotherHashMap.get(anotherKey), loadType, nestCount + 1, resultingState));
									addToConfig(DebugSetting.VERBOSE, nestCount, "End case \"" + anotherKey + "\"\n", LoadState.SUCCESS);
								}
								routine = SwitchRoutine.getNew(switchMatcher, routineHashMap);
								if(routine != null)
								{
									if(routine.isLoaded) routines.add(routine);
									else 
									{
										currentState = LoadState.FAILURE;
										for(String caseName : routine.failedCases)
											addToConfig(DebugSetting.QUIET, 0, "Error: invalid case \"" + caseName + "\"", currentState);
									}
									addToConfig(DebugSetting.VERBOSE, nestCount, "End Switch \"" + key + "\"", LoadState.SUCCESS);
								}
								else
								{
									currentState = LoadState.FAILURE;
									addToConfig(DebugSetting.QUIET, 0, "Error: invalid Switch \"" + key + "\"", currentState);
								}
							}
						}
						else 
						{
							currentState = LoadState.FAILURE;
							addToConfig(DebugSetting.QUIET, 0, " No match found for nested node \"" + key + "\"", currentState);							
						}
					}
				else
				{
					currentState = LoadState.FAILURE;
					addToConfig(DebugSetting.QUIET, nestCount, "Parse error: bad nested routine.", currentState);				
				} 
			}
			else if(object instanceof List)
			{
				for(Object nestedObject : (List<Object>)object)
					routines.addAll(parse(nestedObject, loadType, nestCount, resultingState));
			}
			else
			{
				currentState = LoadState.FAILURE;
				addToConfig(DebugSetting.QUIET, nestCount, "Parse error: object " + object.toString() + " of type " + object.getClass().getName(), currentState);
			}
		}
		else 
		{
			currentState = LoadState.FAILURE;
			addToConfig(DebugSetting.QUIET, nestCount, "Parse error: null", currentState);
		}
		if(currentState.equals(LoadState.FAILURE))
			resultingState[0] = LoadState.FAILURE;
		return routines;
	}
		
	public void registerBase(Class<? extends Routine> routineClass, Pattern syntax)
	{
		try
		{
			Method method = routineClass.getMethod("getNew", Matcher.class);
			if(method != null)
			{
				assert(method.getReturnType().equals(routineClass));
				method.invoke(null, (Matcher)null);
				register(registeredBaseRoutines, method, syntax);
			}
			else log.severe("Method getNew not found for statement " + routineClass.getName());
		}
		catch(AssertionError e){ log.severe("[ModDamage] Error: getNew doesn't return class " + routineClass.getName() + "!");}
		catch(SecurityException e){ log.severe("[ModDamage] Error: getNew isn't public for class " + routineClass.getName() + "!");}
		catch(NullPointerException e){ log.severe("[ModDamage] Error: getNew for class " + routineClass.getName() + " is not static!");}
		catch(NoSuchMethodException e){ log.severe("[ModDamage] Error: Class \"" + routineClass.toString() + "\" does not have a getNew() method!");} 
		catch (IllegalArgumentException e){ log.severe("[ModDamage] Error: Class \"" + routineClass.toString() + "\" does not have matching method getNew(Matcher)!");} 
		catch (IllegalAccessException e){ log.severe("[ModDamage] Error: Class \"" + routineClass.toString() + "\" does not have valid getNew() method!");} 
		catch (InvocationTargetException e){ log.severe("[ModDamage] Error: Class \"" + routineClass.toString() + "\" does not have valid getNew() method!");}
	}

	public static void registerConditional(Class<? extends ConditionalStatement> statementClass, Pattern syntax)
	{
		try
		{
			Method method = statementClass.getMethod("getNew", Matcher.class);
			if(method != null)
			{
				assert(method.getReturnType().equals(statementClass));
				method.invoke(null, (Matcher)null);
				register(ConditionalRoutine.registeredStatements, method, syntax);
			}
			else log.severe("Method getNew not found for statement " + statementClass.getName());
		}
		catch(AssertionError e){ log.severe("[ModDamage] Error: getNew doesn't return class " + statementClass.getName() + "!");}
		catch(SecurityException e){ log.severe("[ModDamage] Error: getNew isn't public for class " + statementClass.getName() + "!");}
		catch(NullPointerException e){ log.severe("[ModDamage] Error: getNew for class " + statementClass.getName() + " is not static!");}
		catch(NoSuchMethodException e){ log.severe("[ModDamage] Error: Class \"" + statementClass.toString() + "\" does not have a getNew() method!");} 
		catch (IllegalArgumentException e){ log.severe("[ModDamage] Error: Class \"" + statementClass.toString() + "\" does not have matching method getNew(Matcher)!");} 
		catch (IllegalAccessException e){ log.severe("[ModDamage] Error: Class \"" + statementClass.toString() + "\" does not have valid getNew() method!");} 
		catch (InvocationTargetException e){ log.severe("[ModDamage] Error: Class \"" + statementClass.toString() + "\" does not have valid getNew() method!");} 
	}

	public static void registerSwitch(Class<? extends SwitchRoutine<?>> statementClass, Pattern syntax)
	{
		try
		{
			Method method = statementClass.getMethod("getNew", Matcher.class, LinkedHashMap.class);
			if(method != null)
			{
				assert(method.getReturnType().equals(statementClass));
				method.invoke(null, (Matcher)null, (LinkedHashMap<String, List<Routine>>)null);
				register(SwitchRoutine.registeredStatements, method, syntax);
			}
			else log.severe("Method getNew not found for statement " + statementClass.getName());
		}
		catch(AssertionError e){ log.severe("[ModDamage] Error: getNew doesn't return class " + statementClass.getName() + "!");}
		catch(SecurityException e){ log.severe("[ModDamage] Error: getNew isn't public for class " + statementClass.getName() + "!");}
		catch(NullPointerException e){ log.severe("[ModDamage] Error: getNew for class " + statementClass.getName() + " is not static!");}
		catch(NoSuchMethodException e){ log.severe("[ModDamage] Error: Class \"" + statementClass.toString() + "\" does not have a getNew() method!");} 
		catch (IllegalArgumentException e){ log.severe("[ModDamage] Error: Class \"" + statementClass.toString() + "\" does not have matching method getNew(Matcher, LinkedHashMap)!");} 
		catch (IllegalAccessException e){ log.severe("[ModDamage] Error: Class \"" + statementClass.toString() + "\" does not have valid getNew() method!");} 
		catch (InvocationTargetException e){ log.severe("[ModDamage] Error: Class \"" + statementClass.toString() + "\" does not have valid getNew() method!");} 
	}
	
	public static void registerEffect(Class<? extends CalculationRoutine<?>> routineClass, Pattern syntax)
	{
		try
		{
			Method method = routineClass.getMethod("getNew", Matcher.class, List.class);
			if(method != null)//XXX Is this necessary?
			{
				assert(method.getReturnType().equals(routineClass));
				method.invoke(null, (Matcher)null, (List<Routine>)null);
				register(CalculationRoutine.registeredStatements, method, syntax);
			}
			else log.severe("Method getNew not found for statement " + routineClass.getName());
		}
		catch(AssertionError e){ log.severe("[ModDamage] Error: getNew doesn't return class " + routineClass.getName() + "!");}
		catch(SecurityException e){ log.severe("[ModDamage] Error: getNew isn't public for class " + routineClass.getName() + "!");}
		catch(NullPointerException e){ log.severe("[ModDamage] Error: getNew for class " + routineClass.getName() + " is not static!");}
		catch(NoSuchMethodException e){ log.severe("[ModDamage] Error: Class \"" + routineClass.toString() + "\" does not have a getNew() method!");} 
		catch (IllegalArgumentException e){ log.severe("[ModDamage] Error: Class \"" + routineClass.toString() + "\" does not have matching method getNew(Matcher, List)!");} 
		catch (IllegalAccessException e){ log.severe("[ModDamage] Error: Class \"" + routineClass.toString() + "\" does not have valid getNew() method!");} 
		catch (InvocationTargetException e){ log.severe("[ModDamage] Error: Class \"" + routineClass.toString() + "\" does not have valid getNew() method!");} 
	}
	
	//TODO 0.9.6 Implement a reload hook for other plugins, make /md r reload routine library.
	private static void register(HashMap<Pattern, Method> registry, Method method, Pattern syntax)
	{
		boolean successfullyRegistered = false;
		if(syntax != null)
		{
			registry.put(syntax, method);	
			successfullyRegistered = true;
		}
		else log.severe("[ModDamage] Error: Bad regex for registering class \"" + method.getClass().getName() + "\"!");
		if(successfullyRegistered)
		{
			if(debugSetting.shouldOutput(DebugSetting.VERBOSE)) log.info("[ModDamage] Registering class " + method.getClass().getName() + " with pattern " + syntax.pattern());
		}
	}
		
//// LOGGING ////
	private static void setDebugging(Player player, DebugSetting setting)
	{ 
		if(setting != null) 
		{
			if(!debugSetting.equals(setting))
			{
				String sendThis = "Changed debug from " + debugSetting.name().toLowerCase() + " to " + setting.name().toLowerCase();
				log.info("[ModDamage] " + sendThis);
				if(player != null) player.sendMessage(ModDamageString(ChatColor.GREEN) + " " + sendThis);
				debugSetting = setting;
				config.setProperty("debugging", debugSetting.name().toLowerCase());
				config.save();
			}
			else
			{
				log.info("[ModDamage] Debug already set to " + setting.name().toLowerCase() + "!");
				if(player != null) player.sendMessage(ModDamageString(ChatColor.GREEN) + " Debug already set to " + setting.name().toLowerCase() + "!");
			}
		}
		else log.severe("[ModDamage] Error: bad debug setting. Valid settings: normal, quiet, verbose");//shouldn't happen
	}
	
	private static void toggleDebugging(Player player) 
	{
		switch(debugSetting)
		{
			case QUIET: 
				setDebugging(player, DebugSetting.NORMAL);
				break;
				
			case NORMAL:
				setDebugging(player, DebugSetting.VERBOSE);
				break;
				
			case VERBOSE:
				setDebugging(player, DebugSetting.QUIET);
				break;
		}
	}
	
	public static void addToConfig(DebugSetting outputSetting, int nestCount, String string, LoadState loadState)
	{
		if(loadState.equals(LoadState.FAILURE)) state_plugin = LoadState.FAILURE;
		if(debugSetting.shouldOutput(outputSetting))
		{
			ChatColor color = null;
			switch(loadState)
			{
				case NOT_LOADED:
					color = ChatColor.GRAY;
					break;
				case FAILURE:
					color = ChatColor.RED;
					break;
				case SUCCESS:
					color = ChatColor.AQUA;
					break;
			}
			if(!outputSetting.equals(DebugSetting.CONSOLE))
			{
				if(string.length() > 50)
				{
					String ingameString = string;
					configStrings_ingame.add(nestCount + "] " + color + ingameString.substring(0, 49));
					ingameString = ingameString.substring(49);
					while(ingameString.length() > 50)
					{
						configStrings_ingame.add("     " + color + ingameString.substring(0, 49));
						ingameString = ingameString.substring(49);
					}
					configStrings_ingame.add("     " + color + ingameString);
				}
				else configStrings_ingame.add(nestCount + "] " + color + string);
			}

			String nestIndentation = "";
			for(int i = 0; i < nestCount; i++)
				nestIndentation += "    ";
			configStrings_console.add(nestIndentation + string);
			
			switch(loadState)
			{
				case NOT_LOADED:
					log.warning(nestIndentation + string);
					break;
				case SUCCESS:
					log.info(nestIndentation + string);
					break;
				case FAILURE:
					log.severe(string);
					break;
			}
		}
		configPages = configStrings_ingame.size()/9 + (configStrings_ingame.size()%9 > 0?1:0);
	}

	private static boolean sendConfig(Player player, int pageNumber)
	{
		if(player == null)
		{
			String printString = "[ModDamage] Complete configuration for this server:";
			for(String configString : configStrings_console)
				printString += "\n" + configString;
			log.info(printString);
			return true;
		}
		else if(pageNumber > 0)
		{
			if(pageNumber <= configPages)
			{
				player.sendMessage(ModDamage.ModDamageString(ChatColor.GOLD) + " Configuration: (" + pageNumber + "/" + (configPages + additionalConfigChecks) + ")");
				for(int i = (9 * (pageNumber - 1)); i < (configStrings_ingame.size() < (9 * pageNumber)?configStrings_ingame.size():(9 * pageNumber)); i++)
					player.sendMessage(ChatColor.DARK_AQUA + configStrings_ingame.get(i));
				return true;
			}
		}
		else
		{
			//XXX Tighten up the formatting here - unify the placement.
			player.sendMessage(ModDamage.ModDamageString(ChatColor.GOLD) + " Config Overview: " + state_plugin.statusString() + ChatColor.GOLD + " (Total pages: " + configPages + ")");
			player.sendMessage(ChatColor.AQUA + "Aliases:    " + state_aliases.statusString() + "        " + ChatColor.DARK_GRAY + "Routines: " + state_routines.statusString());
			player.sendMessage(ChatColor.DARK_AQUA + "   Armor:        " + state_armorAliases.statusString() + "     " + ChatColor.DARK_GREEN + "Damage: " + state_damageRoutines.statusString());
			player.sendMessage(ChatColor.DARK_AQUA + "   Element:     " + state_elementAliases.statusString() + "       " + ChatColor.DARK_GREEN + "Death:  " + state_deathRoutines.statusString());
			player.sendMessage(ChatColor.DARK_AQUA + "   Group:        " + state_groupAliases.statusString() + "     " + ChatColor.DARK_GREEN + "Food:  " + state_foodRoutines.statusString());
			player.sendMessage(ChatColor.DARK_AQUA + "   Item:        " + state_itemAliases.statusString() + "      " + ChatColor.DARK_GREEN + "Spawn:  " + state_spawnRoutines.statusString());
			player.sendMessage(ChatColor.DARK_AQUA + "   Message:   " + state_messageAliases.statusString() + "        " + ChatColor.DARK_AQUA + "Biome:  " + state_biomeAliases.statusString());
			String bottomString = null;
			switch(state_plugin)
			{
				case NOT_LOADED:
					bottomString = ChatColor.GRAY + "No configuration found.";
					break;
				case FAILURE:
					bottomString = ChatColor.DARK_RED + "There were one or more read errors in config.";
					break;
				case SUCCESS:
					bottomString = ChatColor.GREEN + "No errors loading configuration!";
					break;
			}
			player.sendMessage(bottomString);	
		}
		return false;
	}
	
//// CONFIG MATCHING ////
	//XXX matchesValidEntity - currently defaults to "target". Change to reject?
	public static boolean matchesValidEntity(String string)
	{
		if(string.equalsIgnoreCase("target") || string.equalsIgnoreCase("attacker"))
			return true;
		else
		{
			addToConfig(DebugSetting.QUIET, 0, "Invalid entity identifier \"" + string + "\" - defaulting to \"target\"", LoadState.FAILURE);
			return false;
		}
	}
	public static boolean matchEntity(String string){ return string.equalsIgnoreCase("attacker");}
	
	public static Biome matchBiome(String biomeName)
	{
		for(Biome biome : Biome.values())
			if(biomeName.equalsIgnoreCase(biome.name()))
				return biome;
		return null;
	}
	
	public static Environment matchEnvironment(String environmentName)
	{
		for(Environment environment : Environment.values())
			if(environmentName.equalsIgnoreCase(environment.name()))
				return environment;
		return null;
	}

	public static List<ArmorSet> matchArmorAlias(String key){ return armorAliaser.matchAlias(key);}
	public static List<Biome> matchBiomeAlias(String key){ return biomeAliaser.matchAlias(key);}
	public static List<ModDamageElement> matchElementAlias(String key){ return elementAliaser.matchAlias(key);}
	public static List<Material> matchItemAlias(String key){ return itemAliaser.matchAlias(key);}
	public static List<String> matchGroupAlias(String key){ return groupAliaser.matchAlias(key);}
	public static List<String> matchMessageAlias(String key){ return messageAliaser.matchAlias(key);}
	//TODO 0.9.6 ADD routine aliaser
	public static List<String> matchWorldAlias(String key){ return worldAliaser.matchAlias(key);}
}