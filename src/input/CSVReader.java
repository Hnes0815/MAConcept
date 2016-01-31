package input;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import data.ChangedFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;


public class CSVReader {

	private String detectionPath;
	
	// Liste für geänderte Dateien
	public TreeMap<ChangedFile, String> changedFiles = new TreeMap<ChangedFile, String>();
	// Liste für jeden x-ten Bugfix Commit
	public TreeMap<Date, String> bugHashes = new TreeMap<Date, String>();
	
	/**
	 * Instantiates a new CSVReader
	 *
	 * @param detPath the path of the bugdetection csv
	 */
	public CSVReader(String detPath){
		this.detectionPath = detPath;
	}
	
	public void processFile(){
		String csvFile = this.detectionPath;
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		
		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

			    // use comma as separator
				String[] commit = line.split(cvsSplitBy);
				String curHash = commit[0];
				boolean bugfixCommit = Boolean.parseBoolean(commit[1]);
				int bugfixCount = Integer.parseInt(commit[8]);
				String strDate = commit[7];
				String fileName = commit[3];
				
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		        Date dateStr;
		        Date comDate = null;
				try {
					dateStr = formatter.parse(strDate);
					String formattedDate = formatter.format(dateStr);
			        //System.out.println("yyyy-MM-dd date is ==>"+formattedDate);
			        comDate = formatter.parse(formattedDate);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        
				
				//System.out.println("Hash: " + curHash 
	            //                     + " , isBugfix: " + bugfixCommit 
	            //                     + " , Datum: " + comDate);

				ChangedFile chFile = new ChangedFile(fileName, curHash, comDate);
				
				changedFiles.put(chFile, fileName);
				if(((bugfixCount % 100) == 0) && bugfixCommit)			// TODO: geändert auf 100 für Testzwecke
					bugHashes.put(comDate, curHash);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
