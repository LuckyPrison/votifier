package com.vexsoftware.votifier.command;

import java.util.Optional;

import org.bukkit.entity.Player;

import com.ulfric.lib.api.command.Argument;
import com.ulfric.lib.api.command.SimpleCommand;
import com.ulfric.lib.api.command.arg.ArgStrategy;
import com.ulfric.lib.api.java.Strings;
import com.ulfric.lib.api.player.PlayerUtils;
import com.ulfric.lib.api.server.Events;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class CommandFakevote extends SimpleCommand {

	public CommandFakevote()
	{
		this.withArgument(Argument.REQUIRED_PLAYER);

		this.withArgument("str", ArgStrategy.ENTERED_STRING);
	}

	private Vote vote;

	@Override
	public void run()
	{
		Player player = (Player) this.getObject("player");

		synchronized(this.vote)
		{
			if (this.vote == null)
			{
				this.vote = new Vote();
			}

			this.vote.setUsername(player.getName());
			this.vote.setAddress(PlayerUtils.getIP(player));
			this.vote.setServiceName(Optional.ofNullable((String) this.getObject("str")).orElse("FakeVote"));
			// TODO - Set this to something
			this.vote.setTimeStamp(Strings.BLANK);

			Events.call(new VotifierEvent(this.vote));
		}
	}

}