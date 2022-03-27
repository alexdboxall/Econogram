import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Canvas extends JPanel implements KeyListener {
	List<DrawObject> children;
	ZoomPanSettings zoomPanSettings;
	Econogram econogram;
	boolean showingParentGuides = false;
	boolean showingGrid = false;
	boolean showingPrimaryAxisHint = false;
	double usedWidth = 750;
	double usedHeight = 550;
	int gridMinorColour = 0xC0C0C0;
	int gridMajorColour = 0x808080;
	
	public boolean isShowingPrimaryAxisHint() {
		return showingPrimaryAxisHint;
	}
	
	public void showPrimaryAxisHint(boolean state) {
		showingPrimaryAxisHint = state;
	}
	
	public boolean isShowingGrid() {
		return showingGrid;
	}
	
	public void showGrid(boolean state) {
		showingGrid = state;
	}
	
	public boolean isShowingParentGuides() {
		return showingParentGuides;
	}
	
	public void showParentGuides(boolean state) {
		showingParentGuides = state;
	}

	protected double width;
	protected double height;
	
	public double getUsedWidth() {
		return (usedWidth < 450 ? 750 : usedWidth + 300);
	}
	
	public double getUsedHeight() {
		return (usedHeight < 250 ? 550 : usedHeight + 300);
	}
	
	public double getPrintUsedWidth() {
		return usedWidth;
	}
	
	public double getPrintUsedHeight() {
		return usedHeight;
	}
	
	public double getZoom() {
		return zoomPanSettings.zoom;
	}
	
	public void zoomIn() {
		if (zoomPanSettings.zoom > 0.70 && zoomPanSettings.zoom < 0.95) {
			zoomPanSettings.zoom = 1.0;
		} else {
			zoomPanSettings.zoom += 0.25;
			if (zoomPanSettings.zoom > 5.0) {
				zoomPanSettings.zoom = 5.0;
			}
		}
		repaint();
	}
	
	public void zoomOut() {
		if (zoomPanSettings.zoom > 1.05 && zoomPanSettings.zoom < 1.30) {
			zoomPanSettings.zoom = 1.0;
		} else {
			zoomPanSettings.zoom -= 0.25;
			if (zoomPanSettings.zoom < 0.25) {
				zoomPanSettings.zoom = 0.25;
			}
		}
		repaint();
	}
	
	public double getPanX() {
		return zoomPanSettings.x;
	}
	
	public double getPanY() {
		return zoomPanSettings.y;
	}
	
	public void deleteChild(DrawObject obj) {
		children.remove(obj);
	}
	
	public void scrollY(double amount) {
		zoomPanSettings.y += amount;

		if (zoomPanSettings.y > usedHeight * zoomPanSettings.zoom) {
			zoomPanSettings.y = usedHeight * zoomPanSettings.zoom;
		}
		if (zoomPanSettings.y < 0.0) {
			zoomPanSettings.y = 0.0;
		}
		
		repaint();
	}
	
	public void scrollX(double amount) {
		zoomPanSettings.x += amount;

		if (zoomPanSettings.x > usedWidth * zoomPanSettings.zoom) {
			zoomPanSettings.x = usedWidth * zoomPanSettings.zoom;
		}
		if (zoomPanSettings.x < 0.0) {
			zoomPanSettings.x = 0.0;
		}
		
		repaint();
	}
	
	public Canvas(Econogram eg) {
		width = 5000.0;
		height = 3000.0;
				
        setFocusable(true);
        addKeyListener(this);

		econogram = eg;
		
		children = new ArrayList<DrawObject>();
		zoomPanSettings = new ZoomPanSettings();
		
		setPreferredSize(new Dimension((int) width, (int) height));
	}
	
	Canvas(String serial) {	
        setFocusable(true);
        addKeyListener(this);
        
		children = new ArrayList<DrawObject>();

		String[] parts = serial.substring(1).split(",");
		showingParentGuides = Integer.parseInt(parts[0]) == 1;
		width = Double.parseDouble(parts[1]);
		height = Double.parseDouble(parts[2]);
		
		zoomPanSettings = new ZoomPanSettings();
		zoomPanSettings.zoom = Double.parseDouble(parts[3]);
		zoomPanSettings.x = Double.parseDouble(parts[4]);
		zoomPanSettings.y = Double.parseDouble(parts[5]);
		
		int numChildren = Integer.parseInt(parts[6]);
		
		gridMajorColour = Integer.parseInt(parts[7]);
		gridMinorColour = Integer.parseInt(parts[8]);
		showingGrid = parts[9].charAt(0) == 1;

		int skipIndex = parts[0].length() + 1 + parts[1].length() + 1 + 
						parts[2].length() + 1 + parts[3].length() + 1 + 
						parts[4].length() + 1 + parts[5].length() + 1 +
						parts[6].length() + 1 + parts[7].length() + 1 +
						parts[8].length() + 1 + parts[9].length() + 1;
		
		String allChildrenRemainingData = serial.substring(1).substring(skipIndex);
		
		/*
		 * TODO: work out what is the primary axis and set it correctly
		 */
		
		for (int i = 0; i < numChildren; ++i) {
			String byteString = allChildrenRemainingData.split(":")[0];
			int bytes = Integer.parseInt(byteString) + 11;
			
			allChildrenRemainingData = allChildrenRemainingData.substring(byteString.length() + 1);		//skip the colon
			
			String thisChildData = allChildrenRemainingData.substring(0, bytes);
			allChildrenRemainingData = allChildrenRemainingData.substring(bytes);
			
			String thisChildUniqueID = thisChildData.substring(0, 8);
			int oldUniqueID = Integer.parseInt(thisChildUniqueID, 16);
			thisChildData = thisChildData.substring(8);

			String thisChildType = thisChildData.substring(0, 3);
			thisChildData = thisChildData.substring(3);
						
			if (thisChildType.equals("ARH")) {
				addObject(new Arrowhead(thisChildData, oldUniqueID, this, null));
				
			} else if (thisChildType.equals("AXS")) {
				addObject(new Axis(thisChildData, oldUniqueID, this, null));
			
			} else if (thisChildType.equals("LBL")) {
				addObject(new Label(thisChildData, oldUniqueID, this, null));
			
			} else if (thisChildType.equals("KNS")) {
				addObject(new KeynesianLRAS(thisChildData, oldUniqueID, this, null));
			
			} else if (thisChildType.equals("PNT")) {
				addObject(new Point(thisChildData, oldUniqueID, this, null));
			
			} else if (thisChildType.equals("CPT")) {
				addObject(new CalculatedPoint(thisChildData, oldUniqueID, this, null));
			
			} else if (thisChildType.equals("ARR")) {
				addObject(new Arrow(thisChildData, oldUniqueID, this, null));
			
			}
		}
	}
	
	public String serialise() {		
		String data = String.format("{%d,%f,%f,%f,%f,%f,%d,%d,%d,%d,", 
						showingParentGuides ? 1 : 0, 
						width, 
						height, 
						zoomPanSettings.zoom,
						zoomPanSettings.x, 
						zoomPanSettings.y, 
						children.size(),
						gridMajorColour,
						gridMinorColour,
						showingGrid ? 1 : 0
				);
		

		for (DrawObject child : children) {
			String childSerial = child.serialise();
			data += String.format("%d:%08X%s%s", childSerial.length(), child.hashCode(), child.objectType3DigitID(), childSerial);
			
		}
		
		data += "}";
		return data;
	}
	
	public void print() {
		PrintManager mgr = new PrintManager(this);
		mgr.print();
	}
	
	
	public void export(String filename) throws IOException {
		boolean primaryHintOn = showingPrimaryAxisHint;
		boolean gridOn = showingGrid;
		boolean parentOn = showingParentGuides;
		showingParentGuides = false;
		showingGrid = false;
		showingPrimaryAxisHint = false;
		double zoom = zoomPanSettings.zoom;
		zoomPanSettings.zoom = 4.0;
		
	    BufferedImage imagebuf = new BufferedImage((int)(getUsedWidth() * zoomPanSettings.zoom), (int)(getUsedHeight() * zoomPanSettings.zoom), BufferedImage.TYPE_3BYTE_BGR);
	    Graphics2D graphics2D = imagebuf.createGraphics();
	    graphics2D.setColor(Color.WHITE);
	    graphics2D.fillRect(0, 0, (int)(getUsedWidth() * zoomPanSettings.zoom), (int)(getUsedHeight() * zoomPanSettings.zoom));
	    printAll(graphics2D);
	    ImageIO.write(imagebuf, "png", new File(filename));
	    
	    zoomPanSettings.zoom = zoom;
	    showingPrimaryAxisHint = primaryHintOn;
	    showingGrid = gridOn;
	    showingParentGuides = parentOn;
	}
	
	public void updatePropertiesPanel() {
		econogram.propertiesPanel.regenerate();
	}
	
	public void setPan(double x, double y) {
		zoomPanSettings.x = x;
		zoomPanSettings.y = y;
		
		if (zoomPanSettings.x > usedWidth * zoomPanSettings.zoom) {
			zoomPanSettings.x = usedWidth * zoomPanSettings.zoom;
		}
		if (zoomPanSettings.y > usedHeight * zoomPanSettings.zoom) {
			zoomPanSettings.y = usedHeight * zoomPanSettings.zoom;
		}
		if (zoomPanSettings.x < 0.0) {
			zoomPanSettings.x = 0.0;
		}
		if (zoomPanSettings.y < 0.0) {
			zoomPanSettings.y = 0.0;
		}
		
		repaint();
	}
	
	public void setZoom(double zoom) {
		zoomPanSettings.zoom = zoom;
		
		if (zoomPanSettings.zoom < 0.25) {
			zoomPanSettings.zoom = 0.25;
		}
		if (zoomPanSettings.zoom > 5.0) {
			zoomPanSettings.zoom = 5.0;
		}
		
		repaint();
	}
	
	public void addObject(DrawObject obj) {
		obj.setCanvasParent(this);
		children.add(obj);
	}
	
	public DrawObject getObjectAtPosition(double x, double y) {
		List<DrawPrimative> primatives = getPrimatives();
		
		double lenience = -1.0;

		//start by requiring very high precision (i.e. slightly inside the object)
		//and if nothing's found, keep iterating with more lenience until we find something
		//to click on, or there's no object there (lenience reaches 5.0)
		while (lenience < 4.1) {
			for (DrawPrimative primative : primatives) {			
				if (x >= primative.getX() - lenience && x < primative.getX() + primative.getWidthGivenPosition(x, y) + 2 * lenience && y >= primative.getY() - lenience && y < primative.getY() + primative.getHeightGivenPosition(x, y) + 2 * lenience) {
					return primative.parent;
				}
			}
			lenience += 1.0;
		}
		
		return null;
	}
	
	public List<DrawPrimative> getPrimatives() {
		List<DrawPrimative> primatives = new ArrayList<DrawPrimative>();

		for (DrawObject child : children) {
			primatives.addAll(child.getRender(child.relativePosition));
		}
		
		return primatives;
	}
	
	@Override
	public void paint(Graphics g) { 
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, (int) width, (int) height);
		
		double oldW = getUsedWidth();
		double oldH = getUsedHeight();
		
		if (showingGrid) {
			for (int i = 0; i < 500  / zoomPanSettings.zoom; ++i) {
				int val = (int) (i * 12 * zoomPanSettings.zoom);
				int xshift = (int) -zoomPanSettings.x;
				int yshift = (int) -zoomPanSettings.y;
	
				g.setColor(new Color((i % 4) == 0 ? gridMajorColour : gridMinorColour));
	
				g.drawLine(xshift + 0, yshift + val, xshift + 5000, yshift + val);
				g.drawLine(xshift + val, yshift + 0, xshift + val, yshift + 5000);
			}
		}
		
		usedHeight = 550;
		usedWidth = 750;
		
		List<DrawPrimative> primatives = getPrimatives();
		for (DrawPrimative primative : primatives) {
			primative.draw(g, zoomPanSettings);
			
			if (primative.getX() + primative.getWidth() > usedWidth) {
				usedWidth = primative.getX() + primative.getWidth();
			}
			if (primative.getY() + primative.getHeight() > usedHeight) {
				usedHeight = primative.getY() + primative.getHeight();
			}
		}
		
		if (oldW != getUsedWidth() || oldH != getUsedHeight()) {
			econogram.updateScrollbarSizes();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();		
		String keyString = KeyEvent.getKeyText(keyCode);
		
		if (econogram.propertiesPanel.object != null) {
			double deltaX = 0;
			double deltaY = 0;
			if (keyString.equals("Up")) deltaY = -12;
			if (keyString.equals("Down")) deltaY = 12;
			if (keyString.equals("Left")) deltaX = -12;
			if (keyString.equals("Right")) deltaX = 12;

			/*
			 * C A S	Pixels	Blocks
			 * 0 0 0 	12		1
			 * 0 0 1	6		0.5
			 * 0 1 0	12		1
			 * 0 1 1	1		1/6
			 * 1 0 0	48		4
			 * 1 0 1	24		2
			 * 1 1 0	0.5		1/12
			 * 1 1 1	0.1		1/60
			 */
			
			if (e.isShiftDown() && !e.isControlDown()) {
				deltaX /= 2;
				deltaY /= 2;
				if (e.isAltDown()) {
					deltaX /= 6;
					deltaY /= 6;
				}
			}
			if (e.isControlDown()) {
				deltaX *= 4;
				deltaY *= 4;
				if (e.isShiftDown()) {
					deltaX /= 2;
					deltaY /= 2;
				}
				if (e.isAltDown()) {
					deltaX /= 96;
					deltaY /= 96;
					if (e.isShiftDown()) {
						deltaX /= 2.5;
						deltaY /= 2.5;
					}
				}
			}
			
			if (deltaX != 0 || deltaY != 0) {
				econogram.propertiesPanel.object.mouseDragging(deltaX, deltaY);
				econogram.propertiesPanel.regenerate();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}
}
