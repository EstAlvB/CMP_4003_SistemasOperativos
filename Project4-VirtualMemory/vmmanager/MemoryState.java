package vmmanager;

import java.util.HashMap;
import java.util.Map;

class MemoryState {
    private HashMap<Integer, Boolean> state;

    public MemoryState(int numOfFrames){
        state = new HashMap<>(){{
            for (int i = 0; i < numOfFrames; i++) {
                put(i, true);
            }
        }};
    }

    public HashMap<Integer, Boolean> getMemoryState(){
        return state;
    }

    public boolean getLoadedState(int frameNum) {
        return state.get(frameNum);
    }

    public void setLoadedState(int frameNum, boolean loaded) {
        state.put(frameNum, loaded);
    }

    public void clearState(int frameNum) {
        state.put(frameNum, true);
    }

    public Integer getFreeFrame(){
        Integer freeFrame = null;
        for (Map.Entry<Integer, Boolean> entry : state.entrySet()) {
            if (entry.getValue()) {
                freeFrame = entry.getKey();
                break;
            }
        }
        return freeFrame;
    }
}
