package com.ModDamage.Routines.Nested.Conditionals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;

import com.ModDamage.Backend.TargetEventInfo;

public class ServerOnlineMode extends Conditional
{
	public static final Pattern pattern = Pattern.compile("server\\.onlineMode", Pattern.CASE_INSENSITIVE);
	protected ServerOnlineMode()
	{
	}
	
	@Override
	public boolean evaluate(TargetEventInfo eventInfo){ return Bukkit.getOnlineMode();}
	
	public static void register()
	{
		Conditional.register(new ConditionalBuilder());
	}
	
	protected static class ConditionalBuilder extends Conditional.SimpleConditionalBuilder
	{
		public ConditionalBuilder() { super(pattern); }

		@Override
		public ServerOnlineMode getNew(Matcher matcher)
		{
			return new ServerOnlineMode();
		}
	}
}
