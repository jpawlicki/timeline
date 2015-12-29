import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
	public static TriangleTree tree;
	public static ArrayList<double[]> points = new ArrayList<double[]>();

	public static double camera = 0;
	public static int width = 700;
	public static int height = 700;

	public static int vertexes;
	public static int vertexesPerFrame;

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: java Main [num_vertexes] [num_vertexes_to_frame]");
			return;
		}
		vertexes = new Integer(args[0]);
		vertexesPerFrame = new Integer(args[1]);

		Canvas c = new Canvas();
		Frame f = new Frame();
		f.setSize(width, height);
		f.add(c);
		f.setVisible(true);
		c.createBufferStrategy(3);
		tree = new TriangleTree();

		/*
		TriangleTree a = tree;
		for (int i = 0; i < 8; i++) {
			System.out.println(a.triangle.contains(new Vertex(MathManager.normalize(MathManager.add3dall(a.triangle.verts[0].point, a.triangle.verts[1].point, a.triangle.verts[2].point)))));
			a = a.outside;
		}*/

		firstUpdate();
		while(true) {
			update();
			Graphics2D g = (Graphics2D)c.getBufferStrategy().getDrawGraphics();
			g.setPaint(new Color(0, 0, 0));
			g.fillRect(0, 0, 900, 900);
			camera += 0.005;
			draw(g, tree);
			g.dispose();
			c.getBufferStrategy().show();
			try {
				Thread.sleep(5);
			} catch (Exception e) {
			}
		}
	}

	public static void draw(Graphics2D g, TriangleTree root) {
		double viewTheta = 1 / 2.0;
		double[][] rotz = MathManager.fetchrot3d(new double[]{0, 0, 1}, camera);
		double[] viewVector = MathManager.matmult(rotz, new double[]{1, 0, 0});
		for (TriangleTree t : root.leafs.leafs) {
			double[][] pp = new double[3][];
			boolean draw = false;
			for (int i = 0; i < t.triangle.verts.length; i++) {
				pp[i] = t.triangle.verts[i].point;
				if (MathManager.dot3d(pp[i], viewVector) > viewTheta) {
					draw = true;
				}
			}
			if (!draw) continue;
			pp = MathManager.matmult(pp, rotz);
			float[] color = Triangle.getColor(t.triangle.color);
			g.setPaint(new Color(color[0], color[1], color[2]));
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
				points[i] = MathManager.normalize(MathManager.add3d(
						MathManager.scale3d(a, (numPoints - i) / (numPoints - 1.0)),
						MathManager.scale3d(b, i / (numPoints - 1.0))));
			}
			for (int i = 1; i < numPoints; i++) {
				x2 = (int)(width / 2 + points[i][1] * width / (2 - points[i][0]));
				y2 = (int)(height + points[i][2] * width / (2 - points[i][0]));
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
		path.moveTo(width / 2 + poly[0][1] * width / (2 - poly[0][0]), height + poly[0][2] * width / (2 - poly[0][0]));
		path.lineTo(width / 2 + poly[1][1] * width / (2 - poly[1][0]), height + poly[1][2] * width / (2 - poly[1][0]));
		path.lineTo(width / 2 + poly[2][1] * width / (2 - poly[2][0]), height + poly[2][2] * width / (2 - poly[2][0]));
		path.closePath();
		g.fill(path);
	}

	public static void firstUpdate() {
		for (int i = 6; i < vertexes; i++)
			tree.add(new Vertex());
	}

	public static void update() {
		for (int i = 0; i < vertexesPerFrame; i++) {
			double maxArea = 0;
			TriangleTree max = null;
			for (TriangleTree t : tree.leafs.leafs) {
				double a = t.triangle.area();
				if (a > maxArea) {
					maxArea = a;
					max = t;
				}
			}
			double[] add = max.triangle.random();
			tree.add(new Vertex(add));
		}
	}
}

class Vertex {
	public final double[] point;
	public Vertex(double... point) {
		this.point = point;
	}
	public Vertex() {
		double theta = Math.random() * 2 * Math.PI;
		double u = Math.random() * 2 - 1;
		double r = Math.sqrt(1 - u * u);
		point = MathManager.normalize(new double[] {r * Math.cos(theta), r * Math.sin(theta), u});
	}
}

class Sphere {
	public final double radius;
	public final double[] circumcenter;
	public Sphere(double radius, double[] circumcenter) {
		this.radius = radius;
		this.circumcenter = circumcenter;
	}
	public boolean contains(Vertex v) {
		return MathManager.magnitude3d(MathManager.subtract3d(v.point, circumcenter)) < radius;
	}
}

