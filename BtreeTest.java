// - NAME: Wen Gu
// - NEU ID: 001063033
// - CS 7280: Special Topics in Database Management Spring 2022
//     -    Project 1: B-tree Indexing Structure
import java.util.Random;

public final class BtreeTest {
    public static void main(String[] args) {
        System.out.println("==== Begin to test B+tree ====\n");

        constructBtreeTest(new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });
        constructBtreeTest(new int[] { 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 });
        constructBtreeTest(new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3 });


        // Random test b+tree
        for(int a = 0; a < 5; a++) {
            Random rd = new Random(); // creating Random object
            int[] arr = new int[10];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = rd.nextInt(); // storing random integers in an array
            }
            constructBtreeTest(arr);
        }

        System.out.println("==== B+tree test end ====\n");
    }


    public static void constructBtreeTest(int[] values) {

        // number of strings to be inserted
        int number = values.length;

        System.out.println("Create B-tree with " + number + " elements...\n");

        Btree tree = new Btree(8);

        System.out.println("==== Begin to test insertion of B+tree ====\n");
        System.out.println("Insert Values...");
        for (int v : values) {
            tree.Insert(v);
        }
        System.out.println("==== B+tree insertion test end ====\n");

        System.out.println("==== Begin to test display of B+tree ====\n");
        tree.Display();
        int size = tree.UsedValueCount();
        System.out.println("Stored Nodes: " + size + "\n");
        System.out.println("==== B+tree display test end ====\n");


        System.out.println("==== Begin to test lookup of B+tree ====\n");
        System.out.println("Lookup Values...");

        int found = 0;
        for (int v : values) {
            if (tree.Lookup(v)) {
                System.out.println(String.format("%d found", v));
                found++;
            }
        }
        System.out.println(found + " found, " + number + " expected.\n");
        System.out.println("==== B+tree lookup test end ====\n");
    }
}
