package vmmanager;

import vmsimulation.BackingStore;
import vmsimulation.BitwiseToolbox;
import vmsimulation.MainMemory;
import vmsimulation.MemoryException;
import java.util.*;
import java.util.stream.IntStream;

public class VirtualMemoryManagerV3 {

    MainMemory memory;    // The main memory
    BackingStore disk;    // The disk
    Integer pageSize;    // Page size
    PageTable pageTable;
    MemoryState memoryState;
    Queue<Integer> pageQueue;
    int numPageFaults;
    int bytesTransferred;
    int numFrames, numPages;

    private int log2(int x) {
        return (int) (Math.log(x) / Math.log(2));
    }

    // Constructor
    public VirtualMemoryManagerV3(MainMemory memory,
                                  BackingStore disk,
                                  Integer pageSize) throws MemoryException {
        this.numFrames = memory.size()/pageSize;
        this.numPages = disk.size()/pageSize;
        this.memory = memory;
        this.disk = disk;
        this.pageSize = pageSize;
        this.pageTable = new PageTable();
        this.memoryState = new MemoryState(numFrames);
        this.pageQueue = new ArrayDeque<>(numFrames);
        this.numPageFaults = 0;
        this.bytesTransferred = 0;
    }

    // Method to write a byte to memory given a virtual address
    public void writeByte(Integer fourByteBinaryString, Byte value) throws MemoryException {
        int address = BitwiseToolbox.extractBits(fourByteBinaryString, 0, log2(disk.size()));
        int page = address/pageSize;
        if(pageTable.lookup(page)==null) {
            numPageFaults++;
            putPageOnMemory(page);
        }
        Integer frame = pageTable.lookup(page);
        int physicalAddr = translateVirtualAddrToPhysical(address, frame);
        System.out.println("RAM: @" + BitwiseToolbox.getBitString(physicalAddr, log2(memory.size()-1))
                           + " <-- " + String.valueOf(value));
        memory.writeByte(physicalAddr, value);
        pageTable.setDirtyBitForPage(page, 1);
        bytesTransferred++;
    }


    // Method to read a byte to memory given a virtual address
    public Byte readByte(Integer fourByteBinaryString) throws MemoryException {
        int address = BitwiseToolbox.extractBits(fourByteBinaryString, 0, log2(disk.size()));
        int page = address/pageSize;
        if(pageTable.lookup(page)==null) {
            numPageFaults++;
            putPageOnMemory(page);
        }
        Integer frame = pageTable.lookup(page);
        int physicalAddr = translateVirtualAddrToPhysical(address, frame);
        byte memoryByte = memory.readByte(physicalAddr);
        System.out.println("RAM: @" + BitwiseToolbox.getBitString(physicalAddr, log2(memory.size()-1))
                           + " --> " + String.valueOf(memoryByte));
        bytesTransferred--;
        return memoryByte;
    }

    public void putPageOnMemory(int page) throws MemoryException {

        Integer freeFrame = memoryState.getFreeFrame();

        if(freeFrame==null){
            Integer firstPageAdded = pageQueue.poll();
            freeFrame = pageTable.lookup(firstPageAdded);
            if (pageIsDirty(firstPageAdded)){
                putPageOnDisk(firstPageAdded);
                System.out.println("Evicting page " + Integer.toString(firstPageAdded));
            }else{
                pageTable.update(firstPageAdded, null);
                System.out.println("Evicting page " + Integer.toString(firstPageAdded) + " (NOT DIRTY)");
            }
        }

        int address = pageSize * freeFrame;
        memoryState.setLoadedState(freeFrame, false);
        pageTable.update(page, freeFrame);
        pageTable.setDirtyBitForPage(page, 0);
        byte[] pageBytes = disk.readPage(page); // read page data

        for (int i = 0; i < pageBytes.length; i++) {
            memory.writeByte(address + i, pageBytes[i]); // write bytes on memory
        }

        bytesTransferred += pageBytes.length;
        pageQueue.add(page);
        System.out.println("Bringing page " + Integer.toString(page)
                + " into frame " + Integer.toString(freeFrame));
    }

    public void putPageOnDisk(Integer page) throws MemoryException {
        byte[] memoryBytes = new byte[pageSize];
        int address = pageTable.lookup(page)*pageSize;
        for (int i = 0; i < pageSize; i++) {
            memoryBytes[i] = memory.readByte(address+i);
            memory.writeByte(address+i, (byte) 0);
        }
        pageTable.update(page, null);
        bytesTransferred += memoryBytes.length;
        disk.writePage(page, memoryBytes);
    }

    // Method to write back all pages to disk
    public void writeBackAllPagesToDisk() throws MemoryException {
        for (Map.Entry<Integer, Boolean> entry : memoryState.getMemoryState().entrySet()) {
            if(!entry.getValue()){
                Integer page = pageTable.getPageByFrame(entry.getKey());
                if(pageIsDirty(page)){
                    putPageOnDisk(page);
                }
            }
        }
    }

    public Boolean pageIsDirty(int page){
        Integer dirtyBit = pageTable.getDirtyBitFromPage(page);
        return dirtyBit != 0;
    }

    public int translateVirtualAddrToPhysical(int address, int frame){
        float offset = (((float) address/pageSize)%1)*pageSize;
        return (frame*pageSize)+((int)offset);
    }

    // Method to print all memory content
    public void printMemoryContent() throws MemoryException {
        for (int i = 0; i < memory.size(); i++){
            byte b = memory.readByte(i);
            String bits = BitwiseToolbox.getBitString(i, log2(memory.size()-1));
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
            System.out.println("PAGE"+String.valueOf(page)+": "+ Arrays.toString(valuesOnPage));
            page++;
        }
    }

    // Method to retrieve the page fault count
    public int getPageFaultCount() {
        return numPageFaults;
    }

    // Method to retrieve the number of bytes transferred between RAM and disk
    public int getTransferedByteCount() {
        return bytesTransferred;
    }
}


