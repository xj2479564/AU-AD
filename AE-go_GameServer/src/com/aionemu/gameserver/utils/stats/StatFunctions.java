/*
 * This file is part of aion-unique <aion-unique.smfnew.com>.
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
package com.aionemu.gameserver.utils.stats;

import org.apache.log4j.Logger;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.Config;
import com.aionemu.gameserver.model.SkillElement;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Inventory;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.gameobjects.stats.CreatureGameStats;
import com.aionemu.gameserver.model.gameobjects.stats.StatEnum;
import com.aionemu.gameserver.model.templates.item.WeaponType;
import com.aionemu.gameserver.model.templates.stats.NpcRank;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author ATracer
 * @author alexa026
 */
public class StatFunctions
{
	private static Logger log = Logger.getLogger(StatFunctions.class);

	/**
	 * 
	 * @param player
	 * @param target
	 * @return XP reward from target
	 */
	public static long calculateSoloExperienceReward(Player player, Creature target)
	{
		int playerLevel = player.getCommonData().getLevel();
		int targetLevel = target.getLevel();

		//TODO take baseXP from target object (additional attribute in stats template is needed)
		int baseXP = targetLevel * 80;

		int xpPercentage =  XPRewardEnum.xpRewardFrom(targetLevel - playerLevel);

		return (int) Math.floor(baseXP * xpPercentage * Config.XP_RATE / 100);
	}

	public static long calculateGroupExperienceReward(Player player, Creature target)
	{
		int playerLevel = player.getCommonData().getLevel();
		int targetLevel = target.getLevel();

		//TODO take baseXP from target object (additional attribute in stats template is needed)
		int baseXP = targetLevel * 90; //promotion the group

		int xpPercentage =  XPRewardEnum.xpRewardFrom(targetLevel - playerLevel);

		return (int) Math.floor(baseXP * xpPercentage * Config.GROUPXP_RATE / 100);
	}

	/**
	 * ref: http://www.aionsource.com/forum/mechanic-analysis/42597-character-stats-xp-dp-origin-gerbator-team-july-2009-a.html
	 * 
	 * @param player
	 * @param target
	 * @return DP reward from target
	 */

	public static int calculateSoloDPReward(Player player, Creature target) 
	{
		int playerLevel = player.getCommonData().getLevel();
		int targetLevel = target.getLevel();
		NpcRank npcRank = ((Npc) target).getTemplate().getRank();								

		//TODO: fix to see monster Rank level, NORMAL lvl 1, 2 | ELITE lvl 1, 2 etc..
		//look at: http://www.aionsource.com/forum/mechanic-analysis/42597-character-stats-xp-dp-origin-gerbator-team-july-2009-a.html
		int baseDP = targetLevel * calculateRankMultipler(npcRank);

		int xpPercentage =  XPRewardEnum.xpRewardFrom(targetLevel - playerLevel);
		return (int) Math.floor(baseDP * xpPercentage * Config.XP_RATE / 100);

	}

	public static int calculateGroupDPReward(Player player, Creature target)
	{
		int playerLevel = player.getCommonData().getLevel();
		int targetLevel = target.getLevel();
		NpcRank npcRank = ((Npc) target).getTemplate().getRank();								

		//TODO: fix to see monster Rank level, NORMAL lvl 1, 2 | ELITE lvl 1, 2 etc..
		int baseDP = targetLevel * calculateRankMultipler(npcRank);

		int xpPercentage =  XPRewardEnum.xpRewardFrom(targetLevel - playerLevel);

		return (int) Math.floor(baseDP * xpPercentage * Config.GROUPXP_RATE / 100);
	}	

	/**
	 * 
	 * @param player
	 * @param target
	 * @return Damage made to target (-hp value)
	 */
	public static int calculateBaseDamageToTarget(Creature attacker, Creature target)
	{
		return calculatePhysicDamageToTarget(attacker, target, 0);
	}

