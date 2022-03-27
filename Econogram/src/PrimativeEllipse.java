import java.awt.*;
import java.awt.geom.*;

public class PrimativeEllipse extends DrawPrimative {
	Coordinate point;
	double actualWidth;
	double actualHeight;
	
	int outlineColour;
	int fillColour;

	double width;
	
	public PrimativeEllipse(DrawObject parent, Coordinate p, double w, double h) {
		super(parent);

		point = p;
		actualWidth = w;
		actualHeight = h;
		
		outlineColour = 0x000000;
		fillColour = 0xFFFFFF;
		width = 3.0;
	}

	@Override
	public void draw(Graphics g, ZoomPanSettings settings) {		
		Graphics2D internalGraphics2D = (Graphics2D) g;
		internalGraphics2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		internalGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); 
				
		internalGraphics2D.setColor(new Color(parent.isSelected() ? 0xFF5500 : fillColour));
		internalGraphics2D.fill(new Ellipse2D.Double(point.x * settings.zoom - settings.x, point.y * settings.zoom - settings.y, actualWidth * settings.zoom, actualHeight * settings.zoom));
		internalGraphics2D.setColor(new Color(parent.isSelected() ? 0xFF0000 : outlineColour));
		internalGraphics2D.setStroke(new BasicStroke((float) (width * settings.zoom)));
		internalGraphics2D.draw(new Ellipse2D.Double(point.x * settings.zoom - settings.x, point.y * settings.zoom - settings.y, actualWidth * settings.zoom, actualHeight * settings.zoom));
	}

	@Override
	public double getWidth() {
		return actualWidth;
	}

	@Override
	public double getHeight() {
		return actualHeight;
	}

	@Override
	public double getX() {
		return point.x;
	}

	@Override
	public double getY() {
		return point.y;
	}
}
