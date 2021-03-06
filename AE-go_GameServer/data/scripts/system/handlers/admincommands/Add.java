/*
 * This file is part of aion-unique <aion-unique.com>.
 *
 * aion-unique is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aion-unique is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
 */
package admincommands;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.utils.LocaleManager;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;
import com.google.inject.Inject;

/**
 * @author Phantom, ATracer
 *
 */

public class Add extends AdminCommand
{
	@Inject
	private ItemService itemService;
	@Inject
	private World	world;
	
	public Add()
	{
		super("add");
	}

	@Override
	public void executeCommand(Player admin, String[] params)
	{
		if(admin.getAccessLevel() < AdminConfig.COMMAND_ADD)
		{
			PacketSendUtility.sendMessage(admin, LocaleManager.getString(1));
			return;
		}

		if(params.length == 0 || params.length > 3)
		{
			PacketSendUtility.sendMessage(admin, LocaleManager.getString(2));
			return;
		}

		int itemId = 0;
		int itemCount = 1;
		Player receiver = null;

		try
		{
			itemId = Integer.parseInt(params[0]);
			
			if( params.length == 2 )
			{
				itemCount = Integer.parseInt(params[1]);
			}
			
			if(admin.getTarget() instanceof Player)
			{
				receiver = (Player)admin.getTarget();
			}
			else
			{
				receiver = admin;
			}
		}
		catch (NumberFormatException e)
		{
			receiver = world.findPlayer(Util.convertName(params[0]));
			try
			{
				itemId = Integer.parseInt(params[1]);
				
				if( params.length == 3 )
				{
					itemCount = Integer.parseInt(params[2]);
				}
			}
			catch (NumberFormatException ex)
			{
			
				PacketSendUtility.sendMessage(admin, LocaleManager.getString(3));
				return;
			}
			catch (Exception ex2)
			{
				PacketSendUtility.sendMessage(admin, LocaleManager.getString(7));
				return;
			}
		}
		
		int count = itemService.addItem(receiver, itemId, itemCount);

		if(count == 0)
		{
			PacketSendUtility.sendMessage(admin, LocaleManager.getString(8));
			PacketSendUtility.sendMessage(receiver, LocaleManager.getString(9));
		}
		else
		{
			PacketSendUtility.sendMessage(admin, LocaleManager.getString(10));
		}
		
	}
}
