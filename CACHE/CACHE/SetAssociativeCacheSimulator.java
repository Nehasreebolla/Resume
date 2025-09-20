import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SetAssociativeCacheSimulator {
    private int cacheSize; // in bytes
    private int associativity;
    private int blockSize; // in bytes
    private int numSets;
    private int setIndexBits;
    private int offsetBits;
    private int hitCount;
    private int missCount;
    private int[] setHitCounts;
    private int[] setMissCounts;
    private List<Set<Block>> cache;

    public SetAssociativeCacheSimulator(int cacheSize, int associativity, int blockSize) {
        this.cacheSize = cacheSize;
        this.associativity = associativity;
        this.blockSize = blockSize;

        // Calculate the number of sets
        int setSize = associativity * blockSize;
        numSets = (cacheSize * 1024) / setSize;

        // Calculate set index bits and offset bits
        setIndexBits = (int) (Math.log(numSets) / Math.log(2));
        offsetBits = (int) (Math.log(blockSize) / Math.log(2));

        // Initialize cache
        cache = new ArrayList<>(numSets);
        for (int i = 0; i < numSets; i++) {
            cache.add(new LinkedHashSet<>(associativity));
        }

        // Initialize set-wise hit and miss counts
        setHitCounts = new int[numSets];
        setMissCounts = new int[numSets];
    }

    public void processTraceFile(String traceFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(traceFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int address = Integer.parseInt(line);
                processAddress(address);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processAddress(int address) {
        int tag = address >>> (setIndexBits + offsetBits);
        int setIndex = (address >>> offsetBits) & ((1 << setIndexBits) - 1);

        Set<Block> set = cache.get(setIndex);
        Block requestedBlock = new Block(tag, setIndex);

        if (set.contains(requestedBlock)) {
            // Cache hit
            hitCount++;
            setHitCounts[setIndex]++;
        } else {
            // Cache miss
            missCount++;
            setMissCounts[setIndex]++;
            if (set.size() >= associativity) {
                // Replace existing block using LRU policy
                Iterator<Block> iterator = set.iterator();
                iterator.next(); // Access the least recently used block
                iterator.remove();
            }
        }

        // Add the requested block as the most recently used
        set.add(requestedBlock);
    }

    public int getHitCount() {
        return hitCount;
    }

    public int getMissCount() {
        return missCount;
    }

    public int[] getSetHitCounts() {
        return setHitCounts;
    }

    public int[] getSetMissCounts() {
        return setMissCounts;
    }

    private static class Block {
        private int tag;
        private int setIndex;

        public Block(int tag, int setIndex) {
            this.tag = tag;
            this.setIndex = setIndex;
        }

        @Override
        public int hashCode() {
            return Objects.hash(tag, setIndex);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Block other = (Block) obj;
            return tag == other.tag && setIndex == other.setIndex;
        }
    }

    public static void main(String[] args) {
        // Parse command line arguments
        if (args.length < 4) {
            System.out.println("Usage: java SetAssociativeCacheSimulator <cacheSize> <associativity> <blockSize> <traceFilePath>");
            return;
        }

        int cacheSize = Integer.parseInt(args[0]);
        int associativity = Integer.parseInt(args[1]);
        int blockSize = Integer.parseInt(args[2]);

        SetAssociativeCacheSimulator cacheSimulator = new SetAssociativeCacheSimulator(cacheSize, associativity, blockSize);

        // Process the trace file
        String traceFilePath = args[3];
        cacheSimulator.processTraceFile(traceFilePath);

        // Print overall statistics
        System.out.println("Cache Size: " + cacheSize + "KB");
        System.out.println("Associativity: " + associativity);
        System.out.println("Block Size: " + blockSize + " bytes");
        System.out.println("Number of Sets: " + cacheSimulator.numSets);
        System.out.println("Total Hits: " + cacheSimulator.getHitCount());
        System.out.println("Total Misses: " + cacheSimulator.getMissCount());

        // Print set-wise statistics
        System.out.println("\nSet-wise Hits and Misses:");
        for (int i = 0; i < cacheSimulator.numSets; i++) {
            System.out.println("Set " + i + ": Hits=" + cacheSimulator.getSetHitCounts()[i] + ", Misses=" + cacheSimulator.getSetMissCounts()[i]);
        }
    }
}