	/**
	 * 
	 * @param player
	 * @param target
	 * @param skillDamages
	 * @return Damage made to target (-hp value)
	 */
	public static int calculatePhysicDamageToTarget(Creature attacker, Creature target, int skillDamages)
	{
		CreatureGameStats<?> ags = attacker.getGameStats();
		CreatureGameStats<?> tgs = target.getGameStats();

		log.debug("Calculating base damages (skill base damages: "+skillDamages+") ...");
		log.debug("| Attacker: "+ags);
		log.debug("| Target  : "+tgs);

		int Damage = 0;

		if (attacker instanceof Player)
		{
			int totalMin = ags.getCurrentStat(StatEnum.MIN_DAMAGES);
			int totalMax = ags.getCurrentStat(StatEnum.MAX_DAMAGES);
			int average = Math.round((totalMin + totalMax)/2);
			int mainHandAttack = ags.getCurrentStat(StatEnum.MAIN_HAND_POWER);

			Inventory inventory = ((Player)attacker).getInventory();

			WeaponType weaponType = inventory.getMainHandWeaponType();

			if(weaponType != null)
			{

				switch(weaponType)
				{
					case ORB_2H:
					case BOOK_2H:
						break;

					default:
						mainHandAttack -= 100;
						break;
				}

				int min = Math.round((((mainHandAttack * 100)/ average) * totalMin)/100);
				int max = Math.round((((mainHandAttack * 100)/ average) * totalMax)/100);

				if(skillDamages != 0)
				{
					int base = Rnd.get(min,max);
					Damage = base + Math.round(skillDamages * (ags.getCurrentStat(StatEnum.POWER)/100f));
				}
				else
				{
					int base = Rnd.get(min,max);
					Damage = Math.round(base + (ags.getBaseStat(StatEnum.MAIN_HAND_POWER)/ 2 + ags.getBaseStat(StatEnum.OFF_HAND_POWER)/ 2)/10f)
					+ ags.getStatBonus(StatEnum.MAIN_HAND_POWER) + ags.getStatBonus(StatEnum.OFF_HAND_POWER);
				}

			}
			else   //if hand attack
			{
				int base = Rnd.get(16,20);
				Damage = Math.round(base + (ags.getBaseStat(StatEnum.MAIN_HAND_POWER)/ 2 + ags.getBaseStat(StatEnum.OFF_HAND_POWER)/ 2)/10f)
				+ ags.getStatBonus(StatEnum.MAIN_HAND_POWER) + ags.getStatBonus(StatEnum.OFF_HAND_POWER);
			}

			//adjusting baseDamages according to attacker and target level
			//
			Damage = adjustDamages(attacker, target, Damage);

			if(attacker.isInState(CreatureState.POWERSHARD))
			{
				Item mainHandPowerShard = ((Player)attacker).getInventory().getMainHandPowerShard();
				if(mainHandPowerShard != null)
				{
					Damage += mainHandPowerShard.getItemTemplate().getWeaponBoost();

					((Player)attacker).getInventory().usePowerShard(mainHandPowerShard, 1);
				}
			}
		}
		else
		{
			NpcRank npcRank = ((Npc) attacker).getTemplate().getRank();
			int multipler = calculateRankMultipler(npcRank);
			Damage += ags.getCurrentStat(StatEnum.MAIN_HAND_POWER);
			Damage = (Damage * multipler) + ((Damage*attacker.getLevel())/10);
		}

		Damage -= Math.round(tgs.getCurrentStat(StatEnum.PHYSICAL_DEFENSE) * 0.10f);

		if (Damage<=0)
			Damage=1;

		log.debug("|=> Damages calculation result: damages("+Damage+")");

		return Damage;
	}

