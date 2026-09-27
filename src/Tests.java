import java.util.ArrayList;
import java.util.Random;

public class Tests {

    static int passed = 0;
    static int failed = 0;

    static void check(String name, boolean condition) {
        if (condition) {
            passed++;
        } else {
            failed++;
            System.out.println("FAILED: " + name);
        }
    }

    static void expectThrows(String name, Runnable r) {
        try {
            r.run();
            failed++;
            System.out.println("FAILED (expected exception): " + name);
        } catch (IndexOutOfBoundsException | java.util.NoSuchElementException e) {
            passed++;
        }
    }

    public static void main(String[] args) {
        testDynamicArray();
        testLinkedList();
        testMinHeap();
        testAgainstJavaCollections();

        System.out.println();
        System.out.println("=== Test summary: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) {
            System.exit(1);
        }
    }

    static void testDynamicArray() {
        System.out.println("-- DynamicArray --");

        DynamicArray<Integer> a = new DynamicArray<>();
        check("empty size == 0", a.size() == 0);
        check("empty isEmpty", a.isEmpty());
        check("empty contains false", !a.contains(1));
        expectThrows("empty get(0) throws", () -> a.get(0));
        expectThrows("empty remove(0) throws", () -> a.remove(0));

        a.add(42);
        check("one element size == 1", a.size() == 1);
        check("one element get(0)", a.get(0) == 42);
        check("one element contains", a.contains(42));
        check("one element !contains other", !a.contains(7));

        DynamicArray<Integer> b = new DynamicArray<>();
        for (int i = 0; i < 20; i++) b.add(i);
        check("multiple size", b.size() == 20);
        boolean orderOk = true;
        for (int i = 0; i < 20; i++) if (b.get(i) != i) orderOk = false;
        check("multiple order preserved", orderOk);

        DynamicArray<Integer> c = new DynamicArray<>();
        for (int i = 0; i < 5; i++) c.add(i);
        c.add(0, -1);
        c.add(c.size(), 99);
        c.add(3, 555);
        int[] expected = {-1, 0, 1, 555, 2, 3, 4, 99};
        boolean insOk = c.size() == expected.length;
        for (int i = 0; insOk && i < expected.length; i++) insOk = c.get(i) == expected[i];
        check("add(index,x) places elements correctly", insOk);
        expectThrows("add(index,x) out of bounds throws", () -> c.add(999, 0));
        expectThrows("add(index,x) negative index throws", () -> c.add(-1, 0));

        DynamicArray<Integer> d = new DynamicArray<>();
        for (int i = 0; i < 5; i++) d.add(i);
        int removed = d.remove(0);
        check("remove(0) returns correct value", removed == 0);
        check("remove(0) shifts left", d.get(0) == 1 && d.size() == 4);
        d.remove(d.size() - 1); // remove last -> 1 2 3
        check("remove(last) works", d.size() == 3 && d.get(d.size() - 1) == 3);
        expectThrows("remove out-of-bounds throws", () -> d.remove(100));

        DynamicArray<Integer> e = new DynamicArray<>();
        e.add(7); e.add(7); e.add(7);
        check("duplicates size", e.size() == 3);
        check("duplicates contains", e.contains(7));
        e.remove(1);
        check("remove one duplicate leaves two", e.size() == 2 && e.contains(7));

        DynamicArray<Integer> f = new DynamicArray<>();
        for (int i = 0; i < 3; i++) f.add(i);
        check("get first boundary", f.get(0) == 0);
        check("get last boundary", f.get(f.size() - 1) == 2);
        expectThrows("get(size) throws", () -> f.get(f.size()));

        DynamicArray<Integer> g = new DynamicArray<>();
        int N = 50_000;
        for (int i = 0; i < N; i++) g.add(i);
        boolean largeOk = g.size() == N && g.get(0) == 0 && g.get(N - 1) == N - 1;
        check("large input basic correctness", largeOk);
        g.add(N / 2, -999);
        check("large input mid-insert correctness", g.get(N / 2) == -999 && g.size() == N + 1);
    }

