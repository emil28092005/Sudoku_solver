import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    List<Chromosome> population = new ArrayList<>(); // Список хромосом
    Random random = new Random();

    public static void main(String[] args) {
        // Чтение входной матрицы из консоли
        int[][] baseSudoku = new int[9][9];
        List<int[]> mutablePositions = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            for (int i = 0; i < 9; i++) {
                String[] tokens = reader.readLine().split(" ");
                for (int j = 0; j < 9; j++) {
                    if (tokens[j].equals("-")) {
                        baseSudoku[i][j] = 0;
                        mutablePositions.add(new int[]{i, j});
                    } else {
                        baseSudoku[i][j] = Integer.parseInt(tokens[j]);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
            return;
        }

        // Создаем экземпляр класса Main
        Main mainInstance = new Main();
        mainInstance.generateInitialChromosomes(100, baseSudoku, mutablePositions); // Генерация 100 хромосом

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
            generation++;

            // Критерий завершения (если найдено идеальное решение)
            if (bestSolution.getFitness() == 0) {
                bestSolution.printChromosome(false);
                return;
            }
        }
    }

    // Генерация начальной популяции хромосом
    public void generateInitialChromosomes(int numberOfChromosomes, int[][] baseSudoku, List<int[]> mutablePositions) {
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
       
        // Подсчет нарушений в строках
        private int countRowViolations() {
            int violations = 0;
            for (int i = 0; i < 9; i++) {
                boolean[] present = new boolean[10]; // Используем индекс от 1 до 9
                for (int j = 0; j < 9; j++) {
                    int value = sudoku[i][j];
                    if (value != 0) {
                        if (present[value]) {
                            violations++;
                        } else {
                            present[value] = true;
                        }
                    }
                }
            }
            return violations;
        }

        // Подсчет нарушений в колонках
        private int countColumnViolations() {
            int violations = 0;
            for (int j = 0; j < 9; j++) {
                boolean[] present = new boolean[10]; // Используем индекс от 1 до 9
                for (int i = 0; i < 9; i++) {
                    int value = sudoku[i][j];
                    if (value != 0) {
                        if (present[value]) {
                            violations++;
                        } else {
                            present[value] = true;
                        }
                    }
                }
            }
            return violations;
        }

        // Подсчет нарушений в подрешетках
        private int countSubgridViolations() {
            int violations = 0;
            for (int gridRow = 0; gridRow < 3; gridRow++) {
                for (int gridCol = 0; gridCol < 3; gridCol++) {
                    boolean[] present = new boolean[10]; // Используем индекс от 1 до 9
                    for (int row = gridRow * 3; row < gridRow * 3 + 3; row++) {
                        for (int col = gridCol * 3; col < gridCol * 3 + 3; col++) {
                            int value = sudoku[row][col];
                            if (value != 0) {
                                if (present[value]) {
                                    violations++;
                                } else {
                                    present[value] = true;
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
            //System.out.println("Sudoku Matrix:");
            for (int[] row : sudoku) {
                for (int num : row) {
                    System.out.print(num + " ");
                }
                System.out.println();
            }
            if (printMutPos) {
                System.out.println("Mutable Positions:");
                for (int[] pos : mutablePositions) {
                    System.out.print("(" + pos[0] + ", " + pos[1] + ") ");
                }
                System.out.println();
            }
        }
    }
}