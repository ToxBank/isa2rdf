package org.isa2rdf.datamatrix;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

public class DataMatrix {
	protected ObjectNode json;
	protected ObjectMapper m;

	public DataMatrix(InputStream in) throws JsonProcessingException, IOException {
		m = new ObjectMapper();
		this.json = (ObjectNode)m.readTree(in);
		in.close();
	}
	
	public DataMatrix(ObjectNode json,  ObjectMapper m) {
		this.json = json;
		this.m = m;
	}
	
	public ObjectNode getValues() {
		return (ObjectNode)json.get("row").get("values");
	}
	public ObjectNode getGene() {
		return (ObjectNode)json.get("row").get("gene");
	}	
	public ObjectNode getFeatures() {
		return (ObjectNode)json.get("feature");
	}	
	public ObjectNode getColumns() {
		return (ObjectNode)json.get("columns");
	}
	public ObjectNode getColumn(String key) {
		return (ObjectNode)json.get("columns").get(key);
	}
	/**
	 * Adds feature ID
	 * @param key
	 * @param sampleName
	 * @param URI
	 * @return
	 */
	public String createFeatureURI(String key, String sampleName, String URI) {
		if (key==null) return null;
		if (sampleName==null) return key;
		ObjectNode column = getColumn(key);
		if (column!=null) {
			ObjectNode samples = (ObjectNode)column.get("samples");
			if (samples !=null) {
				JsonNode uri = samples.get(sampleName);
				if (uri==null) { 
					samples.put(sampleName, URI); 
					// store 
					ObjectNode features = (ObjectNode)json.get("feature");
					ObjectNode feature = m.createObjectNode();
					feature.put("title",String.format("%s[%s]", key,sampleName));
					feature.put("isNumeric",Boolean.TRUE);
					feature.put("sameAs",column.get("sameAs"));
					ObjectNode source = m.createObjectNode();
					source.put("URI", sampleName);
					source.put("type", "AssayName");
					feature.put("source",source);
					JsonNode export = column.get("export");
					feature.put("export",export==null?Boolean.FALSE:export.getBooleanValue());					
					features.put(URI,feature);
					return URI; 
				}
				else return uri.asText();
			}
		}
		return null;
	}
	
	/**
	 * Returns feature ID
	 * @param key
	 * @param sampleName
	 * @return
	 */
	public String getFeatureURI(String key, String sampleName) {
		if (key==null) return null;
		if (sampleName==null) return key;
		ObjectNode column = getColumn(key);
		if (column!=null) try {
			return column.get("samples").get(sampleName).asText();
		} catch (Exception x) {return null;}
		return null;
	}
	@Override
	public String toString() {
		return json.toString();
	}
	
}
