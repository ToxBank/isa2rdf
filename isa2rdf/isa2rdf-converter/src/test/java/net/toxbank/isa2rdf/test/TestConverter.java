package net.toxbank.isa2rdf.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import net.toxbank.isa2rdf.ISAObject;
import net.toxbank.isa2rdf.ISAParser;

import org.junit.Test;

public class TestConverter {
	@Test
	public void testBII_I_1() throws Exception {
		String path= getClass().getClassLoader().getResource("BII-I-1").getFile();
		test(path,"BII_I_1");
	}
	
	public void test(String path,String name) throws Exception {
		
		File dir = new File(path);
		ISAParser parser = new ISAParser();
		ISAObject model = parser.parse(dir,name);
		
		OutputStream output = new FileOutputStream(new File(dir,"isatab.n3"));
	//	ISA.write(model.getModel(), output, "text/n3", true);
		output.close();
		
		output = new FileOutputStream(new File(dir,"isatab.owl"));
	//	ISA.write(model.getModel(), output, "application/rdf+xml", true);
		output.close();
		
		//TODO validate
	}	
	
	public void testBII_S_3() throws Exception {
		String path= getClass().getClassLoader().getResource("BII-S-3").getFile();
		test(path,"BII_S_3");
		
	}	
	
	public void testBII_S_6() throws Exception {
		String path= getClass().getClassLoader().getResource("BII-S-6").getFile();
		test(path,"BII_S_6");
	
	}		
	
	public void testJNS() throws Exception {
		String path= getClass().getClassLoader().getResource("JNS").getFile();
		test(path,"JNS");
		
	}	
	
	public void testNERC_S_2() throws Exception {
		String path= getClass().getClassLoader().getResource("NERC-S-2").getFile();
		test(path,"NERC_S_2");

		
	}	
	
	public void testBII_S_9() throws Exception {
		String path= getClass().getClassLoader().getResource("BII-S-9").getFile();
		test(path,"BII_S_9");
		
	}
}	
