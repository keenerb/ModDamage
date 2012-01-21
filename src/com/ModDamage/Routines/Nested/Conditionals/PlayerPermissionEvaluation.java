package com.ModDamage.Routines.Nested.Conditionals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import com.ModDamage.ExternalPluginManager;
import com.ModDamage.Backend.EntityReference;
import com.ModDamage.Backend.ModDamageElement;
import com.ModDamage.Backend.TargetEventInfo;

public class PlayerPermissionEvaluation extends Conditional
{
	public static final Pattern pattern = Pattern.compile("(\\w+)\\.haspermission\\.(\\w+)", Pattern.CASE_INSENSITIVE);
	final EntityReference entityReference;
	final String permission;
	public PlayerPermissionEvaluation(EntityReference entityReference, String permission)
	{  
		this.entityReference = entityReference;
		this.permission = permission;
	}
	@Override
	public boolean evaluate(TargetEventInfo eventInfo) 
 	{
		return (entityReference.getElement(eventInfo).matchesType(ModDamageElement.PLAYER))?ExternalPluginManager.getPermissionsManager().hasPermission(((Player)entityReference.getEntity(eventInfo)), permission):false;//XXX Include hasPermission in EntityReference?
	}
	
	public static void register()
	{
		Conditional.register(new ConditionalBuilder());
	}
	
	protected static class ConditionalBuilder extends Conditional.SimpleConditionalBuilder
	{
		public ConditionalBuilder() { super(pattern); }

		@Override
		public PlayerPermissionEvaluation getNew(Matcher matcher)
		{
			EntityReference reference = EntityReference.match(matcher.group(1));
			if(reference != null)
				return new PlayerPermissionEvaluation(reference, matcher.group(2));
			return null;
		}
	}
}
