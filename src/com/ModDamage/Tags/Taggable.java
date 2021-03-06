package com.ModDamage.Tags;


import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import com.ModDamage.ModDamage;
import com.ModDamage.PluginConfiguration.OutputPreset;
import com.ModDamage.Backend.BailException;
import com.ModDamage.EventInfo.EventData;
import com.ModDamage.EventInfo.EventInfo;
import com.ModDamage.Parsing.DataProvider;
import com.ModDamage.Parsing.IDataProvider;

public abstract class Taggable<T> {
    public final IDataProvider<T> inner;

    protected Taggable(IDataProvider<T> inner) {
        this.inner = inner;
    }

    @SuppressWarnings("rawtypes")
    public static Taggable<?> get(IDataProvider dp, EventInfo info) {
        if (dp == null) return null;

        {
            IDataProvider<World> worldDP = DataProvider.transform(World.class, dp, info, false);
            if (worldDP != null) {
                return new Taggable<World>(worldDP) {
                    @Override
                    protected <D> ITags<D, World> getTags(TagsHolder<D> holder) {
                        return holder.onWorld;
                    }
                };
            }
        }

        {
            IDataProvider<OfflinePlayer> playerDP = DataProvider.transform(OfflinePlayer.class, dp, info, false);
            if (playerDP != null) {
                return new Taggable<OfflinePlayer>(playerDP) {
                    @Override
                    protected <D> ITags<D, OfflinePlayer> getTags(TagsHolder<D> holder) {
                        return holder.onPlayer;
                    }
                };
            }
        }

        {
            IDataProvider<Entity> entityDP = DataProvider.transform(Entity.class, dp, info, false);
            if (entityDP != null) {
                return new Taggable<Entity>(entityDP) {
                    @Override
                    protected <D> ITags<D, Entity> getTags(TagsHolder<D> holder) {
                        return holder.onEntity;
                    }
                };
            }
        }

        {
            IDataProvider<Location> locDP = DataProvider.transform(Location.class, dp, info, false);
            if (locDP != null) {
                return new Taggable<Location>(locDP) {
                    @Override
                    protected <D> ITags<D, Location> getTags(TagsHolder<D> holder) {
                        return holder.onLocation;
                    }
                };
            }
        }

        {
            IDataProvider<Chunk> chunkDP = DataProvider.transform(Chunk.class, dp, info, false);
            if (chunkDP != null) {
                return new Taggable<Chunk>(chunkDP) {
                    @Override
                    protected <D> ITags<D, Chunk> getTags(TagsHolder<D> holder) {
                        return holder.onChunk;
                    }
                };
            }
        }

        ModDamage.addToLogRecord(OutputPreset.FAILURE, dp.provides().getSimpleName() + " is not a taggable type: "+dp);
        return null;
    }
    
    public <D> D get(Tag<D> tag, EventData data) throws BailException {
    	T innerval = inner.get(data);
    	if (innerval == null) return null;
        return getTags(tag.getHolder(ModDamage.getTagger())).getTagValue(innerval, tag.getName(data));
    }

    public <D> void set(Tag<D> tag, EventData data, D value) throws BailException {
    	T innerval = inner.get(data);
    	if (innerval == null) return;
        getTags(tag.getHolder(ModDamage.getTagger())).addTag(inner.get(data), tag.getName(data), value);
    }

    public boolean has(Tag<?> tag, EventData data) throws BailException {
    	T innerval = inner.get(data);
    	if (innerval == null) return false;
        return getTags(tag.getHolder(ModDamage.getTagger())).isTagged(inner.get(data), tag.getName(data));
    }

    public void remove(Tag<?> tag, EventData data) throws BailException {
    	T innerval = inner.get(data);
    	if (innerval == null) return;
        getTags(tag.getHolder(ModDamage.getTagger())).removeTag(inner.get(data), tag.getName(data));
    }

    // Avoid double getting inner in some cases
    public <D> D get(Tag<D> tag, T obj, EventData data) throws BailException {
    	if (obj == null) return null;
        return getTags(tag.getHolder(ModDamage.getTagger())).getTagValue(obj, tag.getName(data));
    }

    public <D> void set(Tag<D> tag, T obj, EventData data, D value) throws BailException {
    	if (obj == null) return;
        getTags(tag.getHolder(ModDamage.getTagger())).addTag(obj, tag.getName(data), value);
    }

    public boolean has(Tag<?> tag, T obj, EventData data) throws BailException {
    	if (obj == null) return false;
        return getTags(tag.getHolder(ModDamage.getTagger())).isTagged(obj, tag.getName(data));
    }

    public void remove(Tag<?> tag, T obj, EventData data) throws BailException {
    	if (obj == null) return;
        getTags(tag.getHolder(ModDamage.getTagger())).removeTag(obj, tag.getName(data));
    }

    protected abstract <D> ITags<D, T> getTags(TagsHolder<D> holder);

    public String toString() {
        return inner.toString();

        //return "("+ inner.provides().getSimpleName() + ")["+ inner.getClass().getSimpleName() +"]" + inner.toString();
    }
}
