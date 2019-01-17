/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.core.features.registries;

import cpw.mods.fml.common.Loader;

import java.util.HashSet;

import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

import appeng.api.features.IWorldGen;
import java.util.Random;
import rpcore.RPCore;
import rpcore.constants.CelestialClass;
import rpcore.constants.CelestialType;
import rpcore.module.dimension.ForgeDimension;


public final class WorldGenRegistry implements IWorldGen
{

	public static final WorldGenRegistry INSTANCE = new WorldGenRegistry();
	private final TypeSet[] types;

	private WorldGenRegistry()
	{

		this.types = new TypeSet[WorldGenType.values().length];

		for( final WorldGenType type : WorldGenType.values() )
		{
			this.types[type.ordinal()] = new TypeSet();
		}
	}

	@Override
	public void disableWorldGenForProviderID( final WorldGenType type, final Class<? extends WorldProvider> provider )
	{
		if( type == null )
		{
			throw new IllegalArgumentException( "Bad Type Passed" );
		}

		if( provider == null )
		{
			throw new IllegalArgumentException( "Bad Provider Passed" );
		}

		this.types[type.ordinal()].badProviders.add( provider );
	}

	@Override
	public void enableWorldGenForDimension( final WorldGenType type, final int dimensionID )
	{
		if( type == null )
		{
			throw new IllegalArgumentException( "Bad Type Passed" );
		}

		this.types[type.ordinal()].enabledDimensions.add( dimensionID );
	}

	@Override
	public void disableWorldGenForDimension( final WorldGenType type, final int dimensionID )
	{
		if( type == null )
		{
			throw new IllegalArgumentException( "Bad Type Passed" );
		}

		this.types[type.ordinal()].badDimensions.add( dimensionID );
	}

	@Override
	public boolean isWorldGenEnabled( final WorldGenType type, final World w )
	{
		if( type == null )
		{
			throw new IllegalArgumentException( "Bad Type Passed" );
		}

		if( w == null )
		{
			throw new IllegalArgumentException( "Bad Provider Passed" );
		}

		final boolean isBadProvider = this.types[type.ordinal()].badProviders.contains( w.provider.getClass() );
		final boolean isBadDimension = this.types[type.ordinal()].badDimensions.contains( w.provider.dimensionId );
		final boolean isGoodDimension = this.types[type.ordinal()].enabledDimensions.contains( w.provider.dimensionId );

		if( isBadProvider || isBadDimension )
		{
			return false;
		}
                
                
                // STARGATEMC CODE
                
                if (Loader.isModLoaded("RPCore")) {                
                    double chance = 100.0;
                    ForgeDimension d = RPCore.getDimensionRegistry().getForDimensionId(w.provider.dimensionId);
                    if (!d.getType().equals(CelestialType.Landable)) return false; // Prevents spawning unless dimension is a Landable planet/moon.
                    if (d.hasAtmosphere()) chance -= 50.0;
                    if (d.getPos().getCelestialClassesInSystem().contains(CelestialClass.CLASS_BLACKHOLE_STAR)) chance += 50.0;
                    if (d.getPos().getCelestialClassesInSystem().contains(CelestialClass.CLASS_NEUTRON_STAR)) chance += 25.0;
                    if (d.getCelestialClass().equals(CelestialClass.TEMPERATE_WORLD)) chance -= 30.0;
                    if (d.getCelestialClass().equals(CelestialClass.ICY_WORLD)) chance -= 20.0;
                    if (d.getCelestialClass().equals(CelestialClass.DESERT_WORLD)) chance += 20.0;
                    if (d.getCelestialClass().equals(CelestialClass.OCEANIC_WORLD)) chance -= 20.0;
                    if (d.getCelestialClass().equals(CelestialClass.UNSTABLE_WORLD)) chance += 50.0;
                    if (d.getCelestialClass().equals(CelestialClass.ISLAND_WORLD)) chance -= 20.0;
                    Random r = new Random();
                    if (r.nextInt(100) < chance) return true;
                    return false;                    
                }
                /// END STARGATEMC CODE
                
		if( isGoodDimension && type == WorldGenType.Meteorites )
		{
			return false;
		}

		return true;
	}

	private static class TypeSet
	{

		final HashSet<Class<? extends WorldProvider>> badProviders = new HashSet<Class<? extends WorldProvider>>();
		final HashSet<Integer> badDimensions = new HashSet<Integer>();
		final HashSet<Integer> enabledDimensions = new HashSet<Integer>();
	}
}
