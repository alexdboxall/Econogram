
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class PrimaryLineRightClickMenu extends RightClickMenu {

	PrimaryLineRightClickMenu(Econogram e, DrawObject c) {
		super(e, c);
	}
	
	public void init() {
		JMenuItem setPrimary = new JMenuItem("Delete");
		setPrimary.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				econogram.actionManager.add(econogram.DELETE_SELECTED_OBJECT.build());
			}
		});
		add(setPrimary); 
		
		add(new JSeparator());
		
		JMenuItem addPoint = new JMenuItem("Create Point");
		addPoint.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				econogram.actionManager.add(econogram.INSERT_BOUND_POINT_AT_MOUSE.build());
			}
		});
		add(addPoint); 
		
		List<PrimaryLine> overlaps = ((PrimaryLine) object).getOverlappingPrimaryLines(econogram.trueMouseDownX, econogram.trueMouseDownY);
		for (PrimaryLine overlappingLine : overlaps) {
			String itext = "Create Point at Intersection";
			
			if (overlappingLine.label != null && ((PrimaryLine) object).label != null) {
				itext = String.format("Create Point at Intersection of %s and %s", ((PrimaryLine) object).label.text.replace("^", ""), overlappingLine.label.text.replace("^", ""));
			}
			
			JMenuItem addPoint2 = new JMenuItem(itext);
			addPoint2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {					
					econogram.actionManager.add(new Action() {
						CalculatedPoint addedChild;

						@Override
						public boolean execute() {
							addedChild = new CalculatedPoint(overlappingLine, (PrimaryLine) object, econogram.primaryAxis);
							econogram.primaryAxis.addChild(addedChild);
							econogram.canvas.repaint();
							
							return false;
						}
				
						@Override
						public boolean undo() {
							return false;
						}
						
						@Override
						public boolean redo() {
							return false;
						}
					});
				}
			});
			add(addPoint2); 
		}
		
		PrimaryLine line = (PrimaryLine) object;
		if (line.label == null || line.label.parent == null || !line.children.contains(line.label)) {
			add(new JSeparator());

			JMenuItem reintroduceLabel = new JMenuItem("Add label");
			reintroduceLabel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					econogram.actionManager.add(new Action() {
						@Override
						public boolean execute() {
							if (line.label != null) {
								line.addChild(line.label);
							} else {
								Label newLabel = new Label(new Coordinate(0.0, 0.0), "");
								line.label = newLabel;
								line.addChild(newLabel);
								line.firstRightmostCalculationDoneYet = false;
								line.recalculateLabelPosition();
								line.addDrawPrimativesPostChild(line.getAbsolutePosition(), new ArrayList<DrawPrimative>());
							}
							
							line.getCanvasParent().repaint();
							return true;
						}
				
						@Override
						public boolean undo() {
							line.label.delete();
							line.getCanvasParent().repaint();
							return true;
						}
						
						@Override
						public boolean redo() {
							line.addChild(line.label);
							line.getCanvasParent().repaint();
							return false;
						}
					});
				}
			});
			add(reintroduceLabel); 
		}
	}
}
