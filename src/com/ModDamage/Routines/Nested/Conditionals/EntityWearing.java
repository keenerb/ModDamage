package com.ModDamage.Routines.Nested.Conditionals;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ModDamage.Backend.ArmorSet;
import com.ModDamage.Backend.EntityReference;
import com.ModDamage.Backend.TargetEventInfo;
import com.ModDamage.Backend.Aliasing.ArmorAliaser;

public class EntityWearing extends Conditional
{
	public static final Pattern pattern = Pattern.compile("(\\w+)\\.wearing(only)?\\.([\\w*]+)", Pattern.CASE_INSENSITIVE);
	final EntityReference entityReference;
	final boolean only;
	final Collection<ArmorSet> armorSets;
	public EntityWearing(EntityReference entityReference, boolean only, Collection<ArmorSet> armorSets)
	{  
		this.entityReference = entityReference;
		this.only = only;
		this.armorSets = armorSets;
	}
	@Override
	public boolean evaluate(TargetEventInfo eventInfo)
	{
		ArmorSet playerSet = entityReference.getArmorSet(eventInfo);
		if(playerSet != null)
			for(ArmorSet armorSet : armorSets)
				if(only? armorSet.equals(playerSet) : armorSet.contains(playerSet))
					return true;
		return false;
	}
	
	public static void register()
	{
		Conditional.register(new ConditionalBuilder());
	}
	
	protected static class ConditionalBuilder extends Conditional.SimpleConditionalBuilder
	{
		public ConditionalBuilder() { super(pattern); }

		@Override
		public EntityWearing getNew(Matcher matcher)
		{
			EntityReference reference = EntityReference.match(matcher.group(1));
			Collection<ArmorSet> armorSet = ArmorAliaser.match(matcher.group(3));
			if(!armorSet.isEmpty() && reference != null)
				return new EntityWearing(reference, matcher.group(2) != null, armorSet);
			return null;
		}
	}
}
