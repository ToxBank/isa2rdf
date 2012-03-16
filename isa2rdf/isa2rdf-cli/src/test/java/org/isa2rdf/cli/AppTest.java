package org.isa2rdf.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream.GetField;
import java.net.URL;

import junit.framework.Assert;

import org.isatools.tablib.utils.BIIObjectStore;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;



/**
 * Unit test for simple App.
 */
public class AppTest  {

	@org.junit.Test
	public void testValidate() throws Exception {
		//String globalConfig = "/isaconfig-default/";
		//String localConfig = getClass().getClassLoader().getResource("isa-tab/config/isa_configurator").getFile();
		//ISAConfigurationSet.setConfigPath(globalConfig);

		URL url = getClass().getClassLoader().getResource("isa-tab/BII-I-1");
		Assert.assertNotNull(url);
		//String filesPath = url.getFile();
		String filesPath = url.getFile();
		System.out.println(filesPath);
		IsaClient cli = new IsaClient();

		BIIObjectStore store = cli.validate(filesPath);
		Assert.assertNotNull(store);
	}	
	@org.junit.Test
	public void testRDF() throws Exception {

	//	ISAConfigurationSet.setConfigPath(getClass().getClassLoader().getResource("isa-tab/config/isa_configurator").getFile());

		URL url = getClass().getClassLoader().getResource("isa-tab//BII-I-1");
		Assert.assertNotNull(url);
		String filesPath = url.getFile();
		IsaClient cli = new IsaClient();
		Model model = cli.process(filesPath);

		File out = new File(new File(filesPath),"isatab.owl");
		FileOutputStream output = new FileOutputStream(out);
		IsaClient.writeStream(model, output, "application/rdf+xml", true);
		output.close();

		Model test = ModelFactory.createOntologyModel();
		FileReader reader = new FileReader(out);
		test.read(reader,null);
		reader.close();
		
		out = new File(new File(filesPath),"isatab.n3");
		output = new FileOutputStream(out);
		IsaClient.writeStream(model, output, "text/n3", true);
		output.close();

	}
	

}
