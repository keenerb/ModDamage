package com.ModDamage.Variables;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;

import com.ModDamage.StringMatcher;
import com.ModDamage.Utils;
import com.ModDamage.Backend.BailException;
import com.ModDamage.EventInfo.DataProvider;
import com.ModDamage.EventInfo.EventData;
import com.ModDamage.EventInfo.EventInfo;
import com.ModDamage.EventInfo.IDataProvider;

public class EntityEntity extends DataProvider<Entity, Entity>
{
	public static void register()
	{
		DataProvider.register(Entity.class, Entity.class, 
				Pattern.compile("_("+Utils.joinBy("|", EntityType.values()) +")", Pattern.CASE_INSENSITIVE),
				new IDataParser<Entity, Entity>()
				{
					@Override
					public IDataProvider<Entity> parse(EventInfo info, Class<?> want, IDataProvider<Entity> entityDP, Matcher m, StringMatcher sm)
					{
						return sm.acceptIf(new EntityEntity(
								entityDP, 
								EntityType.valueOf(m.group(1).toUpperCase())));
					}
				});
		DataProvider.register(LivingEntity.class, Creature.class, Pattern.compile("_target", Pattern.CASE_INSENSITIVE),
				new IDataParser<LivingEntity, Creature>() {
					public IDataProvider<LivingEntity> parse(EventInfo info, Class<?> want, IDataProvider<Creature> creatureDP, Matcher m, StringMatcher sm) {
						return new DataProvider<LivingEntity, Creature>(Creature.class, creatureDP) {
								public LivingEntity get(Creature creature, EventData data) { return creature.getTarget(); }
								public Class<LivingEntity> provides() { return LivingEntity.class; }
							};
					}
				});
		DataProvider.register(Player.class, LivingEntity.class, Pattern.compile("_killer", Pattern.CASE_INSENSITIVE),
				new IDataParser<Player, LivingEntity>() {
					public IDataProvider<Player> parse(EventInfo info, Class<?> want, IDataProvider<LivingEntity> livingDP, Matcher m, StringMatcher sm) {
						return new DataProvider<Player, LivingEntity>(LivingEntity.class, livingDP) {
								public Player get(LivingEntity living, EventData data) { return living.getKiller(); }
								public Class<Player> provides() { return Player.class; }
							};
					}
				});
		DataProvider.register(AnimalTamer.class, Tameable.class, Pattern.compile("_owner", Pattern.CASE_INSENSITIVE),
				new IDataParser<AnimalTamer, Tameable>() {
					public IDataProvider<AnimalTamer> parse(EventInfo info, Class<?> want, IDataProvider<Tameable> tameableDP, Matcher m, StringMatcher sm) {
						return new DataProvider<AnimalTamer, Tameable>(Tameable.class, tameableDP) {
								public AnimalTamer get(Tameable tameable, EventData data) { return tameable.getOwner(); }
								public Class<AnimalTamer> provides() { return AnimalTamer.class; }
							};
					}
				});
	}
	
	enum EntityType {
		PASSENGER {
			public Entity getItem(Entity entity) {
				return entity.getPassenger();
			}
		},
		VEHICLE {
			public Entity getItem(Entity entity) {
				return entity.getVehicle();
			}
		};
		
		public abstract Entity getItem(Entity entity);
	}
	

	private final EntityType entityType;

	public EntityEntity(IDataProvider<Entity> entityDP, EntityType entityType)
	{
		super(Entity.class, entityDP);
		this.entityType = entityType;
	}

	@Override
	public Entity get(Entity entity, EventData data) throws BailException
	{
		return entityType.getItem(entity);
	}

	@Override
	public Class<Entity> provides() { return Entity.class; }
	
	@Override
	public String toString()
	{
		return startDP + "_" + entityType.name().toLowerCase();
	}
}