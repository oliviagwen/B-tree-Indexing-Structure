// - NAME: Wen Gu
// - NEU ID: 001063033
// - CS 7280: Special Topics in Database Management Spring 2022
//     -    Project 1: B-tree Indexing Structure

import java.util.Arrays;


final class Btree {

    /*
     * Node data structure.
     *   - This is the simplest structure for nodes used in B-tree
     *   - This will be used for both internal and leaf nodes.
     */
    final class Node {

        // Node Values
        int[] values;

        // Node Array, pointing to the children nodes
        int[] children;

        // Number of entries
        int size;
    }


    // Default size of Node
    private int NODE_SIZE = 5;

    // Pointer to the root node
    private int root;

    // Node array, default lenght 1 for root node
    private Node[] nodes = new Node[1];

    // Count of used nodes
    private int usedNodeCount;

    // Count of currently used values
    private int usedValueCount;

    // Node used in backward insert
    private Node tmpNode;

    public Btree() {
        root = createChild();
    }

    /**
     * initialize a btree with node size
     * @param nodeSize the node size of btree
     */
    public Btree(int nodeSize) {
        this.NODE_SIZE = nodeSize;
        root = createChild();
    }

    /*
     * Lookup a value in btree
     *   @param value the value to find
     *   @return - True if the value was found
     */
    public boolean Lookup(int value) {
        return nodeLookup(value, root);
    }

    /*
     * Inert a value in btree
     *   @param value the value would be inserted
     *   @return
     *    - If -1 is returned, the value is inserted and increase usedValueCount.
     *    - If -2 is returned, the value already exists.
     */
    public void Insert(int value) {
        int ret = nodeInsert(value, root);
        switch (ret) {
        case -1:
            // return -1 if insert successfully
            usedValueCount++;
            System.out.println("Insert " + Integer.toString(value) + " successfully");
            break;
        case -2:
            //return -2 if index already exist
            System.out.println("This index already exist");
            break;

        default:
            // add a new node if root node merge with a propagate new node and caused overflow,
            // so init a new node to store this new root and make the count of nodes++
            int newRoot = initNode();
            this.nodes[newRoot] = this.tmpNode;
            this.root = newRoot;
            usedValueCount++;
            System.out.println("Insert " + Integer.toString(value) + "successfully");
        }
    }

    /**
     * display the tree structure from root node with format is like json file
     */
    public void Display() {
        Display(root);
    }

    /**
     * display the tree structure from specified node with format is like json file
     * @param node
     */
    public void Display(int node){
        String displayStr = getDisplayText(node, 0, 1);
        System.out.println("{\n" + displayStr + "}");
    }

    /**
     * display the structure of btree recursively
     * @param nodeInd current node index to display
     * @param displayid current node id
     * @param depth current node depth in tree
     * @return json-like text
     */
    String getDisplayText(int nodeInd, int displayid, int depth){
        // add 2 x " " which is like one tab
        String indentation = new String(new char[2 * depth]).replace('\0', ' ');
        // the indentation needed to add before the data pointer in this node
        String singleIndentation = new String(new char[2]).replace('\0', ' ');

        String resultStr = "";
        // get the current node
        Node curNode = this.nodes[nodeInd];
        // check whether current node is leaf node
        boolean isleaf = this.isLeaf(curNode);

        resultStr += indentation + (isleaf ? "\"leaf-node-" + displayid + "\": ": "\"node-" + displayid + "\": ") + "{\n";

        for(int i = 0; i < curNode.size; i++) {
            if(!isleaf) {
                resultStr += getDisplayText(curNode.children[i], i, depth+1);
            }
            // display the data pointer in current node
            resultStr += indentation + singleIndentation + "\"data-pointer-" + i + "\":";
            resultStr += curNode.values[i];
            resultStr += ",\n ";
        }

        // because for nodes which are not leaf nodes, the children number is greater than the data pointers number by 1,
        // we need to display the last child information in this way
        if(!isleaf) {
            resultStr += getDisplayText(curNode.children[curNode.size], curNode.size, depth+1);
        }
        resultStr += indentation + "}, \n";
        return resultStr;
    }

