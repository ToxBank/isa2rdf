package org.isa2rdf.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
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
	
	
	enum _protocol {
		ROS {
			@Override
			public String getProtocol() {
				return "oxidative stress staining kit";
			}
		},
		STEATOSIS {
			@Override
			public String getProtocol() {
				return "oxidative stress staining kit";
			}
		};
		public abstract String getProtocol();
	}
	private String _FACTORS = "factors";
	private String _READOUT = "readout";
	private String _NAME = "name";
	private String _EXPERIMENT = "experiment";
	
	//study
	private String _SOURCE_NAME = "Source Name";
	private String _CHARACTERISTIC = "Characteristics[%s]";
	
	//assay
	private String _SAMPLE_NAME = "Sample Name";
	private String _PROTOCOL_REF = "Protocol REF";
	private String _PARAMETER_VALUE = "Parameter Value[%s]";
	private String _ASSAY_NAME = "Assay Name";
	private String _IMAGE_FILE = "Image File";
	private String _RAW_DATA_FILE = "Raw Data File";
	private String _FACTOR_VALUE = "Factor Value[%s]";
	private String _UNIT = "Unit";

	
	HashMap<String,BufferedWriter> studyWriters = new HashMap<String, BufferedWriter>();
	HashMap<String,BufferedWriter> assayWriters = new HashMap<String, BufferedWriter>();
	HashMap<String,BufferedWriter> dataWriters = new HashMap<String, BufferedWriter>();

	
	public static void main(String[] args) {
		if (args.length<1) return;
		
		for (String arg: args) {
			qHTSConverter q = new qHTSConverter();
			q.run(arg);
		}	
	}
	public void run(String arg) {
		
		try {
			String line;
			File file = new File(arg);
			String experimentname= file.getName().replace(".csv", "");
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			ObjectMapper m = new ObjectMapper();
			InputStream in = getClass().getClassLoader().getResourceAsStream("org/isa2json/qhts.json");
			JsonNode protocols = m.readTree(in);

			in.close();
			ArrayNode root = m.createArrayNode();

			ArrayList<String> header = new ArrayList<String>();
			int row = 0;
			while ((line = reader.readLine()) != null) {
				ObjectNode node = m.createObjectNode();
				
				String protocolRef = experimentname.startsWith("ROS")?"ROS":experimentname.startsWith("STEATOSIS")?"STEATOSIS":"UNKNOWN";
				
				node.put(_NAME,experimentname);
				node.put("protocol",protocols.get(protocolRef));
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
					writeStudyFile(node);
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
			for (BufferedWriter w : assayWriters.values()) try {w.close();} catch (Exception x) {}
			for (BufferedWriter w : studyWriters.values()) try {w.close();} catch (Exception x) {}
		}
	}
	//Source Name	Characteristics[plate identifier]	Characteristics[plate well]	Characteristics[organism]	Characteristics[cell line]	
	//Characteristics[cell line provider]	Protocol REF	Parameter Value[cell culture medium]	Parameter Value[CO2 concentration]	
	//Parameter Value[incubation temperature]	Sample Name	Factor Value[compound]	Factor Value[concentration]	Unit	Factor Value[duration of exposure]	Unit

	protected void writeStudyFile(ObjectNode node) throws Exception {
		String key = node.get(_NAME).getTextValue();
		BufferedWriter studyWriter = studyWriters.get(key);
		if (studyWriter==null) {
			studyWriter = new BufferedWriter(new FileWriter(new File(String.format("s_%s.txt",key))));
			studyWriter.write(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
					_SOURCE_NAME,
					String.format(_CHARACTERISTIC,"plate identifier"),
					String.format(_CHARACTERISTIC,"well"),
					String.format(_CHARACTERISTIC,"organism"),
					String.format(_CHARACTERISTIC,"cell line"),
					String.format(_CHARACTERISTIC,"cell line provider"),
					_PROTOCOL_REF,
					String.format(_PARAMETER_VALUE,"cell culture medium"),
					String.format(_PARAMETER_VALUE,"CO2 concentration"),
					String.format(_PARAMETER_VALUE,"incubation temperature"),
					"Performer",
					"Date",
					_SAMPLE_NAME,
					String.format(_FACTOR_VALUE,"compound"),
					String.format(_FACTOR_VALUE,"concentration"),
					_UNIT,
					String.format(_FACTOR_VALUE,"duration of exposure"),
					_UNIT
			));
			studyWriters.put(key,studyWriter);
		}
		
		ArrayNode experiment = (ArrayNode)node.get(_EXPERIMENT);
		for (int e = 0; e < experiment.size(); e++) {
			ObjectNode sample = (ObjectNode)experiment.get(e);
			ObjectNode factors = (ObjectNode)sample.get(_FACTORS);

			studyWriter.write(node.get(_NAME).getTextValue());
			studyWriter.write("\t");
			studyWriter.write(sample.get(_SAMPLE_NAME).getTextValue().replace(sample.get("Position").getTextValue(),"").replace("-", "")); //plate
			studyWriter.write("\t");
			studyWriter.write(sample.get("Position").getTextValue());
			studyWriter.write("\t");
			studyWriter.write("Homo sapiens"); //organism
			studyWriter.write("\t");
			studyWriter.write("HepRG"); //cell
			studyWriter.write("\t");
			studyWriter.write("Biopredic International");//cell line prov
			studyWriter.write("\t");
			studyWriter.write("cell growth");//protocol
			studyWriter.write("\t");
			studyWriter.write("William’s E medium supplemented with 10% serum, 1% L-glutamine, 1% penicillin/streptomycin, 5 \u00B5g/ml bovine insulin and 50 µM hydrocortisone hemisuccinate");//protocol
			studyWriter.write("\t");
			studyWriter.write("5%");//protocol
			studyWriter.write("\t");
			studyWriter.write("37 Degree C");//protocol
			studyWriter.write("\t");
			studyWriter.write(hackPerformer(key)); //performer
			studyWriter.write("\t");
			studyWriter.write(hackDate(key)); //date
			studyWriter.write("\t");
			studyWriter.write(sample.get(_ASSAY_NAME).getTextValue());
			studyWriter.write("\t");
			studyWriter.write(factors.get("Compound").getTextValue());//comp
			studyWriter.write("\t");
			studyWriter.write(factors.get("concentration").getTextValue());//concen
			studyWriter.write("\t");
			studyWriter.write("Molar");//unit
			studyWriter.write("\t");
			studyWriter.write("72");//exposure
			studyWriter.write("\t");
			studyWriter.write("hour");//unit
			studyWriter.write("\n");
		}
		studyWriter.flush();
	}
	
	protected void writeAssayFile(ObjectNode node) throws Exception {
		String key = node.get(_NAME).getTextValue();
		BufferedWriter assayWriter = assayWriters.get(key);
		if (assayWriter==null) {
			assayWriter = new BufferedWriter(new FileWriter(new File(String.format("a_%s.txt",key))));
			assayWriter.write(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
					_SAMPLE_NAME,
					_PROTOCOL_REF,
					String.format(_PARAMETER_VALUE,"dye"),
					String.format(_PARAMETER_VALUE,"emission wavelength"),
					String.format(_PARAMETER_VALUE,"cellular component"),
					String.format(_PARAMETER_VALUE,"software"),
					_ASSAY_NAME,
					_IMAGE_FILE,
					_RAW_DATA_FILE
			));
		
			/*
			assayWriter.write(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
					_SAMPLE_NAME,
					_PROTOCOL_REF,
					String.format(_PARAMETER_VALUE,"dye"),
					String.format(_PARAMETER_VALUE,"emission wavelength"),
					String.format(_PARAMETER_VALUE,"cellular component"),
					String.format(_PARAMETER_VALUE,"software"),
					"Performer",
					"Date",
					_ASSAY_NAME,
					_IMAGE_FILE,
					_RAW_DATA_FILE,
					String.format(_FACTOR_VALUE,"compound"),
					String.format(_FACTOR_VALUE,"concentration"),
					_UNIT,
					String.format(_FACTOR_VALUE,"duration of exposure"),
					_UNIT
			));
			*/
			assayWriters.put(key,assayWriter);
		}

		ArrayNode experiment = (ArrayNode)node.get(_EXPERIMENT);
		for (int e = 0; e < experiment.size(); e++) {
			ObjectNode sample = (ObjectNode)experiment.get(e);
			
			Iterator<String> fields = sample.get(_READOUT).getFieldNames();
			while (fields.hasNext()) {
				String field = fields.next();
				JsonNode protocolapp = node.get("protocol").get("protocol_application").get(field);
				String dataFilename = String.format("data_%s_%s.txt",node.get(_NAME).getTextValue(),field);
				assayWriter.write(sample.get(_ASSAY_NAME).getTextValue());
				assayWriter.write("\t");
				assayWriter.write(node.get("protocol").get("ref").getTextValue()); //protocol
				assayWriter.write("\t");
				assayWriter.write(protocolapp.get("dye").getTextValue()); //dye
				assayWriter.write("\t");
				assayWriter.write(protocolapp.get("emission wavelength").getTextValue()); //wavelen
				assayWriter.write("\t");
				assayWriter.write(protocolapp.get("cellular component").getTextValue()); //cell
				assayWriter.write("\t");
				assayWriter.write(protocolapp.get("software").getTextValue()); //cell
				assayWriter.write("\t");
				assayWriter.write(String.format("%s-%s", sample.get(_ASSAY_NAME).getTextValue(),field));
				assayWriter.write("\t");
				assayWriter.write("");//img file
				assayWriter.write("\t");
				assayWriter.write(dataFilename);//data file
				/*
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
				*/
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
			ObjectNode factors = (ObjectNode)sample.get(_FACTORS);
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
				dataWriter.write(factors.get("Compound").getTextValue());//comp
				dataWriter.write("\t");
				dataWriter.write(factors.get("concentration").getTextValue());//concen
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
	protected String hackDate(String name) {
		if (name.indexOf("experiment1")>0) return "10/8/2010";
		else if (name.indexOf("experiment6")>0) return "5/18/2011";
		else return "";
	}
	protected String hackPerformer(String name) {
		if (name.indexOf("experiment1")>0) return "Milena/Taina";
		else if (name.indexOf("experiment2")>0) return "Milena";
		else if (name.indexOf("experiment4")>0) return "Milena";
		else return "Milena/Georgina";
	}
}
