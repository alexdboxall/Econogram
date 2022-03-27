
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.print.*;

public class PrintManager implements Printable {
	Canvas canvas;
	boolean hasCalculatedPrint = false;
	
	PrintManager(Canvas c) {
		canvas = c;
	}
	
	void print() {
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setPrintable(this);
		
		HashPrintRequestAttributeSet printParams = new HashPrintRequestAttributeSet();
		boolean doPrint = job.printDialog(printParams);
		if (doPrint) {
			try {
				prePrintAdjust();
				job.print();
				postPrintAdjust();
				
			} catch (PrinterException e) {
		        JOptionPane.showMessageDialog(null, "An unknown error occured while trying to print.", "Printer Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	boolean primaryHintOn;
	boolean gridOn;
	boolean parentOn;
	double zoom;
	
	private void prePrintAdjust() {
		primaryHintOn = canvas.showingPrimaryAxisHint;
		gridOn = canvas.showingGrid;
		parentOn = canvas.showingParentGuides;
		canvas.showingParentGuides = false;
		canvas.showingGrid = false;
		canvas.showingPrimaryAxisHint = false;
		zoom = canvas.zoomPanSettings.zoom;
		canvas.zoomPanSettings.zoom = 0.8;
	}
	
	private void postPrintAdjust() {
		canvas.zoomPanSettings.zoom = zoom;
		canvas.showingPrimaryAxisHint = primaryHintOn;
		canvas.showingGrid = gridOn;
		canvas.showingParentGuides = parentOn;
	}
	
	int calculatedPagesWidth;
	int calculatedPagesHeight;
	int calculatedTotalPages;
	
	double paperWidth;
	double paperHeight;

	void calculatePrint(Graphics g, PageFormat pageFormat) {
		paperWidth = pageFormat.getWidth();			//in 1/72nds of an inch
		paperHeight = pageFormat.getHeight();		//in 1/72nds of an inch
		
		double documentWidth = canvas.getPrintUsedWidth() * canvas.zoomPanSettings.zoom;
		double documentHeight = canvas.getPrintUsedHeight() * canvas.zoomPanSettings.zoom;
		
		calculatedPagesWidth = (int) Math.ceil(documentWidth / paperWidth);
		calculatedPagesHeight = (int) Math.ceil(documentHeight / paperHeight);
		calculatedTotalPages = calculatedPagesWidth * calculatedPagesHeight;
		
		hasCalculatedPrint = true;
	}
	
	@Override
	public int print(Graphics g, PageFormat pageFormat, int page) throws PrinterException {
		if (!hasCalculatedPrint) {
			calculatePrint(g, pageFormat);
		}
		
		if (page >= calculatedTotalPages) {
			return NO_SUCH_PAGE;
		}
		
		int pageX = page % calculatedPagesWidth;
		int pageY = page / calculatedPagesWidth;

		Graphics2D graphics2D = (Graphics2D) g;
				
		graphics2D.translate(-paperWidth * pageX, -paperHeight * pageY);

	    graphics2D.setColor(Color.WHITE);
	    graphics2D.fillRect(0, 0, (int)(canvas.getUsedWidth() * canvas.zoomPanSettings.zoom), (int)(canvas.getUsedHeight() * canvas.zoomPanSettings.zoom));
	    canvas.printAll(graphics2D);
		
		return PAGE_EXISTS;	
	}
}