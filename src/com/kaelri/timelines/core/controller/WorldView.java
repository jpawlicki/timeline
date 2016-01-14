package com.kaelri.timelines.core.controller;

class WorldView {
	public void draw(Model model) {
		double viewTheta = 1 / 2.0;
		double[][] rotz = Math2.fetchrot3d(new double[]{0, 0, 1}, camera);
		double[] viewVector = Math2.matmult(rotz, new double[]{1, 0, 0});
		for (Region r : regions) {
			double[][] pp = new double[r.corners.length][];
			boolean draw = false;
			for (int i = 0; i < r.corners.length; i++) {
				pp[i] = r.corners[i].point;
				if (Math2.dot3d(pp[i], viewVector) > viewTheta) {
					draw = true;
				}
			}
			if (!draw) continue;
			pp = Math2.matmult(pp, rotz);
			g.setPaint(r.color);
			drawPolySimple(pp, g);
		}
	}

	public static void drawPoly(final double[][] poly, Graphics2D g) {
		boolean first = true;
		Path2D.Double path = new Path2D.Double();
		for (int n = 0; n < poly.length; n++) {
			double[] a = poly[n];
			double[] b = poly[(n + 1) % poly.length];
			int x1 = (int)(width / 2 + a[1] * width / (2 - a[0]));
			int y1 = (int)(height + a[2] * width / (2 - a[0]));
			int x2 = (int)(width / 2 + b[1] * width / (2 - b[0]));
			int y2 = (int)(height + b[2] * width / (2 - b[0]));
			int numPoints = Math.max(2, Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2)) / 2);
			double[][] points = new double[numPoints][0];
			points[0] = a;
			points[numPoints - 1] = b;
			for (int i = 1; i < numPoints - 1; i++) {
				points[i] = Math2.normalize(Math2.add3d(
						Math2.scale3d(a, (numPoints - i) / (numPoints - 1.0)),
						Math2.scale3d(b, i / (numPoints - 1.0))));
			}
			for (int i = 1; i < numPoints; i++) {
				x2 = (int)(width / 2 + points[i][1] * width / (3 - points[i][0]));
				y2 = (int)(height / 2 + points[i][2] * width / (3 - points[i][0]));
				if (first) {
					first = false;
					path.moveTo(x1, y1);
				}
				path.lineTo(x2, y2);
				x1 = x2;
				y1 = y2;
			}
		}
		path.closePath();
		g.fill(path);
	}

	public static void drawPolySimple(double[][] poly, Graphics2D g) {
		Path2D.Double path = new Path2D.Double();
		path.moveTo(width / 2 + poly[0][1] * width / (2.5 - poly[0][0]), height / 2 + poly[0][2] * width / (2.5 - poly[0][0]));
		for (int i = 1; i < poly.length; i++) {
			path.lineTo(width / 2 + poly[i][1] * width / (2.5 - poly[i][0]), height / 2 + poly[i][2] * width / (2.5 - poly[i][0]));
		}
		path.closePath();
		g.fill(path);
	}

}
