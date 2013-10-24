package org.isa2rdf.datamatrix;

import org.codehaus.jackson.node.ObjectNode;

public class DataMatrix {
	protected ObjectNode json;
	
	public DataMatrix(ObjectNode json) {
		this.json = json;
	}
	
	public ObjectNode getValues() {
		return (ObjectNode)json.get("row").get("values");
	}
	public ObjectNode getGene() {
		return (ObjectNode)json.get("row").get("gene");
	}	
	public ObjectNode getColumns() {
		return (ObjectNode)json.get("columns");
	}
	public ObjectNode getColumn(String key) {
		return (ObjectNode)json.get("columns").get(key);
	}
}
