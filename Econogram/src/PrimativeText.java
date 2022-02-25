import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class PrimativeText extends DrawPrimative {
	protected Coordinate pos;
	
	protected int colour;
	protected double size;
	protected String text;
	protected double prevWidth;
	protected double prevHeight;
	
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

		internalGraphics2D.setFont(new Font("Arial", Font.PLAIN, (int) (size * settings.zoom)));
		internalGraphics2D.setColor(new Color(parent.isSelected() ? 0xFF5500 : colour));
		internalGraphics2D.drawString(text, (int) x, (int) y);
		
		prevWidth = internalGraphics2D.getFontMetrics().stringWidth(text);
		prevHeight = size;
	}

	@Override
	public double getWidth() {
		return prevWidth;
	}

	@Override
	public double getHeight() {
		return prevHeight;
	}

	@Override
	public double getX() {
		return pos.x;
	}

	@Override
	public double getY() {
		return pos.y - getHeight();
	}
}
