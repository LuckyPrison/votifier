package com.vexsoftware.votifier.model.reward;

import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.ulfric.lib.api.collect.Sets;
import com.ulfric.lib.api.hook.Hooks;
import com.ulfric.lib.api.java.WeightedWrapper;
import com.ulfric.lib.api.math.RandomUtils;
import com.ulfric.lib.api.module.SimpleModule;
import com.vexsoftware.votifier.model.VotifierEvent;

@SuppressWarnings("unchecked")
public class VoteRewardModule extends SimpleModule {

	public VoteRewardModule()
	{
		super("votereward", "Vote rewarding module", "1.0.0-REL", "Packet");

		this.withConf();

		this.addListener(new Listener()
		{
			@EventHandler(ignoreCancelled = true)
			public void onVote(VotifierEvent event)
			{
				Player player = event.getPlayer();

				if (player == null) return;

				((WeightedWrapper<VoteReward>) RandomUtils.getWeighted(VoteRewardModule.this.rewards, VoteRewardModule.this.totalWeight)).getValue().execute(player);
			}
		});
	}

	private int totalWeight;
	private Set<WeightedWrapper<VoteReward>> rewards;

	@Override
	public void postEnable()
	{
		FileConfiguration conf = this.getConf().getConf();

		Set<String> keys = conf.getKeys(false);

		int size = keys.size();

		if (size <= 0)
		{
			this.disable();

			return;
		}

		this.rewards = Sets.newHashSetWithExpectedSize(size);

		this.totalWeight = 0;

		for (String key : keys)
		{
			Integer weight = Integer.parseInt(key);

			int intValue = weight.intValue();

			if (intValue < 1) continue;

			this.totalWeight += intValue;

			ConfigurationSection section = conf.getConfigurationSection(key);

			this.rewards.add(new WeightedWrapper<>(weight, new VoteReward(Hooks.SCRIPT.getScript(section.getString("script")), section.getString("message"))));
		}
	}

}