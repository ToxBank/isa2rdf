package org.isa2rdf.cli;


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
