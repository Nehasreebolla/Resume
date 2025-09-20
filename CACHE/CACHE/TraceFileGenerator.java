public class TraceFileGeneratorExample {
    public static void main(String[] args) {
        String traceFilePath = "trace.txt";
        int totalAddresses = 100;
        int repeatCount = 10; // Number of times each address is repeated

        generateTraceFile(traceFilePath, totalAddresses, repeatCount);
    }

    private static void generateTraceFile(String filePath, int totalAddresses, int repeatCount) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            Random random = new Random();
            List<Integer> addresses = new ArrayList<>();

            // Generate unique addresses
            for (int i = 0; i < totalAddresses; i++) {
                int address = random.nextInt(1 << 26) & 0xFFFFFFF0; // Generate a 32-bit address with first four bits zero
                addresses.add(address);
            }

            // Repeat addresses to introduce hits and misses
            for (int i = 0; i < repeatCount; i++) {
                for (int j = 0; j < totalAddresses; j++) {
                    int address = addresses.get(j);
                    for (int k = 0; k < repeatCount; k++) {
                        String hexAddress = String.format("%08X", address);
                        writer.write(hexAddress);
                        writer.newLine();
                        address += 64; // Increment address by block size (64)
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