    /*
     * Get used values count
     * @return the count of used values.
     */
    public int UsedValueCount() {
        return usedValueCount;
    }

    /*
     * Lookup value in tree
     * @param value the value need to find
     * @param pointer the index on nodes array.
     * @return
     *    - True if the value was found in the specified node.
     */
    private boolean nodeLookup(int value, int pointer) {
        Node curNode = this.nodes[pointer];

        // The return value of the binary search can be the exact index of the value we searched for, or it can be
        // Index of children nodes that may contain search values.
        int potentialIndex = binarySearch(curNode.values, curNode.size, value);
        boolean isleaf = this.isLeaf(curNode);

        // This check is to avoid index out of bound exception, if potentialIndex == curNode.size,
        // This means we're searching for a value greater than the max value of curNode.values
        if(potentialIndex == curNode.size){
            return isleaf ? false : nodeLookup(value, curNode.children[potentialIndex]);
        }

        //Check whether the potential index we get from binary search is exactly the value we search
        if(curNode.values[potentialIndex] == value) {
            return true;
        } else {
            return isleaf ? false : nodeLookup(value, curNode.children[potentialIndex]);
        }
    }

    /*
     *   Insert elements into the btree
     *   @return
     *    - -2 if the value already exists in the specified node
     *    - -1 if the value is inserted into the node or
     *            something else if the parent node has to be restructured
     */
    private int nodeInsert(int value, int pointer) {
        Node curNode = this.nodes[pointer];
        // check whether current node size is 0, this could happen when the first node is being inserted into the tree
        if (curNode.size == 0) {
            curNode.values[0] = value;
            curNode.size++;
            return -1;
        }
        // just like the way in nodelookup, use binary search to find the potential index to insert the value
        int potentialIndex = this.binarySearch(curNode.values,curNode.size,value);

        //check whether the value is already in the current node, if true, return -2 indicating it already exists
        if(potentialIndex < curNode.size && curNode.values[potentialIndex] == value) {
            return -2;
        }

        //if we reach the leaf node
        if (this.isLeaf(curNode)) {
            // cause this node is the leaf node, we should insert value exactly into current node's values array
            insertValue(curNode.values,value, potentialIndex);
            curNode.size++;

            // If the current node size (number of values) is equal to the max node size, we need
            // split the node and use a temporary node to store this new node and return 1, indicating that it needs to be added
            // midpoint to parent node,
            // Otherwise, just return -1, indicates that the insertion is successful
            if(curNode.size < this.NODE_SIZE) {
                return -1;
            } else {
                this.tmpNode = this.split(pointer);
                return 1;
            }
        }

        int insertStatus = nodeInsert(value, curNode.children[potentialIndex]);
        if(insertStatus >= 0){
            return merge(pointer, this.tmpNode);
        }
        return insertStatus;
    }

    /**
     * merge two node, return -1 if the merge operation does not exceed the max node size, otherwise return 1
     * @param originNodeIndex
     * @param newNode
     * @return
     */
    int merge(int originNodeIndex, Node newNode) {
        Node originNode = this.nodes[originNodeIndex];
        int pos = this.binarySearch(originNode.values, originNode.size, newNode.values[0]);
        this.insertValue(originNode.values, newNode.values[0], pos);
        this.insertValue(originNode.children, newNode.children[0], pos);
        originNode.children[pos+1] = newNode.children[1];
        originNode.size++;

        if (originNode.size >= this.NODE_SIZE) {
            this.tmpNode = split(originNodeIndex);
            return 1;
        }
        return -1;
    }

