import java.io.*;
import java.util.Random;

public class Main {
    int[][] population = new int[9][9]; // Матрица для хранения всего Sudoku
    Random random = new Random();

    public static void main(String[] args) {
        Main main = new Main();
        main.generateInitialMatrix();
        main.printPopulation();
    }

    // Генерация начальной матрицы из файла
    public void generateInitialMatrix() {
        String inputFileName = "input.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(inputFileName))) {
            String line;
            int rowIndex = 0; // Индекс строки в матрице
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(" "); // Разделяем строку по пробелам
                for (int colIndex = 0; colIndex < tokens.length; colIndex++) {
                    if (tokens[colIndex].equals("-")) {
                        population[rowIndex][colIndex] = random.nextInt(10); // Генерируем случайное число
                    } else {
                        population[rowIndex][colIndex] = Integer.parseInt(tokens[colIndex]); // Конвертируем строку в число
                    }
                }
                rowIndex++; // Переход к следующей строке
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }

    // Вывод содержимого population для проверки
    public void printPopulation() {
        System.out.println("Generated Matrix:");
        for (int[] row : population) {
            for (int num : row) {
                System.out.print(num + " ");
            }
            System.out.println();
        }
    }
}
