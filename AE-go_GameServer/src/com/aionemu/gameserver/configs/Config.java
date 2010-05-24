/**
 * This file is part of aion-emu <aion-emu.com>.
 *
 *  aion-emu is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-emu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-emu.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.configs;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.aionemu.commons.configuration.ConfigurableProcessor;
import com.aionemu.commons.database.DatabaseConfig;
import com.aionemu.commons.utils.PropertiesUtils;
import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.configs.main.CacheConfig;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.EnchantsConfig;
import com.aionemu.gameserver.configs.main.FallDamageConfig;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.configs.main.LegionConfig;
import com.aionemu.gameserver.configs.main.PeriodicSaveConfig;
import com.aionemu.gameserver.configs.main.RateConfig;
import com.aionemu.gameserver.configs.main.ShutdownConfig;
import com.aionemu.gameserver.configs.main.TaskManagerConfig;
import com.aionemu.gameserver.configs.main.ThreadConfig;
import com.aionemu.gameserver.configs.network.IPConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.utils.Util;

/**
 * @author -Nemesiss-
 * @author SoulKeeper
 */
public class Config
{
	/**
	 * Logger for this class.
	 */
	protected static final Logger	log	= Logger.getLogger(Config.class);

	public static void loadAionDream(String specifiedConfigPath)
	{
		try
		{
			Properties[] props = PropertiesUtils.load(specifiedConfigPath, "");
			ConfigurableProcessor.process(Config.class, props);
			ConfigurableProcessor.process(AdminConfig.class, props);
			ConfigurableProcessor.process(LegionConfig.class, props);
			ConfigurableProcessor.process(RateConfig.class, props);
			ConfigurableProcessor.process(CacheConfig.class, props);
			ConfigurableProcessor.process(ShutdownConfig.class, props);
			ConfigurableProcessor.process(TaskManagerConfig.class, props);
			ConfigurableProcessor.process(GroupConfig.class, props);
			ConfigurableProcessor.process(CustomConfig.class, props);
			ConfigurableProcessor.process(EnchantsConfig.class, props);
			ConfigurableProcessor.process(FallDamageConfig.class, props);
			ConfigurableProcessor.process(GSConfig.class, props);
			ConfigurableProcessor.process(PeriodicSaveConfig.class, props);
			ConfigurableProcessor.process(ThreadConfig.class, props);
			ConfigurableProcessor.process(NetworkConfig.class, props);
			ConfigurableProcessor.process(DatabaseConfig.class, props);
			log.info("Loaded AD Emu configuration from: " + specifiedConfigPath);
		}
		catch(Exception e)
		{
			log.fatal("Cannot load configuration from: " + specifiedConfigPath);
			throw new Error("Cannot load configuration from: " + specifiedConfigPath, e);
		}
	}
	
	/**
	 * Initialize all configs in com.aionemu.gameserver.configs package
	 */
	public static void load()
	{
		try
		{
			Properties[] props = PropertiesUtils.loadAllFromDirectory("./config");

			ConfigurableProcessor.process(Config.class, props);

			// Administration
			Util.printSection("Administration");
			String administration = "./config/administration";
			Properties[] adminProps = PropertiesUtils.loadAllFromDirectory(administration);
			
			ConfigurableProcessor.process(AdminConfig.class, adminProps);
			log.info("Loading: " + administration + "/admin.properties");

			// Main
			Util.printSection("Main");
			String main = "./config/main";
			Properties[] mainProps = PropertiesUtils.loadAllFromDirectory(main);
			
			ConfigurableProcessor.process(LegionConfig.class, mainProps);
			log.info("Loading: " + main + "/legion.properties");
			
			ConfigurableProcessor.process(RateConfig.class, mainProps);
			log.info("Loading: " + main + "/rates.properties");
			
			ConfigurableProcessor.process(CacheConfig.class, mainProps);
			log.info("Loading: " + main + "/cache.properties");
			
			ConfigurableProcessor.process(ShutdownConfig.class, mainProps);
			log.info("Loading: " + main + "/shutdown.properties");
			
			ConfigurableProcessor.process(TaskManagerConfig.class, mainProps);
			log.info("Loading: " + main + "/taskmanager.properties");
			
			ConfigurableProcessor.process(GroupConfig.class, mainProps);
			log.info("Loading: " + main + "/group.properties");
			
			ConfigurableProcessor.process(CustomConfig.class, mainProps);
			log.info("Loading: " + main + "/custom.properties");
			
			ConfigurableProcessor.process(EnchantsConfig.class, mainProps);
			log.info("Loading: " + main + "/enchants.properties");
			
			ConfigurableProcessor.process(FallDamageConfig.class, mainProps);
			log.info("Loading: " + main + "/falldamage.properties");
			
			ConfigurableProcessor.process(GSConfig.class, mainProps);
			log.info("Loading: " + main + "/gameserver.properties");
			
			ConfigurableProcessor.process(PeriodicSaveConfig.class, mainProps);
			log.info("Loading: " + main + "/periodicsave.properties");
			
			ConfigurableProcessor.process(ThreadConfig.class, mainProps);
			log.info("Loading: " + main + "/thread.properties");

			// Network
			Util.printSection("Network");
			String network = "./config/network";
			Properties[] networkProps = PropertiesUtils.loadAllFromDirectory(network);	
			
			ConfigurableProcessor.process(NetworkConfig.class, networkProps);		
			log.info("Loading: " + network + "/database.properties");
			log.info("Loading: " + network + "/network.properties");
		}
		catch(Exception e)
		{
			log.fatal("Can't load gameserver configuration: ", e);
			throw new Error("Can't load gameserver configuration: ", e);
		}

		IPConfig.load();
	}
}