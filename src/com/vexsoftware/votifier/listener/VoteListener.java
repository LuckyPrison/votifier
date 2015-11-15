package com.vexsoftware.votifier.listener;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.ulfric.data.coll.DataColl;
import com.ulfric.lib.plugin.Listener;
import com.ulfric.lib.util.java.collect.CollectionUtils;
import com.ulfric.lib.util.player.Locale;
import com.ulfric.lib.util.player.PlayerUtils;
import com.ulfric.lib.util.string.Strings;
import com.ulfric.lib.util.task.Tasks;
import com.ulfric.lib.util.time.Milliseconds;
import com.ulfric.lib.util.time.TimeUtils;
import com.ulfric.luckyscript.lang.Script;
import com.vexsoftware.votifier.Votifier;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class VoteListener implements Listener {


	public VoteListener(Script script)
	{
		this.script = script;

		this.total = "vote.total." + TimeUtils.MONTH;
	}

	private final String total;
	private final Script script;

	@EventHandler
	public void onVote(VotifierEvent event)
	{
		Vote vote = event.getVote();

		String username = vote.getUsername();

		OfflinePlayer player = PlayerUtils.getOffline(username);

		if (player == null || !player.hasPlayedBefore()) return;

		String path = "vote.service." + vote.getServiceName();

		UUID uuid = player.getUniqueId();

		long current = System.currentTimeMillis();

		long lastVote = DataColl.getPlayerDataAsLong(uuid, path);

		if (current - lastVote < Milliseconds.fromHours(23))
		{
			Votifier.getInstance().warn(Strings.format("Tried to process vote to {0} from {1} at time {2}.", username, vote.getServiceName(), current - lastVote));

			return;
		}

		DataColl.setPlayerData(uuid, path, current);

		DataColl.setPlayerData(uuid, this.total, DataColl.getPlayerDataAsInt(uuid, this.total)+1);

		if (!player.isOnline())
		{
			DataColl.setPlayerData(uuid, "vote.await", DataColl.getPlayerDataAsInt(uuid, path)+1);

			return;
		}

		Player online = player.getPlayer();

		this.script.run(online, online);

		Tasks.runAsync(() ->
		{
			long min = current - Milliseconds.DAY;

			for (Player lplayer : Bukkit.getOnlinePlayers())
			{
				if (CollectionUtils.containsAtLeast(DataColl.getPlayerDataRecursively(lplayer.getUniqueId(), "vote.service"), min, 3)) continue;

				Locale.send(lplayer, "vote.process", username);
			}
		});
	}


}