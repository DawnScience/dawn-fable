package fable.imageviewer.psf;

import java.io.*;
import java.util.regex.*;
 
public class WildCardFileFilter implements FileFilter
{
    private String _pattern;
 
    public WildCardFileFilter(String pattern)
    {
        _pattern = pattern
        		.replace("\\", "\\\\")
        		.replace(".", "\\.")
        		.replace("+", "\\+")
        		.replace("(", "\\(")
        		.replace(")", "\\)")
        		.replace("^", "\\^")
        		.replace("$", "\\$")
        		.replace("{", "\\{")
        		.replace("}", "\\}")
        		.replace("[", "\\[")
        		.replace("]", "\\]")
        		.replace("|", "\\|")
        		.replace("*", ".*")
        		.replace("?", ".");
    }
 
    public boolean accept(File file)
    {
    	return Pattern.compile(_pattern).matcher(file.getName()).find();
    }
}
