package org.isa2rdf.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import junit.framework.Assert;
import net.toxbank.client.io.rdf.TOXBANK;

import org.isatools.isatab.isaconfigurator.ISAConfigurationSet;
import org.isatools.tablib.utils.BIIObjectStore;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
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
		Model model = testRDF("toxbank//BII-I-1");
		testKeywords(model, 3);
		testTitleAndAbstract(model);
		model.close();
	}
	
	protected void testTitleAndAbstract(Model model) throws Exception {
		String sparqlQuery = String.format(
				"PREFIX tb:<%s>\n"+
				"PREFIX isa:<%s>\n"+
				//"PREFIX dc:<http://purl.org/dc/elements/1.1/>\n"+
				"PREFIX dcterms:<http://purl.org/dc/terms/>\n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"SELECT ?investigation ?title ?abstract where {\n" +
				" ?investigation rdf:type isa:Investigation.\n" +				
				" OPTIONAL {?investigation dcterms:title ?title.}\n" +
				" OPTIONAL {?investigation dcterms:abstract ?abstract.}\n" +
				"} \n",
				TOXBANK.URI,
				ISA.URI);
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query,model);
		ResultSet rs = qe.execSelect();
		int n = 0;
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			//System.out.println(qs);
			Literal abstrakt = qs.getLiteral("abstract");
			Assert.assertNotNull(abstrakt);
			Literal title = qs.getLiteral("title");
			Assert.assertNotNull(title);

			n++;
		}
		qe.close();
		Assert.assertTrue(n>0);		
	}
	/**
	 * Find keywords of isa:Investigation instance
	 * @param model
	 * @param nkeywords
	 * @throws Exception
	 */
	protected void testKeywords(Model model, int nkeywords) throws Exception {
		String sparqlQuery = String.format("SELECT ?investigation ?keyword where { ?investigation <%s> ?keyword.\n ?investigation <%s> <%s> } \n",
				TOXBANK.HASKEYWORD.getURI(),
				RDF.type.getURI(),
				ISA.Investigation.getURI());
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query,model);
		ResultSet rs = qe.execSelect();
		int n = 0;
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			//RDFNode node = solution.get(vars.get(i));
			Literal keyword = qs.getLiteral("keyword");
			Assert.assertTrue(keyword.getString().startsWith(ISA.TBKeywordsNS));
			//System.out.println(qs);
			n++;
		}
		qe.close();
		Assert.assertEquals(nkeywords,n);
	}
	@org.junit.Test
	public void testRDF_E_MTAB_798() throws Exception {
		testRDF("toxbank//E-MTAB-798");
	}
	
	public Model testRDF(String dir) throws Exception {

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
		return model;

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
