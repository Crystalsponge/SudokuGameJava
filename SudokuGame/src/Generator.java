import java.util.*;
import java.util.random.RandomGenerator;

public class Generator {
    int[][] numbers = new int[9][9];
    int[][] correctNumbers = new int[9][9];
    boolean[][] isGiven = new boolean[9][9];
    Generator() {
        super();
    }
    SudokuSolver solver = new SudokuSolver();
    boolean generateCell(int[][] board, int row, int col) {
        Integer[] num = {1,2,3,4,5,6,7,8,9};
        List<Integer> numList = Arrays.asList(num);
        if(col > 8) {
            col = 0;
            row++;
        }
        if(row > 8) {return true;}
        
        if(board[row][col] != 0) {
            if(generateCell(board,row,col+1)) {
                return true;
            }
        }

        //generating cells
        else {
            Collections.shuffle(numList);
            numList.toArray(num);
            for(int i : num) {
                if(solver.validate(board,row,col,i)) {
                    board[row][col] = i;
                    
                    if (generateCell(board, row, col + 1)) {
                        return true;
                    }
                }
                board[row][col] = 0;
            }
        }
        return false;
    }
    void generateSolvedBoard(int[][] board) {
        int[][] solvedBoard = new int[9][9];
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                solvedBoard[i][j] = 0;
            }
        }
        if(generateCell(solvedBoard,0,0)) {
            if(solver.solve(solvedBoard,0,0)) {
        
        for(int i = 0; i < board.length; i++) {
            board[i] = solvedBoard[i].clone();
        }return;
            }
    }
    }
    void generateBoard(int[][] board, String message) {


        int cells = switch (message) {
            case "easy" -> 81 - 41;
            case "medium" -> 81 - 33;
            case "hard" -> 81 - 28;
            default -> 0;
        };
        int hint = switch (message) {
            case "easy" -> 5;
            case "medium" -> 3;
            case "hard" -> 2;
            default -> 0;
        };
        Main.setHint(hint);
        generateSolvedBoard(board);
        for(int i = 0; i < 9; i++) {
            System.arraycopy(board[i],0,numbers[i],0,9);
        }
        while(cells > 0) {
            int row = (int) (Math.random()*9);
            int col = (int) (Math.random()*9);
            if(board[row][col] != 0) {
                board[row][col] = 0;
                cells--;
            }
        }
        
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                isGiven[i][j] = board[i][j] != 0;
            }
        }
        
    }
}
