package DS.Queue.LRUCache;

import java.util.HashMap;
import java.util.Map;

class Node {
    Node prev;
    Node next;
    int key;
    int val;

    public Node(int key, int val) {
        this.key = key;
        this.val = val;
    }
}

public class LRUCache {

    Node head = new Node(0, 0);
    Node tail = new Node(0, 0);

    Map<Integer, Node> map = new HashMap<>();
    int capacity;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        head.next = tail;
        tail.prev = head;
    }

    public int get(int key) {
        if (!map.containsKey(key)) {
            return -1;
        }

        Node node = map.get(key);
        // Move accessed node to the front (Most Recently Used)
        remove(node);
        insert(node);

        return node.val;
    }

    public void put(int key, int value) {
        // If key already exists, remove the old node
        if (map.containsKey(key)) {
            remove(map.get(key));
        }

        // If cache is full, remove Least Recently Used node
        if (map.size() == capacity) {
            remove(tail.prev);
        }

        // Insert new node at the front
        insert(new Node(key, value));
    }

    private void remove(Node node) {
        map.remove(node.key);
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void insert(Node node) {
        map.put(node.key, node);
        Node headNext = head.next;
        head.next = node;
        node.prev = head;
        headNext.prev = node;
        node.next = headNext;
    }
}