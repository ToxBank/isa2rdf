package org.isa2rdf.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import junit.framework.Assert;
import net.toxbank.client.io.rdf.TOXBANK;

import org.isatools.isatab.isaconfigurator.ISAConfigurationSet;
import org.isatools.tablib.utils.BIIObjectStore;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;



/**
 * Unit test for simple App.
 */
public class AppTest  {

	@org.junit.Test
	public void testValidate() throws Exception {
		//String globalConfig = "/isaconfig-default/";
		//String localConfig = getClass().getClassLoader().getResource("isa-tab/config/isa_configurator").getFile();
		//ISAConfigurationSet.setConfigPath(globalConfig);
		ISAConfigurationSet.setConfigPath(getClass().getClassLoader().getResource("toxbank-config").getFile());
		
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
	public void testRDF_BII_I_1() throws Exception {
		testRDF("toxbank//BII-I-1");
	}
	
	@org.junit.Test
	public void testRDF_E_MTAB_798() throws Exception {
		testRDF("toxbank//E-MTAB-798");
	}
	
	public void testRDF(String dir) throws Exception {

		ISAConfigurationSet.setConfigPath(getClass().getClassLoader().getResource("toxbank-config").getFile());

		URL url = getClass().getClassLoader().getResource(dir);
		Assert.assertNotNull(url);
		String filesPath = url.getFile();
		IsaClient cli = new IsaClient();
		Model model = cli.process(filesPath);

		File out = new File(new File(filesPath),"isatab.owl");
		FileOutputStream output = new FileOutputStream(out);
		IsaClient.writeStream(model, output, "application/rdf+xml", true);
		output.close();

		/*
		Model test = ModelFactory.createOntologyModel();
		FileReader reader = new FileReader(out);
		test.read(reader,null);
		reader.close();
		*/
		
		out = new File(new File(filesPath),"isatab.n3");
		output = new FileOutputStream(out);
		IsaClient.writeStream(model, output, "text/n3", true);
		output.close();
	
		print(model);

	}
	
	protected void print(Model model) throws Exception {
		Model newModel = ModelFactory.createDefaultModel();
		newModel.setNsPrefix("tbisa", ISA.URI);
		newModel.setNsPrefix("dcterms", DCTerms.NS);
		newModel.setNsPrefix("foaf", ISA.FOAF);
		newModel.setNsPrefix("rdfs", RDFS.getURI());
		newModel.setNsPrefix("rdf", RDF.getURI());
		newModel.setNsPrefix("owl", OWL.getURI());
		newModel.setNsPrefix("tb", TOXBANK.URI);

		//newModel.setNsPrefix("bii1", ISA.URI+"BIII1/");
		
		/*
		StmtIterator sti = model.listStatements(ISA.HASSTUDY, RDFS.domain,(RDFNode) null);
		while (sti.hasNext()) {
			Statement st = sti.next();
			newModel.add(st);
		}
		sti.close();
		*/

		StmtIterator sti = model.listStatements(null, RDFS.subClassOf, ISA.PROCESSINGNODE);
		while (sti.hasNext()) {
			Statement st = sti.next();
			newModel.add(st);
		//	System.out.println(String.format("ISA.Investigation\t%s",st.getSubject()));
			StmtIterator u = model.listStatements(st.getSubject(), null, (RDFNode)null);
			while (u.hasNext()) {
				Statement s = u.next();
				newModel.add(s);
				//System.out.println(String.format("\t\t%s\t%s",s.getPredicate(),s.getObject()));
			}		
			u.close();
		}
		sti.close();
		
		IsaClient.writeStream(newModel, System.out, "text/n3", true);
	}
	

}
