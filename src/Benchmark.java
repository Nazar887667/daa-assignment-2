import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class Benchmark {

    static final int[] SIZES = {100, 1_000, 10_000, 100_000};
    static final int REPEATS = 5;
    static final long SEED = 42;

    public static void main(String[] args) throws IOException {
        String outDir = args.length > 0 ? args[0] : "../results/tables";

        workload1RandomAccess(outDir);
        workload2Search(outDir);
        workload3InsertionRemoval(outDir);
        workload4PriorityProcessing(outDir);

        System.out.println("All benchmarks complete. CSVs written to " + outDir);
    }

    static int[] generateData(int n, Random rnd) {
        int[] data = new int[n];
        for (int i = 0; i < n; i++) data[i] = rnd.nextInt(1_000_000);
        return data;
    }

    static DynamicArray<Integer> buildArray(int[] data) {
        DynamicArray<Integer> a = new DynamicArray<>(Math.max(1, data.length));
        for (int x : data) a.add(x);
        return a;
    }

    static LinkedList<Integer> buildList(int[] data) {
        LinkedList<Integer> l = new LinkedList<>();
        for (int x : data) l.add(x);
        return l;
    }

    static void workload1RandomAccess(String outDir) throws IOException {
        System.out.println("Running Workload 1: Random Access...");
        try (PrintWriter w = new PrintWriter(new FileWriter(outDir + "/workload1_random_access.csv"))) {
            w.println("structure,n,avg_time_ns,accesses,theoretical_complexity");

            for (int n : SIZES) {
                double arrTotal = 0, listTotal = 0;
                long arrAccesses = 0, listAccesses = 0;

                for (int rep = 0; rep < REPEATS; rep++) {
                    Random rnd = new Random(SEED + rep);
                    int[] data = generateData(n, rnd);
                    int[] indices = new int[10_000];
                    for (int i = 0; i < indices.length; i++) indices[i] = rnd.nextInt(n);

                    DynamicArray<Integer> arr = buildArray(data);
                    arr.resetCounter();
                    long start = System.nanoTime();
                    for (int idx : indices) arr.get(idx);
                    long end = System.nanoTime();
                    arrTotal += (end - start);
                    arrAccesses = arr.opCount;

                    LinkedList<Integer> list = buildList(data);
                    list.resetCounter();
                    start = System.nanoTime();
                    for (int idx : indices) list.get(idx);
                    end = System.nanoTime();
                    listTotal += (end - start);
                    listAccesses = list.opCount;
                }

                w.printf("DynamicArray,%d,%.1f,%d,O(1)%n", n, arrTotal / REPEATS, arrAccesses);
                w.printf("LinkedList,%d,%.1f,%d,O(n)%n", n, listTotal / REPEATS, listAccesses);
            }
        }
    }

    static void workload2Search(String outDir) throws IOException {
        System.out.println("Running Workload 2: Search...");
        try (PrintWriter w = new PrintWriter(new FileWriter(outDir + "/workload2_search.csv"))) {
            w.println("structure,n,avg_time_ns,comparisons,theoretical_complexity");

            for (int n : SIZES) {
                double arrTotal = 0, listTotal = 0;
                long arrComparisons = 0, listComparisons = 0;

                for (int rep = 0; rep < REPEATS; rep++) {
                    Random rnd = new Random(SEED + rep);
                    int[] data = generateData(n, rnd);
                    int[] searchValues = new int[1_000];
                    for (int i = 0; i < searchValues.length; i++) {
                        searchValues[i] = rnd.nextInt(1_000_000);
                    }

                    DynamicArray<Integer> arr = buildArray(data);
                    arr.resetCounter();
                    long start = System.nanoTime();
                    for (int v : searchValues) arr.contains(v);
                    long end = System.nanoTime();
                    arrTotal += (end - start);
                    arrComparisons = arr.opCount;

                    LinkedList<Integer> list = buildList(data);
                    list.resetCounter();
                    start = System.nanoTime();
                    for (int v : searchValues) list.contains(v);
                    end = System.nanoTime();
                    listTotal += (end - start);
                    listComparisons = list.opCount;
                }

                w.printf("DynamicArray,%d,%.1f,%d,O(n)%n", n, arrTotal / REPEATS, arrComparisons);
                w.printf("LinkedList,%d,%.1f,%d,O(n)%n", n, listTotal / REPEATS, listComparisons);
            }
        }
    }

    static void workload3InsertionRemoval(String outDir) throws IOException {
        System.out.println("Running Workload 3: Insertion and Removal...");
        try (PrintWriter w = new PrintWriter(new FileWriter(outDir + "/workload3_insertion_removal.csv"))) {
            w.println("structure,n,position,operation,avg_time_ns,movements,theoretical_complexity,ops_performed");

            for (int n : SIZES) {
                runInsertRemove(w, n, 0, "beginning");
                runInsertRemove(w, n, n / 2, "middle");
            }
        }
    }

    static void runInsertRemove(PrintWriter w, int n, int index, String label) {
        final int M = 1_000;

        double arrInsTotal = 0, arrRemTotal = 0, listInsTotal = 0, listRemTotal = 0;
        long arrInsMoves = 0, arrRemMoves = 0, listInsMoves = 0, listRemMoves = 0;
        int removalsPerformed = 0;

        for (int rep = 0; rep < REPEATS; rep++) {
            Random rnd = new Random(SEED + rep);
            int[] baseData = generateData(n, rnd);
            int[] valuesToInsert = new int[M];
            for (int i = 0; i < M; i++) valuesToInsert[i] = rnd.nextInt(1_000_000);

            DynamicArray<Integer> arr = buildArray(baseData);
            arr.resetCounter();
            long start = System.nanoTime();
            for (int v : valuesToInsert) arr.add(index, v);
            long end = System.nanoTime();
            arrInsTotal += (end - start);
            arrInsMoves = arr.opCount;

            LinkedList<Integer> list = buildList(baseData);
            list.resetCounter();
            start = System.nanoTime();
            for (int v : valuesToInsert) list.add(index, v);
            end = System.nanoTime();
            listInsTotal += (end - start);
            listInsMoves = list.opCount;

            DynamicArray<Integer> arr2 = buildArray(baseData);
            arr2.resetCounter();
            start = System.nanoTime();
            int arrRemovalsDone = 0;
            for (int i = 0; i < M && arr2.size() > index; i++) {
                arr2.remove(index);
                arrRemovalsDone++;
            }
            end = System.nanoTime();
            arrRemTotal += (end - start);
            arrRemMoves = arr2.opCount;

            LinkedList<Integer> list2 = buildList(baseData);
            list2.resetCounter();
            start = System.nanoTime();
            int listRemovalsDone = 0;
            for (int i = 0; i < M && list2.size() > index; i++) {
                list2.remove(index);
                listRemovalsDone++;
            }
            end = System.nanoTime();
            listRemTotal += (end - start);
            listRemMoves = list2.opCount;
            removalsPerformed = Math.min(arrRemovalsDone, listRemovalsDone);
        }

        String arrComplexity = index == 0 ? "O(n)" : "O(n)";
        String listInsertComplexity = index == 0 ? "O(1)" : "O(n)";
        String listRemoveComplexity = index == 0 ? "O(1)" : "O(n)";

        w.printf("DynamicArray,%d,%s,insert,%.1f,%d,%s,%d%n", n, label, arrInsTotal / REPEATS, arrInsMoves, arrComplexity, M);
        w.printf("DynamicArray,%d,%s,remove,%.1f,%d,%s,%d%n", n, label, arrRemTotal / REPEATS, arrRemMoves, arrComplexity, removalsPerformed);
        w.printf("LinkedList,%d,%s,insert,%.1f,%d,%s,%d%n", n, label, listInsTotal / REPEATS, listInsMoves, listInsertComplexity, M);
        w.printf("LinkedList,%d,%s,remove,%.1f,%d,%s,%d%n", n, label, listRemTotal / REPEATS, listRemMoves, listRemoveComplexity, removalsPerformed);
    }

    static void workload4PriorityProcessing(String outDir) throws IOException {
        System.out.println("Running Workload 4: Priority Processing...");
        try (PrintWriter w = new PrintWriter(new FileWriter(outDir + "/workload4_priority_processing.csv"))) {
            w.println("n,avg_insert_time_ns,avg_extract_time_ns,insert_comparisons,extract_comparisons,non_decreasing_verified");

            for (int n : SIZES) {
                double insertTotal = 0, extractTotal = 0;
                long insertComparisons = 0, extractComparisons = 0;
                boolean verified = true;

                for (int rep = 0; rep < REPEATS; rep++) {
                    Random rnd = new Random(SEED + rep);
                    int[] data = generateData(n, rnd);

                    MinHeap<Integer> heap = new MinHeap<>();
                    heap.resetCounter();
                    long start = System.nanoTime();
                    for (int x : data) heap.insert(x);
                    long end = System.nanoTime();
                    insertTotal += (end - start);
                    insertComparisons = heap.opCount;

                    heap.resetCounter();
                    int prev = Integer.MIN_VALUE;
                    start = System.nanoTime();
                    for (int i = 0; i < n; i++) {
                        int v = heap.extractMin();
                        if (v < prev) verified = false;
                        prev = v;
                    }
                    end = System.nanoTime();
                    extractTotal += (end - start);
                    extractComparisons = heap.opCount;
                }

                w.printf("%d,%.1f,%.1f,%d,%d,%b%n", n, insertTotal / REPEATS, extractTotal / REPEATS,
                        insertComparisons, extractComparisons, verified);
            }
        }
    }
}