class Triangle {
	public final Sphere circumcircle;
	public final Vertex[] verts;
	public float[] color;
	protected Triangle() {
		verts = new Vertex[0];
		circumcircle = new Sphere(1, new double[]{0, 0, 0});
		color = getColor();
	}

	public static float[] getColor() {
		if (Math.random() < 0.65) {
			return new float[] { 0.15f, 0.44f, 0.33f };
		} else {
			return new float[] { 0.58f, 0.65f, 0.34f };
		}
	}

	public static float[] getColor(float[] color) {
		return new float[] {
			(float) Math.max(0, Math.min(1, color[0] + (Math.random() - 0.5) * 0.1)),
			(float) Math.max(0, Math.min(1, color[1] + (Math.random() - 0.5) * 0.1)),
			(float) Math.max(0, Math.min(1, color[2] + (Math.random() - 0.5) * 0.1)),
		};
	}

	public void setColor(float[] color) {
		this.color = color;
	}

	public Triangle(Vertex... verts) {
		color = getColor();
		this.verts = verts;
		/*
		a = A - C
		b = B - C

		((|a|*|a|*b - |b|*|b|*a) x (a x b))
		/ (2 * |a x b| * |a x b|)
		+ C
		== circumcenter

		radius ==
		|a|*|b|*|a - b|/2/|a x b|
		*/
		double[] a = MathManager.subtract3d(verts[0].point, verts[2].point);
		double[] b = MathManager.subtract3d(verts[1].point, verts[2].point);
		double la = MathManager.magnitude3d(a);
		double lb = MathManager.magnitude3d(b);
		double[] axb = MathManager.cross3d(a, b);
		double laxb = MathManager.magnitude3d(axb);
		double[] circumcenter = MathManager.cross3d(MathManager.subtract3d(MathManager.scale3d(b, la * la), MathManager.scale3d(a, lb * lb)), axb);
		circumcenter = MathManager.add3d(MathManager.scale3d(circumcenter, 1 / (2 * laxb * laxb)), verts[2].point);
		double radius = la * lb * MathManager.magnitude3d(MathManager.subtract3d(a, b)) / 2 / laxb;
		circumcircle = new Sphere(radius, circumcenter);
	}
	public boolean contains(Vertex v) {
		double a = MathManager.dot3d(MathManager.cross3d(verts[0].point, verts[1].point), v.point);
		double b = MathManager.dot3d(MathManager.cross3d(verts[1].point, verts[2].point), v.point);
		double c = MathManager.dot3d(MathManager.cross3d(verts[2].point, verts[0].point), v.point);
		return a >= 0 && b >= 0 && c >= 0;
	}
	public double area() { // Not really the area. Just the weight.
		return Math.acos(MathManager.dot3d(verts[0].point, verts[1].point)) * Math.acos(MathManager.dot3d(verts[1].point, verts[2].point));
	}
	public double[] random() {
		double u = Math.random();
		double w = Math.random();
		if (u + w > 1) {
			u = 1 - u;
			w = 1 - w;
		}
		return MathManager.normalize(MathManager.add3d(
				MathManager.add3d(MathManager.scale3d(verts[0].point, 1 - u), MathManager.scale3d(verts[1].point, u)),
				MathManager.add3d(MathManager.scale3d(verts[0].point, 1 - w), MathManager.scale3d(verts[2].point, w))));
	}
}

class AllTriangle extends Triangle {
	public boolean contains(Vertex v) {
		return true;
	}
}

class TreeMap {
	public ArrayList<TriangleTree> leafs = new ArrayList<>();
	private HashMap<ArrayList<Vertex>, ArrayList<TriangleTree>> map = new HashMap<>();
	public ArrayList<TriangleTree> getNeighbors(TriangleTree t) {
		ArrayList<TriangleTree> ret = new ArrayList<>();
		for (ArrayList<Vertex> key : getKeys(t)) {
			ret.addAll(map.get(key));
		}
		while (ret.remove(t));
		return ret;
	}
	public void remove(TriangleTree t) {
		for (ArrayList<Vertex> key : getKeys(t)) {
			map.get(key).remove(t);
		}
		while (leafs.remove(t));
	}
	public void add(TriangleTree t) {
		for (ArrayList<Vertex> key : getKeys(t)) {
			if (!map.containsKey(key)) {
				map.put(key, new ArrayList<TriangleTree>());
			}
			map.get(key).add(t);
		}
		leafs.add(t);
	}
	private ArrayList<ArrayList<Vertex>> getKeys(TriangleTree t) {
		ArrayList<ArrayList<Vertex>> keys = new ArrayList<>();
		ArrayList<Vertex> key = new ArrayList<Vertex>();
		key.add(t.triangle.verts[0]);
		key.add(t.triangle.verts[1]);
		keys.add(key);
		key = new ArrayList<Vertex>();
		key.add(t.triangle.verts[1]);
		key.add(t.triangle.verts[0]);
		keys.add(key);
		key = new ArrayList<Vertex>();
		key.add(t.triangle.verts[1]);
		key.add(t.triangle.verts[2]);
		keys.add(key);
		key = new ArrayList<Vertex>();
		key.add(t.triangle.verts[2]);
		key.add(t.triangle.verts[1]);
		keys.add(key);
		key = new ArrayList<Vertex>();
		key.add(t.triangle.verts[2]);
		key.add(t.triangle.verts[0]);
		keys.add(key);
		key = new ArrayList<Vertex>();
		key.add(t.triangle.verts[0]);
		key.add(t.triangle.verts[2]);
		keys.add(key);
		return keys;
	}
}

