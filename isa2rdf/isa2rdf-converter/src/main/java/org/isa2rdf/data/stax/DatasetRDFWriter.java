package org.isa2rdf.data.stax;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonNode;
import org.isa2rdf.data.OT;
import org.isa2rdf.data.OT.DataProperty;
import org.isa2rdf.data.OT.OTClass;
import org.isa2rdf.data.OT.OTProperty;
import org.isa2rdf.datamatrix.DataMatrix;
import org.isa2rdf.datamatrix.IRowProcessor;
import org.isa2rdf.model.ISA;

import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

public class DatasetRDFWriter extends AbstractStaxRDFWriter<DataMatrix> implements IRowProcessor<DataMatrix>{
	
	protected static Logger logger = Logger.getLogger("DatasetRDFWriter");
	/**
	 * 
	 */
	private static final long serialVersionUID = -1825074197173628894L;
	protected String datasetIndividual = null;
	protected DataMatrix matrix;
	public DatasetRDFWriter() {
		super();
		datasetIndividual = null;
	}
	public String getDatasetIndividual() {
		return datasetIndividual;
	}

	public void setDatasetIndividual(String datasetIndividual) {
		this.datasetIndividual = datasetIndividual;
	}
	
	@Override
	public void footer(DataMatrix row) throws Exception {
		
	}
	@Override
	public void header(DataMatrix row) throws Exception {
	}
	@Override
	public DataMatrix process(DataMatrix item) throws Exception {
		matrix = item;
		if (datasetIndividual == null) try {
			getOutput().writeStartElement(OT.NS,"Dataset");
			datasetIndividual = createDatasetURI(item);
			if (datasetIndividual!= null) getOutput().writeAttribute(RDF.getURI(),"about",datasetIndividual);

		} catch (Exception x) {
			logger.log(Level.WARNING,x.getMessage(),x);
		}

		try {

			getOutput().writeStartElement(OT.NS,"dataEntry"); //property
			getOutput().writeStartElement(OT.NS,"DataEntry");  //object
			
			Iterator<String> probes = item.getProbe().getFieldNames();
			while (probes.hasNext()) {
				String uri = createProbeURI(item,probes.next());
				getOutput().writeStartElement(ISA.URI,"hasProbe"); //property
				getOutput().writeStartElement(ISA.URI,"MicroarrayProbe"); //property
				getOutput().writeAttribute(RDF.getURI(),"about",uri);
				getOutput().writeEndElement();
				getOutput().writeEndElement();
			}
			
			Iterator<String> genes = item.getGene().getFieldNames();
			while (genes.hasNext()) {
				String uri = createGeneURI(item,genes.next());
				getOutput().writeStartElement(SKOS,"closeMatch"); //property
				getOutput().writeStartElement(ISA.URI,"Gene"); //todo
				getOutput().writeAttribute(RDF.getURI(),"about",uri);
				getOutput().writeEndElement();
				getOutput().writeEndElement();
			}			

			Iterator<Entry<String,JsonNode>> features = item.getFeatures().getFields();
			while (features.hasNext()) {
				Entry<String,JsonNode> feature = features.next();
				JsonNode f = (feature.getValue()).get("export");
				if ((f==null)  || !f.asBoolean()) continue;
				JsonNode value = item.getValues().get(feature.getKey());
				if (value == null) continue;
				try {
					getOutput().writeStartElement(OT.NS,"values"); //property
					getOutput().writeStartElement(OT.NS,"FeatureValue"); //property
					
					getOutput().writeStartElement(OT.NS,"feature"); //feature
					
					String featureURI = createFeatureURI(feature);
					getOutput().writeAttribute(RDF.getURI(),"resource",featureURI);
		
					getOutput().writeEndElement(); //feature
					
					getOutput().writeStartElement(OT.NS,"value"); //value

					if (value.isDouble()) {
						getOutput().writeAttribute(RDF.getURI(),"datatype","http://www.w3.org/2001/XMLSchema#double");
						getOutput().writeCharacters(Double.toString(value.asDouble()));
					} else if (value.isInt()) {
						getOutput().writeAttribute(RDF.getURI(),"datatype","http://www.w3.org/2001/XMLSchema#int");
						getOutput().writeCharacters(Integer.toString(value.asInt()));
					} else if (value.isBoolean()) {
						getOutput().writeAttribute(RDF.getURI(),"datatype","http://www.w3.org/2001/XMLSchema#boolean");
						getOutput().writeCharacters(Boolean.toString(value.asBoolean()));
					} else {
						getOutput().writeAttribute(RDF.getURI(),"datatype","http://www.w3.org/2001/XMLSchema#string");
						getOutput().writeCharacters(value.asText());
					}
					
					getOutput().writeEndElement(); //value					

				} catch (Exception x) {
					logger.log(Level.WARNING,x.getMessage(),x);
				} finally {

					
					getOutput().writeEndElement(); //FeatureValue
					getOutput().writeEndElement();//values
					
				}
	
				//if (p.isNominal())
				//	feature.addProperty(OTProperty.acceptValue.createProperty(getJenaModel()), value.toString());
					
			}

			return item;
		} catch (Exception x) {
			throw x;
		} finally {
			try { getOutput().writeEndElement(); } catch (Exception x) {}
			try { getOutput().writeEndElement(); } catch (Exception x) {}
		}
	
	}

