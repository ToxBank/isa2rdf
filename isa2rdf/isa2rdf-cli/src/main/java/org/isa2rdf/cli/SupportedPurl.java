package org.isa2rdf.cli;

import org.isa2rdf.model.ISA;


public enum SupportedPurl {
	GO,
	OBO,
	BIBO,
	NCBITaxon {
		@Override
		public String getPURL() {
			return "http://purl.obolibrary.org/obo";
		}
		public String getEntry( String source_acc, String acc) {
			return String.format("%s/%s",getPURL(),acc.replace("obo:",""));
		}
	},
	CHEBI {
	},
	EFO {
		@Override
		public String getPURL() {
			return  "http://www.ebi.ac.uk/efo";
		}
	},
	BAO {

	},
	QIBO {
		@Override
		public String getPURL() {
			return "http://purl.bioontology.org/ontology/QIBO";
		}
	},
	OBI {
		@Override
		public String getPURL() {
			return "http://purl.org/obo/owl/OBI#";
		}
		public String getEntry( String source_acc, String acc) {
			return String.format("%s%s",getPURL(),acc.replace("obo:",""));
		}
	},	
	
	bii {
		@Override
		public String getPURL() {
			return ISA.URI;
		}
		public String getEntry( String source_acc, String acc) {
			return String.format("%sbii/%s/%s",getPURL(),source_acc,acc.replace("bii:",""));
		}		
	}
	;
	public String getPURL() {
		return String.format("http://purl.obolibrary.org/%s",name().toLowerCase());
	}
	public String getEntry( String source_acc, String acc) {
//		return String.format("%s/%s_%s",getPURL(),source_acc,acc);
		return String.format("%s/%s",getPURL(),acc);
	}
}
