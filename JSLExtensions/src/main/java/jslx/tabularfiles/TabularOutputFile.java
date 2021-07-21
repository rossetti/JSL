package jslx.tabularfiles;
/*
   Writing requires:
    - definition of columns
    - creating the file if it not already existing
    - if already existing, deciding to write over or append
    - each write goes to a new row
    - assume sequential, cannot go back rows
    - insert new row (writes and commits)
    - write adds data to row, commit saves data in current row
    - convenience for writing array of same type
    - need a row or record abstraction
    - probably need a Cell abstraction, intersection of row and column
 */
//TODO use a builder pattern to define and add the columns

public class TabularOutputFile {
}
