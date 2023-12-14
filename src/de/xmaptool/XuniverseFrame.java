package de.xmaptool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class XuniverseFrame extends JFrame implements MouseListener {

	/**
	 * generated serial version id
	 */
	private static final long serialVersionUID = -1796593078694975804L;

	private static final int universe_width = 22;

	private static final int universe_height = 18;

	private static final int sector_witdh = 50;

	private static final int sector_height = 50;

	private static final int frame_witdh = universe_width * 50 + 220;

	private static final int frame_height = universe_height * 50 + 20;

	private static final HashMap<String, Color> byColor = new HashMap<String, Color>();
	static {
		byColor.put("Argonen", Color.YELLOW);
		byColor.put("Boronen", Color.CYAN);
		byColor.put("Goner", Color.YELLOW);
		byColor.put("Khaak", Color.MAGENTA);
		byColor.put("Paraniden", Color.PINK);
		byColor.put("Piraten", Color.LIGHT_GRAY);
		byColor.put("Split", Color.ORANGE);
		byColor.put("Teladi", Color.GREEN);
		byColor.put("Terraner", Color.WHITE);
		byColor.put("Unbekannt", Color.BLUE);
		byColor.put("Yaki", Color.GRAY);
		byColor.put("Xenon", Color.RED);
	}

	private JPanel mainJPanel = new JPanel();

	private ArrayList<String> titleList = new ArrayList<String>();

	private ArrayList<HashMap<String, Sector>> sectorsList = new ArrayList<HashMap<String, Sector>>();

	private ArrayList<HashMap<String, String>> sectorinfosList = new ArrayList<HashMap<String, String>>();

	private ArrayList<Integer> maxJumpsList = new ArrayList<Integer>();

	private HashMap<String, Sector> sectors;

	private HashMap<String, String> sectorinfos;

	private Sector currentSector = null, dragSector = null;

	private ArrayList<Sector> sectorPath = null;

	private ArrayList<Sector> farSectors = new ArrayList<Sector>();

	private int amountOfUniverses;

	private int currentUniverse = 0;

	public XuniverseFrame(List<XuniverseMap> xmaps) {
		super();

		// initialize universes
		initializeUniverses(xmaps);

		// calculate longest jump paths for each sector of each universe
		for (int i = 0; i < amountOfUniverses; i++) {
			sectors = sectorsList.get(i);
			sectorinfos = sectorinfosList.get(i);
			calculateLongestJumpPathsForUniverse();
		}

		// current universe
		currentUniverse = amountOfUniverses - 2;
		sectors = sectorsList.get(currentUniverse);
		sectorinfos = sectorinfosList.get(currentUniverse);
		setTitle(xmaps.get(currentUniverse).getTitle());

		initializeFrame();
	}

	private void initializeUniverses(List<XuniverseMap> xmaps) {
		sectorsList = new ArrayList<HashMap<String, Sector>>();
		for (XuniverseMap xmap : xmaps) {
			titleList.add(xmap.getTitle());
			// map sectors to hashmaps for better performance
			HashMap<String, Sector> sectormap = new HashMap<String, Sector>();
			for (Sector sector : xmap.getSector()) {
				sectormap.put(sector.getId(), sector);
			}
			sectorsList.add(sectormap);
			sectorinfosList.add(new HashMap<String, String>());
		}
		amountOfUniverses = xmaps.size();
	}

	private void calculateLongestJumpPathsForUniverse() {
		int maxJumps = 999;
		for (Sector sector : sectors.values()) {
			if (sector.getGate() != null && !sector.getGate().isEmpty()) {
				HashMap<String, Boolean> reached = new HashMap<String, Boolean>();
				ArrayList<Sector> actual = new ArrayList<Sector>();
				actual.add(sector);
				int jumps = calculateLongestJumpPath(actual, 0, reached, null);
				sectorinfos.put(sector.getId(), String.valueOf(jumps));
				if (maxJumps > jumps) {
					maxJumps = jumps;
				}
				checkGateConsistency(sector);
			}
		}
		maxJumpsList.add(maxJumps);
	}

	private void checkGateConsistency(Sector sector) {
		for (Gate gate : sector.getGate()) {
			Sector dest = sectors.get(gate.getTo());
			if (dest != null) {
				boolean found = false;
				for (Gate destg : dest.getGate()) {
					if (sector.getId().equals(destg.getTo())) {
						found = true;
						break;
					}
				}
				if (!found) {
					System.out.println("Gate-Inkonsistenz zwischen: " + sector.getId() + " / " + dest.getId());
				}
			} else {
				System.out.println("Gate-Fehler in Sektor " + sector.getId());
			}
		}
	}

	private void initializeFrame() {
		setResizable(false);
		setLayout(new BorderLayout());
		@SuppressWarnings("serial")
		JPanel mainJPanel = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(frame_witdh, frame_height);
			}

			@Override
			public void paint(Graphics g) {
				myPaint(g);
			}
		};
		mainJPanel.addMouseListener(this);
		add(mainJPanel);
		pack();
	}

	private void myPaint(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, frame_witdh, frame_height);
		if (sectorPath != null) {
			drawSectorPath(g);
		}
		drawConnectionsAndHighlightMostCentralSectors(g);
		drawSectors(g);
		drawShortInfoOnSectors(g);
		if (currentSector != null) {
			drawSectorInfos(currentSector, g);
		} else {
			drawLegende(g);
		}
		drawBottomTabs(g);
	}

	private void drawSectorPath(Graphics g) {
		g.setColor(Color.BLUE);
		for (Sector sector : sectorPath) {
			g.fillRect((sector.getX() - 1) * sector_witdh, (sector.getY() - 1) * sector_height, sector_witdh,
					sector_height);
		}
	}

	private void drawConnectionsAndHighlightMostCentralSectors(Graphics g) {
		for (Sector sector : sectors.values()) {
			g.setColor(Color.WHITE);
			String sectorinfo = sectorinfos.get(sector.getId());
			if (sectorinfo != null && sectorinfo.equals(String.valueOf(maxJumpsList.get(currentUniverse)))) {
				g.fillRect((sector.getX() - 1) * sector_witdh, (sector.getY() - 1) * sector_height, sector_witdh,
						sector_height);
			}
			for (Gate gate : sector.getGate()) {
				Sector dest = sectors.get(gate.getTo());
				if (dest != null) {
					g.drawLine(sector.getX() * sector_witdh - (sector_witdh / 2),
							sector.getY() * sector_witdh - (sector_witdh / 2),
							dest.getX() * sector_witdh - (sector_witdh / 2),
							dest.getY() * sector_witdh - (sector_witdh / 2));
				}
			}
		}
	}

	private void drawSectors(Graphics g) {
		g.setColor(Color.BLACK);
		g.setFont(new Font("Dialog", Font.PLAIN, 9));
		for (Sector sector : sectors.values()) {
			g.setColor(byColor.get(sector.getBy()));
			g.fill3DRect(sector.getX() * sector_witdh - 45, sector.getY() * sector_height - 45, sector_witdh - 10,
					sector_height - 10, true);
			if ("Goner".equals(sector.getBy())) {
				g.setColor(Color.white);
				g.fill3DRect(sector.getX() * sector_witdh - 40, sector.getY() * sector_height - 40, sector_witdh - 20,
						sector_height - 20, true);
			}
			g.setColor(Color.BLACK);
			g.drawString(sector.getId().length() > 10 ? sector.getId().substring(0, 10) : sector.getId(),
					sector.getX() * sector_witdh - 45, sector.getY() * sector_height - 35);
		}
	}

	private void drawShortInfoOnSectors(Graphics g) {
		g.setColor(Color.BLACK);
		g.setFont(new Font("Dialog", Font.PLAIN, 12));
		for (String key : sectorinfos.keySet()) {
			String info = sectorinfos.get(key);
			Sector sector = sectors.get(key);
			if (info != null && sector != null) {
				g.drawString(info, sector.getX() * sector_witdh - 44, sector.getY() * sector_height - 15);
			}
		}
	}

	private void drawBottomTabs(Graphics g) {
		int yBottom = frame_height - 20;
		g.setColor(Color.WHITE);
		g.drawLine(0, yBottom, 10, yBottom);
		g.drawLine(200 * amountOfUniverses + 10, yBottom, frame_witdh, yBottom);
		for (int i = 0; i < amountOfUniverses; i++) {
			g.drawLine(i * 200 + 10, yBottom, i * 200 + 20, frame_height - 1);
			g.drawLine(i * 200 + 20, frame_height - 1, i * 200 + 200, frame_height - 1);
			g.drawLine(i * 200 + 210, yBottom, i * 200 + 200, frame_height - 1);
			if (i != currentUniverse) {
				g.drawLine(i * 200 + 10, yBottom, i * 200 + 210, yBottom);
			}
			g.setFont(new Font("Dialog", i == currentUniverse ? Font.BOLD : Font.PLAIN, 12));
			g.drawString(titleList.get(i), i * 200 + 30, yBottom + 15);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Sector clickSector = mapCoords2Sector(e.getX(), e.getY());
		if (clickSector != null) {
			currentSector = clickSector;
			mainJPanel.invalidate();
			validate();
			repaint();
		} else if (e.getY() > universe_height * 50) {
			int i = (e.getX() - 10) / 200;
			if (i >= 0 && i < amountOfUniverses) {
				currentUniverse = i;
				sectors = sectorsList.get(currentUniverse);
				sectorinfos = sectorinfosList.get(currentUniverse);
				setTitle(titleList.get(i));
				currentSector = null;
				sectorPath = null;
				mainJPanel.invalidate();
				validate();
				repaint();
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		dragSector = mapCoords2Sector(e.getX(), e.getY());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		Sector sector = mapCoords2Sector(e.getX(), e.getY());
		if (sector != null && dragSector != null && sector != dragSector) {
			// System.out.println("Berechnung kürzester Weg von " + dragSector.getId() + "
			// nach " + sector.getId());
			HashMap<String, Boolean> reached = new HashMap<String, Boolean>();
			ArrayList<Sector> actual = new ArrayList<Sector>();
			actual.add(sector);
			ArrayList<Sector> path = new ArrayList<Sector>();
			path.add(dragSector);
			sectorPath = null;
			sectorPath = calculateShortestJumpPath(actual, reached, path);
			if (sectorPath != null) {
				sectorPath.add(sector);
				mainJPanel.invalidate();
				validate();
				repaint();
			}
		}
		dragSector = null;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// nothing to do
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// nothing to do
	}

	private ArrayList<Sector> calculateShortestJumpPath(ArrayList<Sector> actual, HashMap<String, Boolean> reached,
			ArrayList<Sector> path) {
		ArrayList<Sector> nextSectors = new ArrayList<Sector>();
		Sector from = path.get(path.size() - 1);
		for (Sector sector : actual) {
			reached.put(sector.getId(), Boolean.TRUE);
			for (Gate gate : sector.getGate()) {
				String id = gate.getTo();
				Sector next = sectors.get(id);
				if (next == from) {
					path.add(sector);
					return path;
				}
				if (next != null && !Boolean.TRUE.equals(reached.get(next.getId())) && !nextSectors.contains(next)) {
					nextSectors.add(next);
				}
			}
		}
		if (nextSectors.isEmpty()) {
			return null;
		}
		ArrayList<Sector> result = calculateShortestJumpPath(nextSectors, reached, path);
		if (result != null) {
			from = path.get(path.size() - 1);
			for (Sector sector : actual) {
				for (Gate gate : sector.getGate()) {
					String id = gate.getTo();
					Sector next = sectors.get(id);
					if (next == from) {
						path.add(sector);
						return result;
					}
				}
			}
		}
		return result;
	}

	private int calculateLongestJumpPath(ArrayList<Sector> actual, int jumps, HashMap<String, Boolean> reached,
			Graphics g) {
		ArrayList<Sector> nextSectors = new ArrayList<Sector>();
		for (Sector sector : actual) {
			reached.put(sector.getId(), Boolean.TRUE);
		}
		for (Sector sector : actual) {
			for (Gate gate : sector.getGate()) {
				String id = gate.getTo();
				Sector next = sectors.get(id);
				if (next != null && !Boolean.TRUE.equals(reached.get(next.getId())) && !nextSectors.contains(next)) {
					nextSectors.add(next);
				}
			}
		}
		if (g != null) {
			g.setColor(Color.WHITE);
			g.fillRect(frame_witdh - 210, 50 + (jumps * 20), reached.size() * 200 / sectors.size(), 20);
//			if (jumps >= 19) {
//				System.out.println(reached.size() + " of " + sectors.size() + ", next " + nextSectors.size());
//				for (Sector s : nextSectors) {
//					System.out.println("  " + s.getId());
//				}
//			}
			if (!nextSectors.isEmpty()) {
				farSectors.clear();
				farSectors.addAll(nextSectors);
//                System.out.print(jumps + 1);
//                String sep = ": ";
//                for (Sector s : nextSectors) {
//                	System.out.print(sep + s.getId());
//                	sep = ", ";
//                }
//                System.out.println();
			}
		}
		return nextSectors.isEmpty() ? jumps : calculateLongestJumpPath(nextSectors, jumps + 1, reached, g);
	}

	private void drawLegende(Graphics g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Dialog", Font.PLAIN, 12));
		g.drawString("Legende", frame_witdh - 150, 30);
		int y = 70;
		for (String by : byColor.keySet()) {
			g.setColor(byColor.get(by));
			g.fill3DRect(frame_witdh - 150, y, sector_witdh / 2, sector_height / 2, true);
			if ("Goner".equals(by)) {
				g.setColor(Color.white);
				g.fill3DRect(frame_witdh - 145, y + 5, sector_witdh / 2 - 10, sector_height / 2 - 10, true);
			}
			g.drawString(by, frame_witdh - 110, y + 18);
			y += sector_height / 2 + 10;
		}
	}

	private void drawSectorInfos(Sector sector, Graphics g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Dialog", Font.BOLD, 16));
		g.drawString(sector.getId(), frame_witdh - 210, 30);
		if (sector.getGate() != null && !sector.getGate().isEmpty()) {
			g.setColor(Color.LIGHT_GRAY);
			int n = Integer.valueOf(sectorinfos.get(sector.getId()));
			g.drawRect(frame_witdh - 211, 50, 201, n * 20 + 20);
			HashMap<String, Boolean> reached = new HashMap<String, Boolean>();
			ArrayList<Sector> actual = new ArrayList<Sector>();
			actual.add(sector);
			int jumps = calculateLongestJumpPath(actual, 0, reached, g);
			g.setFont(new Font("Dialog", Font.PLAIN, 12));
			g.drawString(jumps + " Sprünge nach:", frame_witdh - 210, n * 20 + 92);
			int y = n * 20 + 110;
			for (Sector s : farSectors) {
				g.drawString(s.getId(), frame_witdh - 210, y);
				y += 12;
			}
		}
	}

	Sector mapCoords2Sector(int x, int y) {
		Sector result = null;
		if (x <= universe_width * 50 && y <= universe_height * 50) {
			x = x / sector_witdh + 1;
			y = y / sector_height + 1;
			for (Sector sector : sectors.values()) {
				if (sector.getX() == x && sector.y == y) {
					result = sector;
				}
			}
		}
		return result;
	}
}
