package org.isa2rdf.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import net.idea.opentox.cli.csv.QuotedTokenizer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;


public class qHTSConverter {

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
			BufferedWriter dataWriter = new BufferedWriter(new FileWriter(new File("data.txt")));
			ObjectMapper m = new ObjectMapper();
			ArrayNode root = m.createArrayNode();
			
			ArrayList<String> header = new ArrayList<String>();
			int row = 0;
			while ((line = reader.readLine()) != null) {
				ObjectNode node = m.createObjectNode();
				ArrayNode experiment = m.createArrayNode();
				node.put("experiment", experiment);

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
								((ObjectNode) concentration).put("Assay Name",String.format("C%d",i));
								
								ObjectNode factors = getNode("factors", (ObjectNode)concentration, m);
								
								factors.put("concentration",value);
								isAdded = true;
								break;
							}
							concentrationHeader = String.format("C%d Feature:",i);
							if (header.get(col).startsWith(concentrationHeader)) {
								JsonNode readout = concentration.get("readout");
								if (readout == null) { 
									readout = m.createObjectNode(); 
									((ObjectNode)concentration).put("readout", readout);
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
									ObjectNode factors = getNode("factors", (ObjectNode)experiment.get(e), m);
									factors.put("Compound",value);
								}
								else ((ObjectNode)experiment.get(e)).put(header.get(col),value);
								((ObjectNode)experiment.get(e)).put("name",experimentname);
							}	
						}	
					}
					col++;	
				}
				
				for (int e = 0; e < experiment.size(); e++) {
					((ObjectNode)experiment.get(e)).put("Assay Name",
							experiment.get(e).get("Sample Name").getTextValue() + "-" + experiment.get(e).get("Assay Name").getTextValue()
						);
				}
				
				if (row>0) root.add(node);
				System.out.println(node);
				row++;
			}
			//System.out.println(root);
			reader.close();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
	
	protected ObjectNode getNode(String key, ObjectNode parent, ObjectMapper m) {
		JsonNode node = parent.get(key);
		if (node == null) { node = m.createObjectNode(); parent.put(key,node);}
		return (ObjectNode) node;
	}
}
