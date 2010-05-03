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
package com.aionemu.gameserver.model.gameobjects;

import java.sql.Timestamp;
import java.util.Calendar;

import com.aionemu.gameserver.model.templates.broker.BrokerRace;
import com.mysql.jdbc.Util;

/**
 * @author kosyachok
 *
 */
public class BrokerItem
{
	private Item item;
	private int itemId;
	private int itemUniqueId;
	private int itemCount;
	private int price;
	private String seller;
	private int sellerId;
	private BrokerRace itemBrokerRace;
	private boolean isSold;
	private boolean isSettled;
	private Timestamp expireTime;
	private Timestamp settleTime;
	
	PersistentState state;
	
	/**
	 * Used where registering item
	 * @param item
	 * @param price
	 * @param seller
	 * @param sellerId
	 * @param sold
	 * @param itemBrokerRace
	 */
	public BrokerItem(Item item, int price, String seller, int sellerId, BrokerRace itemBrokerRace)
	{
		this.item = item;
		this.itemId = item.getItemTemplate().getTemplateId();
		this.itemUniqueId = item.getObjectId();
		this.itemCount = item.getItemCount();
		this.price = price;
		this.seller = seller;
		this.sellerId = sellerId;
		this.itemBrokerRace = itemBrokerRace;
		this.isSold = false;
		this.isSettled = false;
		this.expireTime = new Timestamp(Calendar.getInstance().getTimeInMillis() + 691200000); // 8 days
		this.settleTime = new Timestamp(Calendar.getInstance().getTimeInMillis());
		
		this.state = PersistentState.NEW;
	}
	
	/**
	 * Used onDBLoad
	 * @param item
	 * @param itemId
	 * @param price
	 * @param seller
	 * @param sellerId
	 * @param itemBrokerRace
	 */
	public BrokerItem(Item item, int itemId, int itemUniqueId, int itemCount,int price, String seller, int sellerId, BrokerRace itemBrokerRace, boolean isSold, boolean isSettled, Timestamp expireTime, Timestamp settleTime)
	{
		this.item = item;
		this.itemId = itemId;
		this.itemUniqueId = itemUniqueId;
		this.price = price;
		this.seller = seller;
		this.sellerId = sellerId;
		this.itemBrokerRace = itemBrokerRace;
		if(item == null)
		{
			this.isSold = true;
			this.isSettled = true;
			
		}
		else
		{
			this.isSold = isSold;
			this.isSettled = isSettled;
		}
		
		this.expireTime = expireTime;
		this.settleTime = settleTime;
		
		this.state = PersistentState.NOACTION;
	}
	
	/**
	 * 
	 * @return
	 */
	public Item getItem()
	{
		return item;
	}
	
	public void removeItem()
	{
		this.item = null;
		this.isSold = true;
		this.isSettled = true;
		this.settleTime = new Timestamp(Calendar.getInstance().getTimeInMillis());
	}
	
	public int getItemId()
	{
		return itemId;
	}
	
	public int getItemUniqueId()
	{
		return itemUniqueId;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getPrice()
	{
		return price;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getSeller()
	{
		return seller;
	}
	
	public int getSellerId()
	{
		return sellerId;
	}
	
	/**
	 * 
	 * @return
	 */
	public BrokerRace getItemBrokerRace()
	{
		return itemBrokerRace;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSold()
	{
		return this.isSold;
	}
	
	public void setPersistentState(PersistentState persistentState)
	{
		switch(persistentState)
		{
			case DELETED:
				if(this.state == PersistentState.NEW)
					this.state = PersistentState.NOACTION;
				else
					this.state = PersistentState.DELETED;
				break;
			case UPDATE_REQUIRED:
				if(this.state == PersistentState.NEW)
					break;
			default:
				this.state = persistentState;
		}

	}
	
	public PersistentState getPersistentState()
	{
		return state;
	}
	
	public boolean isSettled()
	{
		return isSettled;
	}
	
	public void setSettled()
	{
		this.isSettled = true;
		this.settleTime = new Timestamp(Calendar.getInstance().getTimeInMillis());
	}
	
	public Timestamp getExpireTime()
	{
		return expireTime;
	}
	
	public Timestamp getSettleTime()
	{
		return settleTime;
	}
	
	public int getItemCount()
	{
		return itemCount;
	}
}