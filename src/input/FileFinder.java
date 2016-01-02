package input;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

public class FileFinder {

	public static List<File> find( String start, String extensionPattern )
	  {
	    List<File> files = new ArrayList<File>( 1024 );
	    Stack<File> dirs = new Stack<File>();
	    File startdir = new File( start );
	    Pattern p = Pattern.compile( extensionPattern, Pattern.CASE_INSENSITIVE );

	    if ( startdir.isDirectory() )
	      dirs.push( startdir );

	    while ( dirs.size() > 0 )
	      for ( File file : dirs.pop().listFiles() )
	        if ( file.isDirectory() )
	          dirs.push( file );
	        else
	          if ( p.matcher(file.getName()).matches() )
	            files.add( file );

	    return files;
	  }
}
