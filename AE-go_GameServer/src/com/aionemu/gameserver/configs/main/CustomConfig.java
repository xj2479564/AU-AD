/*
 * This file is part of aion-unique <aion-unique.org>.
 *
 *  aion-unique is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-unique is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

public class CustomConfig
{
	/**
	 * Factions speaking mode
	 */
	@Property(key = "gameserver.factions.speaking.mode", defaultValue = "0")
	public static int		FACTIONS_SPEAKING_MODE;

	/**
	 * Skill autolearn
	 */
	@Property(key = "gameserver.skill.autolearn", defaultValue = "false")
	public static boolean	SKILL_AUTOLEARN;

	/**
	 * Stigma autolearn
	 */
	@Property(key = "gameserver.stigma.autolearn", defaultValue = "false")
	public static boolean	STIGMA_AUTOLEARN;

	/**
	 * Disable monsters aggressive behave
	 */
	@Property(key = "gameserver.disable.mob.aggro", defaultValue = "false")
	public static boolean	DISABLE_MOB_AGGRO;

	/**
	 * Enable 2nd class change simple mode
	 */
	@Property(key = "gameserver.enable.simple.2ndclass", defaultValue = "false")
	public static boolean	ENABLE_SIMPLE_2NDCLASS;

	/**
	 * Unstuck delay
	 */
	@Property(key = "gameserver.unstuck.delay", defaultValue = "3600")
	public static int		UNSTUCK_DELAY;

	/**
	 * Enable instances
	 */
	@Property(key = "gameserver.instances.enable", defaultValue = "true")
	public static boolean	ENABLE_INSTANCES;
	
	/**
	 * Base Fly Time
	 */
	@Property(key = "gameserver.base.flytime", defaultValue = "60")
	public static int		BASE_FLYTIME;

    /**
	* Allows players of opposite factions to bind in enemy territory
	*/
	@Property(key = "gameserver.cross.faction.binding", defaultValue = "false")
	public static boolean				ENABLE_CROSS_FACTION_BINDING;

	/**
	 * Fly damage activator
	 */
	@Property(key = "gameserver.fall.damage.active", defaultValue = "true")
	public static boolean	ACTIVE_FALL_DAMAGE;

	/**
	 * Percentage of damage per meter.
	 */
	@Property(key = "gameserver.fall.damage.percentage", defaultValue = "1.0")
	public static float		FALL_DAMAGE_PERCENTAGE;

	/**
	 * Minimum fall damage range
	 */
	@Property(key = "gameserver.fall.damage.distance.minimum", defaultValue = "10")
	public static int		MINIMUM_DISTANCE_DAMAGE;

	/**
	 * Maximum fall distance after which you will die after hitting the ground.
	 */
	@Property(key = "gameserver.fall.damage.distance.maximum", defaultValue = "50")
	public static int		MAXIMUM_DISTANCE_DAMAGE;

	/**
	 * Maximum fall distance after which you will die in mid air.
	 */
	@Property(key = "gameserver.fall.damage.distance.midair", defaultValue = "200")
	public static int		MAXIMUM_DISTANCE_MIDAIR;

	/**
	 * ManaStone Rates
	 */
	@Property(key = "gameserver.manastone.percent", defaultValue = "57")
	public static int		MSPERCENT;
	@Property(key = "gameserver.manastone.percent1", defaultValue = "43")
	public static int		MSPERCENT1;	
	@Property(key = "gameserver.manastone.percent2", defaultValue = "33")
	public static int		MSPERCENT2;	
	@Property(key = "gameserver.manastone.percent3", defaultValue = "25")
	public static int		MSPERCENT3;	
	@Property(key = "gameserver.manastone.percent4", defaultValue = "19")
	public static int		MSPERCENT4;	
	@Property(key = "gameserver.manastone.percent5", defaultValue = "2")
	public static int		MSPERCENT5;	
	
	/**
	 * Disable chat server connection
	 */
	@Property(key = "gameserver.disable.chatserver", defaultValue = "true")
	public static boolean	DISABLE_CHAT_SERVER;
	
	@Property(key = "gameserver.locale", defaultValue = "en")
	public static String 	LOCALE;
}
