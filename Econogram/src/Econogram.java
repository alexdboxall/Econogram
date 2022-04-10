
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

public class Econogram implements MouseWheelListener, MouseListener, MouseMotionListener {
	
	JFrame frame;
	String filename;
	String filepath;
	boolean unsavedChanges;
	
	ActionManager actionManager;
	
	double mouseDownX;
	double mouseDownY;
	boolean panDragMode;
	double panOnMouseDownX;
	double panOnMouseDownY;
	DrawObject draggingObject;
	boolean lineShiftMode = false;
	boolean verticalShiftMode = false;
	double trueMouseDownY;
	double trueMouseDownX;
	double mouseMoveX;
	double mouseMoveY;
	
	MainToolbar mainToolbar;
	
	Canvas canvas;
	Axis primaryAxis;
	PropertiesPanel propertiesPanel;
	
	JScrollBar hzScrollBar;
	JScrollBar vtScrollBar;
	
	JMenuItem showGridlines;
	JMenuItem showPrimaryHint;
	JMenuItem showHideParentGuides;
	JSlider zoomSlider;
	JMenu dummyMenu;
	
	Random rng;
	
	final ActionFactory PRINT_ACTION = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				@Override
				boolean execute() {
					canvas.print();
					return false;
				}

				@Override
				boolean undo() {
					return false;
				}

