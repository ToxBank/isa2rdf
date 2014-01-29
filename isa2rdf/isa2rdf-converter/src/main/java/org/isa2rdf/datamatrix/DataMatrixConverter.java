package org.isa2rdf.datamatrix;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.UUID;

import javax.xml.stream.XMLStreamWriter;

import net.idea.opentox.cli.csv.QuotedTokenizer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.isa2rdf.data.stax.DatasetRDFWriter;


public class DataMatrixConverter extends AbstractDataMatrixConverter {
	public DataMatrixConverter(String datatype,Hashtable<String,String> lookup,String investigationURI) {
		super(datatype,lookup,investigationURI);
	}

	
	public void writeRDF(Reader reader,String experimentname, final int maxrows, OutputStream out) throws Exception {
		final DatasetRDFWriter rdfwriter = new DatasetRDFWriter(investigationURI, lookup);
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
					//System.out.println(row);
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


	public DataMatrix parse(Reader reader, String filename, IRowProcessor<DataMatrix> processor , int maxrows) throws Exception {
		BufferedReader breader = null ;
		try {
			String line;

			breader = new BufferedReader(reader);
			
			//read config
			InputStream in = getClass().getClassLoader().getResourceAsStream(String.format("org/isa2rdf/data/%s.json",datatype));
			if (in==null) throw new Exception("Unsupported data type "+ datatype); 
			DataMatrix matrix = new DataMatrix(filename,in);

			
			//read config completed
			ArrayList<String> header = new ArrayList<String>();
			ArrayList<String> samples = new ArrayList<String>();
			
			int row = 0;
			while ((line = breader.readLine()) != null) {
				ObjectNode probes =  matrix.getProbe();
				ObjectNode compounds =  matrix.getCompound();
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
						} else if (matrix.getColumn("")!=null) {
							lookupValue = "";
							sampleName = value;
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
								} else if ("compound".equals(columnType)) {
									ObjectNode compound = (ObjectNode)compounds;
									compound.put(feature,value);									
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

}