    static void testLinkedList() {
        System.out.println("-- LinkedList --");

        LinkedList<Integer> a = new LinkedList<>();
        check("empty size == 0", a.size() == 0);
        check("empty isEmpty", a.isEmpty());
        check("empty contains false", !a.contains(1));
        expectThrows("empty get(0) throws", () -> a.get(0));
        expectThrows("empty remove(0) throws", () -> a.remove(0));

        a.add(42);
        check("one element size == 1", a.size() == 1);
        check("one element get(0)", a.get(0) == 42);
        check("one element contains", a.contains(42));

        LinkedList<Integer> b = new LinkedList<>();
        for (int i = 0; i < 20; i++) b.add(i);
        check("multiple size", b.size() == 20);
        boolean orderOk = true;
        for (int i = 0; i < 20; i++) if (b.get(i) != i) orderOk = false;
        check("multiple order preserved", orderOk);

        LinkedList<Integer> c = new LinkedList<>();
        for (int i = 0; i < 5; i++) c.add(i);
        c.add(0, -1);
        c.add(c.size(), 99);
        c.add(3, 555);
        int[] expected = {-1, 0, 1, 555, 2, 3, 4, 99};
        boolean insOk = c.size() == expected.length;
        for (int i = 0; insOk && i < expected.length; i++) insOk = c.get(i) == expected[i];
        check("add(index,x) places elements correctly", insOk);
        expectThrows("add(index,x) out of bounds throws", () -> c.add(999, 0));

        LinkedList<Integer> d = new LinkedList<>();
        for (int i = 0; i < 5; i++) d.add(i);
        int removed = d.remove(0);
        check("remove(0) returns correct value", removed == 0);
        check("remove(0) shifts head", d.get(0) == 1 && d.size() == 4);
        d.remove(d.size() - 1);
        check("remove(last) works (tail pointer updated)", d.size() == 3 && d.get(d.size() - 1) == 3);
        d.add(777);
        check("append after tail removal works", d.get(d.size() - 1) == 777);
        expectThrows("remove out-of-bounds throws", () -> d.remove(100));

        LinkedList<Integer> e = new LinkedList<>();
        e.add(7); e.add(7); e.add(7);
        check("duplicates size", e.size() == 3);
        check("duplicates contains", e.contains(7));
        e.remove(1);
        check("remove one duplicate leaves two", e.size() == 2 && e.contains(7));

        LinkedList<Integer> f = new LinkedList<>();
        for (int i = 0; i < 3; i++) f.add(i);
        check("get first boundary", f.get(0) == 0);
        check("get last boundary", f.get(f.size() - 1) == 2);
        expectThrows("get(size) throws", () -> f.get(f.size()));

        LinkedList<Integer> g = new LinkedList<>();
        int N = 50_000;
        for (int i = 0; i < N; i++) g.add(i);
        boolean largeOk = g.size() == N && g.get(0) == 0 && g.get(N - 1) == N - 1;
        check("large input basic correctness", largeOk);
        g.add(N / 2, -999);
        check("large input mid-insert correctness", g.get(N / 2) == -999 && g.size() == N + 1);
    }

