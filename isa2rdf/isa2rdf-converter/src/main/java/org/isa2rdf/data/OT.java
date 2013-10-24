package org.isa2rdf.data;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class OT {
	public enum OTClass {
		Compound,
		Conformer,
		Dataset,
		DataEntry,
		Feature,
		ModelConfidenceFeature {
			@Override
			public OntClass createOntClass(OntModel model) {
				OntClass feature = Feature.getOntClass(model);
				OntClass stringFeature = model.createClass(getNS());
				feature.addSubClass(stringFeature);
				return stringFeature;
			}
			@Override
			public void assignType(OntModel model, Individual individual) {
				super.assignType(model, individual);
				individual.addOntClass(Feature.getOntClass(model));
			}			
		},
		ModelPredictionFeature {
			@Override
			public OntClass createOntClass(OntModel model) {
				OntClass feature = Feature.getOntClass(model);
				OntClass stringFeature = model.createClass(getNS());
				feature.addSubClass(stringFeature);
				return stringFeature;
			}
			@Override
			public void assignType(OntModel model, Individual individual) {
				super.assignType(model, individual);
				individual.addOntClass(Feature.getOntClass(model));
			}
		},
		NumericFeature {
			@Override
			public OntClass createOntClass(OntModel model) {
				OntClass feature = Feature.getOntClass(model);
				OntClass stringFeature = model.createClass(getNS());
				feature.addSubClass(stringFeature);
				return stringFeature;
			}			
		},
		NominalFeature {
			@Override
			public OntClass createOntClass(OntModel model) {
				OntClass feature = Feature.getOntClass(model);
				OntClass stringFeature = model.createClass(getNS());
				feature.addSubClass(stringFeature);
				return stringFeature;
			}
		},
		StringFeature {
			@Override
			public OntClass createOntClass(OntModel model) {
				OntClass feature = Feature.getOntClass(model);
				OntClass stringFeature = model.createClass(getNS());
				feature.addSubClass(stringFeature);
				return stringFeature;
			}
			
		},
		TupleFeature {
			@Override
			public OntClass createOntClass(OntModel model) {
				OntClass feature = Feature.getOntClass(model);
				OntClass tupleFeature = model.createClass(getNS());
				feature.addSubClass(tupleFeature);
				return tupleFeature;
			}
			
		},		
		FeatureValue,
		Algorithm,
		Model,
		Parameter,
		Validation,
		ValidationInfo,
		Task,
		ErrorReport,
		OTMaterial;
		public String getNS() {
			return String.format(_NS, toString());
		}
		public OntClass getOntClass(OntModel model) {
			OntClass c = model.getOntClass(getNS());
			return (c==null)?createOntClass(model):c;
		}
		public OntClass createOntClass(OntModel model) {
			return model.createClass(getNS());
		}		
		public void assignType(OntModel model,Individual individual) {
			individual.addOntClass(getOntClass(model));
		}	

	};
	/** <p>The RDF model that holds the vocabulary terms</p> */
	private static Model m_model = ModelFactory.createDefaultModel();
	/** <p>The namespace of the vocabalary as a string ({@value})</p> */
	protected static final String _NS = "http://www.opentox.org/api/1.1#%s";
	public static final String NS = String.format(_NS,"");
	
	public static String getURI() {return NS;}
	/** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    /**
     * Object properties
     */
    public enum OTProperty {
		   	dataEntry,
		    compound ,
		    feature ,
		    values ,
		    hasSource,
		    conformer ,
		    model ,
		    parameters ,
		    report ,
		    algorithm ,
		    dependentVariables ,
		    independentVariables ,
		    predictedVariables,
		    trainingDataset,
		    validationReport ,
		    validation ,
		    hasValidationInfo,
		    validationModel ,
		    validationPredictionDataset ,
		    validationTestDataset,
		    //Nominal features
		    acceptValue,
		    error,
		    smarts,
		    confidenceOf;

		   	public Property createProperty(OntModel jenaModel) {
		   		Property p = jenaModel.getObjectProperty(String.format(_NS, toString()));
		   		return p!= null?p:
		   				jenaModel.createObjectProperty(String.format(_NS, toString()));
		   	}
		   	public String getURI() {
		   		return String.format(_NS, toString());
		   	}
    }
    /**
     * Data properties
     */
    public enum DataProperty {
    	value,
    	units,
    	has3Dstructure,
    	hasStatus,
    	percentageCompleted,
    	resultURI,
    	paramScope,
    	paramValue,
	    errorCode,
	    actor,
	    message,
	    errorDetails,
	    errorCause;    	
	   	public Property createProperty(OntModel jenaModel) {
	   		Property p = jenaModel.getDatatypeProperty(String.format(_NS, toString()));
	   		return p!= null?p:
	   				jenaModel.createDatatypeProperty(String.format(_NS, toString()));
	   	}
	   	public String getURI() {
	   		return String.format(_NS, toString());
	   	}
    };
   
}
