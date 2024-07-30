/************************************************************************************************
 * Purpose:      Parallel program to simulate an Abelian Sandpile cellular automaton
 *               This class runs the simulation using the grid from the ParallelGrid.java class
 * Adapted from: Michelle Kuttel 2024, University of Cape Town
 * Copyright:    Copyright M.M.Kuttel 2024 CSC2002S, UCT
 *
 * @author:      Genevieve Chikwanha
 * @version:     29/07/2024
 ************************************************************************************************/

//package serialAbelianSandpile;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.concurrent.ForkJoinPool;

class ParallelAutomatonSim {
	static final boolean DEBUG = false;           //  For debugging output, off
	
	static long startTime = 0;
	static long endTime = 0;

	static int[][] comboGrid = null;                     // Combination of all the sub-grids

	// Timers - note milliseconds
	private static void tick() {                  //  Start timing
		startTime = System.currentTimeMillis();
	}

	private static void tock() {                  //  End timing
		endTime = System.currentTimeMillis(); 
	}
	
	// Input is via a CSV file
	 public static int [][] readArrayFromCSV(String filePath) {
		 int[][] array = null;
	        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
	            String line = br.readLine();
	            if (line != null) {
				    // Read in the array dimensions, i.e. height and width from input
	                String[] dimensions = line.split(",");
	                int width = Integer.parseInt(dimensions[0]);
	                int height = Integer.parseInt(dimensions[1]);
	               	System.out.printf("Rows: %d, Columns: %d\n", width, height);      //  Do NOT CHANGE  - you must ouput this

	                // Think I'd want splitting to happen here
					array = new int[height][width];                                   //  Creates a new 2D array of specified height and width
	                
					// Read in cell values from .csv file
					int rowIndex = 0;
	                while ((line = br.readLine()) != null && rowIndex < height) {
	                    String[] values = line.split(",");
	                    for (int colIndex = 0; colIndex < width; colIndex++) {
	                        array[rowIndex][colIndex] = Integer.parseInt(values[colIndex]);
	                    }
	                    rowIndex++;
	                }
	            }

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return array;
	    }
	 
    public static void main(String[] args) throws IOException {

    	ParallelGrid simulationGrid;  //  Instantiate the cellular automaton grid
    	  	
    	if (args.length != 2) {       //  Input is the name of the input and output files
    		System.out.println("Incorrect number of command line arguments provided.");   	
    		System.exit(0);
    	}
    	/* Read argument values */
  		String inputFileName = args[0];  //input file name
		String outputFileName = args[1]; // output file name
    
    	// Read from input .csv file and create a ParallelGrid object
    	simulationGrid = new ParallelGrid(readArrayFromCSV(inputFileName));
    	
    	//for debugging - hardcoded re-initialisation options
    	//simulationGrid.set(rows/2,columns/2,rows*columns*2);
    	//simulationGrid.set(rows/2,columns/2,55000);
    	//simulationGrid.setAll(4);
    	//simulationGrid.setAll(8);
   	
    	int counter = 0 ;
    	tick();                                                 //  Start timer

    	if(DEBUG) {
    		System.out.printf("starting config: %d \n", counter);
    		simulationGrid.printGrid();
    	}
		while(simulationGrid.update()) {                         //  Run until no change
	    		if(DEBUG) {
				    simulationGrid.printGrid();
			    }
	    		counter++;
	    	}
   		tock();                                                  //  End timer
   		
        System.out.println("Simulation complete, writing image...");
    	simulationGrid.gridToImage(outputFileName); //write grid as an image - you must do this.
    	//Do NOT CHANGE below!
    	//simulation details - you must keep these lines at the end of the output in the parallel versions      	System.out.printf("\t Rows: %d, Columns: %d\n", simulationGrid.getRows(), simulationGrid.getColumns());
		System.out.printf("Number of steps to stable state: %d \n", counter);
		System.out.printf("Time: %d ms\n",endTime - startTime );			/*  Total computation time */		
    }
}
