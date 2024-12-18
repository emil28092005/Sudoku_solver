import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    // Initalizing variables
    public static int POPULATIONSIZE = 0;
    public static int TOURNAMENTSIZE = 0;
    public static double MUTATIONRATE = 0;
    // Threshold of number of mutable positions for easy level sudoku
    public static int EASYTHRESHOLD = 60; 
    public static int HARDTHRESHOLD = 70;
    // List to store the population of chromosomes  
    List<Chromosome> population = new ArrayList<>();
    Random random = new Random();

    public static void main(String[] args) {
        // Base Sudoku matrix (input matrix)
        int[][] baseSudoku = new int[9][9];
        // List to track positions in Sudoku that are mutable
        List<int[]> mutablePositions = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        try {
            // Reading the Sudoku matrix from the console input
            for (int i = 0; i < 9; i++) {
                // Split the input by space
                String[] tokens = reader.readLine().split(" ");
                for (int j = 0; j < 9; j++) {
                    if (tokens[j].equals("-")) {
                        // Empty cells are marked as 0
                        baseSudoku[i][j] = 0;
                        // Add mutable positions (i, j) to the list
                        mutablePositions.add(new int[]{i, j});
                    } else {
                        // Set fixed value from the input
                        baseSudoku[i][j] = Integer.parseInt(tokens[j]);
                    }
                }
            }
        } catch (IOException e) {
            // If an error occurs during input reading, print the error and stop the program
            System.err.println("Error reading input: " + e.getMessage());
            return;
        }

        Main mainInstance = new Main(); // Create instance of Main class

        // Choosing variables for different sudoku difficulties
        if (mutablePositions.size() < EASYTHRESHOLD) { // Easy sudoku
            POPULATIONSIZE = 100000;
            TOURNAMENTSIZE = 8;
            MUTATIONRATE = 0.34;
        } else if (mutablePositions.size() < HARDTHRESHOLD) { // Hard sudoku
            POPULATIONSIZE = 100000;
            TOURNAMENTSIZE = 8;
            MUTATIONRATE = 0.34;
        }
        else { // Ultra-hard sudoku
            POPULATIONSIZE = 500000;
            TOURNAMENTSIZE = 3;
            MUTATIONRATE = 0.15;
        }
        
        

        // Generate initial population of 100 chromosomes
        mainInstance.generateInitialChromosomes(POPULATIONSIZE, baseSudoku, mutablePositions);

        Chromosome bestSolution = null;

        int generation = 0; // Track the number of generations
        while (true) {
            // Evaluate the fitness of each chromosome in the population
            mainInstance.evaluatePopulation();

            List<Chromosome> newPopulation = new ArrayList<>();
            for (int i = 0; i < mainInstance.population.size() / 2; i++) {
                // Select parents using tournament selection
                List<Chromosome> parents = mainInstance.tournamentSelection(TOURNAMENTSIZE);
                // Perform crossover to create two children from the selected parents
                Chromosome child1 = mainInstance.crossoverBySubgrids(parents.get(0), parents.get(1));
                Chromosome child2 = mainInstance.crossoverBySubgrids(parents.get(1), parents.get(0));

                // Apply mutation to both children
                mainInstance.mutateChromosome(child1, MUTATIONRATE);
                mainInstance.mutateChromosome(child2, MUTATIONRATE);

                // Add both children to the new population
                newPopulation.add(child1);
                newPopulation.add(child2);
            }

            // Replace the old population with the new population
            mainInstance.population = newPopulation;

            // Get the best chromosome from the current population
            bestSolution = mainInstance.getBestChromosome();
            generation++;

            // If the best solution found has a fitness of 0, print it and end the program
            if (bestSolution.getFitness() == 0) {
                bestSolution.printChromosome(false);
                return;
            }
        }
    }

    // Generate initial population of chromosomes
    public void generateInitialChromosomes(int numberOfChromosomes, int[][] baseSudoku, List<int[]> mutablePositions) {
        for (int i = 0; i < numberOfChromosomes; i++) {
            // Create a copy of the base Sudoku
            int[][] sudoku = copyMatrix(baseSudoku);
            // Randomly fill mutable positions while ensuring no duplicates in subgrids
            for (int gridRow = 0; gridRow < 3; gridRow++) {
                for (int gridCol = 0; gridCol < 3; gridCol++) {
                    boolean[] present = new boolean[10];
                    List<int[]> subgridPositions = new ArrayList<>();
                    // Collect all positions in the current 3x3 subgrid
                    for (int row = gridRow * 3; row < gridRow * 3 + 3; row++) {
                        for (int col = gridCol * 3; col < gridCol * 3 + 3; col++) {
                            int value = sudoku[row][col];
                            if (value != 0) {
                                present[value] = true;
                            } else {
                                subgridPositions.add(new int[]{row, col});
                            }
                        }
                    }
                    // Randomly fill the subgrid ensuring no duplicates
                    for (int[] pos : subgridPositions) {
                        int newValue;
                        do {
                            newValue = random.nextInt(9) + 1;
                        } while (present[newValue]);
                        sudoku[pos[0]][pos[1]] = newValue;
                        present[newValue] = true;
                    }
                }
            }
            // Create a new chromosome with the generated Sudoku and mutable positions
            Chromosome chromosome = new Chromosome(sudoku, new ArrayList<>(mutablePositions));
            chromosome.evaluateFitness(); // Evaluate its fitness
            population.add(chromosome); // Add to the population
        }
    }


    // Create a deep copy of a matrix
    private int[][] copyMatrix(int[][] original) {
        int[][] copy = new int[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
        }
        return copy;
    }


    public List<Chromosome> tournamentSelection(int tournamentSize) {
        List<Chromosome> selectedParents = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            List<Chromosome> tournament = new ArrayList<>();
            // Randomly select chromosomes for the tournament
            for (int j = 0; j < tournamentSize; j++) {
                Chromosome randomChromosome = population.get(random.nextInt(population.size()));
                tournament.add(randomChromosome);
            }
            // Determine the best chromosome in the tournament based on fitness
            Chromosome best = tournament.get(0);
            for (Chromosome chromosome : tournament) {
                if (chromosome.getFitness() < best.getFitness()) {
                    best = chromosome;
                }
            }
            // Add the best chromosome to the list of selected parents
            selectedParents.add(best);
        }
        return selectedParents;
    }
    
    public Chromosome crossoverBySubgrids(Chromosome parent1, Chromosome parent2) {
        int[][] childSudoku = new int[9][9];
    
        // Copy the entire Sudoku grid from parent1 to the child
        for (int row = 0; row < 9; row++) {
            System.arraycopy(parent1.getSudoku()[row], 0, childSudoku[row], 0, 9);
        }
    
        // Determine the number of subgrids to swap from parent2 to child (1-5)
        int numSubgridsToSwap = random.nextInt(5) + 1;
        List<Integer> selectedSubgrids = new ArrayList<>();
        while (selectedSubgrids.size() < numSubgridsToSwap) {
            int subgridIndex = random.nextInt(9);
            if (!selectedSubgrids.contains(subgridIndex)) {
                selectedSubgrids.add(subgridIndex);
            }
        }
    
        // Swap the selected subgrids from parent2 into the child
        for (int subgrid : selectedSubgrids) {
            int rowStart = (subgrid / 3) * 3;
            int colStart = (subgrid % 3) * 3;
            for (int row = rowStart; row < rowStart + 3; row++) {
                for (int col = colStart; col < colStart + 3; col++) {
                    childSudoku[row][col] = parent2.getSudoku()[row][col];
                }
            }
        }
    
        // Create a new chromosome with the resulting child Sudoku and evaluate its fitness
        Chromosome child = new Chromosome(childSudoku, parent1.getMutablePositions());
        child.evaluateFitness();
        return child;
    }
    
    public void printPopulation(boolean printMutPos) {
        System.out.println("Generated Population:");
        int count = 1;
        // Print each chromosome's Sudoku and fitness value
        for (Chromosome chromosome : population) {
            System.out.println("Chromosome " + count + ":");
            chromosome.printChromosome(printMutPos);
            System.out.println("Fitness: " + chromosome.getFitness());
            System.out.println();
            count++;
        }
    }
    
    public void mutateChromosome(Chromosome chromosome, double mutationRate) {
        int[][] sudoku = chromosome.getSudoku();
    
        // With a probability defined by mutationRate, perform a mutation by swapping subgrids
        if (random.nextDouble() < mutationRate) {
            // Randomly select two different subgrids to swap
            int subgrid1, subgrid2;
            do {
                subgrid1 = random.nextInt(9);
                subgrid2 = random.nextInt(9);
            } while (subgrid1 == subgrid2);
    
            // Get the starting coordinates for both subgrids
            int rowStart1 = (subgrid1 / 3) * 3;
            int colStart1 = (subgrid1 % 3) * 3;
            int rowStart2 = (subgrid2 / 3) * 3;
            int colStart2 = (subgrid2 % 3) * 3;
    
            // Swap the values in the two selected subgrids
            for (int rowOffset = 0; rowOffset < 3; rowOffset++) {
                for (int colOffset = 0; colOffset < 3; colOffset++) {
                    int temp = sudoku[rowStart1 + rowOffset][colStart1 + colOffset];
                    sudoku[rowStart1 + rowOffset][colStart1 + colOffset] = sudoku[rowStart2 + rowOffset][colStart2 + colOffset];
                    sudoku[rowStart2 + rowOffset][colStart2 + colOffset] = temp;
                }
            }
        }
    
        // Update the chromosome's Sudoku and recalculate its fitness
        chromosome.setSudoku(sudoku);
        chromosome.evaluateFitness();
    }
    
    
    public void evaluatePopulation() {
        // Evaluate the fitness of each chromosome in the population
        for (Chromosome chromosome : population) {
            chromosome.evaluateFitness();
        }
    }
    
    public Chromosome getBestChromosome() {
        // Find and return the chromosome with the best (lowest) fitness in the population
        Chromosome best = population.get(0);
        for (Chromosome chromosome : population) {
            if (chromosome.getFitness() < best.getFitness()) {
                best = chromosome;
            }
        }
        return best;
    }
    
    // Chromosome class representing an individual solution
    public class Chromosome {
        private int[][] sudoku; // Sudoku grid representing the chromosome
        private List<int[]> mutablePositions; // Positions that can be changed (mutable)
        private int fitness; // Fitness value representing the number of conflicts
    
        // Constructor for initializing a Chromosome with a Sudoku grid and mutable positions
        public Chromosome(int[][] sudoku, List<int[]> mutablePositions) {
            this.sudoku = sudoku;
            this.mutablePositions = mutablePositions;
        }
    
        public int[][] getSudoku() {
            return sudoku;
        }
    
        public void setSudoku(int[][] sudoku) {
            this.sudoku = sudoku;
        }
    
        public List<int[]> getMutablePositions() {
            return mutablePositions;
        }
    
        public int getFitness() {
            return fitness;
        }
    
        // Evaluate the fitness of the Sudoku by counting the number of row, column, and subgrid violations
        public void evaluateFitness() {
            fitness = countRowViolations() + countColumnViolations();
        }
    
        private int countRowViolations() {
            int violations = 0;
            // Iterate through each row to count conflicts
            for (int i = 0; i < 9; i++) {
                boolean[] present = new boolean[10];
                for (int j = 0; j < 9; j++) {
                    int value = sudoku[i][j];
                    if (value != 0) {
                        if (present[value]) {
                            violations++; // Increment violations if the value is already seen
                        } else {
                            present[value] = true; // Mark the value as seen
                        }
                    }
                }
            }
            return violations;
        }
        private int countColumnViolations() {
            int violations = 0;
            // Loop through each column
            for (int j = 0; j < 9; j++) {
                boolean[] present = new boolean[10]; // Track numbers present in the column
                for (int i = 0; i < 9; i++) {
                    int value = sudoku[i][j];
                    if (value != 0) {
                        // If the number is already present, increment the violations count
                        if (present[value]) {
                            violations++;
                        } else {
                            // Mark the number as present
                            present[value] = true;
                        }
                    }
                }
            }
            return violations;
        }
        
        private int countSubgridViolations() {
            int violations = 0;
            // Loop through each 3x3 subgrid
            for (int gridRow = 0; gridRow < 3; gridRow++) {
                for (int gridCol = 0; gridCol < 3; gridCol++) {
                    boolean[] present = new boolean[10]; // Track numbers present in the subgrid
                    // Loop through cells in the 3x3 subgrid
                    for (int row = gridRow * 3; row < gridRow * 3 + 3; row++) {
                        for (int col = gridCol * 3; col < gridCol * 3 + 3; col++) {
                            int value = sudoku[row][col];
                            if (value != 0) {
                                // If the number is already present, increment the violations count
                                if (present[value]) {
                                    violations++;
                                } else {
                                    // Mark the number as present
                                    present[value] = true;
                                }
                            }
                        }
                    }
                }
            }
            return violations;
        }
        
        public void printChromosome(boolean printMutPos) {
            // Print the Sudoku matrix
            for (int[] row : sudoku) {
                for (int j = 0; j < row.length; j++) {
                    System.out.print(row[j]);
                    if (j < row.length - 1) {
                        System.out.print(" ");
                    }
                }
                System.out.println();
            }
            // If requested, print mutable positions
            if (printMutPos) {
                System.out.println("Mutable Positions:");
                for (int i = 0; i < mutablePositions.size(); i++) {
                    int[] pos = mutablePositions.get(i);
                    System.out.print("(" + pos[0] + ", " + pos[1] + ")");
                    if (i < mutablePositions.size() - 1) {
                        System.out.print(" ");
                    }
                }
                System.out.println();
            }
        }
    }
}        
