import java.util.List;
import java.util.ArrayList;

public class ActionManager {
	Econogram econogram;
	
	class UndoResult {
		boolean couldUndo;
		boolean onFence;
		
		UndoResult(boolean couldUndo_, boolean onFence_) {
			couldUndo = couldUndo_;
			onFence = onFence_;
		}
	};
	
	List<Action> actionList;
	int currentActionPointer;
	
	public ActionManager(Econogram instance) {
		econogram = instance;
		actionList = new ArrayList<Action>();
		currentActionPointer = 0;
	}
	
	void add(Action a) {		
		a.selectedObjectAtTheTime = econogram.propertiesPanel.object;
		a.primaryAxisAtTheTime = econogram.primaryAxis;
		
		boolean needsUndoEntry = a.execute();
		if (!needsUndoEntry) {
			return;
		}
				
		assert (actionList.size() >= currentActionPointer);
		
		while (actionList.size() > currentActionPointer) {
			actionList.remove(actionList.size() - 1);
		}
		
		actionList.add(a);
		currentActionPointer++;
		
		econogram.performedAction();
	}
	
	boolean canUndo() {
		return currentActionPointer != 0;
	}
	
	void updatePropertiesPanel(DrawObject obj) {
		if (obj == null) {
			econogram.propertiesPanel.detach();
		} else {
			econogram.propertiesPanel.attach(obj);
		}
	}
	
	UndoResult undoSingleStep() {
		if (currentActionPointer != 0) {
			currentActionPointer--;
			Action obj = actionList.get(currentActionPointer);
			boolean couldUndo = obj.undo();
			if (!couldUndo) {
				currentActionPointer++;
			}
			DrawObject selobj = obj.selectedObjectAtTheTime;
			obj.selectedObjectAtTheTime = econogram.propertiesPanel.object;
			updatePropertiesPanel(selobj);
			econogram.primaryAxis = obj.primaryAxisAtTheTime;
			
			econogram.performedAction();
			return new UndoResult(true, obj.isFence());
		}
		return new UndoResult(false, false);
	}
	
	void addFenceBoundary() {
		add(econogram.FENCING_NOP_ACTION.build());
	}
	
	boolean undo() {
		UndoResult result = undoSingleStep();
		boolean couldUndo = result.couldUndo;
		int actionCount = 1;
		while (canUndo() && !result.onFence) {
			result = undoSingleStep();
			++actionCount;
		}
		System.out.printf("we undid %d actions\n", actionCount);
		return couldUndo;
	}

	boolean canRedo() {
		return currentActionPointer < actionList.size();
	}
	
	UndoResult redoSingleStep() {
		if (currentActionPointer < actionList.size()) {
			Action obj = actionList.get(currentActionPointer++);
			boolean worked = obj.redo();
			if (!worked) {
				currentActionPointer--;
			}
			DrawObject selobj = obj.selectedObjectAtTheTime;
			obj.selectedObjectAtTheTime = econogram.propertiesPanel.object;
			updatePropertiesPanel(selobj);
			econogram.primaryAxis = obj.primaryAxisAtTheTime;
			
			econogram.performedAction();

			return new UndoResult(true, obj.isFence());
		}
		
		return new UndoResult(false, false);
	}
	
	boolean redo() {
		UndoResult result = redoSingleStep();
		boolean couldUndo = result.couldUndo;
		int actionCount = 1;
		while (canRedo() && !result.onFence) {
			result = redoSingleStep();
			++actionCount;
		}
		System.out.printf("we redid %d actions\n", actionCount);
		return couldUndo;
	}
}
