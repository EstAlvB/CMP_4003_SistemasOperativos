package vmmanager;

import java.util.HashMap;
import java.util.Map;

class PageTable{
    private HashMap<Integer, Integer> table;

    public PageTable(){
        this.table = new HashMap<>();
    }

    public Integer lookup(Integer page){
        if(!table.containsKey(page)){
            table.put(page, null);
        }
        return table.get(page);
    }

    public void update(Integer page, Integer frame){
        table.put(page, frame);
    }

    public Integer getPageByFrame(int frame){
        Integer key = null;
        for (Map.Entry<Integer, Integer> entry : table.entrySet()) {
            if (entry.getValue()!=null) {
                if (entry.getValue().equals(frame)) {
                    key = entry.getKey();
                    break;
                }
            }
        }
        return key;
    }
}
