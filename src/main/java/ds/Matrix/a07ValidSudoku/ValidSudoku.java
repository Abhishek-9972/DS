package ds.Matrix.a07ValidSudoku;

import java.util.HashSet;

public class ValidSudoku {
        public boolean isValidSudoku(char[][] board) {
            HashSet<String> seen = new HashSet<>();

            for(int i=0; i<9; i++){
                for(int j=0; j<9; j++){
                    char currentVal = board[i][j];
                    if(currentVal != '.'){
                        if(!seen.add(currentVal + "found in row" + i) ||
                                !seen.add(currentVal + "found in col" + j) ||
                                !seen.add(currentVal + "found in box" + i/3 + "-" + j/3))
                        {
                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }

