//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static int hint;
    
    public static void main(String[] args) {
        Constants constants = new Constants();
        int row = constants.GRID_SIZE;
        int col = constants.GRID_SIZE;
    new SudokuDifficulty();
    }
    public static void setHint(int a) {
        hint = a;
    }
    public static int getHint() {
        return hint;
    }
    public static void removeHint() {
        hint--;
    }
}

