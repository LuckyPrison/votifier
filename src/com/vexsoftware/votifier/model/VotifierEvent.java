package com.vexsoftware.votifier.model;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.ulfric.lib.api.server.Events;

/**
 * {@code VotifierEvent} is a custom Bukkit event class that is sent
 * synchronously to CraftBukkit's main thread allowing other plugins to listener
 * for votes.
 * 
 * @author frelling
 * @author Adam 'Packet' Edwards
 */
public class VotifierEvent extends Event implements Cancellable {

	/**
	 * Constructs a vote event that encapsulated the given vote record.
	 * 
	 * @param vote vote record
	 */
	public VotifierEvent(Vote vote)
	{
		this.vote = vote;
	}

	/**
	 * Encapsulated vote record.
	 */
	private Vote vote;
	/**
	 * Return the encapsulated vote record.
	 * 
	 * @return vote record
	 */
	public Vote getVote()
	{
		return this.vote;
	}

	public Player getPlayer()
	{
		return this.vote.getPlayer();
	}

	private boolean cancelled;
	@Override
	public boolean isCancelled() { return this.cancelled; }
	@Override
	public void setCancelled(boolean cancel) { this.cancelled = cancel; }

	private static final HandlerList HANDLERS = Events.newHandlerList();
	@Override
	public HandlerList getHandlers() { return VotifierEvent.HANDLERS; }
	public static HandlerList getHandlerList() { return VotifierEvent.HANDLERS; }

}