	public static int calculateOffHandPhysicDamageToTarget(Creature attacker, Creature target)
	{
		CreatureGameStats<?> ags = attacker.getGameStats();
		CreatureGameStats<?> tgs = target.getGameStats();

		int totalMin = ags.getCurrentStat(StatEnum.MIN_DAMAGES);
		int totalMax = ags.getCurrentStat(StatEnum.MAX_DAMAGES);
		int average = Math.round((totalMin + totalMax)/2);
		int offHandAttack = ags.getCurrentStat(StatEnum.OFF_HAND_POWER);

		Inventory inventory = ((Player)attacker).getInventory();

		WeaponType weaponType = inventory.getOffHandWeaponType();

		switch(weaponType)
		{
			case ORB_2H:
			case BOOK_2H:
				break;

			default:
				offHandAttack -= 100;
				break;
		}

		int Damage = 0;
		int min = Math.round((((offHandAttack * 100)/ average) * totalMin)/100);
		int max = Math.round((((offHandAttack * 100)/ average) * totalMax)/100);

		int base = Rnd.get(min,max);
		Damage = Math.round(base + (ags.getBaseStat(StatEnum.MAIN_HAND_POWER)/ 2 + ags.getBaseStat(StatEnum.OFF_HAND_POWER)/ 2)/10)
		+ ags.getStatBonus(StatEnum.MAIN_HAND_POWER) + ags.getStatBonus(StatEnum.OFF_HAND_POWER);

		Damage = adjustDamages(attacker, target, Damage);

		if(attacker.isInState(CreatureState.POWERSHARD))
		{
			Item offHandPowerShard = ((Player)attacker).getInventory().getOffHandPowerShard();
			if(offHandPowerShard != null)
			{
				Damage += offHandPowerShard.getItemTemplate().getWeaponBoost();
				((Player)attacker).getInventory().usePowerShard(offHandPowerShard, 1);
			}
		}

		Damage -= Math.round(tgs.getCurrentStat(StatEnum.PHYSICAL_DEFENSE) * 0.10f);

		if (Damage<=0)
			Damage=1;

		return Damage;
	}


	/**
	 * @param player
	 * @param target
	 * @param skillEffectTemplate
	 * @return HP damage to target
	 */
	public static int calculateMagicDamageToTarget(Creature speller, Creature target, int baseDamages, SkillElement element)
	{
		CreatureGameStats<?> sgs = speller.getGameStats();
		CreatureGameStats<?> tgs = target.getGameStats();
		log.debug("Calculating magic damages between "+speller.getObjectId()+" and "+target.getObjectId()+" (skill base damages: "+baseDamages+")...");
		log.debug("| Speller : "+sgs);
		log.debug("| Target  : "+tgs);

		// not yet used for now
		//int magicalResistance = tgs.getCurrentStat(StatEnum.MAGICAL_RESIST);

		//adjusting baseDamages according to attacker and target level
		//
		baseDamages = adjustDamages(speller, target, baseDamages);

		// element resist: fire, wind, water, eath
		//
		// 10 elemental resist ~ 1% reduce of magical baseDamages
		//
		int elementaryDefenseBase = tgs.getMagicalDefenseFor(element);
		int elementaryDefense = Math.round( (elementaryDefenseBase / 1000f) * baseDamages);
		baseDamages -= elementaryDefense;

		// then, add magical attack bonus..

		// TODO: fix this to the correct Magical Attack formula
		int magicAttBase = sgs.getCurrentStat(StatEnum.MAGICAL_ATTACK);
		baseDamages += magicAttBase;

		// magicBoost formula
		// i think after researching in several forums, this is correct formula for magicBoost
		//
		int magicBoostBase = sgs.getCurrentStat(StatEnum.BOOST_MAGICAL_SKILL);
		int magicBoost = Math.round(baseDamages * ((magicBoostBase / 12f) / 100f));

		int damages = baseDamages + magicBoost;

		// IMPORTANT NOTES
		//
		// magicalResistance supposed to be counted to EVADE magic, not to reduce damage, only the elementaryDefense it's counted to reduce magic attack
		//
		//     so example if 200 magic resist vs 100 magic accuracy, 200 - 100 = 100/10 = 0.10 or 10% chance of EVADE
		//
		// damages -= Math.round((elementaryDefense+magicalResistance)*0.60f);


		if (damages<=0) {
			damages=1;
		}
		log.debug("|=> Magic damages calculation result: damages("+damages+")");
		return damages;
	}

