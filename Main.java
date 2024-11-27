import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    List<Chromosome> population = new ArrayList<>(); // Список хромосом
    Random random = new Random();

    public static void main(String[] args) {
        // Создаем экземпляр класса Main
        Main mainInstance = new Main();
        mainInstance.generateInitialChromosomes(100); // Генерация 100 хромосом

        // Убираем ограничение на количество поколений
        double mutationRate = 0.05; // Вероятность мутации
        Chromosome bestSolution = null;

        int generation = 0;
        while (true) {
            // Оценка текущего поколения
            mainInstance.evaluatePopulation();

            // Отбор родителей и создание новых потомков
            List<Chromosome> newPopulation = new ArrayList<>();
            for (int i = 0; i < mainInstance.population.size() / 2; i++) {

                List<Chromosome> parents = mainInstance.tournamentSelection(5);
                Chromosome child1 = mainInstance.crossoverBySubgrids(parents.get(0), parents.get(1));
                Chromosome child2 = mainInstance.crossoverBySubgrids(parents.get(1), parents.get(0));

                // Мутация потомков
                mainInstance.mutateChromosome(child1, mutationRate);
                mainInstance.mutateChromosome(child2, mutationRate);

                newPopulation.add(child1);
                newPopulation.add(child2);
            }

            // Замена старой популяции на новую
            mainInstance.population = newPopulation;

            // Логирование лучшего решения текущего поколения
            bestSolution = mainInstance.getBestChromosome();
            System.out.println("Generation " + generation + ": Best Fitness = " + bestSolution.getFitness());
            generation++;

            // Критерий завершения (если найдено идеальное решение)
            if (bestSolution.getFitness() == 0) {
                System.out.println("Optimal solution found in generation " + generation);
                System.out.println("Final Solution:");
                bestSolution.printChromosome(false);
                System.out.println("Fitness: " + bestSolution.getFitness());
                return;
            }
        }

        
        

        
        

    }

    // Генерация начальной популяции хромосом
    public void generateInitialChromosomes(int numberOfChromosomes) {
        String inputFileName = "input.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(inputFileName))) {
            String line;
            int[][] baseSudoku = new int[9][9]; // Матрица Судоку для шаблона
            List<int[]> mutablePositions = new ArrayList<>(); // Список координат изменяемых позиций

            int rowIndex = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(" "); // Разделяем строку по пробелам
                for (int colIndex = 0; colIndex < tokens.length; colIndex++) {
                    if (tokens[colIndex].equals("-")) {
                        baseSudoku[rowIndex][colIndex] = 0; // Пустые ячейки заполняются 0
                        mutablePositions.add(new int[]{rowIndex, colIndex}); // Добавляем координаты изменяемой позиции
                    } else {
                        baseSudoku[rowIndex][colIndex] = Integer.parseInt(tokens[colIndex]); // Конвертируем строку в число
                    }
                }
                rowIndex++;
            }

            // Создаем заданное количество хромосом
            for (int i = 0; i < numberOfChromosomes; i++) {
                int[][] sudoku = copyMatrix(baseSudoku); // Копируем базовый Судоку
                // Заполняем изменяемые позиции случайными числами (1-9)
                for (int[] pos : mutablePositions) {
                    int row = pos[0];
                    int col = pos[1];
                    sudoku[row][col] = random.nextInt(9) + 1;
                }
                // Добавляем хромосому в популяцию
                Chromosome chromosome = new Chromosome(sudoku, new ArrayList<>(mutablePositions));
                chromosome.evaluateFitness(); // Оценка фитнеса хромосомы
                population.add(chromosome);
            }

        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }

    // Копирование матрицы
    private int[][] copyMatrix(int[][] original) {
        int[][] copy = new int[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
        }
        return copy;
    }

    // Турнирная селекция
    public List<Chromosome> tournamentSelection(int tournamentSize) {
        List<Chromosome> selectedParents = new ArrayList<>();
        for (int i = 0; i < 2; i++) { // Выбираем двух родителей
            List<Chromosome> tournament = new ArrayList<>();
            for (int j = 0; j < tournamentSize; j++) {
                Chromosome randomChromosome = population.get(random.nextInt(population.size()));
                tournament.add(randomChromosome);
            }
            Chromosome best = tournament.get(0);
            for (Chromosome chromosome : tournament) {
                if (chromosome.getFitness() < best.getFitness()) {
                    best = chromosome;
                }
            }
            selectedParents.add(best);
        }
        return selectedParents;
    }

        // Кроссовер по подрешеткам
    public Chromosome crossoverBySubgrids(Chromosome parent1, Chromosome parent2) {
        int[][] childSudoku = new int[9][9];

        // Сначала копируем матрицу от первого родителя
        for (int row = 0; row < 9; row++) {
            System.arraycopy(parent1.getSudoku()[row], 0, childSudoku[row], 0, 9);
        }

        // Выбираем случайные подрешётки для обмена (например, 4 подрешётка)
        int numSubgridsToSwap = random.nextInt(5) + 1; // Выбираем от 1 до 5 подрешеток для обмена
        List<Integer> selectedSubgrids = new ArrayList<>();
        while (selectedSubgrids.size() < numSubgridsToSwap) {
            int subgridIndex = random.nextInt(9);
            if (!selectedSubgrids.contains(subgridIndex)) {
                selectedSubgrids.add(subgridIndex);
            }
        }

        // Выполняем обмен выбранных подрешётков
        for (int subgrid : selectedSubgrids) {
            int rowStart = (subgrid / 3) * 3;
            int colStart = (subgrid % 3) * 3;
            for (int row = rowStart; row < rowStart + 3; row++) {
                for (int col = colStart; col < colStart + 3; col++) {
                    childSudoku[row][col] = parent2.getSudoku()[row][col];
                }
            }
        }

        // Создаем и возвращаем нового потомка
        Chromosome child = new Chromosome(childSudoku, parent1.getMutablePositions());
        child.evaluateFitness();
        child.evaluateFitness();
        return child;
    }

    
    // Вывод содержимого популяции для проверки
    public void printPopulation(boolean printMutPos) {
        System.out.println("Generated Population:");
        int count = 1;
        for (Chromosome chromosome : population) {
            System.out.println("Chromosome " + count + ":");
            chromosome.printChromosome(printMutPos);
            System.out.println("Fitness: " + chromosome.getFitness());
            System.out.println();
            count++;
        }
    }

    // Мутация хромосомы
    public void mutateChromosome(Chromosome chromosome, double mutationRate) {
        int[][] sudoku = chromosome.getSudoku();
        List<int[]> mutablePositions = chromosome.getMutablePositions();

        // Проходим по всем изменяемым позициям
        for (int[] pos : mutablePositions) {
            if (random.nextDouble() < mutationRate) {
                int row = pos[0];
                int col = pos[1];
                int newValue = random.nextInt(9) + 1; // Генерируем новое значение от 1 до 9
                sudoku[row][col] = newValue; // Изменяем значение в выбранной позиции
            }
        }

        // Обновляем матрицу и оцениваем фитнес
        chromosome.setSudoku(sudoku);
        chromosome.evaluateFitness();
    }

    // Метод для оценки всей популяции
    public void evaluatePopulation() {
        for (Chromosome chromosome : population) {
            chromosome.evaluateFitness();
        }
    }

    // Метод для получения лучшей хромосомы в популяции
    public Chromosome getBestChromosome() {
        Chromosome best = population.get(0);
        for (Chromosome chromosome : population) {
            if (chromosome.getFitness() < best.getFitness()) {
                best = chromosome;
            }
        }
        return best;
    }

    // Класс хромосомы
    public class Chromosome {
        private int[][] sudoku; // Матрица Судоку
        private List<int[]> mutablePositions; // Список координат изменяемых позиций
        private int fitness; // Значение фитнеса

        // Конструктор для инициализации хромосомы
        public Chromosome(int[][] sudoku, List<int[]> mutablePositions) {
            this.sudoku = sudoku;
            this.mutablePositions = mutablePositions;
        }

        // Геттер для матрицы Судоку
        public int[][] getSudoku() {
            return sudoku;
        }

        // Сеттер для матрицы Судоку
        public void setSudoku(int[][] sudoku) {
            this.sudoku = sudoku;
        }

        // Геттер для изменяемых позиций
        public List<int[]> getMutablePositions() {
            return mutablePositions;
        }

        // Сеттер для изменяемых позиций
        public void setMutablePositions(List<int[]> mutablePositions) {
            this.mutablePositions = mutablePositions;
        }

        // Геттер для фитнеса
        public int getFitness() {
            return fitness;
        }

        // Метод для оценки фитнеса хромосомы
        public void evaluateFitness() {
            fitness = countRowViolations() + countColumnViolations() + countSubgridViolations();
        }

        // Подсчет нарушений в строках
        private int countRowViolations() {
            int violations = 0;
            for (int[] row : sudoku) {
                boolean[] seen = new boolean[10];
                for (int num : row) {
                    if (num != 0) {
                        if (seen[num]) {
                            violations++;
                        } else {
                            seen[num] = true;
                        }
                    }
                }
            }
            return violations;
        }

        // Подсчет нарушений в столбцах
        private int countColumnViolations() {
            int violations = 0;
            for (int col = 0; col < 9; col++) {
                boolean[] seen = new boolean[10];
                for (int row = 0; row < 9; row++) {
                    int num = sudoku[row][col];
                    if (num != 0) {
                        if (seen[num]) {
                            violations++;
                        } else {
                            seen[num] = true;
                        }
                    }
                }
            }
            return violations;
        }

        // Подсчет нарушений в подрешетках 3x3
        private int countSubgridViolations() {
            int violations = 0;
            for (int rowStart = 0; rowStart < 9; rowStart += 3) {
                for (int colStart = 0; colStart < 9; colStart += 3) {
                    boolean[] seen = new boolean[10];
                    for (int row = rowStart; row < rowStart + 3; row++) {
                        for (int col = colStart; col < colStart + 3; col++) {
                            int num = sudoku[row][col];
                            if (num != 0) {
                                if (seen[num]) {
                                    violations++;
                                } else {
                                    seen[num] = true;
                                }
                            }
                        }
                    }
                }
            }
            return violations;
        }

        // Метод для отображения состояния хромосомы
        public void printChromosome(boolean printMutPos) {
            System.out.println("Sudoku Matrix:");
            for (int[] row : sudoku) {
                for (int num : row) {
                    System.out.print(num + " ");
                }
                System.out.println();
            }
            if (printMutPos) {
                System.out.println("Mutable Positions (Coordinates):");
                for (int[] pos : mutablePositions) {
                    System.out.println("Row: " + pos[0] + ", Col: " + pos[1]);
                }
            }
        }
    }
}
