package com.vexsoftware.votifier.model;

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
public class VotifierEvent extends Event {


	/**
	 * Constructs a vote event that encapsulated the given vote record.
	 * 
	 * @param vote vote record
	 */
	protected VotifierEvent(Vote vote)
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
	public Vote getVote() {
		return vote;
	}

	private static final HandlerList HANDLERS = Events.newHandlerList();
	@Override
	public HandlerList getHandlers() { return VotifierEvent.HANDLERS; }
	public static HandlerList getHandlerList() { return VotifierEvent.HANDLERS; }


}