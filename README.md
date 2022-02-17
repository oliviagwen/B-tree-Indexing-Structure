# CS 7280: Special Topics in Database Management Spring 2022
## Project 1: B-tree Indexing Structure

* NAME: Wen Gu
* NEU ID: 001063033

### Get Started:

#### Run Test
```shell
javac *.java
java BtreeTest
```

#### Initialize tree

```java
// create a b-tree with the default node size 5
Btree tree = new Btree();

// create a b-tree with node size 4
Btree tree = new Btree(4);
```

#### Insert elements
```
tree.Insert(1);
tree.Insert(2);
```
#### Lookup elements
```
//return true if 10 is in the tree, otherwise return false
tree.Lookup(10);
```

#### Display node
```
// print out the indexing tree structure under root node
tree.Display();

// print out the indexing tree structure under node 10
tree.Display(10);

// Display example

/*
{
  "node-0": {
    "leaf-node-0": {
      "data-pointer-0":1,
       "data-pointer-1":2,
       "data-pointer-2":3,
       "data-pointer-3":4,
       "data-pointer-4":5,
     },
    "data-pointer-0":6,
     "leaf-node-1": {
      "data-pointer-0":7,
       "data-pointer-1":8,
       "data-pointer-2":9,
       "data-pointer-3":10,
     },
    "data-pointer-1":11,
     "leaf-node-2": {
      "data-pointer-0":12,
       "data-pointer-1":13,
       "data-pointer-2":14,
       "data-pointer-3":15,
     },
  },
}
*/
```

### Any Known Bugs
No found during test.

### Limitation
No found during test.
