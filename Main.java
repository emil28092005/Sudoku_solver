import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        char[][] matrix = readInputs();

        for (char[] row : matrix) {
            for (char c : row) {
                System.out.print(c + " ");
            }
            System.out.println();
        }
    }

    public static char[][] readInputs() {
        char[][] matrix = new char[9][9]; // Initialize a 9x9 matrix
        try {
            File file = new File("./input.txt");
            Scanner scanner = new Scanner(file);
            
            int row = 0;
            while (scanner.hasNextLine() && row < 9) {
                String line = scanner.nextLine();
                if (line.length() != 9) {
                    throw new IllegalArgumentException("Each line in the file must have exactly 9 characters.");
                }
                for (int col = 0; col < 9; col++) {
                    matrix[row][col] = line.charAt(col);
                }
                row++;
            }
            
            if (row != 9) {
                throw new IllegalArgumentException("The file must contain exactly 9 lines.");
            }
            
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        
        return matrix;
    }
}
