package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import data.ChangedFile;
import input.CSVReader;
import input.FileFinder;

public class Program {

	private static String repoPath = "/home/hnes/Masterarbeit/Repositories/";
	private static String smellPath = "/home/hnes/Masterarbeit/SmellConfigs/";
	private static String cppstatsPath = "/home/hnes/Masterarbeit/Tools/cppstats/";
	private static String resultsPath = "/home/hnes/Masterarbeit/Results/";
	private static String tempPath = "/home/hnes/Masterarbeit/Temp/";
	private static String detectionPath = "/home/hnes/Masterarbeit/Repositories/openvpn/revisionsFull.csv";
	private static String project = "openvpn";
	
	public static String modeStr = "";
	public static String smellModeStr = "";
	public static String configStr = "";
	public static String smellModeFile = "";
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args){
		analyzeInput(args);
		System.out.println("Los gehts!");
		
		CSVReader csvreader = new CSVReader(detectionPath);
		csvreader.processFile();
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		
		Date prevDate = csvreader.bugHashes.firstKey();
		for(Date key : csvreader.bugHashes.keySet())
	    {
		  String curDateForm = formatter.format(key);
		  Date curDate = key;
	      System.out.print("Key: " + curDateForm + " - ");
	      System.out.print("Value: " + csvreader.bugHashes.get(key) + "\n");
	      
	      if(modeStr.equals("source")){
	    	  // GIT CHECKOUT
	    	  gitCheckout(csvreader.bugHashes.get(key));
	      
	    	  // Anzahl der .c Dateien checken
	    	  String pathFind = repoPath + project + "/source/";
	    	  System.out.println( "Suche im Pfad: " + pathFind );
	    	  List<File> filesFind = FileFinder.find( pathFind, "(.*\\.c$)" );
	      
	    	  System.out.printf( "Fand %d Datei%s.%n",
                  filesFind.size(), filesFind.size() == 1 ? "" : "en" );
	    	  int filesCount = filesFind.size();
	       	  
	    	  // In CSV Datei schreiben
	    	  File csvOut = new File(resultsPath + project + "/projectAnalyse.csv");
	          File makeDir = new File(resultsPath + project);
	          makeDir.mkdirs();
	    	  BufferedWriter buff;
				try {
					buff = new BufferedWriter(new FileWriter( csvOut, true ));
					buff.write( csvreader.bugHashes.get(key) + "," + curDateForm + "," + filesCount);
				    buff.newLine();
				    buff.close();
				    System.out.println(curDateForm + " HIER WURDE GESCHRIEBEN ... DEBUG DEBUG DEBUG!");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	      
			  String curDir = tempPath+project+"/"+curDateForm;
			  File dir = new File(curDir);
			  dir.mkdirs();  
	      
	      
		      // Copy Files 
		      if(configStr.equals("optimized")){
		    	  // Hier werden die geänderten Dateien eines zwischen zwei Commits in ein Set gepumpt
			      HashSet<String> changedSet = new HashSet<String>();
			      for(ChangedFile keySec : csvreader.changedFiles.keySet())
				    {
					  if(prevDate.after(keySec.comDate)) continue;
					  if(curDate.before(keySec.comDate)) break;
				      System.out.print("Key: " + keySec.comDate + " - ");
				      System.out.print("Value: " + csvreader.changedFiles.get(keySec) + "\n");
				      
				      changedSet.add(csvreader.changedFiles.get(keySec));
				    }
		    	  
		    	  copyFilesOptimized(prevDate, curDate, curDir, filesFind, changedSet);
		      }else if(configStr.equals("complete")){
		    	  copyFilesAll(curDir, filesFind);
		      }else{
		    	  System.out.println("Config Mode is missing!");
		      }
		      
	 	     // CPPSTATS TEXT DATEI ÄNDERN
		      changeCpp(curDir);       
	      }
 	     
	      // je nachdem Skunk mit cppstats oder mit processed Daten aufrufen
	      if(modeStr.equals("source")){ 
	    	  cppSkunk(curDateForm);
	      }
	      if(modeStr.equals("processed")){
	    	  skunkProc(curDateForm);
	      }
	      
	      // Ergebnis kopieren in eigenen Results Ordner
		  String sourcePath = resultsPath + project + "/" + curDateForm;
		  String destPath = resultsPath + project + "/" + smellModeStr + "Res/";
		  File destination = new File(destPath);
		  destination.mkdirs();							// Directories erstellen
		  
		  String pathFind = sourcePath;
    	  System.out.println( "Suche im Pfad: " + pathFind );
    	  List<File> filesFindCSV = FileFinder.find( pathFind, "(.*\\.csv$)" );
    	  List<File> filesFindXML = FileFinder.find(pathFind, "(.*\\.xml$)");
    	  
    	//umbennen und verschieben von CSV Dateien (Smell Severity)
    	  for ( File f : filesFindCSV ){
    		  String fileName = f.getName();
    		  if(fileName.contains(smellModeFile)){
    			  
    			  String absPath = f.getAbsolutePath();
    			  
    			  f.renameTo(new File(absPath.substring(0, absPath.lastIndexOf("/")) +"/"+ curDateForm + ".csv"));
    			  	  
    			  String source = absPath.substring(0, absPath.lastIndexOf("/")) +"/"+ curDateForm + ".csv";
    			  File copyFrom = new File(source);
    			  String destPathCSV = destPath + curDateForm + ".csv";
    			  File copyTo = new File(destPathCSV);
    			  try {
					Files.copy(copyFrom.toPath(),copyTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
				  } catch (IOException e) {
						// TODO Auto-generated catch block
					e.printStackTrace();
				  }
					
    		  }else{
    			  f.delete();
    		  }
    	  }
    	  
    	//umbennen und verschieben von XML Dateien (Smell Location)
    	  if(smellModeStr.equals("LF")){
	    	  for ( File f : filesFindXML ){
	    		  String fileName = f.getName();
	    		  if(fileName.contains(smellModeFile)){
	    			   
	    			  String absPath = f.getAbsolutePath();
	    			  
	    			  //f.renameTo(new File(absPath.substring(0, absPath.lastIndexOf("/")) +"/"+ curDateForm + ".xml"));
	    			  	  
	    			  String source = absPath;
	    			  File copyFrom = new File(source);
	    			  String destPathXML = destPath + curDateForm + ".xml";
	    			  File copyTo = new File(destPathXML);
	    			  try {
						Files.copy(copyFrom.toPath(),copyTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
					  } catch (IOException e) {
							// TODO Auto-generated catch block
						e.printStackTrace();
					  }
						
	    		  }
	    	  }
    	  }
    	  
	     prevDate = curDate;
	    }	
	}
	
	/**
	 * Copies all Files for the current Snapshot
	 * 
	 * @param curDir
	 * @param filesFind
	 */
	private static void copyFilesAll(String curDir, List<File> filesFind){
		for ( File f : filesFind ){
	  		  //System.out.println( f.getAbsolutePath() );
	  		  
	  		  File curFile = new File(f.getAbsolutePath());
	  		  String testPath = f.getAbsolutePath();
	  		  int testIdx = testPath.lastIndexOf("/source/");
	  		  String curFileStr = testPath.substring(testIdx, testPath.length());
	  		  File newDir = new File(curDir + curFileStr);
	  		  newDir.mkdirs();
	  		  try {
				 Files.copy(curFile.toPath(),newDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
			  } catch (IOException e) {
				// TODO Auto-generated catch block
				 e.printStackTrace();
			  }
	  	  }
	}
	
	/**
	 * Copies all Changed Files for the Optimized Mode (Optimized for AB and AF)
	 * 
	 * @param prevDate
	 * @param curDate
	 * @param curDir
	 * @param filesFind
	 * @param changedSet
	 */
	private static void copyFilesOptimized(Date prevDate, Date curDate,String curDir, List<File> filesFind, HashSet<String> changedSet){
		if(prevDate.equals(curDate)){
	  	  // Beim ersten mal müssen alle .c Files verschoben und auf Smells untersucht werden
	  	  for ( File f : filesFind ){
	  		  //System.out.println( f.getAbsolutePath() );
	  		  
	  		  File curFile = new File(f.getAbsolutePath());
	  		  String testPath = f.getAbsolutePath();
	  		  int testIdx = testPath.lastIndexOf("/source/");
	  		  String curFileStr = testPath.substring(testIdx, testPath.length());
	  		  File newDir = new File(curDir + curFileStr);
	  		  newDir.mkdirs();
	  		  try {
						Files.copy(curFile.toPath(),newDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	  	  }
	  		  
	    }else{
	    
		      // Das Set durchgehen und die Files kopieren
		      for (String curFileStr : changedSet) {
		    	     //System.out.println(curFileStr);
		    	     File curFile = new File(repoPath+project+"/source/"+curFileStr);
		    	     File newDir = new File(curDir+"/source/"+curFileStr);
		    	     newDir.mkdirs();
		    	     try {
						Files.copy(curFile.toPath(),newDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	          
		    	 }
	    }    
	}
	
	
	/**
	 * write the current Directory in the cppstats_input.txt
	 * 
	 * @param cppDir Directory to write 
	 */
	public static void changeCpp(String cppDir){
		File cppTxt = new File(cppstatsPath + "cppstats_input.txt");
		try {
			FileOutputStream schreibeStrom = new FileOutputStream(cppTxt);
			for (int i=0; i < cppDir.length(); i++){
			      schreibeStrom.write((byte)cppDir.charAt(i));
			}
			schreibeStrom.close();
			System.out.println("Datei ist geschrieben!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Aufruf von cppstats und Skunk
	 * 
	 * @param comDate
	 */
	public static void cppSkunk(String comDate){
		String resultsDir = resultsPath + project + "/" + comDate;
		String curDir = tempPath + project + "/" + comDate;
		String smellFile = smellPath;
		
		ProcessBuilder pb = new ProcessBuilder("/bin/bash", "cppSkunk.sh", resultsDir, curDir, smellFile);
		pb.directory(new File("/home/hnes/Masterarbeit/Tools/"));
		try {
			Process p = pb.start();
			
			Scanner s = new Scanner( p.getInputStream() ).useDelimiter( "\\Z" );
			System.out.println( s.next() );
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Aufruf Skunk mit processed
	 * 
	 * @param comDate
	 */
	public static void skunkProc(String comDate){
		String resultsDir = resultsPath + project + "/" + comDate;
		String curDir = resultsPath + project + "/" + comDate;
		String smellFile = smellPath;
		
		ProcessBuilder pb = new ProcessBuilder("/bin/bash", "skunkProc.sh", resultsDir, curDir, smellFile);
		pb.directory(new File("/home/hnes/Masterarbeit/Tools/"));
		try {
			Process p = pb.start();
			
			Scanner s = new Scanner( p.getInputStream() ).useDelimiter( "\\Z" );
			System.out.println( s.next() );
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Git Checkout Script Aufruf
	 * 
	 * @param hash
	 */
	public static void gitCheckout(String hash){
		String repoDir = repoPath + project + "/source/";
		String curHash = hash;
		
		ProcessBuilder pb = new ProcessBuilder("/bin/bash", "gitScript.sh", repoDir, curHash);
		 pb.directory(new File("/home/hnes/Masterarbeit/Tools/"));
		 try {
			Process p = pb.start();
			
			Scanner s = new Scanner( p.getInputStream() ).useDelimiter( "\\Z" );
			System.out.println( s.next() );
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Analyze input to decide what to do during runtime
	 *
	 * @param args the input arguments
	 * @return true, if input is correct
	 */
	private static boolean analyzeInput(String[] args)
	{
		// for easier handling, transform to list
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].contains("--"))
				args [i] = args[i].toLowerCase();
		}
		
		List<String> input = Arrays.asList(args);
	
		if (input.contains("--config"))
		{
			try 
			{
				String configPath = input.get(input.indexOf("--config") + 1);
				File f = new File(configPath);

				if(f.exists() && !f.isDirectory()) 
					smellPath = configPath;
				else 
				{
					System.out.println("The path to the configuration file does not exist.");
					return false;
				}
			} 
			catch (Exception e) 
			{
				System.out.println("ERROR: Could not load code smell configuration file!");
				e.printStackTrace();
				return false;
			}
		}else{
			System.out.println("ERROR: You need a Config!");
		}
		
		if (input.contains("--source"))
		{
			modeStr = "source";
		}else if(input.contains("--processed")){
			modeStr = "processed";
		}else{
			System.out.println("");
			return false;
		}
		
		
		if(input.contains("--ab"))
		{
			smellModeStr = "AB";
			smellModeFile = "methods";
		}
		
		if(input.contains("--af"))
		{
			smellModeStr = "AF";
			smellModeFile = "files";
		}
		
		if(input.contains("--lf"))
		{
			smellModeStr = "LF";
			smellModeFile = "features";
		}
		
		if(input.contains("--complete"))
		{
			configStr = "complete";
		}else if(input.contains("--optimized")){
			configStr = "optimized";
		}else{
			configStr = "";
		}
		
		return true;
	}	
}
