package net.toxbank.isa2rdf.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import net.toxbank.isa2rdf.ISA;
import net.toxbank.isa2rdf.ISAParser;

import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;

public class TestConverter {
	@Test
	public void testBII_I_1() throws Exception {
		String path= getClass().getClassLoader().getResource("BII-I-1").getFile();
		test(path);
	}
	
	public void test(String path) throws Exception {
		File dir = new File(path);
		OntModel model = ISA.createModel();
		ISAParser parser = new ISAParser(model);
		parser.parse(dir);
		
		OutputStream output = new FileOutputStream(new File(dir,"isatab.n3"));
		ISA.write(model, output, "text/n3", true);
		output.close();
		
		output = new FileOutputStream(new File(dir,"isatab.owl"));
		ISA.write(model, output, "application/rdf+xml", true);
		output.close();
		
		//TODO validate
	}	
	
	public void testBII_S_3() throws Exception {
		String path= getClass().getClassLoader().getResource("BII-S-3").getFile();
		test(path);
		
	}	
	
	public void testBII_S_6() throws Exception {
		String path= getClass().getClassLoader().getResource("BII-S-6").getFile();
		test(path);
	
	}		
	
	public void testJNS() throws Exception {
		String path= getClass().getClassLoader().getResource("JNS").getFile();
		test(path);
		
	}	
	
	public void testNERC_S_2() throws Exception {
		String path= getClass().getClassLoader().getResource("NERC-S-2").getFile();
		test(path);

		
	}	
	
	public void testBII_S_9() throws Exception {
		String path= getClass().getClassLoader().getResource("BII-S-9").getFile();
		test(path);
		
	}
}	
