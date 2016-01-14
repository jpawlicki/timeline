package com.kaelri.timelines.core.model;

class Vertex {
	public final double[] point;
	public Vertex(double... point) {
		this.point = point;
	}
	public Vertex() {
		double theta = Math.random() * 2 * Math.PI;
		double u = Math.random() * 2 - 1;
		double r = Math.sqrt(1 - u * u);
		point = Math2.normalize(new double[] {r * Math.cos(theta), r * Math.sin(theta), u});
	}
}
