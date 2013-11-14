package org.isa2rdf.datamatrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.UUID;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import net.idea.opentox.cli.csv.QuotedTokenizer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.isa2rdf.data.OT;
import org.isa2rdf.data.stax.DatasetRDFWriter;
import org.isa2rdf.model.ISA;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;


public class DataMatrixConverter {
	protected Hashtable<String,String> lookup;
	public DataMatrixConverter(Hashtable<String,String> lookup) {
		this.lookup = lookup;
	}
	public DataMatrixConverter() {
		this(null);
	}
	public static void main(String[] args) {
		if (args.length<1) return;
		
		for (String arg: args) {
			DataMatrixConverter q = new DataMatrixConverter();
			try {
				File file = new File(arg);
				String experimentname= file.getName().replace(".csv", "");				
				q.writeRDF(new FileReader(file),experimentname,5,System.out);
			} catch (Exception x) {
				x.printStackTrace();
			}
		}	
	}
	
	public void writeRDF(Reader reader,String experimentname, final int maxrows, OutputStream out) throws Exception {
		final DatasetRDFWriter rdfwriter = new DatasetRDFWriter(lookup);
		final XMLStreamWriter writer = initWriter(out);
		rdfwriter.setOutput(writer);
		try {
			
			DataMatrix matrix = parse(reader,experimentname, new IRowProcessor<DataMatrix>() {
				@Override
				public void header(DataMatrix row) throws Exception {
					rdfwriter.header(writer);
				}
				@Override
				public DataMatrix process(DataMatrix row) throws Exception {
					rdfwriter.process(row);	
					return row;
				}
				@Override
				public void footer(DataMatrix row) throws Exception {
					rdfwriter.footer(writer);	
				}
			},maxrows);
			//System.out.println();
			//System.out.println(matrix);
					
				
		} catch (Exception x) {
			x.printStackTrace();
			System.exit(-1);
		} finally {

			try { writer.close();} catch (Exception x) {}
			try { rdfwriter.close();} catch (Exception x) {}
		}
	}	

	private XMLStreamWriter initWriter(OutputStream out) throws Exception {
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
	
	public DataMatrix parse(Reader reader, String experimentname, IRowProcessor<DataMatrix> processor , int maxrows) throws Exception {
		BufferedReader breader = null ;
		try {
			String line;

			breader = new BufferedReader(reader);
			
			//read config
			InputStream in = getClass().getClassLoader().getResourceAsStream("org/isa2rdf/data/transcriptomics/datamatrix.json");
			DataMatrix matrix = new DataMatrix(in);

			
			//read config completed
			ArrayList<String> header = new ArrayList<String>();
			ArrayList<String> samples = new ArrayList<String>();
			
			int row = 0;
			while ((line = breader.readLine()) != null) {
				ObjectNode probes =  matrix.getProbe();
				ObjectNode genes =  matrix.getGene();
				ObjectNode annotations =  matrix.getAnnotation();
				ObjectNode values =  matrix.getValues();
				
				genes.removeAll(); values.removeAll();
				
				QuotedTokenizer st = new QuotedTokenizer(line,'\t');
				int col = 0;
				while (st.hasMoreTokens()) {
					String value = st.nextToken().trim();
					if (row==0) {
						String lookupValue = value;
						String sampleName = null;
						int quote = value.indexOf('\'');
						if (quote>0) {
							lookupValue = value.substring(0,quote).trim();
							sampleName = value.substring(quote+1,value.length()-1).trim();
						}					
						ObjectNode column = matrix.getColumn(lookupValue);
						header.add(column==null?null:lookupValue);
						samples.add(sampleName);
						matrix.createFeatureURI(lookupValue, sampleName, UUID.randomUUID().toString());
						
					} else {
						String feature = matrix.getFeatureURI(header.get(col), samples.get(col));
						if (feature != null) {
							ObjectNode column = matrix.getColumn(header.get(col));
							if (isProcessedData(column)) {
								try {
									values.put(feature,Double.parseDouble(value));
								} catch (Exception x) {
									values.put(header.get(col),value);	
								}
							} else {
								String columnType = column.get("type")==null?null:column.get("type").asText();
								if ("probe".equals(columnType)) {
									ObjectNode probe = (ObjectNode)probes;
									probe.put(feature,value);
								} else if ("gene".equals(columnType)) {
									ObjectNode gene = (ObjectNode)genes;
									gene.put(feature,value);
								} else {
									ObjectNode annotation = (ObjectNode)annotations;
									annotation.put(feature,value);
								}
							}
						}
					}
					col++;	
				}
				if (processor!=null)
					if (row==0) processor.header(matrix);
					else processor.process(matrix);
				row++;
				if ((maxrows>0) && (row>maxrows)) break;
			}
			if (processor!=null) processor.footer(matrix);
			return matrix;
		} catch (Exception x) {
			throw x;
		} finally {
			try {breader.close();} catch (Exception x) {}
		}
	}
	
	protected boolean isProcessedData(JsonNode column) {
		JsonNode node = column.get("Processed data");
		return node==null?false:node.asBoolean();
	}
	
	public static Hashtable<String,Hashtable<String,String>> getDataMatrix(Model model) {
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

		Hashtable<String,Hashtable<String,String>> lookup = new Hashtable<String, Hashtable<String,String>>();
		
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query,model);
		ResultSet rs = qe.execSelect();

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			RDFNode node = qs.get("node");
			RDFNode feature = qs.get("feature");
			RDFNode title = qs.get("title");
			RDFNode value = qs.get("value");
			System.out.println(node + "\t" + feature + "\t" + title + "\t" + value.asLiteral().getDouble());
		}	
		return lookup;
	}
}
