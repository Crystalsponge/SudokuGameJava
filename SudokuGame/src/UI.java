import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.*;
import javax.swing.event.AncestorListener;

import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.JOptionPane.showOptionDialog;

/**
 * The Cell class model the cells of the Sudoku puzzle, by customizing (subclass)
 * the javax.swing.JTextField to include row/column, puzzle number and status.
 */
class Cell extends JTextField {
    
    private static final long serialVersionUID = 1L;  // to prevent serial warning
    
    // Define named constants for JTextField's colors and fonts
    //  to be chosen based on CellStatus
    public static final Color BG_GIVEN = new Color(240, 240, 240); // RGB
    public static final Color FG_GIVEN = Color.BLACK;
    public static final Color FG_NOT_GIVEN = Color.GRAY;
    public static final Color BG_TO_GUESS  = new Color(89,192,250);
    public static final Color BG_CORRECT_GUESS = new Color(0, 216, 0);
    public static final Color BG_WRONG_GUESS   = new Color(216, 0, 0);
    public static final Font FONT_NUMBERS = new Font("OCR A Extended", Font.PLAIN, 28);
    
    // Define properties (package-visible)
    
    /** The row and column number [0-8] of this cell */
    int row, col;
    /** The puzzle number [1-9] for this cell */
    int number;
    int correctNumber;
    /** The status of this cell defined in enum CellStatus */
    CellStatus status;
    
    
    /** Constructor */
    public Cell(int row, int col) {
        super();   // JTextField
        this.row = row;
        this.col = col;
        // Inherited from JTextField: Beautify all the cells once for all
        super.setHorizontalAlignment(JTextField.CENTER);
        super.setFont(FONT_NUMBERS);
        
        
    }

    
    
    /** Reset this cell for a new game, given the puzzle number and isGiven */
    public void newGame(int number, boolean isGiven) {
        this.number = number;
        correctNumber = number;
        status = isGiven ? CellStatus.GIVEN : CellStatus.NOT_GUESSED;
        /** Making a pop-up right click menu*/
        Cell sourceCell = this;
        JPopupMenu menu = new JPopupMenu();
        JMenuItem hintItem = new JMenuItem("Hint");
        
        
        
        hintItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(sourceCell.status == CellStatus.GIVEN) {
                    menu.setVisible(false);
                }
                GameBoardPanel board = GameBoardPanelProvider.getInstance();
                JButton newGame = new JButton("New Game");
                newGame.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(sourceCell);  // Get frame containing the cell
                        currentFrame.dispose();
                        Window w = SwingUtilities.getWindowAncestor(newGame);
                        w.setVisible(false);
                        new SudokuDifficulty();
                        
                    }
                });
                Object[] a = new Object[]{newGame};
                if(Main.getHint() > 0) {
                    int number = sourceCell.correctNumber;
                    sourceCell.setText(String.valueOf(number));
                    sourceCell.status = CellStatus.CORRECT_GUESS;
                    sourceCell.number = sourceCell.correctNumber;
                    if(board.isSolved()) {
                        showOptionDialog(null,"You won","Victory", JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.INFORMATION_MESSAGE,null,a,1);
                    }
                    sourceCell.paint();
                    Main.removeHint();
                    board.drawLabel();
                    hintItem.setVisible(false);}
                else {
                    JLabel msg = new JLabel("You ran out of hints");
                    showMessageDialog(null,msg,"Out of hint",JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        JMenuItem clear = new JMenuItem("Clear");
        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sourceCell.setText("");
                sourceCell.status = CellStatus.NOT_GUESSED;
                sourceCell.number = 0;
                sourceCell.paint();
                hintItem.setVisible(true);
            }
        });
        
        
        menu.add(clear);
        menu.add(hintItem);
        if(sourceCell.status != CellStatus.GIVEN) {
        sourceCell.setComponentPopupMenu(menu);}
        paint();    // paint itself
    }
    
    /** This Cell (JTextField) paints itself based on its status */
    public void paint() {
        if (status == CellStatus.GIVEN) {
            // Inherited from JTextField: Set display properties
            super.setText(number + "");
            super.setEditable(false);
            super.setBackground(BG_GIVEN);
            super.setForeground(FG_GIVEN);
        } else if (status == CellStatus.NOT_GUESSED) {
            // Inherited from JTextField: Set display properties
            super.setText("");
            super.setEditable(true);
            super.setBackground(BG_TO_GUESS);
            super.setForeground(FG_NOT_GIVEN);
        } else if (status == CellStatus.CORRECT_GUESS) {  // from TO_GUESS
            super.setBackground(BG_CORRECT_GUESS);
        } else if (status == CellStatus.WRONG_GUESS) {    // from TO_GUESS
            super.setBackground(BG_WRONG_GUESS);
        } else if (status == CellStatus.GUESSING) {
            super.setBackground(BG_TO_GUESS);
            super.setForeground(FG_NOT_GIVEN);
        }
    }
    
}


