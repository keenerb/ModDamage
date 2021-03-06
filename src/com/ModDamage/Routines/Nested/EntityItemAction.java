package com.ModDamage.Routines.Nested;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

import com.ModDamage.ModDamage;
import com.ModDamage.PluginConfiguration.OutputPreset;
import com.ModDamage.Alias.ItemAliaser;
import com.ModDamage.Alias.RoutineAliaser;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.ItemHolder;
import com.ModDamage.Backend.ModDamageItemStack;
import com.ModDamage.EventInfo.EventData;
import com.ModDamage.EventInfo.EventInfo;
import com.ModDamage.EventInfo.SimpleEventInfo;
import com.ModDamage.Expressions.LiteralInteger;
import com.ModDamage.Parsing.DataProvider;
import com.ModDamage.Parsing.IDataProvider;
import com.ModDamage.Routines.Routine;
import com.ModDamage.Routines.Routines;

public class EntityItemAction extends NestedRoutine
{
	public static final Pattern pattern = Pattern.compile("(.*?)(?:effect)?\\.(give|take)Item\\.(.+?)(?:\\.\\s*(.+))?", Pattern.CASE_INSENSITIVE);
	
	protected enum ItemAction
	{
		GIVE
		{
			@Override
			protected void doAction(HumanEntity entity, ItemStack item)
			{
				entity.getInventory().addItem(item);
			}
		},
		TAKE
		{
			@Override
			protected void doAction(HumanEntity entity, ItemStack item)
			{
				entity.getInventory().removeItem(item);
			}
		};

		abstract protected void doAction(HumanEntity player, ItemStack item);
	}
	
	protected final ItemAction action;
	protected final Collection<ModDamageItemStack> items;
	protected final IDataProvider<HumanEntity> humanDP;
	protected final Routines routines;
	protected final IDataProvider<Integer> quantity;

	public EntityItemAction(String configString, IDataProvider<HumanEntity> humanDP, ItemAction action, Collection<ModDamageItemStack> items, IDataProvider<Integer> quantity, Routines routines)
	{
		super(configString);
		this.humanDP = humanDP;
		this.action = action;
		this.items = items;
		this.quantity = quantity;
		this.routines = routines;
	}
	
	@Override
	public void run(EventData data) throws BailException
	{
		HumanEntity entity = humanDP.get(data);

        for(ModDamageItemStack item : items)
            item.update(data);

        int quantity = this.quantity.get(data);

        for (int i = 0; i < quantity; i++)
        {
            for (ModDamageItemStack item : items)
            {
                ItemHolder holder = new ItemHolder(item.toItemStack());

                if (routines != null)
                {
                    // have to copy the enchantments map because it is immutable
                    routines.run(myInfo.makeChainedData(data, holder));
                }

                action.doAction(entity, holder.getItem());
            }
        }
	}
	
	private static final NestedRoutineBuilder nrb = new NestedRoutineBuilder();
	public static void registerRoutine()
	{
		Routine.registerRoutine(pattern, new Routine.RoutineBuilder()
			{
				@Override public Routine getNew(Matcher matcher, EventInfo info)
				{
					return nrb.getNew(matcher, null, info);
				}
			});
	}
	
	public static void registerNested()
	{
		NestedRoutine.registerRoutine(pattern, nrb);
	}
	
	private static final EventInfo myInfo = new SimpleEventInfo(
			ItemHolder.class, 		"item");
	
	protected static class NestedRoutineBuilder extends NestedRoutine.RoutineBuilder
	{
		@Override
		public EntityItemAction getNew(Matcher matcher, Object nestedContent, EventInfo info)
		{
			String name = matcher.group(1).toLowerCase();
            String action = matcher.group(2).toUpperCase();

			IDataProvider<HumanEntity> humanDP = DataProvider.parse(info, HumanEntity.class, name); if (humanDP == null) return null;
			Collection<ModDamageItemStack> items = ItemAliaser.match(matcher.group(3), info);
			if(items != null && !items.isEmpty())
			{
				Routines routines = null;
				if (nestedContent != null)
					routines = RoutineAliaser.parseRoutines(nestedContent, info.chain(myInfo));

				
				IDataProvider<Integer> quantity;
				if (matcher.group(4) != null)
					quantity = DataProvider.parse(info, Integer.class, matcher.group(4));
				else
					quantity = new LiteralInteger(1);

                if (quantity == null) return null;


                ModDamage.addToLogRecord(OutputPreset.INFO, action.charAt(0) + action.substring(1).toLowerCase() + " at/to " + humanDP + ": " + items);
				
				return new EntityItemAction(matcher.group(), humanDP, ItemAction.valueOf(action), items, quantity, routines);
			}
			return null;
		}
	}
}