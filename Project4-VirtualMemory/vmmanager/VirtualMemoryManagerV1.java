package vmmanager;

import vmsimulation.BackingStore;
import vmsimulation.BitwiseToolbox;
import vmsimulation.MainMemory;
import vmsimulation.MemoryException;

import java.util.*;
import java.util.stream.IntStream;

public class VirtualMemoryManagerV1 {

    MainMemory memory;    // The main memory
    BackingStore disk;    // The disk
    Integer pageSize;    // Page size
    int numOfPages;
    int numOfPagesFault;
    int bytesTransferred;
    PageTable pageTable;
    MemoryState memoryState;

    // log2(): Convenient function to compute the log2 of an integer;
    private int log2(int x) {
        return (int) (Math.log(x) / Math.log(2));
    }

    // Constructor
    public VirtualMemoryManagerV1(MainMemory memory,
                                  BackingStore disk,
                                  Integer pageSize) throws MemoryException {
        this.memory = memory;
        this.disk = disk;
        this.pageSize = pageSize;
        this.numOfPages = disk.size()/pageSize;
        this.numOfPagesFault = 0;
        this.bytesTransferred = 0;
        this.memoryState = new MemoryState(numOfPages);
        this.pageTable = new PageTable();
    }

    // Method to write a byte to memory given a virtual address
    public void writeByte(Integer fourByteBinaryString, Byte value) throws MemoryException {
        int address = BitwiseToolbox.extractBits(fourByteBinaryString, 0, pageSize);
        int page = address/pageSize;
        Integer frame = pageTable.lookup(page);
        if(frame==null) {
            numOfPagesFault++;
            putPageOnMemory(page);
            frame = pageTable.lookup(page);
        }
        int physicalAddr = translateVirtualAddrToPhysical(address, frame);
        System.out.println("RAM: @" + BitwiseToolbox.getBitString(physicalAddr, pageSize)
                            + " <-- " + String.valueOf(value));
        memory.writeByte(physicalAddr, value);
        bytesTransferred++;
    }


    // Method to read a byte to memory given a virtual address
    public Byte readByte(Integer fourByteBinaryString) throws MemoryException {
        int address = BitwiseToolbox.extractBits(fourByteBinaryString, 0, pageSize);
        int page = address/pageSize;
        Integer frame = pageTable.lookup(page);
        if(frame==null) {
            numOfPagesFault++;
            putPageOnMemory(page);
            frame = pageTable.lookup(page);
        }
        int physicalAddr = translateVirtualAddrToPhysical(address, frame);
        byte memoryByte = memory.readByte(physicalAddr);
        System.out.println("RAM: @" + BitwiseToolbox.getBitString(physicalAddr, pageSize)
                           + " --> " + String.valueOf(memoryByte));
        return memoryByte;
    }

    public int translateVirtualAddrToPhysical(int address, int frame){
        float offset = (((float) address/pageSize)%1)*pageSize;
        return (frame*pageSize)+((int)offset);
    }

    public void putPageOnMemory(int page) throws MemoryException {

        int freeFrame = memoryState.getFreeFrame();
        int address = pageSize*freeFrame;
        memoryState.setLoadedState(freeFrame, false);
        pageTable.update(page, freeFrame);
        byte[] pageBytes = disk.readPage(page); // read page data
        disk.writePage(page, new byte[pageSize]); // delete page data on disk

        for (int i = 0; i < pageBytes.length; i++) {
            memory.writeByte(address+i, pageBytes[i]); // write bytes on memory
            bytesTransferred++;
        }

        System.out.println("Bringing page " + Integer.toString(page)
                           + " into frame " + Integer.toString(freeFrame));
    }

    // Method to print all memory content
    public void printMemoryContent() throws MemoryException {
        for (int i = 0; i < memory.size(); i++){
            byte b = memory.readByte(i);
            String bits = BitwiseToolbox.getBitString(i, pageSize);
            System.out.println(bits + ": " + String.valueOf(b));
        }
    }

    // Method to print all disk content
    public void printDiskContent() throws MemoryException {
        int page = 0;
        for (int i = 0; i < disk.size(); i+=pageSize){
            byte[] bytesOnPage = disk.readPage(page);
            int[] valuesOnPage = IntStream.range(0, pageSize)
                    .map(j -> bytesOnPage[j])
                    .toArray();
            System.out.println("PAGE"+String.valueOf(page)+": "+Arrays.toString(valuesOnPage));
            page++;
        }
    }

    // Method to write back all pages to disk
    public void writeBackAllPagesToDisk() throws MemoryException {
        for (Map.Entry<Integer, Boolean> entry : memoryState.getMemoryState().entrySet()) {
            byte[] frameBytes = new byte[pageSize];
            Integer page = pageTable.getPageByFrame(entry.getKey());
            if(page!=null){
                int frameAddr = entry.getKey()*pageSize;
                int j = 0;
                for (int i = frameAddr; i < frameAddr+pageSize; i++) {
                    frameBytes[j] = memory.readByte(i);
                    memoryState.clearState(entry.getKey());
                    j++;
                }
                disk.writePage(page, frameBytes);
                bytesTransferred+=frameBytes.length;
            }
        }
    }

    // Method to retrieve the page fault count
    public int getPageFaultCount() {
        return this.numOfPagesFault;
    }

    // Method to retrieve the number of bytes transferred between RAM and disk
    public int getTransferedByteCount() {
        return bytesTransferred;
    }
}

class PageTable{
    private HashMap<Integer, Integer> table;

    public PageTable(){
        this.table = new HashMap<>();
    }

    Integer lookup(int page){
        if(!table.containsKey(page)){
            table.put(page, null);
        }
        return table.get(page);
    }

    public void update(int page, int frame){
        table.put(page, frame);
    }

    public Integer getPageByFrame(int frame){
        Integer key = null;
        for (Map.Entry<Integer, Integer> entry : table.entrySet()) {
            if (entry.getValue().equals(frame)) {
                key = entry.getKey();
                break;
            }
        }
        return key;
    }
}

class MemoryState {
    private HashMap<Integer, Boolean> state;
    private final int numOfFrames;

    public MemoryState(int numOfFrames){
        this.numOfFrames = numOfFrames;
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

    public int getFreeFrame(){
        Integer freeFrame = 0;
        for (Map.Entry<Integer, Boolean> entry : state.entrySet()) {
            if (entry.getValue()) {
                freeFrame = entry.getKey();
                break;
            }
        }
        return freeFrame;
    }
}
