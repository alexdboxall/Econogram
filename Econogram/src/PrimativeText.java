
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

public class PrimativeText extends DrawPrimative {
	protected Coordinate pos;
	
	protected int colour;
	protected double size;
	protected String text;
	protected double prevWidth;
	protected double prevHeight;
	
	protected double radians = 0;
	
	public double getRotation(double radians) {
		return radians;
	}
	
	public void setRotation(double radians) {
		this.radians = radians;
	}
	
	public void setFontSize(double size) {
		this.size = size;
		
		prevWidth = text.length() * size / 2;
		prevHeight = size;
	}
	
	public PrimativeText(DrawObject parent, String string, Coordinate position) {
		super(parent);

		pos = position;
		colour = 0x000000;
		size = 20.0;
		text = string;
		
		prevWidth = string.length() * size / 2;
		prevHeight = size;
	}
	
	@Override
	public void draw(Graphics g, ZoomPanSettings settings) {
		double x = pos.x * settings.zoom - settings.x;
		double y = pos.y * settings.zoom - settings.y;
		
		Graphics2D internalGraphics2D = (Graphics2D) g;

		Font normalFont = new Font("Arial", Font.PLAIN, (int) (size * settings.zoom));
		AffineTransform affineTransform = new AffineTransform();
		affineTransform.rotate(-radians, 0, 0);
		Font rotatedFont = normalFont.deriveFont(affineTransform);
		internalGraphics2D.setFont(rotatedFont);
		internalGraphics2D.setColor(new Color(parent.isSelected() ? 0xFF5500 : colour));
		internalGraphics2D.drawString(text, (int) x, (int) y);
		prevWidth = internalGraphics2D.getFontMetrics().stringWidth(text);
		prevHeight = size;
		
		System.out.printf("%f, %f\n", prevWidth, prevHeight);
	}

	@Override
	public double getWidth() {
		return radians != 0 ? prevHeight : prevWidth;
	}

	@Override
	public double getHeight() {
		return radians != 0 ? prevWidth : prevHeight;
	}

	@Override
	public double getX() {
		return pos.x - (radians != 0 ? prevHeight : 0);
	}

	@Override
	public double getY() {
		return pos.y - getHeight();
	}
}
