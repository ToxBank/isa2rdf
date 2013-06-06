package org.isa2rdf.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.idea.opentox.cli.csv.QuotedTokenizer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;


public class qHTSConverter {
	private String _ASSAY_NAME = "Assay Name";
	private String _SAMPLE_NAME = "Sample Name";
	private String _FACTORS = "factors";
	private String _READOUT = "readout";
	private String _NAME = "name";
	private String _EXPERIMENT = "experiment";
	
	
	HashMap<String,BufferedWriter> dataWriters = new HashMap<String, BufferedWriter>();
	
	public static void main(String[] args) {
		qHTSConverter q = new qHTSConverter();
		q.run(args);
	}
	public void run(String[] args) {
		if (args.length<1) return;
		try {
			String line;
			File file = new File(args[0]);
			String experimentname= file.getName().replace(".csv", "");
			BufferedReader reader = new BufferedReader(new FileReader(file));
			BufferedWriter studyWriter = new BufferedWriter(new FileWriter(new File("a_assay.txt")));
			BufferedWriter assayWriter = new BufferedWriter(new FileWriter(new File("a_assay.txt")));

			ObjectMapper m = new ObjectMapper();
			ArrayNode root = m.createArrayNode();
			
			ArrayList<String> header = new ArrayList<String>();
			int row = 0;
			while ((line = reader.readLine()) != null) {
				ObjectNode node = m.createObjectNode();
				node.put(_NAME,experimentname);
				ArrayNode experiment = m.createArrayNode();
				node.put(_EXPERIMENT, experiment);

				QuotedTokenizer st = new QuotedTokenizer(line,',');
				int col = 0;
				while (st.hasMoreTokens()) {
					String value = st.nextToken().trim();
					if (row==0) header.add(value);
					else {
						boolean isAdded = false;
						for (int i=1; i < 17; i++) {
							String concentrationHeader = String.format("Concentration %d",i);

							JsonNode concentration = experiment.get(i-1);
							if (concentration == null) { concentration = m.createObjectNode(); experiment.insert(i-1, concentration);}
							
							if (header.get(col).equals(concentrationHeader)) {
								((ObjectNode) concentration).put(_ASSAY_NAME,String.format("C%d",i));
								
								ObjectNode factors = getNode(_FACTORS, (ObjectNode)concentration, m);
								
								factors.put("concentration",value);
								isAdded = true;
								break;
							}
							concentrationHeader = String.format("C%d Feature:",i);
							if (header.get(col).startsWith(concentrationHeader)) {
								JsonNode readout = concentration.get(_READOUT);
								if (readout == null) { 
									readout = m.createObjectNode(); 
									((ObjectNode)concentration).put(_READOUT, readout);
								}
								
								boolean isChannelAdded = false;
								for (int c=1; c < 4; c++) {
									String channelTitle = String.format("Ch%d",c);
									if (header.get(col).endsWith(channelTitle)) {
										ObjectNode channel = getNode(channelTitle, (ObjectNode)readout, m);
										channel.put(header.get(col).replace(concentrationHeader, "").trim(),value);
										isChannelAdded = true;
										break;
									}									
								}
								if (!isChannelAdded) {
									ObjectNode channel = getNode("Ch1", (ObjectNode)readout, m);
									channel.put(header.get(col).replace(concentrationHeader, "").trim(),value);
								}	
								
								isAdded = true;
								break;
							}
						}
						if (!isAdded) {
							for (int e = 0; e < experiment.size(); e++) {
								if (("Compoundname").equals(header.get(col))) {
									ObjectNode factors = getNode(_FACTORS, (ObjectNode)experiment.get(e), m);
									factors.put("Compound",value);
								}
								else ((ObjectNode)experiment.get(e)).put(header.get(col),value);
							}	
						}	
					}
					col++;	
				}
				
				for (int e = 0; e < experiment.size(); e++) {
					((ObjectNode)experiment.get(e)).put(_ASSAY_NAME,
							experiment.get(e).get(_SAMPLE_NAME).getTextValue() + "-" + experiment.get(e).get(_ASSAY_NAME).getTextValue()
						);
				}
				if (row>0) {
					root.add(node);
					writeDataFile(node);
				}

				row++;
			}
			//System.out.println(root);
			reader.close();
		} catch (Exception x) {
			x.printStackTrace();
		} finally {
			for (BufferedWriter w : dataWriters.values()) try {w.close();} catch (Exception x) {}
		}
	}
	
	protected void writeDataFile(ObjectNode node) throws Exception {
		BufferedWriter dataWriter = null;
		ArrayNode experiment = (ArrayNode)node.get(_EXPERIMENT);
		for (int e = 0; e < experiment.size(); e++) {
			ObjectNode sample = ((ObjectNode)experiment.get(e));
			Iterator<String> fields = sample.get(_READOUT).getFieldNames();
			while (fields.hasNext()) {
				String field = fields.next();
				dataWriter = dataWriters.get(field);
				if (dataWriter==null) {
					dataWriter = new BufferedWriter(new FileWriter(new File(String.format("data_%s_%s.txt",node.get(_NAME).getTextValue(),field))));
					dataWriters.put(field,dataWriter);
				}
				dataWriter.write(String.format("%s\t", sample.get(_ASSAY_NAME).getTextValue()));
				
				Iterator<Entry<String,JsonNode>> values = sample.get(_READOUT).get(field).getFields();
				while (values.hasNext()) {
					Entry<String,JsonNode> value = values.next();
					dataWriter.write(String.format("%s\t%s\t", value.getKey() ,value.getValue().getTextValue()));
				}
				dataWriter.write("\n");
				dataWriter.flush();
			}
		}
	}
	
	
	protected ObjectNode getNode(String key, ObjectNode parent, ObjectMapper m) {
		JsonNode node = parent.get(key);
		if (node == null) { node = m.createObjectNode(); parent.put(key,node);}
		return (ObjectNode) node;
	}
}
