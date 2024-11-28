package com.example;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;

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
            POPULATIONSIZE = 75;
            TOURNAMENTSIZE = 4;
            MUTATIONRATE = 0.04;
        } else if (mutablePositions.size() < HARDTHRESHOLD) { // Hard sudoku
            POPULATIONSIZE = 150;
            TOURNAMENTSIZE = 5;
            MUTATIONRATE = 0.055;
        }
        else { // Ultra-hard sudoku
            POPULATIONSIZE = 20000;
            TOURNAMENTSIZE = 4;
            MUTATIONRATE = 0.032;
        }
        
        // Generate initial population of chromosomes
        mainInstance.generateInitialChromosomes(POPULATIONSIZE, baseSudoku, mutablePositions);

        Chromosome bestSolution = null;
        List<Integer> fitnessValues = new ArrayList<>();

        int generation = 0; // Track the number of generations
        while (true) {
            // Evaluate the fitness of each chromosome in the population
            mainInstance.evaluatePopulation();

            // Track the best fitness value in the current generation
            bestSolution = mainInstance.getBestChromosome();
            fitnessValues.add(bestSolution.getFitness());

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

            generation++;

            // Plot the fitness graph every 10 generations
            if (generation % 100 == 0) {
                mainInstance.plotFitness(fitnessValues);
            }

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
            // Randomly fill mutable positions
            for (int[] pos : mutablePositions) {
                int row = pos[0];
                int col = pos[1];   
                sudoku[row][col] = random.nextInt(9) + 1;
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
        List<int[]> mutablePositions = chromosome.getMutablePositions();
    
        // Mutate each mutable position with a probability defined by mutationRate
        for (int[] pos : mutablePositions) {
            if (random.nextDouble() < mutationRate) {
                int row = pos[0];
                int col = pos[1];
                int newValue = random.nextInt(9) + 1; // Assign a new value between 1 and 9
                sudoku[row][col] = newValue;
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
            fitness = countRowViolations() + countColumnViolations() + countSubgridViolations();
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
                for (int j : row) {
                    System.out.print(j + " ");
                }
                System.out.println();
            }
            // If requested, print mutable positions
            if (printMutPos) {
                System.out.println("Mutable Positions:");
                for (int[] pos : mutablePositions) {
                    System.out.print("(" + pos[0] + ", " + pos[1] + ") ");
                }
                System.out.println();
            }
        }
    }

    // Method to plot the fitness values over generations
    public void plotFitness(List<Integer> fitnessValues) {
        XYChart chart = new XYChart(800, 600);
        chart.setTitle("Fitness over Generations");
        chart.setXAxisTitle("Generation");
        chart.setYAxisTitle("Fitness");
        XYSeries series = chart.addSeries("Fitness", null, fitnessValues);
        series.setXYSeriesRenderStyle(XYSeriesRenderStyle.Line);
        new SwingWrapper<>(chart).displayChart();
    }
}   
