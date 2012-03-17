package com.ModDamage.Routines.Nested;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Entity;

import com.ModDamage.Alias.RoutineAliaser;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.IntRef;
import com.ModDamage.EventInfo.DataRef;
import com.ModDamage.EventInfo.EventData;
import com.ModDamage.EventInfo.EventInfo;
import com.ModDamage.EventInfo.SimpleEventInfo;
import com.ModDamage.Expressions.IntegerExp;
import com.ModDamage.Routines.Routines;

public class EntityExplode extends NestedRoutine
{
	private final DataRef<Entity> entityRef;
	private final IntegerExp strength;
	
	public EntityExplode(String configString, DataRef<Entity> entityRef, IntegerExp strength)
	{
		super(configString);
		this.entityRef = entityRef;
		this.strength = strength;
	}
	
	static final EventInfo myInfo = new SimpleEventInfo(IntRef.class, "strength", "-default");
	
	@Override
	public void run(EventData data) throws BailException
	{
		Entity entity = entityRef.get(data);
		
		EventData myData = myInfo.makeChainedData(data, new IntRef(0));
		entity.getWorld().createExplosion(entity.getLocation(), strength.getValue(myData)/10.0f);
	}
	
	public static void register()
	{
		NestedRoutine.registerRoutine(Pattern.compile("(.*)effect\\.explode", Pattern.CASE_INSENSITIVE), new RoutineBuilder());
	}
	
	protected static class RoutineBuilder extends NestedRoutine.RoutineBuilder
	{	
		@Override
		public EntityExplode getNew(Matcher matcher, Object nestedContent, EventInfo info)
		{
			DataRef<Entity> entityRef = info.get(Entity.class, matcher.group(1).toLowerCase());

			EventInfo einfo = info.chain(myInfo);
			Routines routines = RoutineAliaser.parseRoutines(nestedContent, einfo);
			if (routines == null) return null;
			
			IntegerExp strength = IntegerExp.getNew(routines, einfo);
			
			if(entityRef != null && strength != null)
				return new EntityExplode(matcher.group(), entityRef, strength);
			
			return null;
		}
	}
}
