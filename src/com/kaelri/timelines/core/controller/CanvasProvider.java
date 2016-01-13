package com.kaelri.timelines.core.controller;

import java.util.HashMap;

public class CanvasProvider {
	public final Mode mode;

	public static enum Mode {
		FULLSCREEN,
		BORDERLESS_WINDOWED,
		WINDOWED,
		MULTI_WINDOW
	}

	public final class DrawingContext {
		public final Graphics2D surface;
		public final int width;
		public final int height;

		public DrawingContext(Graphics2D surface, int width, int height) {
			this.width = width;
			this.height = height;
		}
	}

	private final HashMap<Object, Panel> panels = new HashMap<>();

	public CanvasProvider(Mode mode) {
		this.mode = mode;
	}

	public DrawingContext getDrawSurface(View requestor) {
		Panel p = panels.get(requestor);
		if (p == null) {
			p = createNewPanel(requestor);
			panels.add(p);
		}
		return p.
	}
}
