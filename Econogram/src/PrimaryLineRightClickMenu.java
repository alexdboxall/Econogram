
import java.awt.event.*;
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
		
		List<PrimaryLine> overlaps = ((PrimaryLine) object).getOverlappingPrimaryLines();
		for (PrimaryLine overlappingLine : overlaps) {
			JMenuItem addPoint2 = new JMenuItem("Create Point at Intersection");
			addPoint2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {					
					econogram.actionManager.add(new Action() {
						CalculatedPoint addedChild;

						@Override
						public boolean execute() {
							addedChild = new CalculatedPoint(overlappingLine, (PrimaryLine) object);
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
	}
}
