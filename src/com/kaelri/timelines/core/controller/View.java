package com.kaelri.timelines.core.controller;

import java.awt.Graphics2D;
import java.awt.image.VolatileImage;

public abstract class View {
  public void draw(Panel p) {
    if (cache == null) {
      recreateCache(p);
      repaintCache(p);
    } else if (refreshRequired()) {
      repaintCache(p);
    }
    do {
      int valid = cache.validate(p.getGraphicsConfiguration());
      if (valid == VolatileImage.IMAGE_RESTORED) {
        repaintCache(p);
      } else if (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
        recreateCache(p);
        repaintCache(p);
      }
      p.getGraphics().drawImage(cache, 0, 0);
    } while (cache.contentsLost());
  }

  protected abstract void redraw(Graphics2D g) {}
  protected abstract boolean refreshRequired() {}

  private VolatileImage cache = null;

  private void recreateCache(Panel p) {
    if (cache != null) {
      cache.flush();
    }
    cache = p.getGraphicsConfiguration().createCompatibleVolatileImage(p.width(), p.height());
  }

  private void repaintCache() {
    redraw(cache.createGraphics());
  }
}
