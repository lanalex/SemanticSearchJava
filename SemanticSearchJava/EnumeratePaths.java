import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class EnumeratePaths {

	public short[][] allPaths = new short[1000000][];
	
	public short[][] ReadSPS()
	{
		short[][] sps = new short[30000][];
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\ASUS\\Dropbox\\Research\\Semantic Search\\Code\\SVN\\SemanticSearch\\ReviewerAnalysis\\SPs.txt")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	
	    try {
			for (String line; (line = reader.readLine()) != null ;) 
			{		
				String[] tokens = line.split(",");
				int source = Integer.parseInt(tokens[0]);
				int target = Integer.parseInt(tokens[1]);
				int length = Integer.parseInt(tokens[2]);
				
				if (sps[source] == null)
					sps[source] = new short[target + 10];
				
				else if(sps[source].length <= target + 1)
				{
					short[] newOne = new short[target + 10];
					
					for(int i = 0; i < sps[source].length; i++)
					{
						newOne[i] = sps[source][i];
					}
					
					sps[source] = newOne;
					
				}
				
				sps[source][target] = (short)length;
			}
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sps;
	}
	public short[][] ReadGraph()
	{
		List<String> allLines = ReadFileHelper.Read("C:\\Users\\ASUS\\Dropbox\\Research\\Semantic Search\\Code\\SVN\\SemanticSearch\\ReviewerAnalysis\\AdjMatrixGraph.txt");
		short[][] adjMatrix = new short[45000][];
				
		for(String line : allLines)
		{
			String[] tokens = line.split(":");
			int source = Integer.parseInt(tokens[0]);
			String[] adjacent = tokens[1].split(",");
			adjMatrix[source] = new short[adjacent.length];
						
			
			for(int i = 0; i < adjacent.length; i++)
			{
				adjMatrix[source][i] = Short.parseShort(adjacent[i]);				
			}
					
		}	
		
		return adjMatrix;
	}
	
	public List<Short[]> ReadPairs()
	{
		List<String> allLines = ReadFileHelper.Read("C:\\Users\\ASUS\\Dropbox\\Research\\Semantic Search\\Code\\SVN\\SemanticSearch\\ReviewerAnalysis\\SourceTargetsReduced.txt");
		List<Short[]> allPairs = new ArrayList<Short[]>();
		
		for(String line : allLines)
		{
			String[] tokens = line.split(",");
			
			Short[] pair = new Short[2];
			pair[0] = Short.parseShort(tokens[0]);
			pair[1] = Short.parseShort(tokens[1]);
			
			allPairs.add(pair);
		}
		
		return allPairs;
	}
	
	
	public int Enumerate(short startNode, short endNode, short[] path, short[][] sps, short[][] graph, short length, int depth, int currentIndex)
	{
		if (depth + 1 > length)
			return currentIndex;
		
		if (path[depth] == -1)
			path[depth] = startNode;
		
		if (depth + 1 < length && startNode == endNode)
		{			
			return currentIndex;
		}
		
		if (depth + 1 == length && startNode == endNode)
		{			
			
			this.allPaths[currentIndex] = path;
			currentIndex += 1;
			
			if (currentIndex % 1000 == 0)
				System.out.println(currentIndex);
			
			return currentIndex;
		}
		
			
		
		short[] neighbours = graph[startNode];
		
		if (neighbours != null)
		{
			boolean found = false;
			for(int i = 0; i < neighbours.length; i++)
			{
				short currentNeighbour = neighbours[i];
				found = false;
			
				
				if (sps[currentNeighbour] != null && sps[currentNeighbour].length > endNode && sps[currentNeighbour][endNode] + depth <= length)
				{
					for(int j = 0; j < path.length && !found; j++)			
						found = (path[j] == currentNeighbour);
					
					if (!found)
						currentIndex = this.Enumerate(currentNeighbour, endNode, (short[])path.clone(), sps, graph, length, depth + 1, currentIndex);
				}
					
			}
		}
		
		return currentIndex;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		EnumeratePaths p = new EnumeratePaths();
		short[][] graph = p.ReadGraph();
		short[][] sps = p.ReadSPS();
	

		List<Short[]> pairs = p.ReadPairs();
		
		System.out.println(pairs.size());
		int totalPaths = 0;
		
	    long startTime = System.currentTimeMillis();

		int pairsFinished = 0;
		for(Short[] pair : pairs)
		{			
			short source = pair[0];
			short target = pair[1];
			
			if (sps[source] != null && sps[source][target] > 0 && sps[source][target] <= 8)
			{
				totalPaths += p.Enumerate(source, target, new short[8], sps, graph, (short)6, 0, 0);									
			}			
			
			pairsFinished++;
			
			System.out.println(pairsFinished);
			if (pairsFinished == 1000)
			{
				break;
			}
			
		}
		
		long end = System.currentTimeMillis();
		
		float total = (end - startTime) / 1000 / 1000;
		System.out.println(total);
		
		System.out.println(totalPaths);
		

	}

}
