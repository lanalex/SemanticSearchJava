
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class Consts
{
	public static final int MAX_ANNOTATIONS = 2000;
	public static final int MAX_LABELINGS = 50000;
	public static final int MAX_LABELINGLENGTH = 12;
	public static final int MAX_SKIPS = 1;
}


class Transition
{
	public List<Transition> Parents = new ArrayList<Transition>();
	public List<Transition> GrandParents = new ArrayList<Transition>();
	public int RemainingSkips = 1;
	public int Layer = 0;

	public double Optimal = 1000;
	
	public Short[] Label;
	
	public Transition(Short[] label, int layer)
	{
		this.Label = label;
		this.Layer = layer;
	}
				
}

class Consumer implements Runnable 
{
   private final BlockingQueue<Object[]> queue;
   
   private final SemanticSearch ParentRef;
   private final String OutputDir;
   public AtomicInteger KeepWorking;
   
   Consumer(BlockingQueue<Object[]> workQueue, String outputDir, SemanticSearch parentRef) 
   { 
	   this.queue = workQueue;
	   this.OutputDir = outputDir;
	   this.ParentRef = parentRef;
	   this.KeepWorking = new AtomicInteger(0);
	   
   }
   
   private void createDirectoryIfNeeded(String directoryName)
   {
	  
     File theDir = new File(directoryName);

     // if the directory does not exist, create it
     if (!theDir.exists())
     {
       System.out.println("creating directory: " + directoryName);
       theDir.mkdir();
     }
   }   
   

   public void PrintLabelings(Transition t, BufferedWriter writer, double score, List<Short[]> topLabelings)
   {	   
	   topLabelings.add(t.Label);
	   	   
	   for(Transition parent : t.Parents)
	   {
		   if (parent.Label[0] != -1)
			   PrintLabelings(parent, writer, score, new ArrayList<Short[]>(topLabelings));
		   else
		   {		
			   for(Transition grandParent : t.GrandParents)
			   {		   
				   List<Short[]> cloned = new ArrayList<Short[]>(topLabelings);
				   cloned.add(parent.Label);
				   PrintLabelings(grandParent, writer, score, cloned);		   			   
			   }			   
		   }
	   }
	   
	   if (t.Parents.size() == 0 && t.Layer == 0 && topLabelings.size() > 0)
	   {
		   String outputString = ":";
		   					   					  					  	   
		   
		   for(int index = topLabelings.size() - 1; index >= 0; index--)		  
		   {
			   Short[] label = topLabelings.get(index);
			  						  
			  outputString += this.ParentRef.NewIDToTerm.get(label[0]);
			 		 		  
			  if (index > 0)
				  outputString += ",";
			  
					  
		   }
		   
		   outputString += ":" + String.valueOf(score);
		   
		   try {
			writer.write(outputString);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   }
	   
   }
   
   public void run() 
   {	     
	    try
	    {
	   
		    BufferedWriter writer = null;
		    int writtenRows = 0;	    
		    double score;
		    double minScore;
		    short[] minLabeling = null ;
		    List<List<Short[]>> topLabelings= null;
		    String outputDir = "";
		    
		    short[][] labelings = new short[Consts.MAX_LABELINGS][Consts.MAX_LABELINGLENGTH];
		    
		    Map<String, BufferedWriter> outputPathToWriter = new HashMap<String, BufferedWriter>();

	      	while(this.KeepWorking.get() == 0) 
	      	{ 	    	    
	      		writtenRows += 1;
				String[] path = new String[3];
				
		    	try 
				{
		    		
					Object[] command = this.queue.take();											
					path = (String[])command[0];
					outputDir = (String)command[1];
					this.createDirectoryIfNeeded(outputDir);
					
					outputDir = outputDir + "/Labelings_" + Thread.currentThread().getId() + ".txt";
		    	   			      	
			      	
			      	if (!outputPathToWriter.containsKey(outputDir))
			      	{
			      		for (String key : outputPathToWriter.keySet())
			      		{
			      			outputPathToWriter.get(key).flush();
			      			outputPathToWriter.get(key).close();
			      		}
			      		
					    outputPathToWriter = new HashMap<String, BufferedWriter>();
			      		
			      		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputDir)));
			      		outputPathToWriter.put(outputDir, writer);
			      	}
			      	
			      	writer = outputPathToWriter.get(outputDir);
			      									    		    		    			    
			    	
			       int labelingLength = path.length * 2;
			    		   
			       Transition t = this.ParentRef.GetOptimalPathLabelingScore(path);
			       	   				  
				   try 
				   {	
					   Boolean written = false;
					   
					   String outputString = "";
					   
					   if (t.Parents.size() > 0 && t.Optimal < 0)				   
					   {			   							   
						   
						   // Prints the path itself
						   for(int i = 0; i < path.length; i++)
						   {
							   outputString += path[i];
							   
							   if (i < path.length - 1)
							   {
								   outputString += ",";
							   }
							   							   
						   }
						   
						   writer.write(outputString);
						   this.PrintLabelings(t, writer, t.Optimal, new ArrayList<Short[]>());
						   writer.newLine();
						   

						  }
					   	
					   }
					   catch (IOException e) 
					   {
						e.printStackTrace();
					   }
					    	
	      		}
		    	catch(InterruptedException e)
		    	{
		    		System.out.println("Thread stopped");
		    	}
			  }
      		
