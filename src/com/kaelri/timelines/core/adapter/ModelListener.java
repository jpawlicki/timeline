package com.kaelri.timelines.core.adapter;

public interface ModelListener {
	/**
	 * Notifies the listener that a model update has taken place.
	 * The listener should then query the adapter for the new model.
	 */
	public void update();
}
