package com.vexsoftware.votifier.model;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.ulfric.lib.api.hook.Hooks;
import com.ulfric.lib.api.java.Strings;
import com.ulfric.lib.api.player.PlayerUtils;
import com.ulfric.lib.api.time.Milliseconds;
import com.ulfric.lib.api.time.TimeUtils;
import com.vexsoftware.votifier.Votifier;

public class VotifierListener implements Listener {

	protected VotifierListener()
	{
		this.total = "vote.total." + TimeUtils.month();
	}

	private final String total;

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onVote(VotifierEvent event)
	{
		Vote vote = event.getVote();

		OfflinePlayer player = vote.getPlayer();

		if (player == null)
		{
			player = PlayerUtils.getOffline(vote.getUsername(), false);
		}

		if (player == null) return;

		String path = "vote.service." + vote.getServiceName();

		UUID uuid = player.getUniqueId();

		long current = System.currentTimeMillis();

		long lastVote = Hooks.DATA.getPlayerDataAsLong(uuid, path);

		if (lastVote != 0 && current - lastVote < (Milliseconds.fromHours(24)-Milliseconds.SECOND))
		{
			Votifier.getInstance().warn(Strings.format("Tried to process vote to {0} from {1} at time {2}.", vote.getUsername(), vote.getServiceName(), current - lastVote));

			return;
		}

		Hooks.DATA.setPlayerData(uuid, path, current);

		Hooks.DATA.setPlayerData(uuid, this.total, Hooks.DATA.getPlayerDataAsInt(uuid, this.total)+1);

		if (player.isOnline()) return;

		Hooks.DATA.setPlayerData(uuid, "vote.await", Hooks.DATA.getPlayerDataAsInt(uuid, path)+1);
	}

}