	public static int calculateRankMultipler(NpcRank npcRank)
	{
		//FIXME: to correct formula, have any reference?
		int multipler;
		switch(npcRank) 
		{
			case JUNK: 
				multipler = 1;
				break;
			case NORMAL: 
				multipler = 2;
				break;
			case ELITE:
				multipler = 3;
				break;
			case HERO: 
				multipler = 4;
				break;
			case LEGENDARY: 
				multipler = 5;
				break;
			default: 
				multipler = 1;
		}

		return multipler;
	}

	/**
	 * adjust baseDamages according to their level || is PVP?
	 *
	 * @ref:
	 *
	 * @param attacker lvl
	 * @param target lvl
	 * @param baseDamages
	 *
	 **/
	public static int adjustDamages(Creature attacker, Creature target, int Damages) {

		int attackerLevel = attacker.getLevel();
		int targetLevel = target.getLevel();
		int baseDamages = Damages;

		//fix this for better monster target condition please
		if ( (attacker instanceof Player) && !(target instanceof Player)) {

			if(targetLevel > attackerLevel) {

				float multipler = 0.0f;
				int differ = (targetLevel - attackerLevel);

				if( differ <= 2 ) {
					return baseDamages;
				}
				else if( differ > 2 && differ < 10 ) {
					multipler = (differ - 2f) / 10f;
					baseDamages -= Math.round((baseDamages * multipler));
				}

				else {
					baseDamages -= Math.round((baseDamages * 0.80f));
				}

				return baseDamages;
			}
		} //end of damage to monster

		//PVP damages is capped of 60% of the actual baseDamage
		else if( (attacker instanceof Player) && (target instanceof Player) ) {
			baseDamages = Math.round(baseDamages * 0.60f);
			return baseDamages;
		}

		return baseDamages;

	}

	public static int calculatePhysicalDodgeRate( Creature attacker, Creature attacked )
	{
		int dodgeRate = ( attacked.getGameStats().getCurrentStat(StatEnum.EVASION) - attacker.getGameStats().getCurrentStat(StatEnum.ACCURACY) ) / 10;
		//maximal dodge rate
		if( dodgeRate > 30)
			dodgeRate = 30;

		return dodgeRate;
	}

	public static int calculatePhysicalParryRate( Creature attacker, Creature attacked )
	{
		int parryRate = ( attacked.getGameStats().getCurrentStat(StatEnum.PARRY) - attacker.getGameStats().getCurrentStat(StatEnum.ACCURACY) ) / 10;
		//maximal parry rate
		if( parryRate > 40)
			parryRate = 40;

		return parryRate;
	}

	public static int calculatePhysicalBlockRate( Creature attacker, Creature attacked )
	{
		int blockRate = ( attacked.getGameStats().getCurrentStat(StatEnum.BLOCK) - attacker.getGameStats().getCurrentStat(StatEnum.ACCURACY) ) / 10;
		//maximal block rate
		if( blockRate > 50 )
			blockRate = 50;

		return blockRate;
	}

	public static double calculatePhysicalCriticalRate( Creature attacker )
	{
		double criticalRate = 75d * Math.sin( ( ( 900 - attacker.getGameStats().getCurrentStat(StatEnum.PHYSICAL_CRITICAL) ) / 1800 ) * Math.PI );
		//minimal critical rate
		if(criticalRate < 0.1d)
			criticalRate = 0.1d;

		return criticalRate * 100d;
	}

	public static int calculateMagicalResistRate( Creature attacker, Creature attacked )
	{
		int resistRate = Math.round((attacked.getGameStats().getCurrentStat(StatEnum.MAGICAL_RESIST) - attacker.getGameStats().getCurrentStat(StatEnum.MAGICAL_ACCURACY)) / 10);

		return resistRate;
	}

}
