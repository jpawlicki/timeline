package com.kaelri.timelines.core.adapter;

public interface ModelAdapter {
	/**
	 * @return the current known model, according to the point of view.
	 */
	public void getApparentModel();

	/**
	 * Register a listener to listen for updates to the model.
	 */
	public void registerListener(ModelListener l);

	/**
	 * Applies a command to the model, probably changing it.
	 * The change is applied asynchronously - this method returns immediately.
	 */
	public void changeModel(Command c);
}
