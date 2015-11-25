package com.vexsoftware.votifier.model;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.ulfric.lib.api.hook.Hooks;
import com.ulfric.lib.api.hook.ScriptHook.Script;
import com.ulfric.lib.api.java.Strings;
import com.ulfric.lib.api.locale.Locale;
import com.ulfric.lib.api.player.PlayerUtils;
import com.ulfric.lib.api.task.Tasks;
import com.ulfric.lib.api.time.Milliseconds;
import com.ulfric.lib.api.time.TimeUtils;
import com.vexsoftware.votifier.Votifier;

public class VotifierListener implements Listener {


	protected VotifierListener(Script script)
	{
		this.script = script;

		this.total = "vote.total." + TimeUtils.month();
	}

	private final String total;
	private final Script script;

	@EventHandler
	public void onVote(VotifierEvent event)
	{
		Vote vote = event.getVote();

		String username = vote.getUsername();

		OfflinePlayer player = PlayerUtils.getOffline(username);

		if (player == null || !PlayerUtils.hasPlayed(player)) return;

		String path = "vote.service." + vote.getServiceName();

		UUID uuid = player.getUniqueId();

		long current = System.currentTimeMillis();

		long lastVote = Hooks.DATA.getPlayerDataAsLong(uuid, path);

		if (current - lastVote < Milliseconds.fromHours(23))
		{
			Votifier.getInstance().warn(Strings.format("Tried to process vote to {0} from {1} at time {2}.", username, vote.getServiceName(), current - lastVote));

			return;
		}

		Hooks.DATA.setPlayerData(uuid, path, current);

		Hooks.DATA.setPlayerData(uuid, this.total, Hooks.DATA.getPlayerDataAsInt(uuid, this.total)+1);

		if (!player.isOnline())
		{
			Hooks.DATA.setPlayerData(uuid, "vote.await", Hooks.DATA.getPlayerDataAsInt(uuid, path)+1);

			return;
		}

		Player online = player.getPlayer();

		this.script.run(online, online);

		Tasks.runAsync(() ->
		{
			long min = current - Milliseconds.DAY;

			for (Player lplayer : Bukkit.getOnlinePlayers())
			{
				if (this.containsAtLeast(Hooks.DATA.getPlayerDataRecursively(lplayer.getUniqueId(), "vote.service"), min, 3)) continue;

				Locale.send(lplayer, "vote.process", username);
			}
		});
	}

	private boolean containsAtLeast(Collection<Long> collection, long minValue, long threshold)
	{
		if (collection.size() < threshold) return false;

		int met = 0;

		for (long number : collection)
		{
			if (number < minValue) continue;

			if (++met < threshold) continue;

			return true;
		}

		return false;
	}


}