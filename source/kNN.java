
import java.io.BufferedReader;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class kNN 
{
	/*
	 * Change these global variables according to your file needs. 
	 * 
	 */
	private static final int WORDS = 71; // number of words +1 in all 3 sets (training, tuning, and test). One is added
										 // because the first entry in training and tuning is the case value
	private static final int PAPERS = 86;
	private static final int PAPERS_TUNE = 20; // the number of papers in the tune set.
	private static final int PAPERS_TEST = 12; //// the number of papers in the test set.
	
	BufferedReader reader = null;
	
	double [] [] words_fr; // The double array containing the frequencies of words in the specified amount of papers. Corresponds to the training file.
	double [] [] tuning_true; // Corresponds to the tuning file
	double [] [] tuning_test; // This is where the tuning is done. 
	
	/*
	 * Tuning is done to adjust the value of k. So for each odd number of k (to avoid tie breaking) starting from 1 to papers/2 (I believe this would be 
	 * a sufficient place to stop) a case is calculated for each paper. That is: we go down the tuning set, paper by paper, and look at each word. We compare this word
	 * to the same word[i] in all the papers in the training set. But we know who actually wrote it (available in the tuning set), so we can calculate how accurate the
	 * prediction was. The idea is that each k value should have varying accuracy, and the goal is to find best k.
	 */
	
	// Global k's
	int k;
	/*
	 * Ultimate_k is derived from tuning and will now be used in the test. 
	 */
	int ultimate_k;
	
	/*
	 * Globals for the test
	 */
	double [] [] test; // Corresponds to the test file.
	double [] [] test_test; // This is where the cases will be calculated.


	double accuracy_ans;
	
	
	/*
	 * Train file will contain 70 function words (so 70 lines). The first digit is a code.
	 * The first entry in each line contains the code number of the author: 1 for Hamilton and 2 for Madison.
	 */
	public kNN (String train, String tune, String test_file)
	{
		words_fr = new double [PAPERS] [WORDS]; 
		/*
		 * Reading the training file and making the array list that will contain all the data by filling in the words_fr double array.
		 */
		try {
		    File file = new File(train);
		    reader = new BufferedReader(new FileReader(file));

		    String line= reader.readLine();
		    int l = 0; // line number
		   
		    while (line != null) {
		    	String[] parts = line.split("\\s+");
		    	
		    	for (int i = 1; i < WORDS ; i ++) // the first entry in a class is a class (1 or 2)
		    	{
		    		words_fr[l][0] = (Double.parseDouble(parts[0])); // the case (i.e. author)
		    		words_fr[l][i] = (Double.parseDouble(parts[i])); 
		    		//System.out.println(l);
		    	}
		    	
		    	l++; // counting the number of data points
		    	line = reader.readLine();
		    }

		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    try {
		        reader.close();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}

		/*
		 * Training. words_fr is updated.
		 * A normalization technique should be done on all the data. 
		 * In this case, because the sample size was very small, the tuning did not seem to increase the accuracy
		 * and so it was not used.
		 * 
		 * Feature scaling was used:
		 * x' = x - min(x) / max(x) - min(x)
		 */
		
		//tuneWord(1);
		//tuneWord(2);
		
		/*
		 * Tuning. Passing in a tuning file. Calculating the case, and comparing it to the given (true) case to see how accurate the k is.
		 * Then accuracy percentage is calculated. Only odd k' s will be used.
		 */
		
		tuning_true = new double [PAPERS_TUNE] [WORDS];
		
		// Filling in tuning_true double array.
		
		try {
		    File file = new File(tune);
		    reader = new BufferedReader(new FileReader(file));

		    String line= reader.readLine();
		    int l = 0; // line number
		   
		    while (line != null) {
		    	String[] parts = line.split("\\s+");
		    	
		    	for (int i = 1; i < WORDS ; i ++) // the first entry in a class is a class (1 or 2)
		    	{
		    		tuning_true[l][0] = (Double.parseDouble(parts[0])); // the case (i.e. author)
		    		tuning_true[l][i] = (Double.parseDouble(parts[i]));   	
		    	}
		    	
		    	l++; 
		    	line = reader.readLine();
		    }

		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    try {
		        reader.close();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
		
		
		ultimate_k = 0;
		
		// We need to keep track which k produced the good accuracy value
		
		tuning_test = new double [PAPERS_TUNE][WORDS]; // the [i][1] = the case (i.e. author)
		
		double max = 0; // keeping track of the best accuracy value
		
		// Main loop for finding the ultimate_k. So k will be tried starting with k = 1 and adding 2 (only using odd k's)
		for (k = 1; k < 10;)
		{
			for (int i = 0; i < PAPERS_TUNE; i++) //in tuning_test
			{
				whatCase(i); // will fill in the tuning_test for all the 20 papers with the predicated value of the author (case) for each paper. 
			}
			
			/* Calculating the accuracy between the current tune_test values and the given cases from tuning_true
			 * for a given k
			 */ 
			
			if (accuracy() > max)
			{
				max = accuracy();
				ultimate_k = k;
			}
			
			k = k + 2;
			
		}
		
		System.out.println("Final accuracy from tuning:  " + max + ".  Ultimate_k found is:" + ultimate_k);
		
		
		/*
		 * Now we are testing the file.
		 */
		testData(test_file);
		
		// Printing the solution. 
		System.out.println("Found the solution for the test case (1 for Hamilton and 2 for Madison).");
		for (int d = 0; d < 12; d++)
			System.out.println("Book: "+ d + ", author: " + test_test[d][0]);
	}
	
	
	/*
	 * A method for the test file.
	 */
	public void testData(String test_file)
	{
		test = new double [PAPERS_TEST][WORDS];
		
		try {
		    File file = new File(test_file);
		    reader = new BufferedReader(new FileReader(file));

		    String line= reader.readLine();
		    int l = 0; // line number
		   
		    while (line != null) {
		    	String[] parts = line.split("\\s+");
		    	
		    	for (int i = 0; i < (WORDS-1) ; i ++) // the first entry in a class is a class (1 or 2) but is unknow here
		    	{
		    		test[l][0] = 0; // the author is unknown and so left blank. The cases will be predicted in the test_test array.
		    		test[l][(i+1)] = (Double.parseDouble(parts[i])); 
		    		//System.out.println(l);
		    	}
		    	
		    	l++; // counting the number of data points
		    	line = reader.readLine();
		    }

		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    try {
		        reader.close();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
		
		// We need to keep track which k produced the good accuracy value
		test_test = new double [PAPERS_TEST][WORDS]; // the [i][1] = the case
		
			for (int i = 0; i < PAPERS_TEST; i++) //in tuning_test
			{
				whatCaseTest(i); // will fill in the test_test for all the 20 papers by comparing the words in each papers from the test to the training set papers
			}
			
	}
	
	/*
	 * Training: calculating the scaling based on "Feature Scaling". 
	 */
	public void tuneWord(int case_num)
	{
		double min = Double.MAX_VALUE;
		double max = 0;
		
		// going through all the papers
		for (int w = 1; w < WORDS; w++)
		{
			for (int p = 0; p < PAPERS; p++)
			{
				if (words_fr[p][0] == case_num)
				{
					if (words_fr[p][w] < min)
						min = words_fr[p][w];
					if (words_fr[p][w] > max)
						max = words_fr[p][w];
				}
			}
			
			for (int p = 0; p < PAPERS; p++)
			{	
				if (words_fr[p][w] == case_num)
					words_fr[p][w] = ((words_fr[p][w] - min) / (max - min));
			}
			
		}
	}
	
	/*
	 * Tuning 
	 */
	
	// trying to find all the cases for a given paper based on k
	public void whatCase(int paper_num) // paper number of the tuning test for k 
	{
		double [] k_temp = new double [k];
		ArrayList <Double> array_temp_wordI = new ArrayList <Double> (); // will fill with all the words for current paper from the training set to compare to the word in a tuning set
		double min;
		
		// i is the word that we are trying to assign a case to in k_temp array
		for (int i = 1; i < WORDS; i++) // for each word in tuning_true (will also use for tuning_test)
		{
			// we go through all the words frequencies in the papers for this word
			for (int j = 0; j < PAPERS; j++)
			{
				array_temp_wordI.add(words_fr[j][i]); // we have added all the words to a temp list
			}
			
			// Now we want to rate the word (find the case)
			// First we fill in the k_temp
			min = Double.MAX_VALUE;
			
			
			//We go through all of the temp word array and find the min distance - add to to k_temp k amount of times.
			for (int m = 0; m < k; m++)
			{
				for (int f = 0; f < PAPERS; f++ )
				{
					if (array_temp_wordI.get(f) != null) 
					{
						//System.out.println(tuning_true.length + ", " + tuning_true[0].length + " with " + paper_num + "; " + i);
						if (distance(array_temp_wordI.get(f), tuning_true[paper_num][i]) < min)
						{
							min = array_temp_wordI.get(f);
							array_temp_wordI.set(f, null);
							//System.out.println("Assigning [" + m + "] for k_temp");
							k_temp[m] = words_fr[f][0];
						}
					}		
				}	
				min = Double.MAX_VALUE;
			}

			tuning_test[paper_num][i] = final_step(paper_num, k_temp); // this will be filled with an average of case for each paper examined in the set tuning.txt
			
			// that we will compare with the tuning_true to determine the accuracy
			array_temp_wordI = new ArrayList <Double>();

		}
	
		tuning_test[paper_num][0]=final_step(paper_num, tuning_test);
	}
	
	
	
	
	
	
	
	
	
	
	// trying to find all the cases for a given paper based on k
		public void whatCaseTest(int paper_num) // paper number of the tuning test for k 
		{
			double [] k_temp = new double [k];
			ArrayList <Double> array_temp_wordI = new ArrayList <Double> (); //will fill with all the words for current paper
			double min;
			
			// i is the word that we are trying to assign a case to in k_temp array
			for (int i = 1; i < WORDS; i++) // for each word in tuning_true (will also use for tuning_test)
			{
				// we go through all the words frequencies in the papers for this word
				for (int j = 0; j < PAPERS; j++)
				{
					array_temp_wordI.add(words_fr[j][i]); // we have added all the words to a temp list
				}
				
				// Now we want to rate the word (find the case)
				// First we fill in the k_temp
				min = Double.MAX_VALUE;

				
				//We go through all of the temp word array and find the min distance - add to to k_temp k amount of times.
				for (int m = 0; m < ultimate_k; m++)
				{
					for (int f = 0; f < PAPERS; f++ )
					{
						if (array_temp_wordI.get(f) != null) 
						{
							//System.out.println(tuning_true.length + ", " + tuning_true[0].length + " with " + paper_num + "; " + i);
							if (distance(array_temp_wordI.get(f), test[paper_num][i]) < min)
							{
								min = array_temp_wordI.get(f);
								array_temp_wordI.set(f, null);
								//System.out.println("Assigning [" + m + "] for k_temp");
								k_temp[m] = words_fr[f][0];
							}
						}		
					}	
					min = Double.MAX_VALUE;
				}
				
				test_test[paper_num][i] = final_step(paper_num, k_temp); // this will be filled with the most common case for each paper in the set test_test
				// that we will compare with the tuning_true to determine the accuracy
				array_temp_wordI = new ArrayList <Double>();
				

			}
	
			test_test[paper_num][0]=final_step(paper_num, test_test);
		}
		
	/*
	 * Calculating the most common value in a riven row from some array
	 */
	public double final_step (int row_number, double[][] data)
	{
		int [] sum1 = new int [2]; // counts of case1
		int [] sum2 = new int [2]; // counts of case2
		
		sum1 [0] = 1;
		sum1 [1] = 0;
		
		sum2 [0] = 2;
		sum2 [1] = 0;
		
		for (int h=1; h < WORDS ; h++)
		{
			if (data[row_number][h] == 1)
				sum1[1]++; 
			else sum2[1]++;
		}
        
		double common_case;
		
		if(sum1[1] > sum2[1])
			common_case = sum1[0];
		else common_case = sum2[0];
		
		return common_case;				
	}
	
	/*
	 * For k_test (single array)
	 */
	
	public double final_step (int row_number, double[] data)
	{
		int [] sum1 = new int [2]; // counts of case1
		int [] sum2 = new int [2]; // counts of case2
		
		sum1 [0] = 1;
		sum1 [1] = 0;
		
		sum2 [0] = 2;
		sum2 [1] = 0;
		
		for (int h=0; h < data.length ; h++)
		{
			if (data[h] == 1)
				sum1[1]++; 
			else sum2[1]++;
		}
		double common_case;
		
		if(sum1[1] > sum2[1])
			common_case = sum1[0];
		else common_case = sum2[0];
		
		return common_case;				
	}
	/*
	 * Comparing the tuning_test with tuning_true
	 */
	public double accuracy()
	{
		double result = 0;
		
		for (int i = 0; i < PAPERS_TUNE; i++)
		{
			if (Math.abs(tuning_test[i][0] - tuning_true[i][0]) < 0.0001)
				result++;
				
		}
		accuracy_ans = result/PAPERS_TUNE;
		return accuracy_ans;
	}
	
	/*
	 * Calculates the distance between 2 given points. 
	 */
	public double distance (Double p1, Double p2)
	{
		double distance_val = Math.sqrt(Math.pow(p2 - p1, 2));
		return distance_val;
	}

}
