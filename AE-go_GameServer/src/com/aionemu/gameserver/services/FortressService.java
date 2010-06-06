package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.ai.desires.impl.AggressionDesire;
import com.aionemu.gameserver.ai.npcai.AggressiveAi;
import com.aionemu.gameserver.ai.state.AIState;
import com.aionemu.gameserver.dao.FortressDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.TribeRelationsData;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.FortressGeneral;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Monster;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.group.PlayerGroup;
import com.aionemu.gameserver.model.templates.spawn.NpcSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawn.SpawnTemplate;
import com.aionemu.gameserver.model.templates.tribe.AggroRelations;
import com.aionemu.gameserver.model.templates.tribe.HostileRelations;
import com.aionemu.gameserver.model.templates.tribe.Tribe;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;
import com.google.inject.Inject;

/*
 * @author Xitanium
 */

public class FortressService 
{
	private static final Logger 	log = Logger.getLogger(FortressService.class);
	@Inject
	private SpawnEngine spawnEngine;
	@Inject
	private World world;
	@Inject
	private TeleportService teleportService;
	@Inject
	private GroupService groupService;
	@Inject
	private ItemService itemService;
	
	public void initialize()
	{
		// spawn individual fortresses
		// 1: Archipel de Soufre
		spawnFortress(1, DAOManager.getDAO(FortressDAO.class).getCurrentFortressOwnerFaction(1));
		// 2: Archipel des Ailes de Siel UP
		/*spawnFortress(2, DAOManager.getDAO(FortressDAO.class).getCurrentFortressOwnerFaction(2));
		// 3: Archipel des Ailes de Siel DOWN
		spawnFortress(3, DAOManager.getDAO(FortressDAO.class).getCurrentFortressOwnerFaction(3));
		// 4: Oeil de Reshanta
		spawnFortress(4, DAOManager.getDAO(FortressDAO.class).getCurrentFortressOwnerFaction(4));
		// 5: Lac Asteria
		spawnFortress(5, DAOManager.getDAO(FortressDAO.class).getCurrentFortressOwnerFaction(5));
		// 6: Eclat oriental de Latesran
		spawnFortress(6, DAOManager.getDAO(FortressDAO.class).getCurrentFortressOwnerFaction(6));
		// 7: Ile de Kysis
		spawnFortress(7, DAOManager.getDAO(FortressDAO.class).getCurrentFortressOwnerFaction(7));
		// 8: Rocher de Krotan
		spawnFortress(8, DAOManager.getDAO(FortressDAO.class).getCurrentFortressOwnerFaction(8));
		// 9: Ruines de Roah
		spawnFortress(9, DAOManager.getDAO(FortressDAO.class).getCurrentFortressOwnerFaction(9));*/
	}
	
	private String getFortressName(int fortressId)
	{
		switch(fortressId)
		{
		case 1: return "Archipel de Soufre";
		case 2: return "Archipel des Ailes de Siel A";
		case 3: return "Archipel des Ailes de Siel B";
		case 4: return "Oeil de Reshanta";
		case 5: return "Lac Asteria";
		case 6: return "Eclat Oriental de Latesran";
		case 7: return "Ile de Kysis";
		case 8: return "Rocher de Krotan";
		case 9: return "Ruines de Roah";
		default: return "Forteresse";
		}
	}
	
	public void spawnFortress(int fortressId, Race ownerFaction)
	{
		log.info("Starting fortress (re)spawn #" + fortressId);
		// remove all spawns from the current fortress instance
		clearFortress(fortressId);
		// get all spawns from DB templates
		ArrayList<NpcSpawnTemplate> spawnTemplates = DAOManager.getDAO(FortressDAO.class).getAllTemplates(fortressId, ownerFaction);
		// spawns npc using the specified race
		for(NpcSpawnTemplate tpl : spawnTemplates)
		{
			SpawnTemplate template = spawnEngine.addNewSpawn(tpl.getMap(), 0, tpl.getNpcTemplateId(), tpl.getX(), tpl.getY(), tpl.getZ(), tpl.getHeading(), 0, 0, false);
			VisibleObject obj = spawnEngine.spawnObject(template, 0);
			DAOManager.getDAO(FortressDAO.class).insertCache(fortressId, obj.getObjectId());
		}
		log.info("Successfully spawned " + spawnTemplates.size() + " " + ownerFaction.name() + " npc for fortress #" + fortressId);
		// send message to world : "Fortress XXXX is now available to attack for Asmodians"
		sendWorldMessage(getFortressName(fortressId), ownerFaction);
	}
	
