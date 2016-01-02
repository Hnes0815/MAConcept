package data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class ChangedFile implements Comparable<ChangedFile> {

	public String Filename;
	public String comHash;
	public Date comDate;
	
	/**
	 * Instantiates a new changedFile.
	 *
	 * @param name the name
	 */
	public ChangedFile(String name, String comHash, Date comDate)
	{
		this.Filename = name;
		this.comHash = comHash;
		this.comDate = comDate;
		
	}
	
	public Date getDate(){
		return this.comDate;
	}
	
	public String getHash(){
		return this.comHash;
	}
	
	public String getFile(){
		return this.Filename;
	}
	
	@Override
	public int compareTo(ChangedFile obj) {
		if (obj.getDate().after(this.getDate()))
			return -1;
		else if(obj.getDate().before(this.getDate()))
			return 1;
		else
			return 0;
	}
	
	
	
}