	public void header(javax.xml.stream.XMLStreamWriter writer) {
		super.header(writer);
		try {
			writeClassTriple(writer, OTClass.Dataset);
			writeClassTriple(writer, OTClass.DataEntry);
			writeClassTriple(writer, OTClass.Feature);
			writeClassTriple(writer, OTClass.FeatureValue);
			writeClassTriple(writer, OTClass.Compound);
			
			writeObjectPropertyTriple(writer, OTProperty.compound);
			writeObjectPropertyTriple(writer, OTProperty.dataEntry);
			writeObjectPropertyTriple(writer, OTProperty.values);
			writeObjectPropertyTriple(writer, OTProperty.feature);
			writeObjectPropertyTriple(writer, OTProperty.hasSource);
			writeObjectPropertyTriple(writer, OTProperty.acceptValue);
			
			writeDataPropertyTriple(writer, DataProperty.units);
			writeDataPropertyTriple(writer, DataProperty.value);
			
			writeAnnotationPropertyTriple(writer,"http://purl.org/dc/elements/1.1/description");
			writeAnnotationPropertyTriple(writer,"http://purl.org/dc/elements/1.1/creator");
			writeAnnotationPropertyTriple(writer,"http://purl.org/dc/elements/1.1/type");
			writeAnnotationPropertyTriple(writer,"http://purl.org/dc/elements/1.1/title");
			



		} catch (Exception x) {
			logger.log(Level.WARNING,x.getMessage(),x);
		}
	};
	
	public void footer(javax.xml.stream.XMLStreamWriter writer) {
		
		try {
			writer.writeEndElement();
		} catch (Exception x) {
			logger.log(Level.WARNING,x.getMessage(),x);
		}
	
			//write properties
		Iterator<Entry<String,JsonNode>> features = matrix.getFeatures().getFields();
		while (features.hasNext()) {
			Entry<String,JsonNode> feature = features.next();
			JsonNode f = (feature.getValue()).get("export");
			if ((f==null)  || !f.asBoolean()) continue;			
			try {
				getOutput().writeStartElement(OT.NS,"Feature"); //feature
				getOutput().writeAttribute(RDF.getURI(),"about",createFeatureURI(feature));
				JsonNode isnumeric= feature.getValue().get("isNumeric");
				if (isnumeric!=null && isnumeric.asBoolean()) {
					//NominalFeature
					getOutput().writeStartElement(RDF.getURI(),"type"); //feature
					getOutput().writeAttribute(RDF.getURI(),"resource","http://www.opentox.org/api/1.1#NumericFeature");
					getOutput().writeEndElement();
				}
		
				writeHasSource(feature);
				
				getOutput().writeStartElement(OWL.getURI(),"sameAs"); //feature
				getOutput().writeAttribute(RDF.getURI(), "resource",feature.getValue().get("sameAs").asText());
				getOutput().writeEndElement();						
				/*
				if (p.getUnits()!=null) {
					getOutput().writeStartElement(OT.NS,"units"); //feature
					getOutput().writeCharacters(p.getUnits());	
					getOutput().writeEndElement();
				}
				*/
				
				getOutput().writeStartElement(DC.getURI(),"title"); //feature
				getOutput().writeCharacters(feature.getValue().get("title").asText());
				getOutput().writeEndElement();	
			} 
			catch (Exception x) {
				x.printStackTrace();
			}
			finally {
				try {getOutput().writeEndElement(); } catch (Exception x) {}
			}
		}
			super.footer(writer);
			
	}
	
	/**
	<pre>
	    <ot:hasSource>
	      <ot:Algorithm rdf:about="algorithm/ambit2.descriptors.PKASmartsDescriptor"/>
	    </ot:hasSource>
	</pre>
		 * @param item
		 * @return
		 * @throws Exception
		 */
		protected String writeHasSource(Entry<String,JsonNode> item) throws Exception {
			String otclass = null;
			String namespace = null;
			String uri = item.getValue().get("source").get("URI").asText();
			getOutput().writeStartElement(OT.NS,"hasSource"); //feature
			
			if (otclass==null) {
				
				getOutput().writeCharacters(uri); //TODO this is wrong, should be a resource 
			} else {
				getOutput().writeStartElement(namespace,otclass); //algorithm or model
				getOutput().writeAttribute(RDF.getURI(),"about",uri);
				getOutput().writeEndElement();
			}
			getOutput().writeEndElement();						
			return uri;
		}


		public void close() {
			try { getOutput().close();} catch (Exception x) {}
		}
		
		protected String createDatasetURI(DataMatrix matrix) {
			return String.format("http://example.org/%s", UUID.randomUUID().toString());
		}
		
		protected String createProbeURI(DataMatrix matrix,String probeType) {
			return String.format("%s%s/%s", ISA.URI,probeType,matrix.getProbe().get(probeType).asText());
		}
		

		protected String createGeneURI(DataMatrix matrix,String gene) {
			return String.format("%s%s/%s", ISA.URI,gene,matrix.getGene().get(gene).asText());
		}
		protected String createFeatureURI(Entry<String,JsonNode> feature) {
			return String.format("%s/%s", feature.getValue().get("sameAs").asText(),feature.getKey());
		}		
}