class GameBoardPanel extends JPanel {
    private static final long serialVersionUID = 1L;  // to prevent serial warning
    
    // Define named constants for UI sizes
    public static final int CELL_SIZE = 60;   // Cell width/height in pixels
    public static final int BOARD_WIDTH  = CELL_SIZE * 9;
    public static final int BOARD_HEIGHT = CELL_SIZE * 9;
    // Board width/height in pixels
    
    // Define properties
    /** The game board composes of 9x9 Cells (customized JTextFields) */
    private Cell[][] cells = new Cell[9][9];
    /** It also contains a Puzzle with array numbers and isGiven */
    private Generator generator = new Generator();
    private JLabel hintLabel;
    private JPanel main;
    private JButton button;
    
    /** Constructor */
    public GameBoardPanel() {
        
        main = new JPanel(new BorderLayout());
        
        // Create the JLabel for hint
        hintLabel = new JLabel();
        button = new JButton("Check");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                check();
            }
        });
        main.add(hintLabel, BorderLayout.PAGE_START);  // Add to north position
        main.add(button, BorderLayout.PAGE_END);
        // Set the main panel as the content pane of this panel
        this.setLayout(new BorderLayout());
        this.add(main, BorderLayout.CENTER);  // Add main panel to center
        
        // Allocate the 2D array of Cell, and added into JPanel
        JPanel gridPanel = new JPanel(new GridLayout(9, 9));  // Create separate panel for grid
        for (int row = 0; row < 9; ++row) {
            for (int col = 0; col < 9; ++col) {
                cells[row][col] = new Cell(row, col);
                gridPanel.add(cells[row][col]);   // Add cells to the grid panel
            }
        }
        
        main.add(gridPanel, BorderLayout.CENTER);

        
       
        CellInputListener listener = new CellInputListener();
        
        for (int row  = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (cells[row][col].isEditable()) {
                    cells[row][col].addKeyListener(listener);   // For all editable rows and cols
                }
            }
        }
        
        super.setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
    }
    
    /**
     * Generate a new puzzle; and reset the game board of cells based on the puzzle.
     * You can call this method to start a new game.
     */
    public void newGame(String difficulty) {
        // Generate a new puzzle
        int[][] board = new int[9][9];
        generator.generateBoard(board,difficulty);
        
        // Initialize all the 9x9 cells, based on the puzzle.
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                cells[row][col].newGame(generator.numbers[row][col], generator.isGiven[row][col]);
            }
        }
        
    }
    boolean applied = false;
    public int[][] getCells() {
        int[][] board = new int[9][9];
        if(!applied) {

        boolean[][] given;
        given = generator.isGiven;
        
        for(int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                board[i][j] = cells[i][j].number;
                if(!given[i][j]) {board[i][j] = 0;
                cells[i][j].number = 0;}
                applied = true;
            }

        }
        return board;}
        for(int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                board[i][j] = cells[i][j].number;
            }
        }
        return board;
    }
    public void drawLabel() {
        hintLabel.setText("Hint" + Main.getHint());
        hintLabel.repaint();
        main.revalidate();
        main.repaint();
        main.updateUI();
        this.repaint();
        this.updateUI();
    }
    
    /**
     * Return true if the puzzle is solved
     * i.e., none of the cell have status of TO_GUESS or WRONG_GUESS
     */
    SudokuSolver solver = new SudokuSolver();
    public void check() {
        for(int row = 0; row <9; row++) {
            for(int col = 0; col < 9; col++) {
                if(cells[row][col].number == cells[row][col].correctNumber && cells[row][col].status != CellStatus.GIVEN && cells[row][col].status != CellStatus.NOT_GUESSED) {
                    cells[row][col].status = CellStatus.CORRECT_GUESS;
                    cells[row][col].paint();
                }
                //Check if error cells is back to correct
                if(cells[row][col].status == CellStatus.WRONG_GUESS) {
                    GameBoardPanel board = GameBoardPanelProvider.getInstance();
                    int[][] board1 = board.getCells();
                    if(solver.validate(board1,row,col,cells[row][col].number)) {
                        cells[row][col].status = CellStatus.GUESSING;
                        cells[row][col].paint();
                    }
                }
            }
        }
    }
    public boolean isSolved() {
        for (int row = 0; row < 9; ++row) {
            for (int col = 0; col < 9; ++col) {
                if (cells[row][col].status == CellStatus.NOT_GUESSED || cells[row][col].status == CellStatus.WRONG_GUESS) {
                    return false;
                }
                if(cells[row][col].number != cells[row][col].correctNumber) {return false;}
                
                if(cells[row][col].status != CellStatus.NOT_GUESSED && cells[row][col].status != CellStatus.WRONG_GUESS && cells[row][col].status != CellStatus.GIVEN) {
                cells[row][col].status = CellStatus.CORRECT_GUESS;}
            }
        }
        return true;
    }
    
}
class GameBoardPanelProvider {
    private static GameBoardPanel instance;
    