class TriangleTree {
	public static TreeMap leafs = new TreeMap();
	public ArrayList<TriangleTree> children = new ArrayList<>();
	public ArrayList<TriangleTree> parents = new ArrayList<>();
	public Triangle triangle;

	public TriangleTree() {
		triangle = new AllTriangle();
		Vertex va = new Vertex(0, 0, 1);
		Vertex vb = new Vertex(0, 0, -1);
		Vertex vc = new Vertex(0, 1, 0);
		Vertex vd = new Vertex(0, -1, 0);
		Vertex ve = new Vertex(1, 0, 0);
		Vertex vf = new Vertex(-1, 0, 0);
		add(new TriangleTree(va, ve, vc));
		add(new TriangleTree(va, vc, vf));
		add(new TriangleTree(va, vd, ve));
		add(new TriangleTree(va, vf, vd));
		add(new TriangleTree(vb, vc, ve));
		add(new TriangleTree(vb, vf, vc));
		add(new TriangleTree(vb, ve, vd));
		add(new TriangleTree(vb, vd, vf));
		for (TriangleTree t : children) {
			leafs.add(t);
		}
	}

	public TriangleTree(Vertex... verts) {
		triangle = new Triangle(verts);
	}

	public void add(TriangleTree t) {
		children.add(t);
		t.parents.add(this);
	}

	public void add(Vertex v) {
		if (children.size() == 0) {
			leafs.remove(this);
			TriangleTree a = new TriangleTree(triangle.verts[0], triangle.verts[1], v);
			TriangleTree b = new TriangleTree(triangle.verts[1], triangle.verts[2], v);
			TriangleTree c = new TriangleTree(triangle.verts[2], triangle.verts[0], v);
			add(a);
			add(b);
			add(c);
			leafs.add(a);
			leafs.add(b);
			leafs.add(c);
			a.checkFlip();
			b.checkFlip();
			c.checkFlip();
		} else {
			for (TriangleTree t : children) {
				if (t.triangle.contains(v)) {
					t.add(v);
				}
			}
		}
	}

	public void checkFlip() {
		TriangleTree flipWith = null;
		Vertex[] pointOrder = null;
		for (TriangleTree n : leafs.getNeighbors(this)) {
			pointOrder = getPointOrder(n);
			if (triangle.circumcircle.contains(pointOrder[0])) {
				flipWith = n;
				break;
			}
		}
		if (flipWith != null) {
			leafs.remove(this);
			leafs.remove(flipWith);
			Vertex[] myPointOrder = flipWith.getPointOrder(this);
			TriangleTree a = new TriangleTree(myPointOrder[1], pointOrder[0], myPointOrder[0]);
			TriangleTree b = new TriangleTree(pointOrder[1], myPointOrder[0], pointOrder[0]);
			add(a);
			add(b);
			flipWith.add(a);
			flipWith.add(b);
			leafs.add(a);
			leafs.add(b);
			a.checkFlip();
			b.checkFlip();
		}
	}

	// Returns the vertexes of |o|, maintaining clockwise/counterclockwise, and starting with the first point not shared by |this|.
	// TODO(waffles): This seems confusingly backwards.
	private Vertex[] getPointOrder(TriangleTree o) {
		ArrayList<Vertex> mine = new ArrayList<Vertex>(3);
		for (Vertex v : triangle.verts) {
			mine.add(v);
		}
		int i = 0;
		for ( ; i < 3; i++) {
			if (!mine.contains(o.triangle.verts[i])) {
				break;
			}
		}
		Vertex[] ret = new Vertex[3];
		for (int j = i; j < 3; j++) {
			ret[j - i] = o.triangle.verts[j];
		}
		for (int j = 0; j < i; j++) {
			ret[3 - i + j] = o.triangle.verts[j];
		}
		return ret;
	}
}
