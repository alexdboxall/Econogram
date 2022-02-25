import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Canvas extends JPanel {
	
	List<DrawObject> children;
	ZoomPanSettings zoomPanSettings;
	Econogram econogram;
	
	protected double width;
	protected double height;
	
	public double getUsedWidth() {
		return 1000;
	}
	
	public double getZoom() {
		return zoomPanSettings.zoom;
	}
	
	public double getUsedHeight() {
		return 1000;
	}
	
	public void zoomIn() {
		zoomPanSettings.zoom += 0.2;
		if (zoomPanSettings.zoom > 8.0) {
			zoomPanSettings.zoom = 8.0;
		}
		repaint();
	}
	
	public void zoomOut() {
		zoomPanSettings.zoom -= 0.2;
		if (zoomPanSettings.zoom < 0.2) {
			zoomPanSettings.zoom = 0.2;
		}
		repaint();
	}
	
	public double getPanX() {
		return zoomPanSettings.x;
	}
	
	public double getPanY() {
		return zoomPanSettings.y;
	}
	
	public void scrollY(double amount) {
		zoomPanSettings.y += amount;

		if (zoomPanSettings.y < 0.0) {
			zoomPanSettings.y = 0.0;
		}
		
		repaint();
	}
	
	public Canvas(Econogram eg) {
		width = 5000.0;
		height = 3000.0;
		
		econogram = eg;
		
		children = new ArrayList<DrawObject>();
		zoomPanSettings = new ZoomPanSettings();
		
		setPreferredSize(new Dimension((int) width, (int) height));
	}
	
	public void updatePropertiesPanel() {
		econogram.propertiesPanel.regenerate();
	}
	
	public void setPan(double x, double y) {
		zoomPanSettings.x = x;
		zoomPanSettings.y = y;
		
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
		
		if (zoomPanSettings.zoom < 0.2) {
			zoomPanSettings.zoom = 0.2;
		}
		if (zoomPanSettings.zoom > 8.0) {
			zoomPanSettings.zoom = 8.0;
		}
		repaint();
	}
	
	public void addObject(DrawObject obj) {
		children.add(obj);
		obj.setCanvasParent(this);
	}
	
	public String serialise() {		
		String data = String.format("{%f,%f,%f,%f,%f,%d,", width, height, zoomPanSettings.zoom, zoomPanSettings.x, zoomPanSettings.y, children.size());
		
		for (DrawObject child : children) {
			String childSerial = child.serialise();
			data += String.format("%d:%s", childSerial.length(), childSerial);
			
		}
		
		data += "}";
		return data;
	}
	
	public DrawObject getObjectAtPosition(double x, double y) {
		List<DrawPrimative> primatives = getPrimatives();

		for (DrawPrimative primative : primatives) {
			System.out.printf("(%f:%f) %f -> %f, %f -> %f\n", x, y, primative.getX(), primative.getX() + primative.getWidth(), primative.getY(), primative.getY() + primative.getHeight());
			
			if (x >= primative.getX() && x < primative.getX() + primative.getWidth() && y >= primative.getY() && y < primative.getY() + primative.getHeight()) {
				return primative.parent;
			}
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
		
		List<DrawPrimative> primatives = getPrimatives();
		for (DrawPrimative primative : primatives) {
			primative.draw(g, zoomPanSettings);
		}
	}
}