    private GameBoardPanelProvider() {}  // Private constructor to prevent external instantiation
    
    public static GameBoardPanel getInstance() {
        if (instance == null) {
            instance = new GameBoardPanel();  // Create the instance on first access
        }
        return instance;
    }
    public static GameBoardPanel newInstance() {
        instance = new GameBoardPanel();
        return instance;
    }
}
class SudokuMain extends JFrame {
    private static final long serialVersionUID = 1L;  // to prevent serial warning
    
    // private variables
    
    GameBoardPanel board = GameBoardPanelProvider.newInstance();
    
    // Constructor
   public SudokuMain(String difficulty) {
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        
        cp.add(board, BorderLayout.CENTER);
        
        // Initialize the game board to start the game
        board.newGame(difficulty);
        board.drawLabel();
        pack();     // Pack the UI components, instead of using setSize()
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // to handle window-closing
        setTitle("Sudoku");
        setVisible(true);
    }
}
class CellInputListener implements KeyListener {
    
    @Override
    public void keyTyped(KeyEvent e) {
        // Get a reference of the JTextField that triggers this action event
        Cell sourceCell = (Cell) e.getSource();
        GameBoardPanel board = GameBoardPanelProvider.getInstance();
        int[][] board1 = board.getCells();
        JButton newGame = new JButton("New Game");
        newGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(sourceCell);  // Get frame containing the cell
                currentFrame.dispose();
                Window w = SwingUtilities.getWindowAncestor(newGame);
                w.setVisible(false);
                new SudokuDifficulty();
                
            }
        });
        Object[] a = new Object[] {newGame};
        
        
        int numberIn = 0;
        // Retrieve the int entered
        if (e.getKeyChar() != KeyEvent.VK_BACK_SPACE) {
            
            try {
                numberIn = Integer.parseInt(String.valueOf(e.getKeyChar()));
            } catch (NumberFormatException ec) {
                showMessageDialog(null, "You inputted incorrect value", "Incorrect value", JOptionPane.WARNING_MESSAGE);
            }
            
            
            /*
             * [TODO 5] (later - after TODO 3 and 4)
             * Check the numberIn against sourceCell.number.
             * Update the cell status sourceCell.status,
             * and re-paint the cell via sourceCell.paint().
             */
            SudokuSolver solver = new SudokuSolver();
            
            
            int row = sourceCell.row;
            int col = sourceCell.col;
            if (solver.validate(board1, row, col, numberIn) && (numberIn >= 0 && numberIn < 10)) {
                sourceCell.status = CellStatus.GUESSING;
                sourceCell.number = numberIn;
                
            } else {
                sourceCell.status = CellStatus.WRONG_GUESS;
                sourceCell.number = numberIn;
            }
            sourceCell.paint();   // re-paint this cell based on its status
            
            /*
             * [TODO 6] (later)
             * Check if the player has solved the puzzle after this move,
             *   by calling isSolved(). Put up a congratulation JOptionPane, if so.
             */
            
            
            if (board.isSolved()) {
                showOptionDialog(null, "You won", "Victory", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, a, 1);
            }
        } else {
            sourceCell.setText("");
            sourceCell.status = CellStatus.NOT_GUESSED;
            sourceCell.number = 0;
            sourceCell.paint();
        }
    }
    @Override
    public void keyPressed(KeyEvent e) {
    
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
    
    }
}
class SudokuDifficulty extends JFrame implements ActionListener {
    
    private final JButton btnEasy, btnMedium, btnHard;
    
    public SudokuDifficulty() {
        super("Sudoku Difficulty");
        // Create buttons for difficulty options
        btnEasy = new JButton("easy");
        btnMedium = new JButton("medium");
        btnHard = new JButton("hard");
        
        // Add action listener to all buttons
        btnEasy.addActionListener(this);
        btnMedium.addActionListener(this);
        btnHard.addActionListener(this);
        
        // Layout the buttons in a vertical flow
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(btnEasy);
        buttonPanel.add(btnMedium);
        buttonPanel.add(btnHard);
        buttonPanel.setPreferredSize(new Dimension(300,300));
        // Add button panel to main frame
        getContentPane().add(buttonPanel, BorderLayout.CENTER);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        JButton clickedButton = (JButton) e.getSource();
        
        String difficulty = clickedButton.getText(); // Get the difficulty level
        new SudokuMain(difficulty);
        
        setVisible(false);
    }
    
    
    
}