    static void testMinHeap() {
        System.out.println("-- MinHeap --");

        MinHeap<Integer> h = new MinHeap<>();
        check("empty size == 0", h.size() == 0);
        check("empty isEmpty", h.isEmpty());
        expectThrows("empty peekMin throws", h::peekMin);
        expectThrows("empty extractMin throws", h::extractMin);

        h.insert(5);
        check("one element peekMin", h.peekMin() == 5);
        check("one element isValidHeap", h.isValidHeap());
        check("one element extractMin", h.extractMin() == 5);
        check("after extract, empty again", h.isEmpty());

        MinHeap<Integer> h2 = new MinHeap<>();
        Random rnd = new Random(42);
        int N = 2000;
        for (int i = 0; i < N; i++) {
            h2.insert(rnd.nextInt(100000));
            check("heap property holds after insert #" + i, h2.isValidHeap());
        }

        MinHeap<Integer> h3 = new MinHeap<>();
        h3.insert(4); h3.insert(4); h3.insert(4);
        check("duplicates: size", h3.size() == 3);
        check("duplicates: valid heap", h3.isValidHeap());
        check("duplicates: extractMin returns 4", h3.extractMin() == 4);

        MinHeap<Integer> h4 = new MinHeap<>();
        for (int i = 0; i < N; i++) h4.insert(rnd.nextInt(100000));
        int prev = Integer.MIN_VALUE;
        boolean nonDecreasing = true;
        boolean validAfterExtract = true;
        while (!h4.isEmpty()) {
            int v = h4.extractMin();
            if (v < prev) nonDecreasing = false;
            prev = v;
            if (!h4.isEmpty() && !h4.isValidHeap()) validAfterExtract = false;
        }
        check("extractMin returns non-decreasing sequence", nonDecreasing);
        check("heap property holds after every extraction", validAfterExtract);

        MinHeap<Integer> h5 = new MinHeap<>();
        int LARGE = 50_000;
        for (int i = 0; i < LARGE; i++) h5.insert(rnd.nextInt(1_000_000));
        check("large input: valid heap", h5.isValidHeap());
        check("large input: size correct", h5.size() == LARGE);
        int min = h5.peekMin();
        for (int i = 0; i < 100; i++) {
            int v = h5.extractMin();
            check("large input extraction #" + i + " non-decreasing", v >= min);
            min = v;
        }
    }

    static void testAgainstJavaCollections() {
        System.out.println("-- Cross-validation vs java.util --");

        Random rnd = new Random(42);
        DynamicArray<Integer> da = new DynamicArray<>();
        java.util.LinkedList<Integer> jdkList = new java.util.LinkedList<>();
        LinkedList<Integer> myList = new LinkedList<>();
        ArrayList<Integer> reference = new ArrayList<>();

        int ops = 5000;
        for (int i = 0; i < ops; i++) {
            int choice = rnd.nextInt(4);
            if (choice == 0 || reference.isEmpty()) {
                int v = rnd.nextInt(10000);
                da.add(v);
                myList.add(v);
                jdkList.add(v);
                reference.add(v);
            } else if (choice == 1) {
                int idx = rnd.nextInt(reference.size() + 1);
                int v = rnd.nextInt(10000);
                da.add(idx, v);
                myList.add(idx, v);
                jdkList.add(idx, v);
                reference.add(idx, v);
            } else if (choice == 2 && !reference.isEmpty()) {
                int idx = rnd.nextInt(reference.size());
                Integer expected = reference.remove(idx);
                check("remove matches ArrayList", (int) da.remove(idx) == expected);
                check("remove matches LinkedList", (int) myList.remove(idx) == expected);
                jdkList.remove(idx);
            } else if (!reference.isEmpty()) {
                int idx = rnd.nextInt(reference.size());
                Integer expected = reference.get(idx);
                check("get matches (DynamicArray)", (int) da.get(idx) == expected);
                check("get matches (LinkedList)", (int) myList.get(idx) == expected);
            }
        }

        boolean sizesMatch = da.size() == reference.size() && myList.size() == reference.size();
        check("final sizes match reference ArrayList", sizesMatch);

        boolean contentsMatch = true;
        for (int i = 0; i < reference.size(); i++) {
            if (!da.get(i).equals(reference.get(i)) || !myList.get(i).equals(reference.get(i))) {
                contentsMatch = false;
                break;
            }
        }
        check("final contents match reference ArrayList", contentsMatch);

        MinHeap<Integer> heap = new MinHeap<>();
        java.util.PriorityQueue<Integer> pq = new java.util.PriorityQueue<>();
        for (int i = 0; i < 5000; i++) {
            int v = rnd.nextInt(1_000_000);
            heap.insert(v);
            pq.add(v);
        }
        boolean heapMatchesPQ = true;
        while (!pq.isEmpty()) {
            if (!heap.extractMin().equals(pq.poll())) {
                heapMatchesPQ = false;
                break;
            }
        }
        check("MinHeap extraction order matches java.util.PriorityQueue", heapMatchesPQ);
    }
}
