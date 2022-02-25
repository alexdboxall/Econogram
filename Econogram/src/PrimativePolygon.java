import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class PrimativePolygon extends DrawPrimative {
	List<Coordinate> points;
	
	int outlineColour;
	int fillColour;

	double width;
	
	public PrimativePolygon(DrawObject parent, List<Coordinate> p) {
		super(parent);

		//deep copy
		points = new ArrayList<Coordinate>();
		for (Coordinate coord : p) {
			points.add(new Coordinate(coord, new Coordinate(0.0, 0.0)));
		}
		
		outlineColour = 0x000000;
		fillColour = 0xFFFFFF;
		width = 3.0;
	}

	@Override
	public void draw(Graphics g, ZoomPanSettings settings) {		
		Graphics2D internalGraphics2D = (Graphics2D) g;
		internalGraphics2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		internalGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); 
		
		Path2D path = new Path2D.Double();
		path.moveTo(points.get(0).x * settings.zoom - settings.x, points.get(0).y * settings.zoom - settings.y);
		for (int i = 1; i < points.size(); ++i) {
			path.lineTo(points.get(i).x * settings.zoom - settings.x, points.get(i).y * settings.zoom - settings.y);
		}
		path.closePath();
				
		internalGraphics2D.setColor(new Color(parent.isSelected() ? 0xFF5500 : fillColour));
		internalGraphics2D.fill(path);
		internalGraphics2D.setColor(new Color(parent.isSelected() ? 0xFF0000 : outlineColour));
		internalGraphics2D.setStroke(new BasicStroke((float) (width * settings.zoom)));
		internalGraphics2D.draw(path);
	}

	@Override
	public double getWidth() {
		double leftmostPoint = points.get(0).x;
		double rightmostPoint = leftmostPoint;
		
		for (Coordinate coord : points) {
			if (coord.x < leftmostPoint) {
				leftmostPoint = coord.x;
			}
			if (coord.x > rightmostPoint) {
				rightmostPoint = coord.x;
			}
		}
		
		return rightmostPoint - leftmostPoint;
	}

	@Override
	public double getHeight() {
		double highestPoint = points.get(0).y;		//highest as in highest value, aka. lower on the page
		double lowestPoint = highestPoint;
		
		for (Coordinate coord : points) {
			if (coord.y < lowestPoint) {
				lowestPoint = coord.y;
			}
			if (coord.y > highestPoint) {
				highestPoint = coord.y;
			}
		}
		
		return highestPoint - lowestPoint;
	}

	@Override
	public double getX() {
		double leftmostPoint = points.get(0).x;
		
		for (Coordinate coord : points) {
			if (coord.x < leftmostPoint) {
				leftmostPoint = coord.x;
			}
		}
		
		return leftmostPoint;
	}

	@Override
	public double getY() {
		double lowestPoint = points.get(0).y;
		
		for (Coordinate coord : points) {
			if (coord.y < lowestPoint) {
				lowestPoint = coord.y;
			}
		}
		
		return lowestPoint;
	}
}
