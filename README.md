SemanticSearchJava
==================

This is a tutorial on how to start working with the semantic search via the console. 
You can compile the code into a jar called SemanticSearch.jar. 
All the prerequisite java libraries are bundled along with this code.

# Quick start
java -jar SemanticSearch.jar [OutputDir] [MatrixFilePath] [FileMappingNodeToCategories] [PathsToAnalyze] [NumberOfWorkers]

### The locations where each file can be found
####FileMappingNodeToCategories 
https://www.dropbox.com/s/dkye70q96uze400/NodeCategories.txt
####MatrixFilePath 
https://www.dropbox.com/s/clvbv27g0a09o3l/0.7-0.0.txt
#### PathsToAnalyze
TRAF2,IRF4,ISG15 <br>
TRAF2,TNFRSF1A,STAT1,ISG15 <br>
TRAF2,HSP90AA1,STAT1,ISG15 <br>
etc..<br>

#### Number of workers
The number of parallel threads to use during the computation. 


#### Extra notes
Provide a full path for each parameter, not a relative path.


### Output

The output is written into the output dir <br>
1) For each pair of nodes in the PathsToAnalyze file a new subdir will be created.<br>
2) Each thread creates a file called Labelings_ThreadNumber.txt <br>
For example if we search for paths linking ANG and MMP2, using four threads <br>
the output dir will contain a subdir called ANG-Protein-MMP2-Gene with four files:<br>
1) Labelings_1.txt <br>
2) Labelings_2.txt <br>
3) Labelings_3.txt <br>

###The file format is:<br>
[Path]:[GO Annotations]:[log score]<br>
####For example:<br>
ANG,PTEN,ESR1,KAT5,TP53,MMP2:0001938,0000079,0045429,0016573,0033077,0060325:-0.011860875523755693<br>









