import java.awt.*;
import java.awt.geom.Line2D;

public class PrimativeLine extends DrawPrimative {

	Coordinate p1;
	Coordinate p2;
	
	int colour;
	double width;
	
	public boolean showingHints() {
		return getParent().getCanvasParent().isShowingPrimaryAxisHint();
	}
	
	public PrimativeLine(DrawObject parent, Coordinate start, Coordinate end) {
		super(parent);

		p1 = start;
		p2 = end;
		
		colour = 0x000000;
		width = 3.0;
	}

	@Override
	public void draw(Graphics g, ZoomPanSettings settings) {		
		double x1 = p1.x * settings.zoom - settings.x;
		double y1 = p1.y * settings.zoom - settings.y;
		double x2 = p2.x * settings.zoom - settings.x;
		double y2 = p2.y * settings.zoom - settings.y;
			
		Graphics2D internalGraphics2D = (Graphics2D) g;
		internalGraphics2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		internalGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); 

		internalGraphics2D.setColor(new Color(parent.isSelected() ? 0xFF5500 : colour));
		internalGraphics2D.setStroke(new BasicStroke((float) (width * settings.zoom)));
		internalGraphics2D.draw(new Line2D.Double(x1, y1, x2, y2));
		
		if (showingHints()) {
			internalGraphics2D.setColor(new Color(0x00A0A0));
			internalGraphics2D.setStroke(new BasicStroke((float) 0.5));
			
			double minX = x1 < x2 ? x1 : x2;
			double minY = y1 < y2 ? y1 : y2;
			double maxX = x1 > x2 ? x1 : x2;
			double maxY = y1 > y2 ? y1 : y2;
			
			internalGraphics2D.drawRect((int) minX, (int) minY, (int) (maxX - minX + 1), (int) (maxY - minY + 1));
		}
	}

	@Override
	public double getWidth() {
		return Math.abs(p1.x - p2.x) + width;
	}

	@Override
	public double getHeight() {
		return Math.abs(p1.y - p2.y) + width;

	}

	@Override
	public double getX() {
		return (p1.x < p2.x ? p1.x : p2.x) - width;
	}

	@Override
	public double getY() {
		return (p1.y < p2.y ? p1.y : p2.y) - width;
	}

}
