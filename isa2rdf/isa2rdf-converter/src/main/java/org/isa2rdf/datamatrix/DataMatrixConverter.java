package org.isa2rdf.datamatrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import net.idea.opentox.cli.csv.QuotedTokenizer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.isa2rdf.data.stax.DatasetRDFWriter;


public class DataMatrixConverter {

	public static void main(String[] args) {
		if (args.length<1) return;
		
		for (String arg: args) {
			DataMatrixConverter q = new DataMatrixConverter();
			try {
				q.writeRDF(arg);
			} catch (Exception x) {
				x.printStackTrace();
			}
		}	
	}
	
	public void writeRDF(String file) throws Exception {
		final DatasetRDFWriter rdfwriter = new DatasetRDFWriter();
		final XMLStreamWriter writer = initWriter(System.out);
		rdfwriter.setOutput(writer);
		try {
			
			DataMatrix matrix = parse(file,new IRowProcessor<DataMatrix>() {
				@Override
				public void header(DataMatrix row) throws Exception {
					rdfwriter.header(writer);
				}
				@Override
				public DataMatrix process(DataMatrix row) throws Exception {
				//	System.out.print(".");
					rdfwriter.process(row);	
					return row;
				}
				@Override
				public void footer(DataMatrix row) throws Exception {
					rdfwriter.footer(writer);	
				}
			});
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
	
	public DataMatrix parse(String arg, IRowProcessor<DataMatrix> processor ) throws Exception {
		BufferedReader reader = null ;
		try {
			String line;
			File file = new File(arg);
			String experimentname= file.getName().replace(".csv", "");
			reader = new BufferedReader(new FileReader(file));
			
			//read config
			InputStream in = getClass().getClassLoader().getResourceAsStream("org/isa2rdf/data/transcriptomics/datamatrix.json");
			DataMatrix matrix = new DataMatrix(in);
			
			//read config completed
			ArrayList<String> header = new ArrayList<String>();
			ArrayList<String> samples = new ArrayList<String>();
			
			int row = 0;
			while ((line = reader.readLine()) != null) {
				ObjectNode genes =  matrix.getGene();
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
							lookupValue = value.substring(0,quote);
							sampleName = value.substring(quote+1,value.length()-1);
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
								ObjectNode gene = (ObjectNode)genes;
								gene.put(feature,value);
							}
						}
					}
					col++;	
				}
				if (processor!=null)
					if (row==0) processor.header(matrix);
					else processor.process(matrix);
				row++;
			}
			if (processor!=null) processor.footer(matrix);
			return matrix;
		} catch (Exception x) {
			throw x;
		} finally {
			try {reader.close();} catch (Exception x) {}
		}
	}
	
	protected boolean isProcessedData(JsonNode column) {
		JsonNode node = column.get("Processed data");
		return node==null?false:node.asBoolean();
	}
}