	      	System.out.println("Finished working, closing open writers");
	      	for (String key : outputPathToWriter.keySet())
      		{
	      		writer = outputPathToWriter.get(key);
	      		writer.flush();
      			writer.close();
      		}
	      	System.out.println("Done");
      			      	
	    }
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
   }	     	  
 }

public class SemanticSearch 
{	
	 
	public double[][] ScoreMatrix;
	private Map<String, String> PairsInMatrix;	
	public Map<String,Short> TermToNewID;	
	private Map<String, Double> CachedTransitions = new ConcurrentHashMap<String, Double>(); 

	 
	Map<String, List<Short[]>> NodeToAnnotations;
	List<String[]> AllPaths;
	public  Map<Short, String> NewIDToTerm;
	private Map<String, Integer> NodeToRawAnnotationCount;
	public short MaxTermID;
	
	
	
	void DoSearch(String matrix, String categories, Object[] commands, int numberOfThreads)
	{
		BlockingQueue<Object[]> workQueue = new LinkedBlockingDeque<Object[]>();
		
		SemanticSearch search = new SemanticSearch();				
		search.ReadMatrixFile(matrix);
		search.ReadEachNodeTerms(categories);		
		int THREADS = numberOfThreads;
		List<Thread> threads = new ArrayList<Thread>();
		List<Consumer> consumers = new ArrayList<Consumer>();
	    
		int counter = 0;	
		
		for (int i = 0; i < THREADS; i++)
		{
		
		    Consumer c1 = new Consumer(workQueue, categories, search);	    
		    Thread t1 = new Thread(c1);
		    t1.start();
		    
		    threads.add(t1);
		    consumers.add(c1);		    		   
		}		
		
		for(int c = 0; c < commands.length; c++)
		{
			Object[] command = (Object[])commands[c];
			String paths = (String)command[0];
			String outputDir = (String)command[1];
							  
			//System.out.println(String.format("Doing: %s", paths));
	
			BufferedReader reader = null;	
			
			try 
			{		    			
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(paths)));				
			    for (String line; (line = reader.readLine()) != null ;) 
			    {
				    	counter += 1;
				    	if (true)
				    	{			    		
							String[] path = line.trim().split(",");
							
							if (path.length <= 10)
							{								
								Object[] queueCommand = new Object[2];
								queueCommand[0] = path;
								queueCommand[1] = outputDir;
								
								workQueue.put(queueCommand);
															
								Boolean slept = false;
								
								while (workQueue.size() >= 75000)
								{
									while (workQueue.size() > 25000)
									{
										Thread.sleep(100);
										slept = true;										
									}
								}
								
								if (slept)
								{
									System.out.println(String.format("Executed: %s, In queue: %s", counter, workQueue.size()));
									slept = false;
								}
							
							}
				    }	
			    }
			} 
			catch(FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e) 
			{
				e.printStackTrace();	
			} catch (InterruptedException e) {
				System.out.println("Thread closed");
			} 
	
