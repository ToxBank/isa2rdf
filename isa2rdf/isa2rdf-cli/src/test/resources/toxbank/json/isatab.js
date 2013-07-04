function demo(url,buildgraph) {
	/*
	$.getJSON("isatab.json", function(data) {
		$.each(data.study, function(key,study){
			console.log(study);
	        $.each(study.processing, function(i,s){
	            console.log(key + " " + s.input + "," + s.output);
	        });
		});
    }).error(function(jqXhr, textStatus, error) {
                alert("ERROR: " + textStatus + ", " + error);
    });
    */
	defineProcessingTable(null,url,buildgraph);
	
}


function defineProcessingTable(root,url,buildgraph) {
	var oTable = $('#processing').dataTable( {
		"sAjaxDataProp" : "processing",
		"bProcessing": true,
		"bServerSide": false,
		"bStateSave": false,
		"aoColumnDefs": [
 				{ 
 				 "mData": function ( source) {
 					return source.type; 
 				  },
 				  "asSorting": [ "asc", "desc" ],
				  "aTargets": [ 0 ],	
				  "bSearchable" : true,
				  "bUseRendered" : false,
				  "bSortable" : true,
				  "fnRender" : function(o,val) {
					  return val;
				  }
				},
				{ "mDataProp": function ( source ) {
					return source.input==null?"":
						source.type=='MaterialProcessing'?
							("["+source.prev.id + "." + source.prev.material.id + "] " + source.prev.material.acc):
						source.type=='DataAcquisition'?
							("["+source.prev.id + "." +source.prev.material.id + "] " + source.prev.material.acc):
							("["+source.prev.id + "." +source.prev.data.id + "] " + source.prev.data.acc);
				  },
				  "asSorting": [ "asc", "desc" ],
				  "aTargets": [ 1 ],	
				  "bSearchable" : true,
				  "bSortable" : true
				},
				{ "mDataProp": function ( source ) {
					return source.applies==null?"":
						("["+source.applies.id + "] " + source.applies.protocol);
				  },
				  "asSorting": [ "asc", "desc" ],
				  "aTargets": [ 2 ],	
				  "bSearchable" : true,
				  "bSortable" : true
				},			
				{ "mDataProp": function ( source ) {
					var sOut = "";
					if (source.applies!=null) 
					 $.each(source.applies.parameters, function(p,pv){
						 sOut += pv.parameter + "= " + pv.value + "<br>";
					 });
					return sOut;
				  },
				  "asSorting": [ "asc", "desc" ],
				  "aTargets": [ 3 ],	
				  "bSearchable" : true,
				  "bSortable" : true
				},				
				{ "mDataProp": function ( source ) {
					return source.output==null?"": 
					source.type=='MaterialProcessing'?
						("["+source.next.id + "." +source.next.material.id + "] " + source.next.material.acc):
						("["+source.next.id + "." +source.next.data.id + "] " + source.next.data.acc);
				  },
				  "asSorting": [ "asc", "desc" ],
				  "aTargets": [ 4 ],	
				  "bSearchable" : true,
				  "bSortable" : true
				}
				
			],
		"fnServerData": function ( sSource, aoData, fnCallback, oSettings ) {
			      oSettings.jqXHR = $.ajax( {
			        "type": "GET",
			        "url": sSource ,
			        "data": aoData,
			        "dataType": "json", 
			        "success": function(json) {
			        	var thestudy = null;
			        	var graph = {"nodes":[],"links":[]};

			        	$.each(json.study, function(key,study){
			        		thestudy = study;
			        		
			    	        $.each(study.nodes, function(i,s){
			    	        	var group = 0;
			    	        	if (s.material !=undefined) {
			    	        		var id = s.material;
			    	        		s.material = study.materials[id];
			    	        		s.material['id'] = id;
			    	        		group=1;
			    	        	} else if (s.data !=undefined) {
			    	        		var id = s.data;
			    	        		s.data = study.data[id];
			    	        		s.data['id'] = id;
			    	        		group=2;
			    	        	};
			    	        	s['index'] = graph.nodes.length;
			    	        	graph.nodes.push({'name':i,'group':group});
			    	        });
			    	        $.each(study.processing, function(i,s){
			    	        	graph.links.push({
			    	        		        "source": study.nodes[s.input].index , 
			    	        				"target": study.nodes[s.output].index,
			    	        				"value": s.input
			    	        				});
			    	        });
			    	        $.each(study.processing, function(i,s){
			    	        	var id = s.input;
		    	        		s.prev = study.nodes[s.input];
		    	        		s.prev['id'] = id;
		    	        		id = s.output;
		    	        		s.next = study.nodes[s.output];
		    	        		s.next['id'] = id;
		    	        		if (s.applies!=undefined) {
			    	        		var id = s.applies;
		    	        			s.applies = study.protocolApplications[id];
		    	        			s.applies['id'] = id;
		    	        		}

			    	        });
			    	        
			    	        

			    	        
			    	        			    	        
			    		});
			        	fnCallback(thestudy);
			        	if (buildgraph)
			        		drawGraph(graph,'#chart1');
			        },
			        "cache": false,
			        "error" : function( xhr, textStatus, error ) {
			        	oSettings.oApi._fnProcessingDisplay( oSettings, false );
			        }
			      } );
		},			
		"sDom" : '<"help remove-bottom"if><"help"p>Trt<"help"l>',	
		"bJQueryUI" : true,
		"bPaginate" : true,
		"sPaginationType": "full_numbers",
		"sPaginate" : ".dataTables_paginate _paging",
		"bDeferRender": true,
		"bSearchable": true,
		"sAjaxSource": url,
		"iDisplayLength": 10,
		"oLanguage": {
				"sSearch": "Filter:",
	            "sProcessing": "<img src='"+root+"/images/progress.gif' border='0'>",
	            "sLoadingRecords": "No records found."
	    }
	} );
	return oTable;
}