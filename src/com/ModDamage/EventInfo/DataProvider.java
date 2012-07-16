package com.ModDamage.EventInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ModDamage.ModDamage;
import com.ModDamage.PluginConfiguration.OutputPreset;
import com.ModDamage.StringMatcher;
import com.ModDamage.Backend.BailException;

public abstract class DataProvider<T, S> implements IDataProvider<T>
{
	public interface IDataParser<T>
	{
		IDataProvider<T> parse(EventInfo info, IDataProvider<?> startDP, Matcher m, StringMatcher sm);
	}
	
	protected final IDataProvider<S> startDP;
	protected final Class<S> wantStart;
	
	@SuppressWarnings("unchecked")
	protected DataProvider(Class<S> wantStart, IDataProvider<?> startDP)
	{
		this.wantStart = wantStart;
		this.startDP = (IDataProvider<S>) startDP;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public final T get(EventData data) throws BailException
	{
		Object ostart = startDP.get(data);
		if (ostart != null && wantStart.isInstance(ostart))
			return get((S) ostart, data);
		
		return null;
	}
	public abstract T get(S start, EventData data) throws BailException;

	public abstract Class<T> provides();
	
	
	
	static class ParserData<T>
	{
		final Class<T> provides;
		final Class<?> wants;
		final Pattern pattern;
		final IDataParser<T> parser;
		
		final int numGroups;
		int compiledGroup;
		
		public ParserData(Class<T> provides, Class<?> wants, Pattern pattern, IDataParser<T> parser)
		{
			this.provides = provides;
			this.wants = wants;
			this.pattern = pattern;
			this.parser = parser;
			
			numGroups = pattern == null? 0 : pattern.matcher("").groupCount();
		}
	}
	
	
	
	@SuppressWarnings("serial")
	static class Parsers extends ArrayList<ParserData<?>>
	{
		Pattern compiledPattern;
	}
	
	
	
	static Map<Class<?>, Parsers> parsersByStart = new HashMap<Class<?>, Parsers>();
	static Map<Class<?>, ArrayList<ParserData<?>>> transformersByStart = new HashMap<Class<?>, ArrayList<ParserData<?>>>();
	
	public static <T> void register(Class<T> provides, Class<?> wants, Pattern pattern, IDataParser<T> parser)
	{
		Parsers parserList = parsersByStart.get(wants);
		if (parserList == null)
		{
			parserList = new Parsers();
			parsersByStart.put(wants, parserList);
		}
		
		parserList.add(new ParserData<T>(provides, wants, pattern, parser));
	}
	
	public static <T> void registerTransformer(Class<T> provides, Class<?> wants, IDataParser<T> parser)
	{
		ArrayList<ParserData<?>> transformersList = transformersByStart.get(wants);
		if (transformersList == null)
		{
			transformersList = new Parsers();
			transformersByStart.put(wants, transformersList);
		}
		
		transformersList.add(new ParserData<T>(provides, wants, null, parser));
	}
	
	public static void clear()
	{
		parsersByStart.clear();
	}
	
	public static void compile()
	{
		for (Entry<Class<?>, Parsers> parsersEntry : parsersByStart.entrySet())
		{
			Parsers parsers = parsersEntry.getValue();
			StringBuilder sb = new StringBuilder("^(?:");
			
			int currentGroup = 1;
			
			boolean first = true;
			for (ParserData<?> parserData : parsers)
			{
				if (first) first = false;
				else sb.append("|");
				
				sb.append("(");
				
				sb.append(parserData.pattern.pattern());
				
				sb.append(")");
				
				parserData.compiledGroup = currentGroup;
				currentGroup += 1 + parserData.numGroups;
			}
			
			sb.append(")");
			parsers.compiledPattern = Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
			
			int groupCount = parsers.compiledPattern.matcher("").groupCount();
			assert (groupCount == currentGroup - 1);
		}
	}
	

	public static <T> IDataProvider<T> parse(EventInfo info, Class<T> cls, String s)
	{
		return parse(info, cls, new StringMatcher(s), true);
	}

	public static <T> IDataProvider<T> parse(EventInfo info, Class<T> cls, StringMatcher sm)
	{
		return parse(info, cls, sm, false);
	}
	@SuppressWarnings("unchecked")
	public static <T> IDataProvider<T> parse(EventInfo info, Class<T> cls, StringMatcher sm, boolean finish)
	{
		String startString = sm.string;
		
		IDataProvider<?> dp = null;
		IDataProvider<T> tdp = null;
		
		// Match EventInfo names first, since they are the most common start
		Map<String, Class<?>> infoMap = info.getAllNames();
		for (Entry<String, Class<?>> entry : infoMap.entrySet())
		{
			if (sm.string.length() < entry.getKey().length()) continue;
			
			String substr = sm.string.substring(0, entry.getKey().length());
			if (substr.equalsIgnoreCase(entry.getKey()))
			{
				StringMatcher sm2 = sm.spawn();
				sm2.matchFront(substr);
				dp = parseHelper(info, cls, info.mget(entry.getValue(), substr), sm2);
				tdp = maybeTransform(info, cls, dp, sm2, finish);
				if (tdp != null)
				{
					sm2.accept();
					sm.accept();
					return tdp;
				}
			}
		}
		
		IDataProvider<?> dp2 = parseHelper(info, cls, null, sm);
		tdp = maybeTransform(info, cls, dp2, sm, finish);
		if (tdp != null)
		{
			sm.accept();
			return (IDataProvider<T>) dp2;
		}
		
		if (dp == null) dp = dp2;
		
		Class<?> dpProvides = dp == null? null : dp.provides();
		String provName = dpProvides == null? "null" : dpProvides.getSimpleName();
		
		String error = "Unable to parse \""+startString+"\"";
		if (sm.isEmpty())
			error += ": wanted "+cls.getSimpleName()+", got "+provName;
		else
			error += " at "+provName+" \""+sm.string+"\"";
		
		ModDamage.addToLogRecord(OutputPreset.FAILURE, error);
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> IDataProvider<T> maybeTransform(EventInfo info, Class<T> cls, IDataProvider<?> dp, StringMatcher sm, boolean finish)
	{
		if (dp == null) return null;
		if (finish && !sm.isEmpty()) return null;
		
		if (cls == null) return (IDataProvider<T>) dp;
		
		if (cls.isAssignableFrom(dp.provides()))
			return (IDataProvider<T>) dp;
		
		// dp doesn't match the required cls, look for any transformers that may convert it to the correct class
		
		Class<?> dpProvides = dp.provides();
		
		for (Entry<Class<?>, ArrayList<ParserData<?>>> entry : transformersByStart.entrySet())
		{
			Class<?> ecls = entry.getKey();
			if (classesMatch(ecls, dpProvides))
			{
				for (ParserData<?> parserData : entry.getValue())
				{
					if (parserData.provides != cls) continue;
					
					IDataProvider<T> provider = (IDataProvider<T>) parserData.parser.parse(info, dp, null, sm.spawn());
					if (provider != null) {
						sm.accept();
						return provider;
					}
				}
			}
		}
		
		return null;
	}
	
	private static boolean classesMatch(Class<?> cls1, Class<?> cls2)
	{
		if (cls1 == cls2) return true;
		if (cls1 == null || cls2 == null) return false;
		
		return cls1.isAssignableFrom(cls2) || cls2.isAssignableFrom(cls1);
	}
	
	
	private static IDataProvider<?> parseHelper(EventInfo info, Class<?> cls, IDataProvider<?> dp, StringMatcher sm)
	{
		/*if (dp == null)
		{
			Parsers parsers = parsersByStart.get(null);
			if (parsers == null) return null;
			IDataProvider<?> end = tryParsers(info, dp, sm.spawn(), parsers);
			if (end != null)
			{
				sm.accept();
				return end;
			}
			
			return null;
		}*/
		
		outerLoop: while (!sm.isEmpty())
		{
			Class<?> dpProvides = dp == null? null : dp.provides();
			
			for (Entry<Class<?>, Parsers> entry : parsersByStart.entrySet())
			{
				Class<?> ecls = entry.getKey();
				
				if (classesMatch(ecls, dpProvides))
				{
					IDataProvider<?> dp2 = tryParsers(info, dp, sm.spawn(), entry.getValue());
					if (dp2 != null)
					{
						dp = dp2;
						continue outerLoop;
					}
				}
			}
			
			break;
		}
		
		sm.accept();
		return dp;
	}
	
	private static IDataProvider<?> tryParsers(EventInfo info, IDataProvider<?> dp, StringMatcher sm, Parsers parserList)
	{
		Matcher m = parserList.compiledPattern.matcher(sm.string);
		if (!m.lookingAt())
			return null;
		
		for (ParserData<?> parserData : parserList)
		{
			if (m.group(parserData.compiledGroup) == null) 
				continue;
			
			StringMatcher sm2 = sm.spawn();
			Matcher m2 = sm2.matchFront(parserData.pattern);
			if (m2 == null) {
				ModDamage.addToLogRecord(OutputPreset.FAILURE, "Matched group failed to match??");
				continue;
			}
			
			IDataProvider<?> provider = parserData.parser.parse(info, dp, m2, sm2.spawn());
			if (provider != null) {
				sm2.accept();
				sm.accept();
				return provider;
				/*IDataProvider<T> end = parseHelper(info, cls, provider, sm2.spawn());
				Class<?> endProvides = end.provides();
				if (end != null && (cls == null || cls.isAssignableFrom(endProvides) || endProvides.isAssignableFrom(cls)))
				{
					sm2.accept();
					sm.accept();
					return end;
				}*/
			}
		}
		/*for (ParserData<?> parserData : parserList)
		{
			StringMatcher sm2 = sm.spawn();
			Matcher m = sm.matchFront(parserData.pattern);
			if (m == null) continue;
			IDataProvider<?> provider = parserData.parser.parse(info, dp, m, sm2.spawn());
			if (provider != null) {
				IDataProvider<T> end = parseHelper(info, cls, provider, sm2.spawn());
				Class<?> endProvides = end.provides();
				if (end != null && (cls == null || cls.isAssignableFrom(endProvides) || endProvides.isAssignableFrom(cls)))
				{
					sm2.accept();
					sm.accept();
					return end;
				}
			}
		}*/
		return null;
	}
	
}