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

import java.security.KeyPair;

import org.bukkit.configuration.file.FileConfiguration;

import com.ulfric.lib.api.hook.Hooks;
import com.ulfric.lib.api.module.Plugin;
import com.vexsoftware.votifier.command.CommandFakevote;
import com.vexsoftware.votifier.crypto.RSAModule;
import com.vexsoftware.votifier.hook.VotifierImpl;
import com.vexsoftware.votifier.model.VoteListenerModule;
import com.vexsoftware.votifier.model.VoteReceiver;

/**
 * The main Votifier plugin class.
 * 
 * @author Blake Beaupain
 * @author Kramer Campbell
 * @author Adam 'Packet' Edwards
 */
public class Votifier extends Plugin {

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
	public void load()
	{
		Votifier.instance = this;

		this.withSubModule(new RSAModule());
		this.withSubModule(new VoteListenerModule());
		this.addCommand("fakevote", new CommandFakevote());

		this.registerHook(Hooks.VOTIFIER, VotifierImpl.INSTANCE);
	}

	@Override
	public void enable() {
		// Set the plugin version.
		version = getDescription().getVersion();

		FileConfiguration config = this.getConfig();
		config.options().copyDefaults(true);
		this.saveConfig();

		debug = config.getBoolean("debug", false);
		if (debug)
		{
			this.log("Debug enabled!");
		}
	}

	@Override
	public void disable() {
		// Interrupt the vote receiver.
		if (voteReceiver != null)
		{
			voteReceiver.shutdown();
		}

		Votifier.instance = null;
	}

	public void gracefulExit()
	{
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
	 * Sets the vote receiver.
	 *
	 * @param receiver The vote receiver
	 */
	public void setVoteReceiver(VoteReceiver receiver) {
		this.voteReceiver = receiver;
	}

	/**
	 * Gets the keyPair.
	 * 
	 * @return The keyPair
	 */
	public KeyPair getKeyPair() {
		return keyPair;
	}

	/**
	 * Sets the keyPair.
	 *
	 * @param pair The keyPair
	 */
	public void setKeyPair(KeyPair pair) {
		this.keyPair = pair;
	}

	public boolean isDebug() {
		return debug;
	}

}