    /**
     * split the node
     * @param nodeIndex
     * @return
     */
    Node split(int nodeIndex) {
        Node curNode = this.nodes[nodeIndex];
        int midInd = (this.NODE_SIZE - 1) / 2;
        Node temp = new Node();
        temp.size = 1;
        temp.values = new int[this.NODE_SIZE];
        // init the first value as the midInd value of origin node
        temp.values[0] = curNode.values[midInd];
        temp.children = new int[this.NODE_SIZE + 1];

        // create a new node which represent the right node
        int newRightNodeInd;
        // if current node is leaf node, then we do not need to init the children,
        // otherwise we need init the children array and copy the right part of children from origin node to the new node
        if (this.isLeaf(curNode)) {
            newRightNodeInd = this.createChild();
        } else {
            int midIndForChild = (this.NODE_SIZE + 1) / 2;
            newRightNodeInd = this.initNode();
            System.arraycopy(curNode.children,
                             midIndForChild,
                             this.nodes[newRightNodeInd].children,
                             0,
                             this.NODE_SIZE+1-midIndForChild);
        }

        System.arraycopy(curNode.values,
                         midInd + 1,
                         this.nodes[newRightNodeInd].values,
                         0,
                         this.NODE_SIZE-midInd - 1
                         );
        this.nodes[newRightNodeInd].size = this.NODE_SIZE - midInd - 1;
        temp.children[1] = newRightNodeInd;

        curNode.size = midInd;
        temp.children[0] = nodeIndex;
        return temp;
    }


    /*
     * binary search an array
     * @param values array need to search
     * @param size the number of key stored in nodes(not tree pointers)
     * @param searchValue value wanted
     * @return this function will return an index of potential position of searchvalue,
     * which means it could be values with the return index equal to the search value or the search
     * value is in the node's chilren with the return index(possibly)
     */
    int binarySearch(int[] values, int size, int searchValue){
        int left = 0;
        int right = size - 1;
        if(searchValue > values[right])
            return size;

        int mid;
        while(left < right) {
            // get the mid value
            mid = left + (right - left)/2;
            if (values[mid] > searchValue) {
                right = mid;
            } else if (values[mid] < searchValue) {
                left = mid+1;
            } else {
                return mid;
            }
        }
        return left;
    }

    /**
     * insert value into values at ind, all elements with its origin index after ind(include the one
     * on ind) will move 1 position backward
     * @param values the array need to insert value
     * @param value the value need insert
     * @param ind the position to insert
     */
    void insertValue(int[] values, int value, int ind){
        System.arraycopy(values, ind, values, ind+1, values.length-ind-1);
        values[ind] = value;
    }

    /*
     * check node whether is leaf node
     * @return
     *    - True if the specified node is a leaf node.
     *         (Leaf node -> a missing children)
     */
    boolean isLeaf(Node node) {
        return node.children == null;
    }

    /*
     * Initialize a new node and returns the pointer.
     * @return
     *    - node pointer
     */
    int initNode() {
        Node node = new Node();
        node.values = new int[this.NODE_SIZE];
        node.children =  new int[this.NODE_SIZE + 1];

        checkSize();
        nodes[usedNodeCount] = node;
        return usedNodeCount++;
    }

    /*
     * Creates a new leaf node and returns the pointer.
     *   @return
     *    - node pointer
     */
    int createChild() {
        Node node = new Node();
        node.values = new int[NODE_SIZE];

        checkSize();
        nodes[usedNodeCount] = node;
        return usedNodeCount++;
    }

    /*
     * checkSize(): To resize the node array if necessary.
     */
    private void checkSize() {
        if(usedNodeCount == nodes.length) {
            Node[] tmp = new Node[usedNodeCount << 1];
            System.arraycopy(nodes, 0, tmp, 0, usedNodeCount);
            nodes = tmp;
        }
    }

    /**
     * get the index of root on this.nodes
     * @return the index of root on this.nodes
     */
    public int getRoot() {
        return root;
    }
}
