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
package com.aionemu.gameserver.controllers;

import java.util.List;

import com.aionemu.gameserver.controllers.attack.AttackResult;
import com.aionemu.gameserver.controllers.attack.AttackUtil;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.Summon.SummonMode;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SUMMON_OWNER_REMOVE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SUMMON_PANEL_REMOVE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SUMMON_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 * 
 */
public class SummonController extends CreatureController<Summon>
{

	@Override
	public Summon getOwner()
	{
		return (Summon) super.getOwner();
	}

	/**
	 * Release summon
	 */
	public void release()
	{
		final Summon owner = getOwner();
		final Player master = owner.getMaster();
		final int summonObjId = owner.getObjectId();
		owner.setMode(SummonMode.RELEASE);
		PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.SUMMON_UNSUMMON(getOwner().getNameId()));
		PacketSendUtility.sendPacket(master, new SM_SUMMON_UPDATE(getOwner()));
		
		ThreadPoolManager.getInstance().schedule(new Runnable(){
			
			@Override
			public void run()
			{
				owner.setMaster(null);
				master.setSummon(null);
				owner.getController().delete();
				PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.SUMMON_DISMISSED(getOwner().getNameId()));
				PacketSendUtility.sendPacket(master, new SM_SUMMON_OWNER_REMOVE(summonObjId));
				//TODO temp till found on retail
				PacketSendUtility.sendPacket(master, new SM_SUMMON_PANEL_REMOVE());
			}
		}, 5000);
	}
	
	/**
	 * Change to rest mode
	 */
	public void restMode()
	{
		getOwner().setMode(SummonMode.REST);
		Player master = getOwner().getMaster();
		PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.SUMMON_RESTMODE(getOwner().getNameId()));
		PacketSendUtility.sendPacket(master, new SM_SUMMON_UPDATE(getOwner()));
	}
	
	/**
	 * Change to guard mode
	 */
	public void guardMode()
	{
		getOwner().setMode(SummonMode.GUARD);
		Player master = getOwner().getMaster();
		PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.SUMMON_GUARDMODE(getOwner().getNameId()));
		PacketSendUtility.sendPacket(master, new SM_SUMMON_UPDATE(getOwner()));
	}
	
	public void attackMode()
	{
		getOwner().setMode(SummonMode.ATTACK);
		Player master = getOwner().getMaster();
		PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.SUMMON_ATTACKMODE(getOwner().getNameId()));
		PacketSendUtility.sendPacket(master, new SM_SUMMON_UPDATE(getOwner()));
	}
	
	@Override
	public void onAttack(Creature creature, int damage)
	{
		if(getOwner().getLifeStats().isAlreadyDead())
		{
			return;
		}
		super.onAttack(creature, damage);
		getOwner().getLifeStats().reduceHp(damage, creature);
		PacketSendUtility.broadcastPacket(getOwner().getMaster(), new SM_ATTACK_STATUS(getOwner(), TYPE.REGULAR, 0, damage), true);
		PacketSendUtility.broadcastPacket(getOwner().getMaster(), new SM_SUMMON_UPDATE(getOwner()), true);
	}
			
	@Override
	public void onDie(Creature lastAttacker)
	{
		super.onDie(lastAttacker);
		Player master = getOwner().getMaster();
		int summonObjId = getOwner().getObjectId();
		getOwner().setMaster(null);
		master.setSummon(null);
		getOwner().getController().delete();
		PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.SUMMON_DISMISSED(getOwner().getNameId()));
		PacketSendUtility.sendPacket(master, new SM_SUMMON_OWNER_REMOVE(summonObjId));
		//TODO temp till found on retail
		PacketSendUtility.sendPacket(master, new SM_SUMMON_PANEL_REMOVE());
	}
			
	@Override
	public void attackTarget(Creature target)
	{		
		Player master = getOwner().getMaster();

		super.attackTarget(target);
		
		List<AttackResult> attackResult = AttackUtil.calculateAttackResult(getOwner(), target);

		int damage = 0;
		for(AttackResult result : attackResult)
		{
			damage += result.getDamage();
		}

		long time = System.currentTimeMillis();
		int attackType = 0; // TODO investigate attack types
		PacketSendUtility.broadcastPacket(master, new SM_ATTACK(getOwner(), target, getOwner().getGameStats().getAttackCounter(),
			274, attackType, attackResult), true);

		target.getController().onAttack(getOwner(), damage);

		getOwner().getGameStats().increaseAttackCounter();
		
		PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.SUMMON_ATTACKMODE(getOwner().getNameId()));
		PacketSendUtility.sendPacket(master, new SM_SUMMON_UPDATE(getOwner()));
	}
}