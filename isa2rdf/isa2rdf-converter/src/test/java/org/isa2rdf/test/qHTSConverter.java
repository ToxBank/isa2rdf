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
	
	private String _PROTOCOL_REF = "experiment";
	private String _PARAMETER_VALUE = "Parameter Value[%s]";
	private String _IMAGE_FILE = "Image File";
	private String _RAW_DATA_FILE = "Raw Data File";
	private String _FACTOR_VALUE = "Factor Value[%s]";
	private String _UNIT = "Unit";

	
	HashMap<String,BufferedWriter> studyWriters = new HashMap<String, BufferedWriter>();
	HashMap<String,BufferedWriter> assayWriters = new HashMap<String, BufferedWriter>();
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
					writeAssayFile(node);
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
	
	protected void writeAssayFile(ObjectNode node) throws Exception {
		String key = node.get(_NAME).getTextValue();
		BufferedWriter assayWriter = assayWriters.get(key);
		if (assayWriter==null) {
			assayWriter = new BufferedWriter(new FileWriter(new File(String.format("a_%s.txt",key))));
			assayWriter.write(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
					_SAMPLE_NAME,
					_PROTOCOL_REF,
					String.format(_PARAMETER_VALUE,"dye"),
					String.format(_PARAMETER_VALUE,"emission wavelength"),
					String.format(_PARAMETER_VALUE,"cellular component"),
					String.format(_PARAMETER_VALUE,"software"),
					_ASSAY_NAME,
					_IMAGE_FILE,
					_RAW_DATA_FILE,
					String.format(_FACTOR_VALUE,"compound"),
					String.format(_FACTOR_VALUE,"concentration"),
					_UNIT,
					String.format(_FACTOR_VALUE,"duration of exposure"),
					_UNIT
			));
			assayWriters.put(key,assayWriter);
		}
		
		ArrayNode experiment = (ArrayNode)node.get(_EXPERIMENT);
		for (int e = 0; e < experiment.size(); e++) {
			ObjectNode sample = (ObjectNode)experiment.get(e);
			ObjectNode factors = (ObjectNode)sample.get(_FACTORS);
			Iterator<String> fields = sample.get(_READOUT).getFieldNames();
			while (fields.hasNext()) {
				String field = fields.next();
				String dataFilename = String.format("data_%s_%s.txt",node.get(_NAME).getTextValue(),field);
				assayWriter.write(sample.get(_SAMPLE_NAME).getTextValue());
				assayWriter.write("\t");
				assayWriter.write("TODO"); //protocol
				assayWriter.write("\t");
				assayWriter.write("TODO"); //dye
				assayWriter.write("\t");
				assayWriter.write("TODO"); //wavelen
				assayWriter.write("\t");
				assayWriter.write("TODO"); //cell
				assayWriter.write("\t");
				assayWriter.write("TODO");//soft
				assayWriter.write("\t");
				assayWriter.write(String.format("%s-%s", sample.get(_ASSAY_NAME).getTextValue(),field));
				assayWriter.write("\t");
				assayWriter.write("");//img file
				assayWriter.write("\t");
				assayWriter.write(dataFilename);//data file
				assayWriter.write("\t");
				assayWriter.write(factors.get("Compound").getTextValue());//concen
				assayWriter.write("\t");
				assayWriter.write(factors.get("concentration").getTextValue());//concen
				assayWriter.write("\t");
				assayWriter.write("TODO");//unit
				assayWriter.write("\t");
				assayWriter.write("TODO");//exposure
				assayWriter.write("\t");
				assayWriter.write("TODO");//unit
				assayWriter.write("\n");
			}
		}
		assayWriter.flush();
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
