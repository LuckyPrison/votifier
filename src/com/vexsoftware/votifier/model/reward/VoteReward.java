package com.vexsoftware.votifier.model.reward;

import org.bukkit.entity.Player;

import com.ulfric.lib.api.hook.Hooks;
import com.ulfric.lib.api.hook.ScriptHook.Script;
import com.ulfric.lib.api.locale.Locale;
import com.ulfric.lib.api.player.PlayerUtils;
import com.ulfric.lib.api.task.Tasks;

class VoteReward {

	protected VoteReward(Script script, String message)
	{
		this.script = script;

		this.message = message;
	}

	private final Script script;

	private final String message;

	public void execute(Player player)
	{
		this.script.run(player, player);

		String name = player.getName();

		Locale.send(player, this.message, name);

		Tasks.runAsync(() ->
		{
			for (Player lplayer : PlayerUtils.getOnlinePlayersExceptFor(player))
			{
				if (Hooks.PRISON.countVotes(lplayer.getUniqueId()) >= 2) continue;

				Locale.send(lplayer, this.message, name);
			}
		});
	}

}