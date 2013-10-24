package org.isa2rdf.datamatrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;

import net.idea.opentox.cli.csv.QuotedTokenizer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

public class DataMatrixConverter {

	public static void main(String[] args) {
		if (args.length<1) return;
		
		for (String arg: args) {
			DataMatrixConverter q = new DataMatrixConverter();
			try {
				q.parse(arg,new IRowProcessor<DataMatrix>() {
					
					@Override
					public void process(DataMatrix row) {
						System.out.println(row.getValues());
					}
				});
			} catch (Exception x) {
				x.printStackTrace();
				System.exit(-1);
			}
		}	
	}
	public int parse(String arg, IRowProcessor<DataMatrix> processor ) throws Exception {
		BufferedReader reader = null ;
		try {
			String line;
			File file = new File(arg);
			String experimentname= file.getName().replace(".csv", "");
			reader = new BufferedReader(new FileReader(file));
			
			//read config
			ObjectMapper m = new ObjectMapper();
			InputStream in = getClass().getClassLoader().getResourceAsStream("org/isa2rdf/data/transcriptomics/datamatrix.json");
			DataMatrix headers = new DataMatrix((ObjectNode)m.readTree(in));
			in.close();
			
			//read config completed
			ArrayList<String> header = new ArrayList<String>();
			ArrayList<String> samples = new ArrayList<String>();
			
			int row = 0;
			while ((line = reader.readLine()) != null) {
				ObjectNode genes =  headers.getGene();
				ObjectNode values =  headers.getValues();
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
						ObjectNode column = headers.getColumn(lookupValue);
						header.add(column==null?null:lookupValue);
						samples.add(sampleName);
					} else {
						ObjectNode column = headers.getColumn(header.get(col));
						if (column !=null) {
							if (isProcessedData(column)) {
								ObjectNode sample = (ObjectNode)values.get(samples.get(col));
								if (sample==null) {
									sample = m.createObjectNode();
									values.put(samples.get(col), sample);
								}
								try {
									sample.put(header.get(col),Double.parseDouble(value));
								} catch (Exception x) {
									sample.put(header.get(col),value);	
								}
							} else {
								ObjectNode gene = (ObjectNode)genes;
								gene.put(header.get(col),value);
							}
						}
					}
					col++;	
				}
				if (processor!=null) processor.process(headers);
				row++;
			}
			
			return row;
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