				@Override
				boolean redo() {
					return false;
				}				
			};
		}
	};
	final ActionFactory EXPORT_ACTION = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				@Override
				boolean execute() {
					export();
					return false;
				}

				@Override
				boolean undo() {
					return false;
				}

				@Override
				boolean redo() {
					return false;
				}				
			};
		}
	};
	
	final ActionFactory REDO_ACTION = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				@Override
				boolean execute() {
					actionManager.redo();
					mainToolbar.updateToolbarEnabledStatus();
					return false;
				}

				@Override
				boolean undo() {
					return false;
				}

				@Override
				boolean redo() {
					return false;
				}				
			};
		}
	};
	
	final ActionFactory UNDO_ACTION = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				@Override
				boolean execute() {
					actionManager.undo();
					mainToolbar.updateToolbarEnabledStatus();
					return false;
				}

				@Override
				boolean undo() {
					return false;
				}

				@Override
				boolean redo() {
					return false;
				}				
			};
		}
	};
	
	final ActionFactory FENCING_NOP_ACTION = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				@Override
				boolean isFence() {
					return true;
				}
				
				@Override
				boolean execute() {
					return true;
				}

				@Override
				boolean undo() {
					return true;
				}

				@Override
				boolean redo() {
					return true;
				}				
			};
		}
	};
	
	final ActionFactory NOP_ACTION = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				@Override
				boolean execute() {
					return false;
				}

				@Override
				boolean undo() {
					return false;
				}

				@Override
				boolean redo() {
					return false;
				}				
			};
		}
	};
	
	final ActionFactory NEW_DOCUMENT_ACTION = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {

				@Override
				boolean execute() {
					Econogram.main(null);
					return false;
				}

				@Override
				boolean undo() {
					return false;
				}

				@Override
				boolean redo() {
					return false;
				}				
			};
		}
	};
	
	final ActionFactory OPEN_ACTION = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {

				@Override
				boolean execute() {
					open();
					return false;
				}

				@Override
				boolean undo() {
					return false;
				}

				@Override
				boolean redo() {
					return false;
				}				
			};
		}
	};
	
	final ActionFactory SAVE_ACTION = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {

				@Override
				boolean execute() {
					save();
					return false;
				}

				@Override
				boolean undo() {
					return false;
				}

				@Override
				boolean redo() {
					return false;
				}				
			};
		}
	};
	
	final ActionFactory DELETE_SELECTED_OBJECT = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				DrawObject object;
				DrawObject objectParent;
				
				@Override
				public boolean execute() {
					if (propertiesPanel.object != null) {
						object = propertiesPanel.object;
						objectParent = object.parent;
						object.delete();
						canvas.repaint();
						return true;
					} else {
						return false;
					}
				}

				@Override
				public boolean undo() {
					if (objectParent != null) {
						objectParent.addChild(object);
					} else {
						canvas.addObject(object);
					}
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					if (object != null) {
						object.delete();
						canvas.repaint();
						return true;
					} else {
						return false;
					}
				}
			};
		}
	};
	
	final ActionFactory INSERT_FREE_POINT_WITHOUT_POSITION = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				double rand1 = getRandomInt(350) + 250.0;
				double rand2 = getRandomInt(250) + 100.0;
				Point addedChild;
				
				@Override
				public boolean execute() {
					if (mouseMoveX > 0 && mouseMoveY > 0) {
						rand1 = (mouseMoveX + canvas.getPanX()) / canvas.getZoom();
						rand2 = (mouseMoveY + canvas.getPanY()) / canvas.getZoom();
					}
					
					addedChild = new Point(new Coordinate(rand1, rand2));
					return redo();
				}


				@Override
				public boolean undo() {
					addedChild.delete();
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					canvas.addObject(addedChild);
					canvas.repaint();
					return true;
				}
			};
		}
	};
	
	final ActionFactory INSERT_FREE_LABEL_WITHOUT_POSITION = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				double rand1 = getRandomInt(350) + 250.0;
				double rand2 = getRandomInt(250) + 100.0;
				Label addedChild;
				
				@Override
				public boolean execute() {
					if (mouseMoveX > 0 && mouseMoveY > 0) {
						rand1 = (mouseMoveX + canvas.getPanX()) / canvas.getZoom();
						rand2 = (mouseMoveY + canvas.getPanY()) / canvas.getZoom();
					}
					
					addedChild = new Label(new Coordinate(rand1, rand2), "New free label");
					return redo();
				}

				@Override
				public boolean undo() {
					addedChild.delete();
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					canvas.addObject(addedChild);
					canvas.repaint();
					return true;
				}
			};
		}
	};
	
	boolean canSetPrimaryAxis() {
		return (propertiesPanel.object != null && propertiesPanel.object.getClass() == (new Axis(new Coordinate(0.0, 0.0))).getClass() && canvas.children.contains(primaryAxis));
	}
	
	final ActionFactory SET_PRIMARY_AXIS = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				Axis newPrimaryAxis = null;
				Axis oldPrimaryAxis = null;
				
				@Override
				public boolean execute() {
					if (propertiesPanel.object != null && propertiesPanel.object.getClass() == (new Axis(new Coordinate(0.0, 0.0))).getClass() && canvas.children.contains(primaryAxis)) {
						oldPrimaryAxis = primaryAxis;
						setPrimaryAxis((Axis) propertiesPanel.object);
						newPrimaryAxis = primaryAxis;
						return true;
					} else {
						JOptionPane.showMessageDialog(frame, "Please select an axis first.", "No axis selected", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
				
				@Override
				public boolean undo() {
					setPrimaryAxis(oldPrimaryAxis);
					return true;
				}
				
				@Override
				public boolean redo() {
					setPrimaryAxis(newPrimaryAxis);
					return true;
				}
			};
		}
	};
	
	final ActionFactory INSERT_BOUND_POINT_WITHOUT_POSITION = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				Point addedChild;
				double rand1 = getRandomInt(350) + 250.0;
				double rand2 = getRandomInt(250) + 100.0;
				
				@Override
				public boolean execute() {
					if (mouseMoveX > 0 && mouseMoveY > 0) {
						rand1 = (mouseMoveX + canvas.getPanX()) / canvas.getZoom();
						rand2 = (mouseMoveY + canvas.getPanY()) / canvas.getZoom();
					}
					addedChild = new Point(new Coordinate(rand1, rand2));
					return redo();
				}
		
				@Override
				public boolean undo() {
					addedChild.delete();
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					primaryAxis.addChild(addedChild);
					canvas.repaint();
					return true;
				}
			};
		}
	};
	
	final ActionFactory INSERT_BOUND_ARROW_WITHOUT_POSITION = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				Arrow addedChild;
				double rand1 = getRandomInt(350) + 250.0;
				double rand2 = getRandomInt(250) + 100.0;
				
				@Override
				public boolean execute() {
					if (mouseMoveX > 0 && mouseMoveY > 0) {
						rand1 = (mouseMoveX + canvas.getPanX()) / canvas.getZoom() - primaryAxis.relativePosition.x;
						rand2 = (mouseMoveY + canvas.getPanY()) / canvas.getZoom() - primaryAxis.relativePosition.y;
					}
					addedChild = new Arrow(new Coordinate(rand1, rand2), 100.0, 0.0);
					return redo();
				}
		
				@Override
				public boolean undo() {
					addedChild.delete();
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					primaryAxis.addChild(addedChild);
					canvas.repaint();
					return true;
				}
			};
		}
	};
	
	final ActionFactory INSERT_BOUND_LABEL_WITHOUT_POSITION = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				Label addedChild;
				double rand1 = getRandomInt(350) + 250.0;
				double rand2 = getRandomInt(250) + 100.0;
				
				@Override
				public boolean execute() {
					if (mouseMoveX > 0 && mouseMoveY > 0) {
						rand1 = (mouseMoveX + canvas.getPanX()) / canvas.getZoom() - primaryAxis.relativePosition.x;
						rand2 = (mouseMoveY + canvas.getPanY()) / canvas.getZoom() - primaryAxis.relativePosition.y;
					}
					addedChild = new Label(new Coordinate(rand1, rand2), "New bound label");
					return redo();
				}
		
				@Override
				public boolean undo() {
					addedChild.delete();
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					primaryAxis.addChild(addedChild);
					canvas.repaint();
					return true;
				}
			};
		}
	};
	
	final ActionFactory INSERT_VERTICAL_LINE = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				SupplyDemandLine addedChild;
				int rand1 = getPseudoRandomPositionBasedOnCount(lrasLabelCounter);

				@Override
				public boolean execute() {
					addedChild = new SupplyDemandLine(new Coordinate(252.0 + rand1, 252.0 + rand1), 1.0, true);
					addedChild.verticalLine = true;
					return redo();
				}

				@Override
				public boolean undo() {
					addedChild.delete();
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					primaryAxis.addChild(addedChild);
					canvas.repaint();
					return true;
				}
			};
		}
	};
	final ActionFactory INSERT_HORIZONTAL_LINE = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				SupplyDemandLine addedChild;
				int rand1 = getPseudoRandomPositionBasedOnCount(hzLineLabelCounter);

				@Override
				public boolean execute() {
					addedChild = new SupplyDemandLine(new Coordinate(252.0 + rand1, 252.0 + rand1), 0.0, false);
					return redo();
				}

				@Override
				public boolean undo() {
					addedChild.delete();
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					primaryAxis.addChild(addedChild);
					canvas.repaint();
					return true;
				}
			};
		}
	};
	
	int lrasLabelCounter = 0;
	int hzLineLabelCounter = 0;
	int demandLineLabelCounter = 0;
	int supplyLineLabelCounter = 0;
	
	int getPseudoRandomPositionBasedOnCount(int count) {
		switch (count) {
		case 0:
			return 0;
		case 1:
			return 42;
		case 2:
			return -42;
		case 3:
			return -82;
		case 4:
			return 82;
		default:
			return getRandomInt(40) * 6 - 120;
		}
	}
	
	final ActionFactory INSERT_DEMAND_LINE = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				SupplyDemandLine addedChild;
				int rand1 = getPseudoRandomPositionBasedOnCount(demandLineLabelCounter);

				@Override
				public boolean execute() {
					addedChild = new SupplyDemandLine(new Coordinate(0.0 + rand1 * 2, 0.0), 1.0, false);
					return redo();
				}

				@Override
				public boolean undo() {
					addedChild.delete();
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					primaryAxis.addChild(addedChild);
					canvas.repaint();
					return true;
				}
			};
		}
	};
	
	final ActionFactory INSERT_KEYNESIAN_LINE = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				KeynesianLRAS addedChild;
				
				int rand1 = getRandomInt(36) * 6 - 36;
				int rand2 = getRandomInt(36) * 6 - 36;

				@Override
				public boolean execute() {
					addedChild = new KeynesianLRAS(new Coordinate(252.0 + rand1, 252.0 + rand2), 120.0);
					return redo();
				}

				@Override
				public boolean undo() {
					addedChild.delete();
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					primaryAxis.addChild(addedChild);
					canvas.repaint();
					return true;
				}
			};
		}
	};
	
	final ActionFactory INSERT_SUPPLY_LINE = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				SupplyDemandLine addedChild;
				int rand1 = getPseudoRandomPositionBasedOnCount(supplyLineLabelCounter);

				@Override
				public boolean execute() {
					addedChild = new SupplyDemandLine(new Coordinate(252.0 + rand1, 252.0 + rand1), -1.0, false);
					return redo();
				}

				@Override
				public boolean undo() {
					addedChild.delete();
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					primaryAxis.addChild(addedChild);
					canvas.repaint();
					return true;
				}
			};
		}
	};
	
	final ActionFactory INSERT_AXIS_ACTION = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				Axis addedChild;
				
				@Override
				public boolean execute() {
					int rand1 = (getRandomInt(350) / 12) * 12;
					int rand2 = (getRandomInt(250) / 12) * 12;
					addedChild = new Axis(new Coordinate(252.0 + rand1, 96.0 + rand2));
					return redo();
				}

				@Override
				public boolean undo() {
					addedChild.delete();
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					canvas.addObject(addedChild);
					canvas.repaint();
					return true;
				}
			};
		}
	};
	
	final ActionFactory INSERT_FREE_LABEL_AT_MOUSE = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				Label addedChild;
				double x;
				double y;
				@Override
				public boolean execute() {
					x = (mouseDownX + canvas.getPanX()) / canvas.getZoom();
					y = (mouseDownY + canvas.getPanY()) / canvas.getZoom();
					return redo();
				}

				@Override
				public boolean undo() {
					addedChild.delete();
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					addedChild = new Label(new Coordinate((int) x, (int) y), "New free label");
					canvas.addObject(addedChild);
					canvas.repaint();
					return true;
				}
			};
		}
	};
	
	final ActionFactory INSERT_BOUND_ARROW_AT_MOUSE = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				Arrow addedChild;
				double x;
				double y;
				@Override
				public boolean execute() {
					x = (mouseDownX + canvas.getPanX()) / canvas.getZoom() - primaryAxis.relativePosition.x;
					y = (mouseDownY + canvas.getPanY()) / canvas.getZoom() - primaryAxis.relativePosition.y;
					return redo();
				}

				@Override
				public boolean undo() {
					addedChild.delete();
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					addedChild = new Arrow(new Coordinate((int) x, (int) y), 100.0, 0.0);
					primaryAxis.addChild(addedChild);
					canvas.repaint();
					return true;
				}
			};
		}
	};
	
	final ActionFactory INSERT_BOUND_LABEL_AT_MOUSE = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				Label addedChild;
				double x;
				double y;
				@Override
				public boolean execute() {
					x = (mouseDownX + canvas.getPanX()) / canvas.getZoom() - primaryAxis.relativePosition.x;
					y = (mouseDownY + canvas.getPanY()) / canvas.getZoom() - primaryAxis.relativePosition.y;
					return redo();
				}

				@Override
				public boolean undo() {
					addedChild.delete();
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					addedChild = new Label(new Coordinate((int) x, (int) y), "New bound label");
					primaryAxis.addChild(addedChild);
					canvas.repaint();
					return true;
				}
			};
		}
	};
	
	final ActionFactory INSERT_FREE_ARROW_AT_MOUSE = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				Arrow addedChild;
				double x;
				double y;
				
				@Override
				public boolean execute() {
					x = (mouseDownX + canvas.getPanX()) / canvas.getZoom();
					y = (mouseDownY + canvas.getPanY()) / canvas.getZoom();
					return redo();
				}

				@Override
				public boolean undo() {
					canvas.deleteChild(addedChild);
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					addedChild = new Arrow(new Coordinate((int) x, (int) y), 100.0, 0.0);
					canvas.addObject(addedChild);
					canvas.repaint();
					return true;
				}
			};
		}
	};
	
	final ActionFactory INSERT_FREE_POINT_AT_MOUSE = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				Point addedChild;
				double x;
				double y;
				
				@Override
				public boolean execute() {
					x = (mouseDownX + canvas.getPanX()) / canvas.getZoom();
					y = (mouseDownY + canvas.getPanY()) / canvas.getZoom();
					return redo();
				}

				@Override
				public boolean undo() {
					canvas.deleteChild(addedChild);
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					addedChild = new Point(new Coordinate((int) x, (int) y));
					canvas.addObject(addedChild);
					canvas.repaint();
					return true;
				}
			};
		}
	};
	
	final ActionFactory INSERT_BOUND_POINT_AT_MOUSE = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				Point addedChild;
				double x;
				double y;
				
				@Override
				public boolean execute() {
					x = (mouseDownX + canvas.getPanX()) / canvas.getZoom() - primaryAxis.relativePosition.x;
					y = (mouseDownY + canvas.getPanY()) / canvas.getZoom() - primaryAxis.relativePosition.y;
					return redo();
				}

				@Override
				public boolean undo() {
					primaryAxis.deleteChild(addedChild);
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					addedChild = new Point(new Coordinate((int) x, (int) y));
					primaryAxis.addChild(addedChild);
					canvas.repaint();
					return true;
				}
			};
		}
	};
	
	boolean canIntersectRecentlyAddedLines() {
		List<DrawObject> children = primaryAxis.children;
		try {
			PrimaryLine line1 = (PrimaryLine) children.get(children.size() - 1);
			PrimaryLine line2 = (PrimaryLine) children.get(children.size() - 2);
			
			if (line1.intersection(line2) != null && line2.intersection(line1) != null) {
				return true;
			}
			
		} catch (Exception e) {
			
		}
		return false;
	}
	
	final ActionFactory INTERSECT_RECENTLY_ADDED_LINES = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {
				Point addedChild;

				@Override
				public boolean execute() {
					List<DrawObject> children = primaryAxis.children;
					try {
						PrimaryLine line1 = (PrimaryLine) children.get(children.size() - 1);
						PrimaryLine line2 = (PrimaryLine) children.get(children.size() - 2);
						
						if (line1.intersection(line2) != null && line2.intersection(line1) != null) {
							addedChild = new CalculatedPoint(line1, line2, primaryAxis);
							primaryAxis.addChild(addedChild);
							canvas.repaint();
							return true;
						}

					} catch (Exception e) {
						
					}
					return false;
				}

				@Override
				public boolean undo() {
					System.out.printf("UNIMPLEMENTED: INTERSECT_RECENTLY_ADDED_LINES.undo()\n");
					return false;
				}
				
				@Override
				public boolean redo() {
					System.out.printf("UNIMPLEMENTED: INTERSECT_RECENTLY_ADDED_LINES.redo()\n");
					return false;
				}
			};
		}
	};
	
	final ActionFactory HIDE_PARENT_GUIDES = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {				
				@Override
				public boolean undo() {
					canvas.showParentGuides(true);
					showHideParentGuides.setText("Hide parent guides");
					canvas.repaint();
					return true;
				}

				@Override
				public boolean execute() {
					canvas.showParentGuides(false);
					showHideParentGuides.setText("Show parent guides");
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					return execute();
				}
			};
		}
	};
	
	final ActionFactory SHOW_PARENT_GUIDES = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {				
				@Override
				public boolean execute() {
					canvas.showParentGuides(true);
					showHideParentGuides.setText("Hide parent guides");
					canvas.repaint();
					return true;
				}

				@Override
				public boolean undo() {
					canvas.showParentGuides(false);
					showHideParentGuides.setText("Show parent guides");
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					return execute();
				}
			};
		}
	};
	
	final ActionFactory HIDE_GRIDLINES = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {				
				@Override
				public boolean undo() {
					canvas.showGrid(true);
					showGridlines.setText("Hide gridlines");
					canvas.repaint();
					return true;
				}

				@Override
				public boolean execute() {
					canvas.showGrid(false);
					showGridlines.setText("Show gridlines");
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					return execute();
				}
			};
		}
	};
	
	final ActionFactory HIDE_PRIMARY_AXIS_HINT = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {				
				@Override
				public boolean undo() {
					canvas.showPrimaryAxisHint(true);
					showPrimaryHint.setText("Hide bounding boxes");
					canvas.repaint();
					return true;
				}

				@Override
				public boolean execute() {
					canvas.showPrimaryAxisHint(false);
					showPrimaryHint.setText("Show bounding boxes");
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					return execute();
				}
			};
		}
	};
	
	final ActionFactory SHOW_PRIMARY_AXIS_HINT = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {				
				@Override
				public boolean execute() {
					canvas.showPrimaryAxisHint(true);
					showPrimaryHint.setText("Hide bounding boxes");
					canvas.repaint();
					return true;
				}

				@Override
				public boolean undo() {
					canvas.showPrimaryAxisHint(false);
					showPrimaryHint.setText("Show bounding boxes");
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					return execute();
				}
			};
		}
	};
	
	final ActionFactory SHOW_GRIDLINES = new ActionFactory() {
		@Override
		public Action build() {
			return new Action() {				
				@Override
				public boolean execute() {
					canvas.showGrid(true);
					showGridlines.setText("Hide gridlines");
					canvas.repaint();
					return true;
				}

				@Override
				public boolean undo() {
					canvas.showGrid(false);
					showGridlines.setText("Show gridlines");
					canvas.repaint();
					return true;
				}
				
				@Override
				public boolean redo() {
					return execute();
				}
			};
		}
	};
	
	void createPointAtMouse(boolean free) {
		if (free) {
			actionManager.add(INSERT_FREE_POINT_AT_MOUSE.build());
			
		} else {
			actionManager.add(INSERT_BOUND_POINT_AT_MOUSE.build());
		}
		
		canvas.repaint();
	}
	
	void createArrowAtMouse(boolean free) {
		if (free) {
			actionManager.add(INSERT_FREE_ARROW_AT_MOUSE.build());
			
		} else {
			actionManager.add(INSERT_BOUND_ARROW_AT_MOUSE.build());
		}
	}
	
	void createLabelAtMouse(boolean free) {
		if (free) {
			actionManager.add(INSERT_FREE_LABEL_AT_MOUSE.build());

		} else {
			actionManager.add(INSERT_BOUND_LABEL_AT_MOUSE.build());
		}
	}
	
	int getRandomInt(int max) {
		if (rng == null) {
			rng = new Random();
		}
		return rng.nextInt(max);
	}
	
	JLabel statusLabel;
	void updateTitle() {
		frame.setTitle(String.format("Econogram - %c%s", unsavedChanges ? '*' : ' ', filename == null ? "Untitled Diagram" : filename));
	}
	
	void updateScrollbarSizes() {
		hzScrollBar.setVisibleAmount((int)((canvas.getWidth() / canvas.getZoom()) / (canvas.getUsedWidth() * canvas.getZoom()) * 1000 / canvas.getZoom()));
		vtScrollBar.setVisibleAmount((int)((canvas.getHeight() / canvas.getZoom()) / (canvas.getUsedHeight() * canvas.getZoom()) * 1000 / canvas.getZoom()));
	}
	
	void performedAction() {
		mainToolbar.updateToolbarEnabledStatus();
		unsavedChanges = true;
		updateTitle();
	}
	
	String compressSerialisedText(String text) {
		String header = "0002";
		String compressed = text.replace("!", "! ").replace("\n", "!N").replace("\r", "!R").replace("\t", "!T").replace(".000000", "!0");
		header += compressed.length();
		header += ";";

		System.out.printf("primaryAxis.getUniqueID() = %d\n", primaryAxis.getUniqueID());
		int hash = 7;
		for (int i = 0; i < compressed.length(); i++) {
		    hash = hash * 31 + compressed.charAt(i);
		} 
		
		header += hash;
		
		return String.format("Econogram! %04d%s%s", header.length(), header, compressed);
	}
	
	static final String OPEN_FILE_ERROR_NEWER_VERSION = "ERROR_NEWER_VERSION";
		
	String decompressSerialisedText(String text) {
		if (!text.startsWith("Econogram! ")) {
			return "";
		}
		text = text.substring(11);
		int headerLength = Integer.parseInt(text.substring(0, 4));
		
		String header = text.substring(4, headerLength + 4);
		int version = Integer.parseInt(header.substring(0, 4));
		text = text.substring(headerLength + 4);

		if (version == 1) {
			return text.replace(" 0", ".000000").replace(" T", "\t").replace(" R", "\r").replace(" N", "\n").replace("  ", " ");
		
		} else if (version == 2) {
			return text.replace("!0", ".000000").replace("!T", "\t").replace("!R", "\r").replace("!N", "\n").replace("! ", "!");

		}
		
		return OPEN_FILE_ERROR_NEWER_VERSION;
	}
	
	void export() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Export...");
		
		FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Image (*.png)", "png");
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);		
		if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {

			String path = fileChooser.getSelectedFile().getAbsolutePath();

			if (!path.toLowerCase().endsWith(".png")) {
				path += ".png";
			}		
			
			try {
				canvas.export(path);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	boolean save() {
		if (filepath == null) {
			return saveAs();
		}
		
		if (filepath == null) {
			JOptionPane.showMessageDialog(frame, "* REPORT THIS BUG *", "Could not save", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		String serial = compressSerialisedText(String.format("%s", canvas.serialise()));
		
		FileOutputStream out;
		try {
			out = new FileOutputStream(filepath);
			out.write(serial.getBytes());
			out.close();
			
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(frame, "The file could not be found, and thus the file could not be saved.", "Could not save", JOptionPane.ERROR_MESSAGE);
			
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame, "An error occured while saving, and thus the file could not be saved.", "Could not save", JOptionPane.ERROR_MESSAGE);
			
		}
				
		System.out.printf("%s\n", serial);
				
		unsavedChanges = false;
		updateTitle();
		
		return true;
	}
	
	void openFilepath(String path, String fname) {
		filepath = path;
		filename = fname;
		unsavedChanges = false;
		
		FileInputStream in;
		try {
			in = new FileInputStream(filepath);
			byte[] data = in.readAllBytes();
			in.close();
			
			frame.remove(canvas);
			
			String dataString = decompressSerialisedText(new String(data, "UTF-8"));
			if (dataString.equals(OPEN_FILE_ERROR_NEWER_VERSION)) {
				JOptionPane.showMessageDialog(frame, "This file was created with a newer version of Econogram.", "Econogram", JOptionPane.ERROR_MESSAGE);
				return;
			}
			Canvas newCanvas = new Canvas(dataString);
			newCanvas.econogram = new Econogram(frame, newCanvas);
			
			for (DrawObject obj : newCanvas.children) {
				if (obj.getClass() == (new Axis(new Coordinate(0.0, 0.0))).getClass()) {
					if (((Axis) obj).makeThisPrimaryOnReload) {
						((Axis) obj).makeThisPrimaryOnReload = false;
						newCanvas.econogram.primaryAxis = (Axis) obj;
						break;
					}
				}
			}
			
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(frame, "An error occured when trying to open the file.", "Econogram", JOptionPane.ERROR_MESSAGE);
			
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame, "An error occured when trying to open the file.", "Econogram", JOptionPane.ERROR_MESSAGE);
			
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "The file is either not an Econogram file, or is corrupt.", "Econogram", JOptionPane.ERROR_MESSAGE);
		}
		
		updateTitle();
	}
	
	void open() {
		if (!saveChangesDialog()) return;
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Open...");
		
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Econogram Diagram (*.edi)", "edi");
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(true);
        
		if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			
			String path = fileChooser.getSelectedFile().getAbsolutePath();
			String name = fileChooser.getSelectedFile().getName();

			if (!path.toLowerCase().endsWith(".edi")) {
				name += ".edi";
				path += ".edi";
			}
			
			openFilepath(path, name);
		}
	}
	
	boolean saveAs() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save As...");
		
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Econogram Diagram (*.edi)", "edi");
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(true);
        
		if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
			
			String path = fileChooser.getSelectedFile().getAbsolutePath();
			filename = fileChooser.getSelectedFile().getName();

			if (!path.toLowerCase().endsWith(".edi")) {
				filename += ".edi";
				path += ".edi";
			}
			
			filepath = path;
			return save();
		}
		
		return false;
	}
	
	void updateZoomSlider() {
		zoomSlider.setValue((int) (canvas.getZoom() * 100));
	}
	
	void addViewMenuBar(JMenuBar menu) {
		JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic('V');
		menu.add(viewMenu);
		
		JMenuItem zoom100Button = new JMenuItem("Zoom to 100%            ");
		zoom100Button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		zoom100Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.setZoom(1.0);
				updateZoomSlider();
				updateScrollbarSizes();
			}
		});
		viewMenu.add(zoom100Button);
		
		JMenuItem zoomInButton = new JMenuItem("Zoom In");
		zoomInButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		zoomInButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.zoomIn();
				updateZoomSlider();
				updateScrollbarSizes();
			}
		});
		viewMenu.add(zoomInButton);
		
		JMenuItem zoomOutButton = new JMenuItem("Zoom Out");
		zoomOutButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		zoomOutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.zoomOut();
				updateZoomSlider();
				updateScrollbarSizes();
			}
		});
		viewMenu.add(zoomOutButton);
		
		viewMenu.add(new JSeparator());
		
		showHideParentGuides = new JMenuItem(canvas.isShowingParentGuides() ? "Hide parent guides" : "Show parent guides");
		showHideParentGuides.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (canvas.isShowingParentGuides()) {
					actionManager.add(HIDE_PARENT_GUIDES.build());

				} else {
					actionManager.add(SHOW_PARENT_GUIDES.build());
				}
				canvas.repaint();
			}
		});
		viewMenu.add(showHideParentGuides);
		
		showGridlines = new JMenuItem(canvas.isShowingParentGuides() ? "Hide gridlines" : "Show gridlines");
		showGridlines.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (canvas.isShowingGrid()) {
					actionManager.add(HIDE_GRIDLINES.build());

				} else {
					actionManager.add(SHOW_GRIDLINES.build());
				}
				canvas.repaint();
			}
		});
		viewMenu.add(showGridlines);
		
		showPrimaryHint = new JMenuItem(canvas.isShowingParentGuides() ? "Hide bounding boxes" : "Show bounding boxes");
		showPrimaryHint.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (canvas.isShowingPrimaryAxisHint()) {
					actionManager.add(HIDE_PRIMARY_AXIS_HINT.build());

				} else {
					actionManager.add(SHOW_PRIMARY_AXIS_HINT.build());
				}
				canvas.repaint();
			}
		});
		viewMenu.add(showPrimaryHint);
		
		//
	}
	
	void addHelpMenuBar(JMenuBar menu) {
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		menu.add(helpMenu);
		
		JMenuItem howToBtn = new JMenuItem("Help...");
		howToBtn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		howToBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});
		helpMenu.add(howToBtn);
		
		JMenuItem aboutBtn = new JMenuItem("About...           ");
		aboutBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				final ImageIcon icon = new ImageIcon();
				try {
					icon.setImage(ImageIO.read(getClass().getResourceAsStream("/img/icon.png")));
				} catch (IOException e1) {
				}
		        JOptionPane.showMessageDialog(null, "Econogram\nVersion 0.1\n\nCopyright Alex Boxall 2022\nAll rights reserved.\n", "About", JOptionPane.INFORMATION_MESSAGE, icon);
			}
		});
		helpMenu.add(aboutBtn);
		
		JMenuItem aboutBtn2 = new JMenuItem("Copyright Notices               ");
		aboutBtn2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPanel mainPanel = new JPanel();

				JTextArea area = new JTextArea(30, 120);
			    JScrollPane scrollPane = new JScrollPane (area);
			    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			 				
				String copyrightText = "";
				
				String licenseData[][] = {
						{"src/licensing/ICON COPYRIGHT", "the Silk Icon Set 1.3 by Mark James"},
					};
				
				for (String license[] : licenseData) {
					copyrightText += String.format("\n\n    This software uses %s.\n\n", license[1]);
					try {
						FileInputStream fstream = new FileInputStream(license[0]);
						BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
	
						String strLine;
	
						while ((strLine = br.readLine()) != null)   {
							copyrightText += String.format("         ---  %s\n", strLine);
						}
	
						fstream.close();
						
					} catch (Exception exception) {
						copyrightText = "\n\n     ** THIS IS A BUG ** \n\n   Please report this immediately.\n\n\n" + copyrightText;
					}
					
					copyrightText += "\n-----------------------------------------------------\n";
				}
				
				copyrightText += "\nThis software dedicated to NMI";
				area.setText(copyrightText);
				area.setCaretPosition(0);
				area.setEditable(false);
				
			    mainPanel.add(scrollPane);

				JDialog copyrightFrame = new JDialog(frame, "Copyright Notices", true);
				copyrightFrame.setContentPane(mainPanel);
				copyrightFrame.pack();
				copyrightFrame.setVisible(true);
			}
		});
		helpMenu.add(aboutBtn2);
	}
	
	boolean canDelete() {
		return propertiesPanel.object != null;
	}
	
	void addWindowMenuBar(JMenuBar menu) {
		JMenu windowMenu = new JMenu("Window");
		windowMenu.setMnemonic('W');
		menu.add(windowMenu);
		
		
	}
	
	void setPrimaryAxis(Axis a) {
		if (a == null) {
			JOptionPane.showMessageDialog(frame, "Please select an axis first.", "Econogram", JOptionPane.ERROR_MESSAGE);

		} else {
			primaryAxis = a;
			canvas.repaint();
		}
	}
	
	void addToolsMenuBar(JMenuBar menu) {
		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic('T');

		menu.add(toolsMenu);
		
		
	}
	
	void addInsertMenuBar(JMenuBar menu) {
		JMenu insertMenu = new JMenu("Insert");
		insertMenu.setMnemonic('I');
		menu.add(insertMenu);
		
		
		JMenuItem axisButton = new JMenuItem("Axis");
		axisButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionManager.add(INSERT_AXIS_ACTION.build());	
			}
		});
		insertMenu.add(axisButton);
		
		insertMenu.add(new JSeparator());
		
		JMenuItem sdButton = new JMenuItem("Supply Line");
		sdButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		sdButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionManager.add(INSERT_SUPPLY_LINE.build());	
			}
		});
		insertMenu.add(sdButton);
		
		JMenuItem sd2Button = new JMenuItem("Demand Line");
		sd2Button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		sd2Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionManager.add(INSERT_DEMAND_LINE.build());	
			}
		});
		insertMenu.add(sd2Button);
		
		JMenuItem hzButton = new JMenuItem("Horizontal Line");
		hzButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		hzButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionManager.add(INSERT_HORIZONTAL_LINE.build());	
			}
		});
		insertMenu.add(hzButton);
		
		JMenuItem vtButton = new JMenuItem("Vertical Line");
		vtButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		vtButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionManager.add(INSERT_VERTICAL_LINE.build());	
			}
		});
		insertMenu.add(vtButton);
		
		JMenuItem keynesButton = new JMenuItem("Keynesian Line");
		keynesButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		keynesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionManager.add(INSERT_KEYNESIAN_LINE.build());	
			}
		});
		insertMenu.add(keynesButton);
		
		insertMenu.add(new JSeparator());

		JMenuItem arrowButton = new JMenuItem("Arrow");
		arrowButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (primaryAxis == null || !canvas.children.contains(primaryAxis)) {
					JOptionPane.showMessageDialog(frame, "There is no primary axis selected. Select an axis\nand then go to Edit > Set Primary Axis", "Econogram", JOptionPane.ERROR_MESSAGE);
				} else {
					actionManager.add(INSERT_BOUND_ARROW_WITHOUT_POSITION.build());	
				}
			}
		});
		insertMenu.add(arrowButton);
		
		JMenuItem labelButton1 = new JMenuItem("Label");
		labelButton1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		labelButton1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (primaryAxis == null || !canvas.children.contains(primaryAxis)) {
					JOptionPane.showMessageDialog(frame, "There is no primary axis selected. Select an axis\nand then go to Edit > Set Primary Axis", "Econogram", JOptionPane.ERROR_MESSAGE);
				} else {
					actionManager.add(INSERT_BOUND_LABEL_WITHOUT_POSITION.build());	
				}
			}
		});
		insertMenu.add(labelButton1);
		
		JMenuItem labelButton2 = new JMenuItem("Free Label          ");
		labelButton2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		labelButton2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionManager.add(INSERT_FREE_LABEL_WITHOUT_POSITION.build());	
			}
		});
		insertMenu.add(labelButton2);
		
		JMenuItem pointButton1 = new JMenuItem("Point");
		pointButton1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		pointButton1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (primaryAxis == null || !canvas.children.contains(primaryAxis)) {
					JOptionPane.showMessageDialog(frame, "There is no primary axis. Select an axis\nand then go to Edit > Set Primary Axis", "No primary axis", JOptionPane.ERROR_MESSAGE);
				} else {
					actionManager.add(INSERT_BOUND_POINT_WITHOUT_POSITION.build());	
				}
			}
		});
		insertMenu.add(pointButton1);
		
		JMenuItem pointButton2 = new JMenuItem("Free Point");
		pointButton2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		pointButton2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionManager.add(INSERT_FREE_POINT_WITHOUT_POSITION.build());	
			}
		});
		insertMenu.add(pointButton2);
		
		insertMenu.add(new JSeparator());
		
		JMenuItem intersectNew = new JMenuItem("Intersect Recently Added Lines  ");
		intersectNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		intersectNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionManager.add(INTERSECT_RECENTLY_ADDED_LINES.build());	
			}
		});
		insertMenu.add(intersectNew);
		
		insertMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				intersectNew.setEnabled(canIntersectRecentlyAddedLines());
			}

			@Override
			public void menuDeselected(MenuEvent e) {
				intersectNew.setEnabled(canIntersectRecentlyAddedLines());
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				intersectNew.setEnabled(canIntersectRecentlyAddedLines());
			}
		});
		
	}
	

	void addEditMenuBar(JMenuBar menu) {
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic('E');
		menu.add(editMenu);
		
		JMenuItem undoButton = new JMenuItem("Undo     ");
		undoButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		undoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionManager.add(UNDO_ACTION.build());
			}
		});
		editMenu.add(undoButton);

		JMenuItem redoButton = new JMenuItem("Redo     ");
		redoButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		redoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionManager.add(REDO_ACTION.build());
			}
		});
		editMenu.add(redoButton);
		JMenuItem redoButton2 = new JMenuItem("...");
		redoButton2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, java.awt.event.InputEvent.SHIFT_DOWN_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		redoButton2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionManager.add(REDO_ACTION.build());
			}
		});
		dummyMenu.add(redoButton2);
		
		JMenuItem deleteButton = new JMenuItem("Delete     ");
		deleteButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionManager.add(DELETE_SELECTED_OBJECT.build());
			}
		});
		editMenu.add(deleteButton);
		JMenuItem deleteButton2 = new JMenuItem("...");
		deleteButton2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));
		deleteButton2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionManager.add(DELETE_SELECTED_OBJECT.build());
			}
		});
		dummyMenu.add(deleteButton2);
	
		editMenu.add(new JSeparator());

		JMenuItem setAxisButton = new JMenuItem("Set Primary Axis      ");
		setAxisButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		setAxisButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionManager.add(SET_PRIMARY_AXIS.build());
			}
		});
		editMenu.add(setAxisButton);
		
		editMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				undoButton.setEnabled(actionManager.canUndo());
			}

			@Override
			public void menuDeselected(MenuEvent e) {
				undoButton.setEnabled(actionManager.canUndo());
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				undoButton.setEnabled(actionManager.canUndo());
			}
		});
		
		dummyMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				redoButton.setEnabled(actionManager.canRedo());
				redoButton2.setEnabled(actionManager.canRedo());
			}

			@Override
			public void menuDeselected(MenuEvent e) {
				redoButton.setEnabled(actionManager.canRedo());
				redoButton2.setEnabled(actionManager.canRedo());
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				redoButton.setEnabled(actionManager.canRedo());
				redoButton2.setEnabled(actionManager.canRedo());
			}
		});
		
		dummyMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				deleteButton.setEnabled(canDelete());
				deleteButton2.setEnabled(canDelete());
			}

			@Override
			public void menuDeselected(MenuEvent e) {
				deleteButton.setEnabled(canDelete());
				deleteButton2.setEnabled(canDelete());
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				deleteButton.setEnabled(canDelete());
				deleteButton2.setEnabled(canDelete());
			}
		});
		
		editMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				redoButton.setEnabled(actionManager.canRedo());
				redoButton2.setEnabled(actionManager.canRedo());
			}

			@Override
			public void menuDeselected(MenuEvent e) {
				redoButton.setEnabled(actionManager.canRedo());
				redoButton2.setEnabled(actionManager.canRedo());
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				redoButton.setEnabled(actionManager.canRedo());
				redoButton2.setEnabled(actionManager.canRedo());
			}
		});
		
		editMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				deleteButton.setEnabled(canDelete());
				deleteButton2.setEnabled(canDelete());
			}

			@Override
			public void menuDeselected(MenuEvent e) {
				deleteButton.setEnabled(canDelete());
				deleteButton2.setEnabled(canDelete());
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				deleteButton.setEnabled(canDelete());
				deleteButton2.setEnabled(canDelete());
			}
		});
		
		editMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				setAxisButton.setEnabled(canSetPrimaryAxis());
			}

			@Override
			public void menuDeselected(MenuEvent e) {
				setAxisButton.setEnabled(canSetPrimaryAxis());				
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				setAxisButton.setEnabled(canSetPrimaryAxis());				
			}
		});
	}
	
	boolean saveChangesDialog() {
		if (unsavedChanges) {
			String[] options = {"Save", "Don't Save", "Cancel"};
			int result = JOptionPane.showOptionDialog(frame, "Would you like to save your changes?\nUnsaved changed will be lost.", "Save?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			if (result == 0) {
				// "yes"
				boolean actuallySaved = save();
				if (!actuallySaved) {
					return false;
				}
				
			} else if (result == 1) {
				// "no"
				return true;
				
			} else {
				// "cancel" is 2 and pressing ESC is -1
				// any future return codes added to Java should "do no harm" and act as cancel
				return false;
			}
		}
		return true;
	}
	
	void quit() {
		if (!saveChangesDialog()) return;
		frame.dispose();
	}
	
	void addFileMenuBar(JMenuBar menu) {
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		menu.add(fileMenu);
		
		
		JMenuItem newButton = new JMenuItem("New from Template...            ");
		newButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});
		fileMenu.add(newButton);
		
		JMenuItem new2Button = new JMenuItem("New Blank Diagram");
		new2Button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, java.awt.event.InputEvent.SHIFT_DOWN_MASK | Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		new2Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		fileMenu.add(new2Button);
		
		JMenuItem new3Button = new JMenuItem("New Window");
		new3Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Econogram.main(null);
			}
		});
		fileMenu.add(new3Button);
		
		JMenuItem openButton = new JMenuItem("Open...");
		openButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				open();
			}
		});
		fileMenu.add(openButton);
		
		fileMenu.add(new JSeparator());
		
		JMenuItem saveButton = new JMenuItem("Save");
		saveButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		fileMenu.add(saveButton);
		
		JMenuItem saveAsButton = new JMenuItem("Save As...");
		saveAsButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_DOWN_MASK | Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		saveAsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAs();
			}
		});
		fileMenu.add(saveAsButton);
		
		fileMenu.add(new JSeparator());

		
		JMenuItem exportButton = new JMenuItem("Export...");
		exportButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		exportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				export();
			}
		});
		fileMenu.add(exportButton);
		
		fileMenu.add(new JSeparator());
		
		JMenuItem printButton = new JMenuItem("Print");
		printButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		printButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.print();
			}
		});
		fileMenu.add(printButton);
		
		fileMenu.add(new JSeparator());
		
		JMenuItem exitButton = new JMenuItem("Exit");
		exitButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_DOWN_MASK));
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});
		fileMenu.add(exitButton);
	
	}
	
	Econogram(JFrame inFrame, Canvas inCanvas) {
		actionManager = new ActionManager(this);
				
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		
		unsavedChanges = false;
		filename = null;
		filepath = null;
		
		frame = inFrame;
		for (WindowListener wa : frame.getWindowListeners()) {
			frame.removeWindowListener(wa);										//ensures we don't end up with multiple calls to quit() after save/load is done 
		}
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent event) {
		    	System.out.printf("About to call quit(). hc = 0x%X\n", this.hashCode());
		        quit();
		    }
		});
		
		
		try {
			ImageIcon icon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(new File("econogram.exe"));
			frame.setIconImage(icon.getImage());
		} catch (Exception e) { ; }
		
		canvas = inCanvas == null ? new Canvas(this) : inCanvas;
		canvas.setZoom(1.0);
		canvas.setPan(0.0, 0.0);
		primaryAxis = new Axis(new Coordinate(144, 96));
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseWheelListener(this);
		
		propertiesPanel = new PropertiesPanel();
		
		JPanel renderScrollPane = new JPanel(new BorderLayout());
		renderScrollPane.add(canvas);
		
		hzScrollBar = new JScrollBar();
		hzScrollBar.setOrientation(Adjustable.HORIZONTAL);
		hzScrollBar.setUnitIncrement(10);
		hzScrollBar.setMinimum(0);
		hzScrollBar.setMaximum(1000);
		hzScrollBar.setVisibleAmount((int)(canvas.getWidth() / canvas.getUsedWidth() * 1000));
		hzScrollBar.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				double pos = hzScrollBar.getValue();
				double relativePos = (pos / ((double)(hzScrollBar.getMaximum() - hzScrollBar.getMinimum()))) * canvas.getUsedWidth();
				canvas.setPan(relativePos * canvas.getZoom(), canvas.getPanY());
			}
		});
		renderScrollPane.add(hzScrollBar, BorderLayout.SOUTH);
		
		
		vtScrollBar = new JScrollBar();
		vtScrollBar.setOrientation(Adjustable.VERTICAL);
		vtScrollBar.setUnitIncrement(10);
		vtScrollBar.setMinimum(0);
		vtScrollBar.setMaximum(1000);
		vtScrollBar.setVisibleAmount((int)(canvas.getHeight() / canvas.getUsedHeight() * 1000));
		vtScrollBar.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				double pos = vtScrollBar.getValue();
				double relativePos = (pos / ((double)(vtScrollBar.getMaximum() - vtScrollBar.getMinimum()))) * canvas.getUsedHeight();
				canvas.setPan(canvas.getPanX(), relativePos * canvas.getZoom());
			}
		});
		renderScrollPane.add(vtScrollBar, BorderLayout.EAST);
		
		renderScrollPane.setPreferredSize(new Dimension(850, 750));
		
		
		
		JLabel zoomLabel;

		statusLabel = new JLabel("Ready");
		statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));

		JPanel statusBar = new JPanel(new BorderLayout());
		JPanel innerStatusBar = new JPanel(new BorderLayout());
		statusBar.setBorder(new MatteBorder(1, 0, 0, 0, Color.BLACK));
		statusBar.add(innerStatusBar);
		innerStatusBar.setBorder(new EmptyBorder(5, 10, 5, 10));
		innerStatusBar.add(statusLabel, BorderLayout.WEST);
		
		JPanel zoomPanel = new JPanel();
		
		zoomSlider = new JSlider(25, 500, 100);
		zoomSlider.setFocusable(false);
		zoomSlider.setPaintTicks(true);
		zoomSlider.setMajorTickSpacing(25);
		zoomSlider.setPreferredSize(new Dimension(120, 20));

		JButton zoomOutButton = new JButton("-");
		zoomOutButton.setFocusable(false);
		zoomOutButton.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		zoomOutButton.setBorder(new EmptyBorder(4, 8, 4, 8));
		zoomOutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.zoomOut();
				updateZoomSlider();
				updateScrollbarSizes();
			}
		});
		JButton zoomInButton = new JButton("+");
		zoomInButton.setFocusable(false);
		zoomInButton.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		zoomInButton.setBorder(new EmptyBorder(4, 8, 4, 8));
		zoomInButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.zoomIn();	
				updateZoomSlider();
				updateScrollbarSizes();
			}
		});
		zoomLabel = new JLabel("100%");
		zoomLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		zoomLabel.setPreferredSize(new Dimension(35, 25));
		JLabel zoomLabel2 = new JLabel("Zoom: ");
		zoomLabel2.setFont(new Font("Arial", Font.PLAIN, 12));
		zoomPanel.add(zoomLabel2, BorderLayout.EAST);
		zoomPanel.add(zoomOutButton);
		zoomPanel.add(zoomSlider);
		zoomPanel.add(zoomInButton);
		zoomPanel.add(zoomLabel);
		zoomSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
            	zoomLabel.setText(String.format("%d%%", zoomSlider.getValue()));
            	canvas.setZoom(((double) zoomSlider.getValue()) / 100.0);
				updateZoomSlider();
				updateScrollbarSizes();
            }
        });
		
		statusBar.add(zoomPanel, BorderLayout.EAST);
		

		JPanel mainGroup = new JPanel(new BorderLayout());
		mainToolbar = new MainToolbar(this);
		mainGroup.add(mainToolbar, BorderLayout.PAGE_START);

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, renderScrollPane, propertiesPanel);
		split.setResizeWeight(1);

		Dimension minimumSize = new Dimension(0, 0);
		split.getLeftComponent().setMinimumSize(minimumSize);
		split.getRightComponent().setMinimumSize(minimumSize);
		
		mainGroup.add(split, BorderLayout.CENTER);
		mainGroup.add(statusBar, BorderLayout.PAGE_END);
		
		JMenuBar menu = new JMenuBar();
		frame.setJMenuBar(menu);
		
		dummyMenu = new JMenu("");
		addFileMenuBar(menu);
		addEditMenuBar(menu);
		addViewMenuBar(menu);
		addInsertMenuBar(menu);
		addToolsMenuBar(menu);
		//addWindowMenuBar(menu);
		addHelpMenuBar(menu);
		menu.add(dummyMenu);

		frame.setPreferredSize(new Dimension(1400, 900));
		frame.setContentPane(mainGroup);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateScrollbarSizes();
				
				Dimension defaultSize = new Dimension(250, 0);
				split.getRightComponent().setSize(defaultSize);
			}
		});
        
		canvas.addComponentListener(new ComponentAdapter() {
		    public void componentResized(ComponentEvent componentEvent) {
		    	updateScrollbarSizes();
		    }
		});
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {		
		mouseMoveX = e.getX();
		mouseMoveY = e.getY();
		
		int notches = e.getWheelRotation();

		if (e.isControlDown()) {
			if (notches > 0) canvas.zoomOut();
			else if (notches < 0) canvas.zoomIn();
			
			updateZoomSlider();
			updateScrollbarSizes();
			
		} else {
			if (e.isShiftDown()) {		//Windows (and Mac?) use this to do horizontal scrollwheel events
				canvas.scrollX(((double) notches) * 25.0);
				
				double posx = ((canvas.zoomPanSettings.x) * ((double)(hzScrollBar.getMaximum() - hzScrollBar.getMinimum()))) / canvas.getUsedWidth() / canvas.getZoom();
				hzScrollBar.setValue((int) posx);
				
			} else {
				canvas.scrollY(((double) notches) * 25.0);
				
				double posy = ((canvas.zoomPanSettings.y) * ((double)(vtScrollBar.getMaximum() - vtScrollBar.getMinimum()))) / canvas.getUsedHeight() / canvas.getZoom();
				vtScrollBar.setValue((int) posy);
			}
			
		}
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		mouseMoveX = e.getX();
		mouseMoveY = e.getY();
	}

	boolean wantFenceAddedOnMouseLift = false;
	boolean addedFenceOnMouseDragYet = false;
	int mouseDragEventsSinceClick = 0;
	
	@Override
	public void mousePressed(MouseEvent e) {
		wantFenceAddedOnMouseLift = false;
		addedFenceOnMouseDragYet = false;
		mouseDragEventsSinceClick = 0;
		
		mouseMoveX = e.getX();
		mouseMoveY = e.getY();
		mouseDownX = e.getX();
		mouseDownY = e.getY();
		
		double x = (e.getX() + canvas.getPanX()) / canvas.getZoom();
		double y = (e.getY() + canvas.getPanY()) / canvas.getZoom();
		trueMouseDownX = x;
		trueMouseDownY = y;

		DrawObject objClickedOn = canvas.getObjectAtPosition(x, y);
		
		if (objClickedOn == null) {
			panDragMode = true;
			panOnMouseDownX = canvas.getPanX();
			panOnMouseDownY = canvas.getPanY();
			statusLabel.setText("Ready");
			propertiesPanel.detach();
		
		} else {
			panOnMouseDownX = x;
			panOnMouseDownY = y;
			draggingObject = objClickedOn;
			panDragMode = false;
			statusLabel.setText(String.format("You clicked on a %s", objClickedOn.getName()));
			propertiesPanel.attach(objClickedOn);
			
			if (e.isShiftDown()) {
				canvas.setCursor(new Cursor(Cursor.MOVE_CURSOR));
			}
			
			if (e.getClickCount() == 2) {
				if (objClickedOn != null) {
					objClickedOn.doubleClick(this);
				}
			}
		}

		if (SwingUtilities.isRightMouseButton(e)) {
			JPopupMenu menu = null;
			
			if (objClickedOn != null) {
				menu = objClickedOn.getRightClickMenu(this, objClickedOn);
			} else {
				menu = new CanvasRightClickMenu(this, null); 
			}
						
			if (menu != null) {
				menu.show(canvas, e.getX(), e.getY());
			}
		}
		
		canvas.repaint();
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		mouseMoveX = e.getX();
		mouseMoveY = e.getY();
		lineShiftMode = false;
		canvas.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
		if (wantFenceAddedOnMouseLift) {
			actionManager.addFenceBoundary();
			wantFenceAddedOnMouseLift = false;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		mouseMoveX = e.getX();
		mouseMoveY = e.getY();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		mouseMoveX = e.getX();
		mouseMoveY = e.getY();
	}

	@Override
	public void mouseDragged(MouseEvent e) {	
		mouseDragEventsSinceClick++;
		
		mouseMoveX = e.getX();
		mouseMoveY = e.getY();
		if (panDragMode && e.isControlDown()) {
			double x = (e.getX() + canvas.getPanX()) / canvas.getZoom();
			double y = (e.getY() + canvas.getPanY()) / canvas.getZoom();
			trueMouseDownX = x;
			trueMouseDownY = y;

			Arrow newArrow = new Arrow(new Coordinate(trueMouseDownX - primaryAxis.relativePosition.x, trueMouseDownY - primaryAxis.relativePosition.y), 0.0, 0.0);
			primaryAxis.addChild(newArrow);
			draggingObject = newArrow.arrowhead;
			canvas.repaint();
			lineShiftMode = false;
			mousePressed(e);
			
		} else if (panDragMode) {
		
			canvas.setCursor(new Cursor(Cursor.HAND_CURSOR));

			double posx = ((panOnMouseDownX - e.getX() + mouseDownX) * ((double)(hzScrollBar.getMaximum() - hzScrollBar.getMinimum()))) / canvas.getUsedWidth() / canvas.getZoom();
			double posy = ((panOnMouseDownY - e.getY() + mouseDownY) * ((double)(vtScrollBar.getMaximum() - vtScrollBar.getMinimum()))) / canvas.getUsedHeight() / canvas.getZoom();
			
			hzScrollBar.setValue((int) posx);
			vtScrollBar.setValue((int) posy);

			canvas.setPan(panOnMouseDownX - e.getX() + mouseDownX, panOnMouseDownY - e.getY() + mouseDownY);
		
		} else {
			if (draggingObject != null && draggingObject.canDrag) {
				double x = (e.getX() + canvas.getPanX()) / canvas.getZoom();
				double y = (e.getY() + canvas.getPanY()) / canvas.getZoom();
				trueMouseDownX = x;
				trueMouseDownY = y;
				
				double deltaX = x - panOnMouseDownX;
				double deltaY = y - panOnMouseDownY;
				
				if ((e.isShiftDown() || lineShiftMode) && PrimaryLine.class.isAssignableFrom(draggingObject.getClass())) {
					if (!lineShiftMode) {
						((PrimaryLine)draggingObject).copy();						
						verticalShiftMode = false;
					}
					
					lineShiftMode = true;
					if ((Math.abs(deltaY) > Math.abs(2.3 * deltaX) && Math.abs(deltaY) > 0.5) || verticalShiftMode) {
						verticalShiftMode = true;
						if (deltaY < 0) canvas.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
						else canvas.setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
					} else {
						if (deltaX < 0) canvas.setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
						else canvas.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
					}
					
					if (Math.abs(deltaX) > Math.abs(2.3 * deltaY) && Math.abs(deltaX) > 0.5) {
						verticalShiftMode = false;
					}
					
				} else {
					canvas.setCursor(new Cursor(Cursor.MOVE_CURSOR));

				}
				
				draggingObject.mouseDragging(deltaX, deltaY);
				propertiesPanel.regenerate();
				
				if (!addedFenceOnMouseDragYet || mouseDragEventsSinceClick % 24 == 16) {
					actionManager.addFenceBoundary();
					addedFenceOnMouseDragYet = true;
				}
				wantFenceAddedOnMouseLift = true;
				
				panOnMouseDownX = x;
				panOnMouseDownY = y;
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseMoveX = e.getX();
		mouseMoveY = e.getY();
	}
	
	public static void main(String args[]) {
		(new Econogram(new JFrame(""), null)).openFilepath("src/template/blankaxis.edi", "Untitled Diagram");
	}
}
