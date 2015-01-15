/**
 * 
 */
package edu.phema.QdmKnime.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
//import edu.vanderbilt.mc.phema.QdmKnime.Connection;

/**
 * @author Huan
 *
 */
public class CopyFolders {

	/**
	 * 
	 */
	public CopyFolders() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println(System.getProperty("java.io.tmpdir"));

		Path tempFolder = Paths.get(System.getProperty("java.io.tmpdir"));
		Path sourceFolder = Paths.get("src/main/resources/metaNodeRepos/logicalOperators/AND.zip");
		System.out.println(sourceFolder.toAbsolutePath());
		
		

				//"/QdmKnimeTranslator/src/main/resources/metaNodeRepos/logicalOperators/AND.zip";
		
		
		
	//	System.out.println(sourceFolder.toString());
		
		Path newFile = Paths.get("qdmKnime/AND.zip");
		Path target = tempFolder.resolve(newFile);
		
				
				
		target.toFile().mkdirs();
		
		try {
			Files.copy(sourceFolder, target, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// unzip a file http://javing.blogspot.com/2013/04/how-to-unzip-zip-file-with-zip4j.html
		
		String zipSource = target.toString();
		Path destination = target.getParent();
		
		System.out.println(destination.toString());
		
		try {
			ZipFile zipFile = new ZipFile(zipSource);
			zipFile.extractAll(destination.toString());
		} catch (ZipException e) {
			e.printStackTrace();
			
		}
		
		Path renamedPath = destination.resolve("AND (#3)");
		try {
			Files.move(destination.resolve("AND"), renamedPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
