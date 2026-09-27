import java.util.NoSuchElementException;

public class LinkedList<T> {

    private static class Node<T> {
        T value;
        Node<T> next;
        Node(T value) { this.value = value; }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public long opCount = 0;

    public void resetCounter() {
        opCount = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void add(T x) {
        Node<T> node = new Node<>(x);
        if (tail == null) {
            head = tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
        opCount++;
    }

    public void add(int index, T x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (index == 0) {
            Node<T> node = new Node<>(x);
            node.next = head;
            head = node;
            if (tail == null) tail = node;
            size++;
            opCount++;
            return;
        }
        if (index == size) {
            add(x);
            return;
        }
        Node<T> prev = head;
        for (int i = 0; i < index - 1; i++) {
            prev = prev.next;
            opCount++;
        }
        Node<T> node = new Node<>(x);
        node.next = prev.next;
        prev.next = node;
        size++;
        opCount++;
    }

    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        T removed;
        if (index == 0) {
            removed = head.value;
            head = head.next;
            if (head == null) tail = null;
            size--;
            opCount++;
            return removed;
        }
        Node<T> prev = head;
        for (int i = 0; i < index - 1; i++) {
            prev = prev.next;
            opCount++;
        }
        Node<T> target = prev.next;
        removed = target.value;
        prev.next = target.next;
        if (target == tail) tail = prev;
        size--;
        opCount++;
        return removed;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        Node<T> cur = head;
        for (int i = 0; i < index; i++) {
            cur = cur.next;
            opCount++;
        }
        opCount++;
        return cur.value;
    }

    public boolean contains(T x) {
        Node<T> cur = head;
        while (cur != null) {
            opCount++;
            if (cur.value == null ? x == null : cur.value.equals(x)) {
                return true;
            }
            cur = cur.next;
        }
        return false;
    }

    public T removeFirstOccurrence(T x) {
        int index = 0;
        Node<T> cur = head;
        while (cur != null) {
            if (cur.value == null ? x == null : cur.value.equals(x)) {
                return remove(index);
            }
            cur = cur.next;
            index++;
        }
        throw new NoSuchElementException();
    }

    public void clear() {
        head = tail = null;
        size = 0;
    }
}
