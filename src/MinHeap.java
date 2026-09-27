import java.util.NoSuchElementException;
import java.util.Arrays;

public class MinHeap<T extends Comparable<T>> {

    private Object[] data;
    private int size;

    public long opCount = 0;

    public MinHeap() {
        this(16);
    }

    public MinHeap(int initialCapacity) {
        if (initialCapacity < 1) initialCapacity = 1;
        data = new Object[initialCapacity];
        size = 0;
    }

    public void resetCounter() {
        opCount = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= data.length) return;
        int newCapacity = data.length * 2;
        if (newCapacity < minCapacity) newCapacity = minCapacity;
        data = Arrays.copyOf(data, newCapacity);
    }

    @SuppressWarnings("unchecked")
    private T at(int i) {
        return (T) data[i];
    }

    private void swap(int i, int j) {
        Object tmp = data[i];
        data[i] = data[j];
        data[j] = tmp;
    }

    public void insert(T x) {
        ensureCapacity(size + 1);
        data[size] = x;
        int i = size;
        size++;
        siftUp(i);
    }

    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            opCount++;
            if (at(i).compareTo(at(parent)) < 0) {
                swap(i, parent);
                i = parent;
            } else {
                break;
            }
        }
    }

    public T peekMin() {
        if (size == 0) throw new NoSuchElementException("Heap is empty");
        return at(0);
    }

    public T extractMin() {
        if (size == 0) throw new NoSuchElementException("Heap is empty");
        T min = at(0);
        size--;
        data[0] = data[size];
        data[size] = null;
        if (size > 0) {
            siftDown(0);
        }
        return min;
    }

    private void siftDown(int i) {
        while (true) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            int smallest = i;

            if (left < size) {
                opCount++;
                if (at(left).compareTo(at(smallest)) < 0) {
                    smallest = left;
                }
            }
            if (right < size) {
                opCount++;
                if (at(right).compareTo(at(smallest)) < 0) {
                    smallest = right;
                }
            }
            if (smallest == i) break;
            swap(i, smallest);
            i = smallest;
        }
    }

    public boolean isValidHeap() {
        for (int i = 1; i < size; i++) {
            int parent = (i - 1) / 2;
            if (at(i).compareTo(at(parent)) < 0) {
                return false;
            }
        }
        return true;
    }
}
