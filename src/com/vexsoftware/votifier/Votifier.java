/*
 * Copyright (C) 2012 Vex Software LLC
 * This file is part of Votifier.
 * 
 * Votifier is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Votifier is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Votifier.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vexsoftware.votifier;

import java.io.File;
import java.security.KeyPair;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import com.ulfric.lib.plugin.UPlugin;
import com.vexsoftware.votifier.crypto.RSAIO;
import com.vexsoftware.votifier.crypto.RSAKeygen;
import com.vexsoftware.votifier.net.VoteReceiver;

/**
 * The main Votifier plugin class.
 * 
 * @author Blake Beaupain
 * @author Kramer Campbell
 * @author Adam 'Packet' Edwards
 */
public class Votifier extends UPlugin {

	/** The Votifier instance. */
	private static Votifier instance;

	/** The current Votifier version. */
	private String version;

	/** The vote receiver. */
	private VoteReceiver voteReceiver;

	/** The RSA key pair. */
	private KeyPair keyPair;

	/** Debug mode flag */
	private boolean debug;

	@Override
	public void onLoad()
	{
		Votifier.instance = this;
	}

	@Override
	public void onEnable() {
		// Set the plugin version.
		version = getDescription().getVersion();

		// Handle configuration.
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		File config = new File(getDataFolder() + "/config.yml");
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(config);
		File rsaDirectory = new File(getDataFolder() + "/rsa");

		/*
		 * Use IP address from server.properties as a default for
		 * configurations. Do not use InetAddress.getLocalHost() as it most
		 * likely will return the main server address instead of the address
		 * assigned to the server.
		 */
		String hostAddr = Bukkit.getServer().getIp();
		if (hostAddr == null || hostAddr.length() == 0)
			hostAddr = "0.0.0.0";

		/*
		 * Create configuration file if it does not exists; otherwise, load it
		 */
		if (!config.exists()) {
			try {
				// First time run - do some initialization.
				this.log("Configuring Votifier for the first time...");

				// Initialize the configuration file.
				config.createNewFile();

				cfg.set("host", hostAddr);
				cfg.set("port", 8192);
				cfg.set("debug", false);

				/*
				 * Remind hosted server admins to be sure they have the right
				 * port number.
				 */
				this.log("------------------------------------------------------------------------------");
				this.log("Assigning Votifier to listen on port 8192. If you are hosting Craftbukkit on a");
				this.log("shared server please check with your hosting provider to verify that this port");
				this.log("is available for your use. Chances are that your hosting provider will assign");
				this.log("a different port, which you need to specify in config.yml");
				this.log("------------------------------------------------------------------------------");

				cfg.save(config);
			} catch (Exception ex) {
				this.warn("Error creating configuration file");
				this.log(ex);
				gracefulExit();
				return;
			}
		} else {
			// Load configuration.
			cfg = YamlConfiguration.loadConfiguration(config);
		}

		/*
		 * Create RSA directory and keys if it does not exist; otherwise, read
		 * keys.
		 */
		try {
			if (!rsaDirectory.exists()) {
				rsaDirectory.mkdir();
				keyPair = RSAKeygen.generate(2048);
				RSAIO.save(rsaDirectory, keyPair);
			} else {
				keyPair = RSAIO.load(rsaDirectory);
			}
		} catch (Exception ex) {
			this.warn("Error reading configuration file or RSA keys");
			this.log(ex);
			gracefulExit();
			return;
		}

		// Initialize the receiver.
		String host = cfg.getString("host", hostAddr);
		int port = cfg.getInt("port", 8192);
		debug = cfg.getBoolean("debug", false);
		if (debug)
		{
			this.log("Debug enabled!");
		}

		try {
			voteReceiver = new VoteReceiver(this, host, port);
			voteReceiver.start();
		} catch (Exception ex) {
			gracefulExit();
			return;
		}
	}

	@Override
	public void annihilate() {
		// Interrupt the vote receiver.
		if (voteReceiver != null) {
			voteReceiver.shutdown();
		}
		this.log("Votifier disabled.");
	}

	private void gracefulExit() {
		this.warn("Votifier did not initialize properly!");
	}

	/**
	 * Gets the instance.
	 * 
	 * @return The instance
	 */
	public static Votifier getInstance() {
		return instance;
	}

	/**
	 * Gets the version.
	 * 
	 * @return The version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Gets the vote receiver.
	 * 
	 * @return The vote receiver
	 */
	public VoteReceiver getVoteReceiver() {
		return voteReceiver;
	}

	/**
	 * Gets the keyPair.
	 * 
	 * @return The keyPair
	 */
	public KeyPair getKeyPair() {
		return keyPair;
	}

	public boolean isDebug() {
		return debug;
	}


}