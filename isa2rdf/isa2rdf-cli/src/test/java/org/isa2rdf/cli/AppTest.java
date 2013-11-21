package org.isa2rdf.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import junit.framework.Assert;
import net.toxbank.client.io.rdf.TOXBANK;

import org.apache.commons.io.FilenameUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.isa2rdf.cli.IsaClient._option;
import org.isa2rdf.datamatrix.DataMatrixConverter;
import org.isa2rdf.model.ISA;
import org.isatools.isatab.isaconfigurator.ISAConfigurationSet;
import org.isatools.tablib.mapping.properties.PropertyMappingHelper;
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
import com.hp.hpl.jena.rdf.model.Resource;
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
	public void testRDF_TGGATES() throws Exception {
		Model model = testRDF("toxbank//Toxbank_TG_GATES_acetaminophen_archive");
		testKeywords(model, 1);
		testTitleAndAbstract(model);
		testToxBankResources(model);
		testRetrieveAllToxbankProtocols(model);
		testRetrieveAllProtocols(model,1);
		testRetrieveAllStudiesAndProtocols(model,1);
		testToxbankHasProtocol(model,1);
		testToxbankHasAuthor(model,1);

		model.close();
	}
	@org.junit.Test
	public void testRDF_BII_I_1() throws Exception {
		Model model = testRDF("toxbank//BII-I-1");
		testKeywords(model, 3);
		testTitleAndAbstract(model);
		testToxBankResources(model);
		testRetrieveAllToxbankProtocols(model);
		testRetrieveAllProtocols(model,10);
		testRetrieveAllStudiesAndProtocols(model,2);
		testToxbankHasProtocol(model,11);
		testToxbankHasAuthor(model,1);
		JsonNode root = testGraph(model, 14,"toxbank//BII-I-1");
		model.close();
	}
	
	@org.junit.Test
	public void testRDF_LCMSMS3d() throws Exception {
		Model model = testRDF(new File("D://src-toxbank//isa-tab-files//NOTOXLCMSMS-VPA-2D3D_archive"));
		//Model model = testRDF("toxbank//LCMSMS_archive");
		testKeywords(model, 4);
		testTitleAndAbstract(model);
		testToxBankResources(model,1);
		//testRetrieveAllToxbankProtocols(model);
		//testRetrieveAllProtocols(model,10);
		//testRetrieveAllStudiesAndProtocols(model);
		//testToxbankHasProtocol(model,11);
		testToxbankHasAuthor(model,1);
		testToxbankHasProject(model,1);
		
		model.close();
	}
	
	@org.junit.Test
	public void testRDF_LCMSMS() throws Exception {
		Model model = testRDF(new File("D://src-toxbank//isa-tab-files//NOTOXLCMSMS_archive"));
		//Model model = testRDF("toxbank//LCMSMS_archive");
		
		
		testKeywords(model, 8);
		testTitleAndAbstract(model);
		testToxBankResources(model,1);
		//testRetrieveAllToxbankProtocols(model);
		//testRetrieveAllProtocols(model,10);
		//testRetrieveAllStudiesAndProtocols(model);
		//testToxbankHasProtocol(model,11);
		//testToxbankHasAuthor(model,1);
		testToxbankHasProject(model,1);
		JsonNode root = testGraph(model, 14,"toxbank//LCMSMS");
		model.close();
	}
	
	@org.junit.Test
	public void testRDF_NOTOX() throws Exception {
		//String file = "D://src-toxbank//isa-tab-files//NOTOX-APAP-Tx";
		//String file = "D:/src-toxbank/isa-tab-files/NOTOX-APAP-Tx 12-nov-2013/NOTOX-APAP-Tx";
		//String file = "D:/src-toxbank/isa-tab-files/NOTOX-APAP-Ex 12-nov-2013/NOTOX-APAP-Ex";
		String file = "D:/src-toxbank/isa-tab-files/NOTOX-APAP-Px 12-nov-2013/NOTOX-APAP-Px";
		Model model = testRDF(new File(file));
		
		
		//Model model = testRDF("toxbank//LCMSMS_archive");
		testKeywords(model, 4);
		//testTitleAndAbstract(model);
		//testToxBankResources(model,1);
		//testRetrieveAllToxbankProtocols(model);
		//testRetrieveAllProtocols(model,10);
		//testRetrieveAllStudiesAndProtocols(model);
		//testToxbankHasProtocol(model,11);
		//testToxbankHasAuthor(model,1);
		//testToxbankHasProject(model,1);
		//JsonNode root = testGraph(model, 14,"toxbank//NOTOX-APAP-Tx");
		//model.write(System.out,"N3");
		model.close();
	}
	
	@org.junit.Test
	public void testRDF_qHTS() throws Exception {
		Model model = testRDF(new File("D://src-toxbank//isa-tab-files//qHTS"));
		String dir = "toxbank//qHTS";
		//Model model = testRDF(dir);
		testKeywords(model, 14);
		testTitleAndAbstract(model);
		testToxBankResources(model,1);
		JsonNode root = testGraph(model, 14,dir);
		
		//testRetrieveAllToxbankProtocols(model);
		testRetrieveAllProtocols(model,2);
		testRetrieveAllStudiesAndProtocols(model,1);
		//testToxbankHasProtocol(model,11);
		//testToxbankHasAuthor(model,1);
		testToxbankHasProject(model,1);
		
		model.close();


	}
	
	
	@org.junit.Test
	public void testRDF_NTAP() throws Exception {
		Model model = testRDF(new File("D://src-toxbank//isa-tab-files//NOTOX-APAP-Tx"));
		testKeywords(model, 4);
		//testTitleAndAbstract(model);
		testToxBankResources(model);
		testRetrieveAllToxbankProtocols(model);
		testRetrieveAllProtocols(model,10);
		testRetrieveAllStudiesAndProtocols(model,2);
		testToxbankHasProtocol(model,11);
		testToxbankHasAuthor(model,1);
		JsonNode root = testGraph(model, 14,"toxbank//NOTOX-APAP-Tx");
		model.close();
	}
	
	protected JsonNode testGraph(Model model,int steps,String dir) throws Exception {
		PropertyMappingHelper pmh;
		String sparqlQuery = String.format(
				"PREFIX tb:<%s>\n"+
				"PREFIX isa:<%s>\n"+
				"PREFIX dcterms:<http://purl.org/dc/terms/>\n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"SELECT ?study ?input ?node ?type ?protocolApplication ?protocol ?inputid ?outputid ?output ?itype ?otype ?idata ?odata ?imat ?omat ?plabel ?pid ?imacc ?omacc ?idacc ?odacc where {\n" +
				//" ?protocol rdf:type tb:Protocol.\n" +
				" ?node isa:hasInputNode ?input.\n" +
				" ?node isa:hasOutputNode ?output.\n" +
				" ?node rdf:type ?type.\n" +
				" ?input rdf:type ?itype.\n" +
				" ?output rdf:type ?otype.\n" +
				" OPTIONAL {" +
				"	?node isa:hasProtocolApplication ?protocolApplication." +
				"	?protocolApplication isa:appliesProtocol ?protocol." +
				"	?protocol rdf:type isa:Protocol." +
				"	?protocol rdfs:label ?plabel." +
				"	?protocol isa:hasAccessionID ?pid." +
				"}."+
				" OPTIONAL {" +
				"	?node isa:hasStudy ?study." +
				"}."+
				" OPTIONAL {?input isa:hasAccessionID ?inputid}."+
				" OPTIONAL {" +
				"	?input isa:hasData ?idata." +
				"	?idata rdf:type isa:Data." +
				"	?idata isa:hasAccessionID ?idacc." +				
				"}."+
				" OPTIONAL {" +
				"	?input isa:hasMaterial ?imat." +
				"	?imat rdf:type isa:Material." +
				"	?imat isa:hasAccessionID ?imacc." +
				"}."+
				" OPTIONAL {?output isa:hasAccessionID ?outputid}."+
				" OPTIONAL {" +
				"	?output isa:hasData ?odata." +
				"	?odata rdf:type isa:Data." +
				"	?odata isa:hasAccessionID ?odacc." +
				"}."+
				" OPTIONAL {" +
				"	?output isa:hasMaterial ?omat." +
				"	?omat rdf:type isa:Material." +
				"	?omat isa:hasAccessionID ?omacc." +
				"}."+
				"} ORDER by ?input \n",
				TOXBANK.URI,
				ISA.URI);
		
		ObjectMapper m = new ObjectMapper();
		ObjectNode root = m.createObjectNode();
		ObjectNode studies = m.createObjectNode();
		root.put("study",studies);

		
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query,model);
		ResultSet rs = qe.execSelect();
		int n = 0;
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			RDFNode protocolApp = qs.get("protocolApplication");
			RDFNode protocol = qs.get("protocol");
			RDFNode input = qs.get("input");
			RDFNode output = qs.get("output");
			RDFNode study = qs.get("study");
			RDFNode nodetype = qs.get("type");

			n++;
			
			JsonNode studyNode = studies.get(study.asNode().getLocalName());
			if (studyNode==null) {
				studyNode = m.createObjectNode();
				studies.put(study.asNode().getLocalName(),studyNode);
			}
			JsonNode processing = studyNode.get("processing");
			if (processing==null) {
				processing = m.createArrayNode();
				((ObjectNode)studyNode).put("processing",processing);
			}
			
			JsonNode nodes = studyNode.get("nodes");
			if (nodes==null) {
				nodes = m.createObjectNode();
				((ObjectNode)studyNode).put("nodes",nodes);
			}
			
			JsonNode protocols = studyNode.get("protocols");
			if (protocols==null) {
				protocols = m.createObjectNode();
				((ObjectNode)studyNode).put("protocols",protocols);
			}
			
			JsonNode protocolApps = studyNode.get("protocolApplications");
			if (protocolApps==null) {
				protocolApps = m.createObjectNode();
				((ObjectNode)studyNode).put("protocolApplications",protocolApps);
			}			
			
			JsonNode materials = studyNode.get("materials");
			if (materials==null) {
				materials = m.createObjectNode();
				((ObjectNode)studyNode).put("materials",materials);
			}
			JsonNode data = studyNode.get("data");
			if (data==null) {
				data = m.createObjectNode();
				((ObjectNode)studyNode).put("data",data);
			}
			
			ObjectNode item = m.createObjectNode();
			item.put("type",nodetype.asNode().getLocalName());
			item.put("input",input.asNode().getLocalName());
			item.put("output",output.asNode().getLocalName());
			if (protocolApp!=null) {
				
				JsonNode pApp = protocolApps.get(protocolApp.asNode().getLocalName());
				if (pApp==null) {
					pApp = m.createObjectNode();
					((ObjectNode)protocolApps).put(protocolApp.asNode().getLocalName(),pApp);
				}
				((ObjectNode)pApp).put("protocol", protocol.asNode().getURI());
				((ObjectNode)pApp).put("parameters", m.createObjectNode());
				item.put("applies",protocolApp.asNode().getLocalName());
				//:PMV62 , :PMV71 , :PMV68 , :PMV64 , :PMV66 .
				JsonNode p = protocols.get(protocol.asNode().getURI());
				if (p==null) p = m.createObjectNode();
				((ObjectNode)p).put("label",qs.get("plabel").asLiteral().getString());
				((ObjectNode)p).put("acc",qs.get("pid").asLiteral().getString());
				((ObjectNode)protocols).put(protocol.asNode().getURI(),((ObjectNode)p));
			}
			((ArrayNode)processing).add(item);
			
			ObjectNode iNode = m.createObjectNode();
			iNode.put("type",qs.get("itype").asNode().getLocalName());
			if (qs.get("inputid")!=null) iNode.put("acc",qs.get("inputid").asLiteral().getString());
			if (qs.get("idata")!=null) {
				iNode.put("data",qs.get("idata").asNode().getLocalName());
				if (protocols.get(qs.get("idata").asNode().getLocalName())==null) {
					 ObjectNode o = m.createObjectNode();
					 o.put("acc",qs.get("idacc").asLiteral().getString());
					((ObjectNode)data).put(qs.get("idata").asNode().getLocalName(), o);
				}
			}
			if (qs.get("imat")!=null) {
				iNode.put("material",qs.get("imat").asNode().getLocalName());
				if (protocols.get(qs.get("imat").asNode().getLocalName())==null) { 
					ObjectNode o = m.createObjectNode();
					o.put("acc",qs.get("imacc").asLiteral().getString());
					((ObjectNode)materials).put(qs.get("imat").asNode().getLocalName(),o);
				}	
			}
			((ObjectNode)nodes).put(input.asNode().getLocalName(), iNode);
			
			ObjectNode oNode = m.createObjectNode();
			oNode.put("type",qs.get("otype").asNode().getLocalName());
			if (qs.get("outputid")!=null) oNode.put("acc",qs.get("outputid").asLiteral().getString());
			if (qs.get("odata")!=null) {
				oNode.put("data",qs.get("odata").asNode().getLocalName());
				if (protocols.get(qs.get("odata").asNode().getLocalName())==null) {
					 ObjectNode o = m.createObjectNode();
					 o.put("acc",qs.get("odacc").asLiteral().getString());
					((ObjectNode)data).put(qs.get("odata").asNode().getLocalName(), o);
				}	

			}
			if (qs.get("omat")!=null) {
				oNode.put("material",qs.get("omat").asNode().getLocalName());
				if (protocols.get(qs.get("omat").asNode().getLocalName())==null) { 
					 ObjectNode o = m.createObjectNode();
					 o.put("acc",qs.get("omacc").asLiteral().getString());
					((ObjectNode)materials).put(qs.get("omat").asNode().getLocalName(), o);
				}	
			}
			((ObjectNode)nodes).put(output.asNode().getLocalName(), oNode);

		}
		qe.close();
		parameters2json(model, m,root);
		parameters2factors(model,m,root);
		ObjectWriter writer = m.defaultPrettyPrintingWriter();
		writer.writeValueAsString(root);
		URL url = getClass().getClassLoader().getResource(dir);
		Assert.assertNotNull(url);
		FileOutputStream fw = new FileOutputStream(new File(url.getFile(),"isatab.json"));
		writer.writeValue(fw, root);
		fw.close();
		
		return root;
		//Assert.assertEquals(nprotocols,n);		
	}

	protected void parameters2json(Model model, ObjectMapper m, ObjectNode root) throws Exception {
		
		String sparqlQuery = String.format(
				"PREFIX tb:<%s>\n"+
				"PREFIX isa:<%s>\n"+
				"PREFIX dcterms:<http://purl.org/dc/terms/>\n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"SELECT ?processing ?study ?protocolapp ?pvalue ?parameter ?ptitle ?value ?term where {\n" +
				" ?processing isa:hasStudy ?study.\n" +
				" ?processing isa:hasProtocolApplication ?protocolapp.\n" +
				" ?protocolapp isa:hasParameterValue ?pvalue.\n" +
				" ?pvalue rdf:type isa:ParameterValue.\n" +
				" ?pvalue isa:hasParameter ?parameter.\n" +
				" ?parameter dcterms:title ?ptitle.\n" +
				" ?pvalue isa:hasValue ?value.\n" +
				" OPTIONAL {" +
				"	?pvalue isa:hasOntologyTerm ?term.\n" +
				" }."+
				"} \n",
				TOXBANK.URI,
				ISA.URI);
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query,model);
		ResultSet rs = qe.execSelect();
		int n = 0;
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			
			String studyId = qs.get("study").asNode().getLocalName();
			JsonNode studyNode = root.get("study").get(studyId);
			if (studyNode==null) {
				studyNode = m.createObjectNode();
				((ObjectNode)root.get("study")).put(studyId,studyNode);
			}
			
			JsonNode parameters = studyNode.get("parameters");
			if (parameters==null) {
				parameters = m.createObjectNode();
				((ObjectNode)studyNode).put("parameters",parameters);
			}
			JsonNode parameterValues = studyNode.get("parameterValues");
			if (parameterValues==null) {
				parameterValues = m.createObjectNode();
				((ObjectNode)studyNode).put("parameterValues",parameterValues);
			}
			
			//Parameters
			String paramId = qs.get("parameter").asNode().getLocalName();
			((ObjectNode)parameters).put(paramId, qs.get("ptitle").asLiteral().getString());

			//Parameter values
			String pValueId = qs.get("pvalue").asNode().getLocalName();
			JsonNode pValue = ((ObjectNode)parameterValues).get(pValueId);
			if (pValue==null) {
				pValue = m.createObjectNode();
				((ObjectNode)parameterValues).put(pValueId, pValue);
				((ObjectNode)pValue).put("parameter",studyNode.get("parameters").get(paramId));
				((ObjectNode)pValue).put("value",qs.get("value").asLiteral().getString());
				if (qs.get("term")!=null)
				((ObjectNode)pValue).put("term",qs.get("term").asNode().getURI());
				
			}
			
			JsonNode protocolApps = studyNode.get("protocolApplications");
			
			JsonNode papp = ((ObjectNode)protocolApps).get(qs.get("protocolapp").asNode().getLocalName());
			if (papp!=null) {
				ObjectNode params = ((ObjectNode)papp.get("parameters"));
				params.put(pValueId,studyNode.get("parameterValues").get(pValueId));
			}	

			n++;
		}
		qe.close();
				
	}
	
	protected void parameters2factors(Model model, ObjectMapper m, ObjectNode root) throws Exception {
		
		String sparqlQuery = String.format(
				"PREFIX tb:<%s>\n"+
				"PREFIX isa:<%s>\n"+
				"PREFIX dcterms:<http://purl.org/dc/terms/>\n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"SELECT ?study ?node ?material ?factorValue ?factor ?factortitle ?value ?term where {\n" +
				" ?node isa:hasStudy ?study.\n"+
				" {{?node isa:hasMaterial ?material} union {?node isa:hasData ?material}}.\n"+
				" ?material isa:hasFactorValue ?factorValue.\n"+
				" ?factorValue isa:hasValue ?value.\n"+
				" ?factorValue isa:hasFactor ?factor.\n"+
				" ?factor dcterms:title ?factortitle.\n"+
				" OPTIONAL {" +
				"	?factorValue isa:hasOntologyTerm ?term.\n" +
				" }."+				
				"} \n",
				TOXBANK.URI,
				ISA.URI);
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query,model);
		ResultSet rs = qe.execSelect();
		int n = 0;
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			
			String studyId = qs.get("study").asNode().getLocalName();
			JsonNode studyNode = root.get("study").get(studyId);
			if (studyNode==null) {
				studyNode = m.createObjectNode();
				((ObjectNode)root.get("study")).put(studyId,studyNode);
			}
			
			String mid = qs.get("material").asNode().getLocalName();
			String fvid = qs.get("factorValue").asNode().getLocalName();
			String fid = qs.get("factor").asNode().getLocalName();
			String ftid = qs.get("factortitle").asLiteral().getString();
			String val = qs.get("value").asLiteral().getString();
			String term = qs.get("term")==null?null:qs.get("term").asNode().getURI();
			
			if (fvid!=null && !"".equals(fvid)) {
				JsonNode material = studyNode.get("materials").get(mid);
				if (material==null) material = studyNode.get("data").get(mid);
				if (material != null) {
					JsonNode factors = material.get("factors");
					if (factors==null) { factors = m.createObjectNode(); ((ObjectNode)material).put("factors",factors);}
					ObjectNode fnode = m.createObjectNode();
					fnode.put(ftid, val);
					if (term!=null) fnode.put("term", term);
					((ObjectNode)factors).put(fvid,fnode);
				}
			}
			n++;
		}
		qe.close();
				
	}
		
	
	protected void testToxbankHasProtocol(Model model,int nprotocols) throws Exception {
		String sparqlQuery = String.format(
				"PREFIX tb:<%s>\n"+
				"PREFIX isa:<%s>\n"+
				"PREFIX dcterms:<http://purl.org/dc/terms/>\n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"SELECT ?investigation ?study ?protocol where {\n" +
				" ?protocol rdf:type isa:Protocol.\n" +
				//" ?protocol rdf:type tb:Protocol.\n" +
				" ?study isa:hasProtocol ?protocol.\n" +
				" ?study rdf:type isa:Study.\n" +
				" ?investigation isa:hasStudy ?study.\n" +
				" ?investigation rdf:type isa:Investigation.\n" +
				"} \n",
				TOXBANK.URI,
				ISA.URI);
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query,model);
		ResultSet rs = qe.execSelect();
		int n = 0;
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			RDFNode protocol = qs.get("protocol");
			Assert.assertNotNull(protocol);
			Assert.assertNotNull(protocol.isURIResource());

			n++;
		}
		qe.close();
		Assert.assertEquals(nprotocols,n);		
	}
	
	protected void testToxbankHasAuthor(Model model,int nauthors) throws Exception {
		String sparqlQuery = String.format(
				"PREFIX tb:<%s>\n"+
				"PREFIX isa:<%s>\n"+
				"PREFIX dcterms:<http://purl.org/dc/terms/>\n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"SELECT ?investigation ?author where {\n" +
				" ?author rdf:type tb:User.\n" +
				" ?investigation tb:hasAuthor ?author.\n" +
				" ?investigation rdf:type isa:Investigation.\n" +
				"} \n",
				TOXBANK.URI,
				ISA.URI);
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query,model);
		ResultSet rs = qe.execSelect();
		int n = 0;
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			RDFNode author = qs.get("author");
			Assert.assertNotNull(author);
			Assert.assertNotNull(author.isURIResource());

			n++;
		}
		qe.close();
		Assert.assertEquals(nauthors,n);		
	}	

	protected void testToxbankHasProject(Model model,int nproject) throws Exception {
		String sparqlQuery = String.format(
				"PREFIX tb:<%s>\n"+
				"PREFIX isa:<%s>\n"+
				"PREFIX dcterms:<http://purl.org/dc/terms/>\n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"SELECT ?investigation ?project where {\n" +
				" ?project rdf:type tb:Project.\n" +
				" ?investigation tb:hasProject ?project.\n" +
				" ?investigation rdf:type isa:Investigation.\n" +
				"} \n",
				TOXBANK.URI,
				ISA.URI);
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query,model);
		ResultSet rs = qe.execSelect();
		int n = 0;
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			RDFNode project = qs.get("project");
			Assert.assertNotNull(project);
			Assert.assertNotNull(project.isURIResource());

			n++;
		}
		qe.close();
		Assert.assertEquals(nproject,n);		
	}	

	protected void testRetrieveAllToxbankProtocols(Model model) throws Exception {
		String sparqlQuery = String.format(
				"PREFIX tb:<%s>\n"+
				"PREFIX isa:<%s>\n"+
				"PREFIX dcterms:<http://purl.org/dc/terms/>\n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"SELECT ?protocol where {\n" +
				" ?protocol rdf:type tb:Protocol.\n" +				
				"} \n",
				TOXBANK.URI,
				ISA.URI);
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query,model);
		ResultSet rs = qe.execSelect();
		int n = 0;
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			RDFNode protocol = qs.get("protocol");
			Assert.assertNotNull(protocol);
			Assert.assertNotNull(protocol.isURIResource());
			Assert.assertTrue(((Resource)protocol).getURI().startsWith("https://services.toxbank.net/toxbank/protocol/"));
			n++;
		}
		qe.close();
		Assert.assertEquals(1,n);		
	}
	/**
	 * Retrieves all available protocols
	 * @param model
	 * @throws Exception
	 */
	protected void testRetrieveAllProtocols(Model model,int numProtocols) throws Exception {
		String sparqlQuery = String.format(
				"PREFIX tb:<%s>\n"+
				"PREFIX isa:<%s>\n"+
				"PREFIX dcterms:<http://purl.org/dc/terms/>\n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"SELECT distinct ?protocol where {\n" +
				" ?protocol rdf:type isa:Protocol.\n" +				
				"} \n",
				TOXBANK.URI,
				ISA.URI);
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query,model);
		
		ResultSet rs = qe.execSelect();
		int n = 0;
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			RDFNode protocol = qs.get("protocol");
			Assert.assertNotNull(protocol);
			Assert.assertNotNull(protocol.isURIResource());
			n++;
		}
		qe.close();
		Assert.assertEquals(numProtocols,n);		
	}
	
	protected void testRetrieveAllStudiesAndProtocols(Model model, int expected) throws Exception {
		String sparqlQuery = String.format(
				"PREFIX tb:<%s>\n"+
				"PREFIX isa:<%s>\n"+
				"PREFIX dcterms:<http://purl.org/dc/terms/>\n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"SELECT ?investigation ?study ?protocol where {\n" +
				" ?investigation rdf:type isa:Investigation.\n" +
				" ?investigation isa:hasStudy ?study.\n" +
				" ?study rdf:type isa:Study.\n" +
				" ?study isa:hasProtocol ?protocol.\n" +
				" ?protocol rdf:type tb:Protocol.\n" +
				"} \n",
				TOXBANK.URI,
				ISA.URI);
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query,model);
		ResultSet rs = qe.execSelect();
		int n = 0;
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			RDFNode study = qs.get("study");
			Assert.assertNotNull(study);
			Assert.assertNotNull(study.isURIResource());
			
			RDFNode investigation = qs.get("investigation");
			Assert.assertNotNull(investigation);
			Assert.assertNotNull(investigation.isURIResource());
			
			RDFNode protocol = qs.get("protocol");
			Assert.assertNotNull(protocol);
			Assert.assertNotNull(protocol.isURIResource());
			n++;
		}
		qe.close();
		Assert.assertEquals(expected,n);		
	}
	protected void testToxBankResources(Model model) throws Exception {
		testToxBankResources(model,1);
	}
	protected void testToxBankResources(Model model, int expected) throws Exception {
		String sparqlQuery = String.format(
				"PREFIX tb:<%s>\n"+
				"PREFIX isa:<%s>\n"+
				"PREFIX dcterms:<http://purl.org/dc/terms/>\n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"SELECT ?investigation ?consortium ?organisation ?owner where {\n" +
				" ?investigation rdf:type isa:Investigation.\n" +				
				" OPTIONAL {" +
				" ?investigation tb:hasProject ?consortium." +
				" ?consortium rdf:type tb:Project." +				
				"}\n" +
				" OPTIONAL {" +
				" ?investigation tb:hasOrganisation ?organisation." +
				" ?organisation  rdf:type tb:Organization." +
				"}\n" +
				" OPTIONAL {" +
				" ?investigation tb:hasOwner ?owner." +
				" ?owner  rdf:type tb:User." +
				"}\n" +
				"} \n",
				TOXBANK.URI,
				ISA.URI);
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query,model);
		ResultSet rs = qe.execSelect();
		int n = 0;
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			RDFNode project = qs.get("consortium");
			Assert.assertNotNull(project);
			Assert.assertNotNull(project.isURIResource());
	
			RDFNode organisation = qs.get("organisation");
			Assert.assertNotNull(organisation);
			Assert.assertNotNull(organisation.isURIResource());

			RDFNode owner = qs.get("owner");
			Assert.assertNotNull(owner);
			Assert.assertNotNull(owner.isURIResource());
			
			n++;
		}
		qe.close();
		Assert.assertEquals(expected,n);		
	}
	
	protected void testTitleAndAbstract(Model model) throws Exception {
		String sparqlQuery = String.format(
				"PREFIX tb:<%s>\n"+
				"PREFIX isa:<%s>\n"+
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
			n++;
		}
		qe.close();
		Assert.assertEquals(nkeywords,n);
	}
	
	public void testRDF_E_MTAB_798() throws Exception {
		testRDF("toxbank//E-MTAB-798");
	}
	
	public Model testRDF(String dir) throws Exception {
		URL url = getClass().getClassLoader().getResource(dir);
		Assert.assertNotNull(url);
		return testRDF(new File(url.getFile()));
	}
	public Model testRDF(File filesDir) throws Exception {
		Assert.assertTrue(filesDir.isDirectory());
		String config = getClass().getClassLoader().getResource("toxbank-config").getFile();
		System.out.println(config);
		ISAConfigurationSet.setConfigPath(config);

		IsaClient cli = new IsaClient();
		cli.setOption(_option.toxbankuri, "https://services.toxbank.net/toxbank");
		cli.setOption(_option.outdatafilesdir, filesDir.getAbsolutePath());
		cli.setOption(_option.dir, filesDir.getAbsolutePath());
		Model model = cli.processAndSave();

		System.err.println("triples " + model.size());
		File out = new File(filesDir,"isatab.n3");
		FileOutputStream output = new FileOutputStream(out);
		IsaClient.writeStream(model, output, "text/n3", false);
		output.close();

		/*
		Model test = ModelFactory.createOntologyModel();
		FileReader reader = new FileReader(out);
		test.read(reader,null);
		reader.close();
		*/

		out = new File(filesDir,"isatab.rdf");
		output = new FileOutputStream(out);
		IsaClient.writeStream(model, output, "application/rdf+xml", false);
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
