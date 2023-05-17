package vmmanager;

import vmsimulation.BitwiseToolbox;
import vmsimulation.MainMemory;
import vmsimulation.MemoryException;

public class VirtualMemoryManagerV0 {

    MainMemory memory;
    int numBitsToAddress;

    // log2(): Convenient function to compute the log2 of an integer;
    private int log2(int x) {
        return (int) (Math.log(x) / Math.log(2));
    }

    boolean memContentFlag = true;
    int numBits = 0;

    // Constructor
    public VirtualMemoryManagerV0(MainMemory memory) throws MemoryException {
        this.memory = memory;
        numBits = log2(this.memory.size());
    }

    // Method to write a byte to memory given a physical address
    public void writeByte(Integer fourByteBinaryString, Byte value) throws MemoryException {
        int address = BitwiseToolbox.extractBits(fourByteBinaryString,0,numBits-1);
        memory.writeByte(address, value);
        System.out.println("RAM write: @"+BitwiseToolbox.getBitString(address, numBits-1)+" <-- " + value);
    }

    // Method to write a byte to memory given a physical address
    public Byte readByte(Integer fourByteBinaryString) throws MemoryException {
        // TO IMPLEMENT
        int address = BitwiseToolbox.extractBits(fourByteBinaryString, 0, numBits-1);
        byte valInAddr = memory.readByte(address);
        System.out.println("RAM read: @"+BitwiseToolbox.getBitString(address, numBits-1)+" --> " + valInAddr);
        return valInAddr; // MUST RETURN THE VALUE THAT WAS READ INSTEAD OF JUST ZERO
    }

    // Method to print all memory content
    public void printMemoryContent() throws MemoryException {
        int i = 0;
        int memSize = memory.size();
        for(int memLocation; i < memSize; ++i){
            System.out.println(BitwiseToolbox.getBitString(i, numBits-1) + ": " + memory.readByte(i));
        }
    }
}
