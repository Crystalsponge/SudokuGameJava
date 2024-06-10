import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SudokuSolver {
    SudokuSolver() {
        super();
    }
    boolean solve(int[][] board, int row, int col) {
        if(col > 8) {
            row++;
            col = 0;
            if( row > 8) {
                return true;
            }
        }
        if(board[row][col] != 0) {
            return solve(board, row, col + 1);
        } else {
            for(int i = 1; i < 10; i++) {
                if(validate(board,row,col,i)) {
                    board[row][col] = i;
                    if(solve(board,row,col + 1)) {return true;}
                }
                else board[row][col] = 0;
            }
        }
        return false;
        
        
    }
    boolean validate(int[][] board, int row, int col, int num) {
        //Check row
        for(int i = 0; i < board.length; i++) {
            if(i == col) {
                continue;
            }
            if(board[row][i] == num) {
                return false;
            }
        }
        //Check col
        for(int i = 0; i < board.length;i++) {
            if(i == row) {
                continue;
            }
            if(board[i][col] == num) {
                return false;
            }
        }
        //check box
        int box_r = row/3;
        int box_c = col/3;
        for(int i = box_r*3; i < box_r*3 + 3; i++) {
            for(int j = box_c*3; j < box_c*3 + 3; j++) {
                if(i == row && j == col) {
                    continue;
                }
                if(board[i][j] == num) {
                    return false;}
            }
        }
        return true;
    }
    
}