	private void sendWorldMessage(String fortressName, Race ownedBy)
	{
		Iterator<Player> players = world.getPlayersIterator();
		while(players.hasNext())
		{
			Player dest = players.next();
			if(dest.getCommonData().getRace() != ownedBy)
			{
				String message = "La forteresse ";
				message += fortressName;
				message += " est maintenant conquise par les ";
				if(ownedBy == Race.ELYOS)
				{
					message += "Elyseens";
				}
				else
				{
					message += "Asmodiens";
				}
				message += ". Vous pouvez attaquer cette forteresse des maintenant.";
				PacketSendUtility.sendMessage(dest, message);
			}
			else
			{
				String message = "La forteresse ";
				message += fortressName;
				message += " est maintenant conquise par votre faction.";
				message += ". Vous devez maintenant defendre cette position contre la faction adverse.";
				PacketSendUtility.sendMessage(dest, message);
			}
		}
	}
	
	private void sendPlayersMessage(ArrayList<Player> players, String message)
	{
		for(Player p : players)
		{
			PacketSendUtility.sendMessage(p, message);
		}
	}
	
	public void clearFortress(int fortressId)
	{
		ArrayList<Integer> spawnedCreatures = DAOManager.getDAO(FortressDAO.class).loadCache(fortressId);
		for(Integer creature : spawnedCreatures)
		{
			AionObject obj = world.findAionObject(creature);
			if(obj instanceof Creature)
			{
				Creature c = (Creature)obj;
				c.getController().delete();
			}
		}
		DAOManager.getDAO(FortressDAO.class).clearCache(fortressId);
		log.info("Successfully cleared cache for fortress #" + fortressId);
	}
	
	public void spawnFortressGeneral(int fortressId, Race race)
	{
		NpcSpawnTemplate generalTemplate = DAOManager.getDAO(FortressDAO.class).getGeneralSpawnTemplate(fortressId, race);
		SpawnTemplate tpl = spawnEngine.addNewSpawn(generalTemplate.getMap(), 0, generalTemplate.getNpcTemplateId(), generalTemplate.getX(), generalTemplate.getY(), generalTemplate.getZ(), generalTemplate.getHeading(), 0, 0, true);
		FortressGeneral general = spawnEngine.spawnFortressGeneral(tpl, fortressId);
	}
	
	public void triggerGeneralKilled(final int fortressId, final Player lastAttacker)
	{
		PlayerGroup group = groupService.getGroup(lastAttacker.getObjectId());
		final ArrayList<Player> players = new ArrayList<Player>();
		if(group == null || group.size() < 2)
		{
			players.add(lastAttacker);
		}
		else
		{
			Collection<Player> plCol = group.getMembers();
			for(Player plr : plCol)
			{
				players.add(plr);
			}
		}
		sendPlayersMessage(players, "Vous venez de prendre " + getFortressName(fortressId) + ". Vous serez teleporte a l'entree dans 5 secondes ...");
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				// get group members of lastAttacker
				
				//TODO: set correct exit point location
				portPlayersToExit(players, null);
				doPlayerMedalReward(players);
				DAOManager.getDAO(FortressDAO.class).setFortressOwner(fortressId, lastAttacker.getCommonData().getRace());
				spawnFortress(fortressId, lastAttacker.getCommonData().getRace());
			}
		}, 5000);
	}
	
	public void doPlayerMedalReward(ArrayList<Player> players)
	{
		for(Player p : players)
		{
			itemService.addItem(p, 186000030, 3);
		}
	}
	
	public void portPlayersToExit(ArrayList<Player> players, WorldPosition exitPoint)
	{
		for(Player p : players)
		{
			teleportService.teleportTo(p, exitPoint.getMapId(), exitPoint.getX(), exitPoint.getY(), exitPoint.getZ(), 0);
		}
	}
	
}