			finally 
			{		    
			    try {
					reader.close();
				} catch (IOException e) { 
					e.printStackTrace();
				}
			}
		}
			
		
		System.out.println(counter);
		
		while (workQueue.size() > 0)
		{
			try {
				System.out.println(workQueue.size());
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
		System.out.println("Done");
		
		for(int i = 0; i < consumers.size(); i++)
		{
			Consumer c = consumers.get(i);			
			Thread t = threads.get(i);
			
			c.KeepWorking.incrementAndGet();			
			t.interrupt();
		}		
	}

	
	private void ReadPathsFile(String pathsFile)
	{
		List<String[]> allPaths = new ArrayList<String[]>();
		
		List<String> allLines = ReadFileHelper.Read(pathsFile);		
		
		for(String x : allLines)
		{
			String[] tokens = x.split(",");
			
			allPaths.add(tokens);
			
		}
		
		this.AllPaths = allPaths;
	}
	
	public double GetPairScore(Short[] currentTerm, Short[] nextTerm)
	{				
		double result = 9999999;
				
		double score = 0.0;
		double skipPenalty = 0.0001;
		
		if (currentTerm[0] == -1)
		{
			result = 1000;
		}
		else if (nextTerm[0] == -1)
		{
			result = Math.log10(1 - skipPenalty);		
		}
		else
		{
			score = 0.0;
			score = this.GetPairScoreFromMatrix(currentTerm, nextTerm);
			
			if (score > 0)
			{							
				if (currentTerm[1].equals(0) || nextTerm[1].equals(0))
				{
					score = score * 0.5;
				}
			}
			else
			{
				score = -100000.0;					
			}
			
			if (score >= 1.0)
			{
				score = 0.999;
			}
			
			result = Math.log10(1 - score);			
		}
					
		return result;
	}
	
	void ReadMatrixFile(String matrixFilePath)
	{
		
		Map<String, Short> termToNewID = new HashMap<String, Short>();
		Map<Short, String> newIDToTerm = new HashMap<Short, String>();
		
		short a = -1;
		newIDToTerm.put(a, "Skip");
		
		short lastId = 1;
		
		List<String> allLines = ReadFileHelper.Read(matrixFilePath);			
		
		
		double[][] matrix = new double[Consts.MAX_ANNOTATIONS][Consts.MAX_ANNOTATIONS];		
		
		
		for(String x : allLines)
		{
			String[] tokens = x.split("\t");			
			double score = Double.parseDouble(tokens[2]);
			
			// No key, meaning create new id
			if (!termToNewID.containsKey(tokens[0]))
			{
				termToNewID.put(tokens[0], lastId);
				newIDToTerm.put(lastId, tokens[0]);
				lastId+=1;
			}
			
			// No key, meaning create new id
			if (!termToNewID.containsKey(tokens[1]))
			{
				termToNewID.put(tokens[1], lastId);
				newIDToTerm.put(lastId, tokens[1]);
				lastId+=1;
			}
			
			int term1NewId = termToNewID.get(tokens[0]);
			int term2NewId = termToNewID.get(tokens[1]);
			
			
			matrix[term1NewId][term2NewId] = score;									
			
		}
		
		this.MaxTermID = lastId;
		this.ScoreMatrix = matrix;
		this.TermToNewID = termToNewID;
		this.NewIDToTerm = newIDToTerm;
	}
	
	void ReadEachNodeTerms(String nodeToTermsFile)
	{
		List<String> allLines = ReadFileHelper.Read(nodeToTermsFile);
		Map<String, List<Short[]>> nodeToAnnotations = new HashMap<String, List<Short[]>>();
		Map<String,Integer> nodeToRawAnnotationCount = new HashMap<String, Integer>();					
		
		List<Short[]> categoriesList;
		
		for (String line : allLines)
		{			
			
			String[] tokens = line.trim().split(",");
			tokens[0] = tokens[0].toUpperCase();
		
			if (!nodeToAnnotations.containsKey(tokens[0]))
			{
				nodeToAnnotations.put(tokens[0], new ArrayList<Short[]>());
				categoriesList = nodeToAnnotations.get(tokens[0]);
				Short[] defaultAnn = new Short[2];
				defaultAnn[0] = -1;
				defaultAnn[1] = 0;
				categoriesList.add(defaultAnn);
				nodeToRawAnnotationCount.put(tokens[0], 0);
				
			}
			
			categoriesList = nodeToAnnotations.get(tokens[0]);
			Short[] data = new Short[2];
			
			nodeToRawAnnotationCount.put(tokens[0], nodeToRawAnnotationCount.get(tokens[0]) + 1);
			
			if (this.TermToNewID.containsKey(tokens[1]))
			{
				data[0] = this.TermToNewID.get(tokens[1]);
				
				if (tokens[2].equals("IEA"))
				{
					data[1] = 0;
				}
				else
				{
					data[1] = 1;
				}
					
				boolean found = false;
				for(Short[] label : categoriesList)
				{
					if (label[0] == data[0] && label[1] == data[1])
						found = true;
				}
				
				if (!found)
					categoriesList.add(data);
			}
		}
		
		this.NodeToAnnotations = nodeToAnnotations;
		this.NodeToRawAnnotationCount = nodeToRawAnnotationCount;
	}
		
	public double CalcLabelingScore(short[] labels)
	{
		double totalScore = 0.0;		
		Boolean doOffset = false;
		
		int length = labels.length;
		
		Short[] currentTerm = new Short[2];
		Short[] nextTerm = new Short[2];
		double currentPairScore = 0.0;
		double lastPairScore = 0.0;
		
		for(int i = 1; i < length - 2; i+=2)
		{
			doOffset = false;
			currentTerm[0] = labels[i - 1];
			currentTerm[1] = labels[i];
			
			if (currentTerm[0] == -1)
			{
				currentTerm[0] = labels[i - 3];
				currentTerm[1] = labels[i - 2];
								
				doOffset = true;
			}

			nextTerm[0] = labels[i + 1];
			nextTerm[1] = labels[i + 2];
								
					
			currentPairScore = this.GetPairScore(currentTerm, nextTerm);
			
			/*
			if (lastPairScore < 0 && currentPairScore * 3 < lastPairScore)
			{
				lastPairScore = currentPairScore;
				currentPairScore = Math.log10(0.0001);
			}
			else
			{
				lastPairScore = currentPairScore;
			}*/
			
			totalScore += currentPairScore;
			
			if (doOffset)
			{
				totalScore += currentPairScore * 0.5;
			}
				
			
		}
		
		
		return totalScore;
		
	}	
	
	public double GetPairScoreFromMatrix(Short[] currentTerm, Short[] nextTerm)
	{
		double score = 0.0;
		int currentTermID = currentTerm[0];
		int nextTermID = nextTerm[0];
		score = this.ScoreMatrix[currentTermID][nextTermID];
		
		return score;
			
	}
	
	public List<Short[]> GetNodeAnnotations(String node)
	{
		List<Short[]> annotations;
		if (this.NodeToAnnotations.containsKey(node))
		{
			annotations = this.NodeToAnnotations.get(node);
		}
		else
		{
			annotations = new ArrayList<Short[]>();
			Short[] defaultAnn = new Short[2];
			defaultAnn[0] = -1;
			defaultAnn[1] = 0;
			annotations.add(defaultAnn);
				
		}
		
		return annotations;		
	}
	
	
	public Transition GetOptimalPathLabelingScore(String[] path)
	{
		List<List<Short[]>> layersGraph = new ArrayList<List<Short[]>>();			
				

		
		int maxAnnotationsCount = -1;
		for(int i = 0; i < path.length; i++)
		{
			List<Short[]> annotations = this.GetNodeAnnotations(path[i]);
			
			if (annotations.size() >= maxAnnotationsCount)
				maxAnnotationsCount = annotations.size();
			
			layersGraph.add(annotations);
		}
		
		Transition[][] resultMatrix = new Transition[layersGraph.size()][maxAnnotationsCount];
		
		int annotationIndex = 0;
		for(Short[] annotation : layersGraph.get(0))
		{
			resultMatrix[0][annotationIndex] = new Transition(annotation, 0);
			resultMatrix[0][annotationIndex].Optimal = 0;
			
			annotationIndex++;
		}
		
		for(int layerIndex = 1; layerIndex < layersGraph.size(); layerIndex++)
		{			
			
			List<Short[]> currentLayer = layersGraph.get(layerIndex);
			List<Short[]> previousLayer = layersGraph.get(layerIndex - 1);
									
		
			// Position in current layer column
			int lcIndex = 0;			
			
			for(Short[] currentAnnotation : currentLayer)
			{										
				Transition transition = new Transition(currentAnnotation, layerIndex);
				
				// Position in previous layer column
				int lpIndex = 0;
				
				for(Short[] previousAnnotation : previousLayer)
				{															
					Transition previousTransition = resultMatrix[layerIndex - 1][lpIndex];
					
					// We allow only ONE consecutive Skip and a total one Consts.MAX_SKIPS
					if (!((currentAnnotation[0] == -1 && previousTransition.Label[0] == -1) || (previousTransition.RemainingSkips == 0 && currentAnnotation[0] == -1)))					
					{
											
						List<Transition> transitionsToCheck = null;
						
						if (previousTransition.Label[0] == -1)
							transitionsToCheck = previousTransition.Parents;
						else
						{
							transitionsToCheck = new ArrayList<Transition>();
							transitionsToCheck.add(previousTransition);
						}
						
						for (Transition transitionToCheck : transitionsToCheck)
						{
							double transitionScore = previousTransition.Optimal + this.GetPairScore(transitionToCheck.Label, currentAnnotation);
							
							if (transitionScore <= transition.Optimal)
							{
								transition.Optimal = transitionScore;
							}
								
						}
					}
					
					// Position in previous layer column
					lpIndex++;
				}
				

				lpIndex = 0;
				
				for(Short[] previousAnnotation : previousLayer)
				{
					Transition previousTransition = resultMatrix[layerIndex - 1][lpIndex];

					// We allow only ONE consecutive Skip and a total one Consts.MAX_SKIPS
					// NOTICE!! the NOT (!) at the beginning of the if
					if (!( (currentAnnotation[0] == -1 && previousTransition.Label[0] == -1) || (previousTransition.RemainingSkips == 0 && currentAnnotation[0] == -1)))
					{															
						List<Transition> transitionsToCheck = null;
						
						if (previousTransition.Label[0] == -1)
							transitionsToCheck = previousTransition.Parents;
						else
						{
							transitionsToCheck = new ArrayList<Transition>();
							transitionsToCheck.add(previousTransition);
						}
						
						for (Transition transitionToCheck : transitionsToCheck)
						{					
							double transitionScore = previousTransition.Optimal + this.GetPairScore(transitionToCheck.Label, currentAnnotation);					
							
							if (transitionScore == transition.Optimal)
							{
								transition.Parents.add(previousTransition);
								
								if (previousTransition.Label[0] == -1)
									transition.GrandParents.add(transitionToCheck);
							}
						}
					}
							
					// Position in previous layer column
					lpIndex++;
				}								
					
				resultMatrix[layerIndex][lcIndex] = transition;
						
				if (currentAnnotation[0] == -1)
					transition.RemainingSkips--;
				
				// Position in current layer column
				lcIndex += 1;
			}
		}
						
		int lastLayerIndex = layersGraph.size() - 1;
		Transition bestTransiton = new Transition(null, lastLayerIndex);
		
		for(int transitionIndex = 0; transitionIndex < maxAnnotationsCount; transitionIndex++)
		{
			Transition currentTransition = resultMatrix[lastLayerIndex][transitionIndex];
			
			if (currentTransition != null && currentTransition.Optimal <= bestTransiton.Optimal)
			{
				bestTransiton = currentTransition;
			}
			
		}
		
		return bestTransiton;
						
	}
	
	public int GetAllLabelingsForPath(String[] path, short[] labels, int pathIndex, int skips, short[][] labelings, int filledValues)
	{
		
		if (filledValues >= Consts.MAX_LABELINGS)
		{
			return filledValues;
		}
		
		if (pathIndex == path.length)
		{				
			labelings[filledValues] = labels;			
			return filledValues + 1;
		}
				
				
		String currentNode = path[pathIndex];
		
		List<Short[]> annotations;
		if (this.NodeToAnnotations.containsKey(currentNode))
		{
			annotations = this.NodeToAnnotations.get(currentNode);
		}
		else
		{
			annotations = new ArrayList<Short[]>();
			Short[] defaultAnn = new Short[2];
			defaultAnn[0] = -1;
			defaultAnn[1] = 0;
			annotations.add(defaultAnn);
				
		}
		
		int termID;
		
		int currentSkips = skips;
						
		short[] newList = null;
		
		int annotationsCount = 0;
		
		if (this.NodeToRawAnnotationCount.get(currentNode) != null)
		{
			annotationsCount = this.NodeToRawAnnotationCount.get(currentNode);
		}
		

		short[][] handled = new short[this.MaxTermID][2];
			
		for (Short[] annotation : annotations)
		{	
			if (annotation[0] == -1 || handled[annotation[0]][annotation[1]] == 0)
			{
				termID = annotation[0];
				
				if (termID > 0)
					handled[termID][annotation[1]] = 1;
				
				int lastTermID = -2;
				Short[] lastTerm = new Short[2];
				double score = 0.0;
						
				
				if (pathIndex > 0)
				{
					lastTerm[0] = labels[(pathIndex - 1) * 2]; 
					lastTerm[1] = labels[(pathIndex - 1) * 2 + 1];
					lastTermID = lastTerm[0];
				}
						
	
				if (lastTermID == -1)
				{
					lastTerm[0] = labels[(pathIndex - 2) * 2]; 
					lastTerm[1] = labels[(pathIndex - 2) * 2 + 1];
					lastTermID = lastTerm[0];
					
				}
							
				
				if (lastTermID != -2 && lastTerm != null &&  termID != -1)
				{
					score = this.GetPairScoreFromMatrix(lastTerm, annotation);
				}
				
				if ((pathIndex == 0 && termID != -1) || (score > 0.0) || (termID == -1 && currentSkips > 0 && pathIndex > 0 && annotationsCount < 3) || (lastTermID == -1 && currentSkips >= 0))
				{
					
					newList = new short[labels.length];
					
					for(int ind = 0; ind < labels.length - 1; ind+=2)					
					{
						newList[ind] = labels[ind];
						newList[ind + 1] = labels[ind + 1];
					}
					
					newList[pathIndex * 2] = annotation[0];
					newList[pathIndex * 2 + 1] = annotation[1];
					
					if (termID == -1)				
						currentSkips--;				
									
					filledValues = GetAllLabelingsForPath(path, newList, pathIndex + 1, currentSkips, labelings, filledValues);
				}
			}
				
			
		}
		
		return filledValues;
			
	}
		
	
	
	/**
	 * @param args
	 */
	
	
	public static void main(String[] args) 
	{			
		
		
		
		String outputDir = args[0];
		String matrix = args[1];
		String categories = args[2];
		String paths = args[3];		
		
		System.out.println(outputDir);
		System.out.println(matrix);
		System.out.println(categories);
			
		System.out.println(paths);

		Object[] commands = new Object[1];
		String[] command = new String[2];
				
		command[0] = paths;
		command[1] = outputDir;
		
		commands[0] = command;
		
		SemanticSearch search = new SemanticSearch();
		search.DoSearch(matrix, categories, commands, 1);
	}
	


}
