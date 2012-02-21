package com.ModDamage.Routines.Nested.Conditionals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Entity;

import com.ModDamage.ModDamage;
import com.ModDamage.EventInfo.DataRef;
import com.ModDamage.EventInfo.EventData;
import com.ModDamage.EventInfo.EventInfo;

public class EntityTagged extends Conditional
{
	public static final Pattern pattern = Pattern.compile("(\\w+)\\.istagged\\.(\\w+)", Pattern.CASE_INSENSITIVE);
	private final DataRef<Entity> entityRef;
	private final String tag;
	public EntityTagged(DataRef<Entity> entityRef, String tag)
	{
		this.entityRef = entityRef;
		this.tag = tag;
	}

	@Override
	public boolean evaluate(EventData data)
	{
		return entityRef.get(data) != null && ModDamage.getTagger().isTagged(entityRef.get(data), tag);
	}
	
	public static void register()
	{
		Conditional.register(new ConditionalBuilder());
	}
	
	protected static class ConditionalBuilder extends Conditional.SimpleConditionalBuilder
	{
		public ConditionalBuilder() { super(pattern); }

		@Override
		public EntityTagged getNew(Matcher matcher, EventInfo info)
		{
			DataRef<Entity> entityRef = info.get(Entity.class, matcher.group(1).toLowerCase());
			if(entityRef != null)
				return new EntityTagged(entityRef, matcher.group(2).toLowerCase());
			return null;
		}
	}
}