package org.isa2rdf.datamatrix;

import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.Hashtable;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.isa2rdf.data.OT;
import org.isa2rdf.model.ISA;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class AbstractDataMatrixConverter {
	protected static final String prefix = "http://onto.toxbank.net/isa/bii/data_types/";
	protected Hashtable<String,String> lookup;
	protected String datatype;
	protected String investigationURI;
	
	public AbstractDataMatrixConverter(String datatype,Hashtable<String,String> lookup,String investigationURI) {
		this.investigationURI = investigationURI;
		this.lookup = lookup;
		if (datatype!=null) {
			int p = datatype.indexOf(prefix);
			if (p>=0) datatype = datatype.substring(prefix.length());
			this.datatype = datatype;
		}
	}
	
	protected XMLStreamWriter initWriter(OutputStream out) throws Exception {
		//BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(out));
		XMLStreamWriter writer = null;
		try {
			XMLOutputFactory factory      =  XMLOutputFactory.newInstance();
			writer  = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(out,"UTF-8"));
			return writer;
		} catch (Exception  x) {
			throw x;
		} finally {
		}
	}
		
		
	
	public static void main(String[] args) {
		if (args.length<1) return;
		
		for (String arg: args) {
			DataMatrixConverter q = new DataMatrixConverter(
										"http://onto.toxbank.net/isa/bii/data_types/microarray_derived_data",
										null,
										"http://example.org/investigation/123");
			try {
				File file = new File(arg);
			
				q.writeRDF(new FileReader(file),file.getName(),5,System.out);
			} catch (Exception x) {
				x.printStackTrace();
			}
		}	
	}

	
	public static int getDataMatrix(Model model) {
		String sparqlQuery = String.format(
				"PREFIX ot:<%s>\n"+
				"PREFIX isa:<%s>\n"+
				"PREFIX dcterms:<http://purl.org/dc/terms/>\n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"SELECT ?node ?feature ?title ?value where {\n" +
				"	?node rdf:type isa:Data." +
				"	?feature ot:hasSource ?node." +
				"	?feature dcterms:title ?title." +
				"	?fv ot:feature ?feature." +
				"	?fv ot:value ?value." +
				"} ORDER by ?node ?sample \n",
				OT.NS,
				ISA.URI);

		//Hashtable<String,Hashtable<String,String>> lookup = new Hashtable<String, Hashtable<String,String>>();
		
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query,model);
		ResultSet rs = qe.execSelect();
		int row = 0;
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			RDFNode node = qs.get("node");
			RDFNode feature = qs.get("feature");
			RDFNode title = qs.get("title");
			RDFNode value = qs.get("value");
			row++;
		}	
		return row;//lookup;
	}	
}
