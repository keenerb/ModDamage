package com.ModDamage.Tags;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.ModDamage.ModDamage;
import com.ModDamage.PluginConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;

import com.ModDamage.PluginConfiguration.LoadState;
import com.ModDamage.PluginConfiguration.OutputPreset;

public class TagManager
{
	public static final String configString_save = "interval-save";
	public static final String configString_clean = "interval-clean";
	public static final int defaultInterval = 10 * 20;

    public final TagsHolder<Integer> intTags = new TagsHolder<Integer>();
	public final TagsHolder<String> stringTags = new TagsHolder<String>();
	
	
	private long saveInterval;
	//private long cleanInterval;
	private int saveTaskID;
	private int cleanTaskID;

	public final File file;
	public final File newFile;
	public final File oldFile;
	private InputStream reader = null;
	private FileWriter writer = null;
	private Yaml yaml = new Yaml();

	public TagManager(File file, long saveInterval, long cleanInterval)
	{
		this.saveInterval = saveInterval;
		//this.cleanInterval = cleanInterval;
		this.file = file;
		newFile = new File(file.getParent(), file.getName()+".new");
		oldFile = new File(file.getParent(), file.getName()+".old");
		
		
		load();

		reload(false);
	}
	
	public void reload(){ reload(true); }
	
	private void reload(boolean initialized)
	{
		//cleanUp();
		save();
		if(initialized)
		{
			if(file != null)
			{
				if(saveTaskID != 0) Bukkit.getScheduler().cancelTask(saveTaskID);
				if(cleanTaskID != 0) Bukkit.getScheduler().cancelTask(cleanTaskID);
			}
		}
		Plugin modDamage = Bukkit.getPluginManager().getPlugin("ModDamage");
		
		saveTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(modDamage, new Runnable(){
			@Override public void run()
			{
				save();
			}
		}, saveInterval, saveInterval);
	}
	
	private boolean dirty = false;
    public void dirty() { dirty = true; }
	
	@SuppressWarnings("unchecked")
	public void load()
	{
		try
		{
			if(!file.exists())
			{
				ModDamage.addToLogRecord(OutputPreset.INFO, "No tags file found at " + file.getAbsolutePath() + ", generating a new one...");
				if(!file.createNewFile())
				{
					ModDamage.addToLogRecord(OutputPreset.FAILURE, "Couldn't make new tags file! Tags will not have persistence between reloads.");
					return;
				}
			}
			reader = new FileInputStream(file);
			Object tagFileObject = yaml.load(reader);
			reader.close();
			if(tagFileObject == null || !(tagFileObject instanceof Map)) return;
			
			Map<UUID, Entity> entities = new HashMap<UUID, Entity>();
			for(World world : Bukkit.getWorlds())
			{
				for (Entity entity : world.getEntities())
					if (!(entity instanceof OfflinePlayer))
						entities.put(entity.getUniqueId(), entity);
			}
			
			Map<String, Object> tagMap = (Map<String, Object>)tagFileObject;
			
			if (!tagMap.containsKey("int")) // Old style tags.yml
			{
				intTags.loadTags(tagMap, entities);
				save(); // upgrade the file
			}
			else // New way
			{
				intTags.loadTags((Map<String, Object>) tagMap.get("int"), entities);
				stringTags.loadTags((Map<String, Object>) tagMap.get("string"), entities);
			}
		}
		catch(Exception e){ ModDamage.addToLogRecord(OutputPreset.FAILURE, "Error loading tags: "+e.toString()); }
	}
	
	
	/**
	 * Saves all tags to a file.
	 */
	public void save()
	{
		if(file != null && dirty)
		{
			Set<Entity> entities = new HashSet<Entity>();
			for (World world : Bukkit.getWorlds())
				entities.addAll(world.getEntities());

			
			Map<String, Object> saveMap = new HashMap<String, Object>();
			
			saveMap.put("tagsVersion", 2);
			saveMap.put("int", intTags.saveTags(entities));
			saveMap.put("string", stringTags.saveTags(entities));
			
			try
			{
				writer = new FileWriter(newFile);
				writer.write(yaml.dump(saveMap));
				writer.close();
			}
			catch (IOException e){
				PluginConfiguration.log.warning("Error saving tags at " + newFile.getAbsolutePath() + "!");
				return;
			}
			
			oldFile.delete();
			file.renameTo(oldFile);
			newFile.renameTo(file);
		}
	}
	
	/**
	 * This is used in the ModDamage main to finish any file IO.
	 */
	public void close()
	{
		//cleanUp();
		save();
	}
	
	/**
	 * @return LoadState reflecting the file's load state.
	 */
	public LoadState getLoadState(){ return file != null? LoadState.SUCCESS : LoadState.NOT_LOADED; }

	public void clear()
	{
		intTags.clear();
		stringTags.clear();
	}
}