package com.vexsoftware.votifier.model;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import com.ulfric.lib.api.hook.Hooks;
import com.ulfric.lib.api.hook.ScriptHook.Script;
import com.ulfric.lib.api.module.SimpleModule;
import com.vexsoftware.votifier.Votifier;
import com.vexsoftware.votifier.model.reward.VoteRewardModule;

public class VoteListenerModule extends SimpleModule {

	protected static VoteListener impl = VoteListener.EMPTY;

	public VoteListenerModule()
	{
		super("votelistener", "Responsible for handling voting", "blakeman8192, Kramer, and Packet", "1.0.0-REL");

		this.withSubModule(new VoteRewardModule());

		this.addListener(new VotifierListener());
	}

	@Override
	public void postEnable()
	{
		VoteListenerModule.impl = new VoteListener()
		{
			@Override
			public VoteReceiver newVoteReceiver(Votifier plugin, String host, int port)
			{
				try
				{
					return new VoteReceiver(plugin, host, port);
				}
				catch (Exception exception)
				{
					exception.printStackTrace();

					return null;
				}
			}
		};

		/*
		 * Use IP address from server.properties as a default for
		 * configurations. Do not use InetAddress.getLocalHost() as it most
		 * likely will return the main server address instead of the address
		 * assigned to the server.
		 */
		String hostAddr = Bukkit.getServer().getIp();
		if (hostAddr == null || hostAddr.length() == 0)
		{
			hostAddr = "0.0.0.0";
		}

		FileConfiguration conf = Votifier.getInstance().getConfig();
		// Initialize the receiver.
		String host = conf.getString("host", hostAddr);
		int port = conf.getInt("port", 8192);

		try
		{
			Votifier.getInstance().setVoteReceiver(VoteListenerModule.newVoteReceiver(Votifier.getInstance(), host, port));

			if (Votifier.getInstance().getVoteReceiver() != null)
			{
				Votifier.getInstance().getVoteReceiver().start();
			}
		}
		catch (Exception ex)
		{
			Votifier.getInstance().gracefulExit();

			return;
		}	

		Script script = Hooks.SCRIPT.getScript(conf.getString("script"));

		if (script == null)
		{
			this.getOwningPlugin().warn("Vote listener script not found!");

			return;
		}

		this.addListener(new VotifierListener());
	}

	@Override
	public void postDisable()
	{
		VoteListenerModule.impl = VoteListener.EMPTY;
	}

	public static VoteReceiver newVoteReceiver(Votifier plugin, String host, int port)
	{
		return VoteListenerModule.impl.newVoteReceiver(plugin, host, port);
	}

	protected interface VoteListener
	{
		VoteListener EMPTY = new VoteListener() { };

		default VoteReceiver newVoteReceiver(Votifier plugin, String host, int port) { return null; }
	}

}