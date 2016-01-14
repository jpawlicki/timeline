package com.kaelri.timelines.core.model;

import com.kaelri.timelines.Math2;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WorldBuilder {
	public static TriangleTree tree;
	public static ArrayList<double[]> points = new ArrayList<double[]>();

	public static double camera = 0;
	public static int width = 700;
	public static int height = 700;

	public static int vertexes;

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: java Main [num_vertexes]");
			return;
		}
		vertexes = new Integer(args[0]);

		// Canvas c = new Canvas();
		// Frame f = new Frame();
		// f.setSize(width, height);
		// f.add(c);
		// f.setVisible(true);
		// c.createBufferStrategy(3);
		tree = new TriangleTree();

		for (int i = 6; i < vertexes; i++) {
			tree.add(new Vertex());
		}

		/*
		 * For every triangle, generate one coordinate (the midpoint), attach it to all three vertexes.
		 * For every vertex, sort the coordinates in clockwise order and call that a polygon.
		 */

		HashMap<Vertex, ArrayList<Vertex>> vCoords = new HashMap<>();
		HashMap<Vertex, HashSet<Vertex>> adjacents = new HashMap<>();
		for (TriangleTree t : tree.leafs.leafs) {
			Vertex x = new Vertex(Math2.normalize(t.triangle.circumcircle.circumcenter));
			for (Vertex v : t.triangle.verts) {
				// Add x to v's corners.
				ArrayList<Vertex> vc = vCoords.get(v);
				if (vc == null) {
					vc = new ArrayList<Vertex>();
					vCoords.put(v, vc);
				}
				vc.add(x);
				// Track the adjacent vertexes of v as well.
				HashSet<Vertex> adj = adjacents.get(v);
				if (adj == null) {
					adj = new HashSet<Vertex>();
					adjacents.put(v, adj);
				}
				for (Vertex vv : t.triangle.verts) {
					if (vv != v) {
						adj.add(vv);
					}
				}
			}
		}

		HashMap<Vertex, Region> regionMap = new HashMap<>();
		ArrayList<Region> regions = new ArrayList<>();

		for (Map.Entry<Vertex, ArrayList<Vertex>> e : vCoords.entrySet()) {
			Region r = new Region(e.getKey(), e.getValue().toArray(new Vertex[0]));
			regions.add(r);
			regionMap.put(e.getKey(), r);
		}

		for (Map.Entry<Vertex, Region> e : regionMap.entrySet()) {
			for (Vertex a : adjacents.get(e.getKey())) {
				e.getValue().addAdjacent(regionMap.get(a));
			}
		}

		setBiomes(regions);

		// while(true) {
		// 	update();
		// 	Graphics2D g = (Graphics2D)c.getBufferStrategy().getDrawGraphics();
		// 	g.setPaint(new Color(0, 0, 0));
		// 	g.fillRect(0, 0, 900, 900);
		// 	camera += 0.005;
		// 	draw(g, regions);
		// 	g.dispose();
		// 	c.getBufferStrategy().show();
		// 	try {
		// 		Thread.sleep(5);
		// 	} catch (Exception e) {
		// 	}
		// }
	}

	private static void setBiomes(ArrayList<Region> regions) {
		ArrayList<HashSet<Region>> continents = new ArrayList<>();
		ArrayList<LinkedList<Region>> continentAdjacents = new ArrayList<>();
		HashSet<Region> unmarked = new HashSet<>(regions);
		for (int i = 0; i < 32; i++) { // 32 continental plates.
			Region r = rand(regions);
			while (!unmarked.contains(r)) {
				r = rand(regions);
			}
			unmarked.remove(r);
			HashSet<Region> continent = new HashSet<>();
			continent.add(r);
			continents.add(continent);
			LinkedList<Region> l = new LinkedList<>();
			for (Region a : r.adjacents) {
				l.add(a);
			}
			continentAdjacents.add(l);
		}
		while (!unmarked.isEmpty()) {
			System.out.println(unmarked.size());
			int c = (int)(Math.random() * continents.size());
			LinkedList<Region> candidates = continentAdjacents.get(c);
			if (candidates.isEmpty()) {
				continue;
			}
			Region r = candidates.remove((int)(Math.random() * candidates.size()));
			if (!unmarked.contains(r)) {
				continue;
			}
			unmarked.remove(r);
			continents.get(c).add(r);
			for (Region a : r.adjacents) {
				candidates.add(a);
			}
		}

		// Set the colors.
		for (HashSet<Region> c : continents) {
			boolean oceanic = Math.random() < 0.7;
			for (Region r : c) {
				r.biome = oceanic ? Biome.OCEANIC : Biome.GRASSLAND;
			}
		}
	}

	private static Region rand(List<Region> regions) {
		return regions.get((int)(Math.random() * regions.size()));
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
		return Math2.magnitude3d(Math2.subtract3d(v.point, circumcenter)) < radius;
	}
}

class Triangle {
	public final Sphere circumcircle;
	public final Vertex[] verts;
	protected Triangle() {
		verts = new Vertex[0];
		circumcircle = new Sphere(1, new double[]{0, 0, 0});
	}

	public Triangle(Vertex... verts) {
		this.verts = verts;
		/*
		a = A - C
		b = B - C

		((|a|*|a|*b - |b|*|b|*a) x (a x b))
		/ (2 * |a x b| * |a x b|)
		+ C
		== circumcenter

		radius == |a|*|b|*|a - b|/2/|a x b|
		*/
		double[] a = Math2.subtract3d(verts[0].point, verts[2].point);
		double[] b = Math2.subtract3d(verts[1].point, verts[2].point);
		double la = Math2.magnitude3d(a);
		double lb = Math2.magnitude3d(b);
		double[] axb = Math2.cross3d(a, b);
		double laxb = Math2.magnitude3d(axb);
		double[] circumcenter = Math2.cross3d(Math2.subtract3d(Math2.scale3d(b, la * la), Math2.scale3d(a, lb * lb)), axb);
		circumcenter = Math2.add3d(Math2.scale3d(circumcenter, 1 / (2 * laxb * laxb)), verts[2].point);
		double radius = la * lb * Math2.magnitude3d(Math2.subtract3d(a, b)) / 2 / laxb;
		circumcircle = new Sphere(radius, circumcenter);
	}
	public boolean contains(Vertex v) {
		double a = Math2.dot3d(Math2.cross3d(verts[0].point, verts[1].point), v.point);
		double b = Math2.dot3d(Math2.cross3d(verts[1].point, verts[2].point), v.point);
		double c = Math2.dot3d(Math2.cross3d(verts[2].point, verts[0].point), v.point);
		return a >= 0 && b >= 0 && c >= 0;
	}
	public double[] random() {
		double u = Math.random();
		double w = Math.random();
		if (u + w > 1) {
			u = 1 - u;
			w = 1 - w;
		}
		return Math2.normalize(Math2.add3d(
				Math2.add3d(Math2.scale3d(verts[0].point, 1 - u), Math2.scale3d(verts[1].point, u)),
				Math2.add3d(Math2.scale3d(verts[0].point, 1 - w), Math2.scale3d(verts[2].point, w))));
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
			v = new Vertex(Math2.normalize(
					Math2.scale3d(
					Math2.add3d(v.point,
							Math2.add3d(triangle.verts[0].point,
									Math2.add3d(triangle.verts[1].point, triangle.verts[2].point))), 3)));
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
