import java.util.List;
import java.util.ArrayList;

public class ActionManager {
	Econogram econogram;
	
	List<Action> actionList;
	int currentActionPointer;
	
	public ActionManager(Econogram instance) {
		econogram = instance;
		actionList = new ArrayList<Action>();
		currentActionPointer = 0;
	}
	
	void add(Action a) {
		boolean needsUndoEntry = a.execute();
		if (!needsUndoEntry) {
			return;
		}
		
		econogram.performSavableAction();
		
		assert (actionList.size() >= currentActionPointer);
		
		while (actionList.size() > currentActionPointer) {
			actionList.remove(actionList.size() - 1);
		}
		
		actionList.add(a);
		currentActionPointer++;
	}
	
	boolean undo() {
		if (currentActionPointer != 0) {
			currentActionPointer--;
			boolean couldUndo = actionList.get(currentActionPointer).undo();
			if (!couldUndo) {
				currentActionPointer++;
			}
			return couldUndo;
		}
		return false;
	}
	
	boolean redo() {
		if (currentActionPointer < actionList.size()) {
			boolean worked = actionList.get(currentActionPointer++).redo();
			if (!worked) {
				currentActionPointer--;
			}
			return worked;
		}
		
		return false;
	}
}
