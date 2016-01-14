package com.kaelri.timelines.core.model;

public class Region {
	public final Vertex[] corners;
	public final ArrayList<Region> adjacents = new ArrayList<>();
	public Biome biome;

	public Region(final Vertex origin, final Vertex... corners) {
		final double[] op = origin.point;
		final double[] zero = Math2.normalize(Math2.subtract3d(corners[0].point, Math2.scale3d(op, Math2.dot3d(op, corners[0].point))));
		Arrays.sort(corners, new Comparator<Vertex>() {
			@Override
			public int compare(Vertex a, Vertex b) {
				double[] ap = a.point;
				// Project every vertex onto the plane normal to origin; normalize the projections.
				ap = Math2.normalize(Math2.subtract3d(ap, Math2.scale3d(op, Math2.dot3d(op, ap))));
				double adot = Math2.dot3d(zero, ap);
				double across = Math2.dot3d(op, Math2.cross3d(zero, ap));
				double aval = 0;
				if (across >= 0 && adot >= 0) {
					aval = across;
				} else if (across >= 0 && adot < 0) {
					aval = 1 + (1 - across);
				} else if (across <= 0 && adot < 0) {
					aval = 2 - across;
				} else {
					aval = 4 + across;
				}

				double[] bp = b.point;
				bp = Math2.normalize(Math2.subtract3d(bp, Math2.scale3d(op, Math2.dot3d(op, bp))));
				double bdot = Math2.dot3d(zero, bp);
				double bcross = Math2.dot3d(op, Math2.cross3d(zero, bp));
				double bval = 0;
				if (bcross >= 0 && bdot >= 0) {
					bval = bcross;
				} else if (bcross >= 0 && bdot < 0) {
					bval = 1 + (1 - bcross);
				} else if (bcross <= 0 && bdot < 0) {
					bval = 2 - bcross;
				} else {
					bval = 4 + bcross;
				}

				return (int)Math.signum(aval - bval);
			}
		});
		this.corners = corners;
	}

	public void addAdjacent(Region r) {
		adjacents.add(r);
	}

	public void setBiome(Biome b) {
		biome = b;
	}
}
