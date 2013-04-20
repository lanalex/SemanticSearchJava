import java.awt.List;


public class SemanticSearchJavaServer 
{
	
	public Integer DoSearch(String matrix, String categories, Object[] commands)
	{
		System.out.println("Executing");
		SemanticSearch searcher = new SemanticSearch();
		searcher.DoSearch(matrix, categories, commands, 6);
		return 0;
	}

	 public static void main (String [] args) 
	 {
			SimpleXmlRpcServer server = null;
			try 
			{
				server = new SimpleXmlRpcServer(7777);						
				server.addHandler("SemanticSearch", new SemanticSearchJavaServer());					
				server.serve_forever();			

				while (true)
				{
					System.out.println("");
					try 
					{
						Thread.sleep(60 * 60 * 60);
					} 
					catch (InterruptedException e) 
					{				
						e.printStackTrace();
					}
				}
			}
			catch (Exception e) 
			{

				e.printStackTrace();
			}
	 }
}
