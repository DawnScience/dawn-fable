package fable.imageviewer.psf;

import java.io.File;
import java.util.Comparator;

public class FilenameCaseInsensitiveComparator implements Comparator<File> {
	public int compare( File file1, File file2 ) {
		return file1.getName().compareToIgnoreCase(file2.getName());
	}
};
