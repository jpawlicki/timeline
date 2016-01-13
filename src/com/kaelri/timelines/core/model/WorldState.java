package com.kaelri.timelines.core.model;

import java.util.ArrayList;

/**
 * A snapshot of the current state of the game.
 * Immutable. Mutations result in cloned copies.
 */
public final class WorldState {
	private ArrayList<String> chatLog;

	private WorldState(ArrayList<String> chatLog) {
		this.chatLog = new ArrayList<>(chatLog.size());
		for (String s : chatLog) this.chatLog.add(s);
	}

	public WorldState clone() {
		return new WorldState(chatLog);
	}
}
