	var map;
	var markers;
	var gg = new OpenLayers.Projection("EPSG:4326");
	var sm = new OpenLayers.Projection("EPSG:900913");
	
	var keepTooltip = false;
	
	$(document).ready(function(){
		map = new OpenLayers.Map("map");
	    base = new OpenLayers.Layer.OSM();
	    map.addLayer(base);  
	    map.setCenter(new OpenLayers.LonLat(-4.291608,57.231968).transform(gg,sm), 7);
	    
	    markers = new OpenLayers.Layer.Markers("Markers");
	    map.addLayer(markers);
	    
	    getSightings();
	});
	
	$("#overlay").mouseover(function(e){
		curX = e.pageX + 10;
    	curY = e.pageY;
    	
    	if(!keepTooltip)
    	{
    		tooltip = $("#tooltip");
    		tooltip.css("left", curX);
    		tooltip.css("top", curY);
    	}
	}); 
	
	function getSightings()
	{
		$.ajax({
			type: "GET",
			url: "GetSightings",
			dataType: "JSON",
			beforeSend: function()
			{

			},
			success: function(results)
			{
				count = 0;
				$.each(results.sightings, function(index, item)
				{
					size = new OpenLayers.Size(40,40);
				    offset = 0;
				    icon = new OpenLayers.Icon("img/mink/mink.png", size, offset);
				    
			    	marker = new OpenLayers.Marker(new OpenLayers.LonLat(item.y, item.x).transform(gg,sm), icon);
			    	
			    	marker.events.register("click", marker, function(e){
			    		minkClick(e.x, e.y, item.uri);
			    	});
			    	
				    markers.addMarker(marker);
				    count++;
				});
			},
			error: function(xhr, errorThrown, textStatus)
			{
				alert("Error downloading Mink sightings");
			}
		});
	}
	
	function minkClick(x, y, uri)
	{    			
		tooltip = $("#tooltip");
		
		tooltip.empty();
		
		tooltip.css("display","block")
			   .css("left",x+10)
			   .css("top",y);
		
		$.ajax({
			type: "GET",
			url: "GetSighting",
			data: {
				sightingUri: uri
			},
			dataType: "JSON",
			beforeSend: function()
			{
				tooltip.fadeIn(250);
				tooltip.css("background", "#fff url(img/ajax-loader.gif) no-repeat center");
			},
			success: function(results)
			{
				tooltip.css("background","#fff");
				tooltip.append($("<h2>Sighting Details</h2>"))
					   .append($("<p><strong>Location</strong>: " + results.x + ", " + results.y + "</p>"));
				
				if(results.foi.name == "RiverNull")
					tooltip.append($("<p><strong>River</strong>: unknown</p>"));
				else
					tooltip.append($("<p><strong>River</strong>: " + results.foi.name + "</p>"));
				
				tooltip.append($("<p><strong>Mink spotted</strong>: " + results.count + "</p>"))
					   .append($("<p><strong>Sighting made</strong>: " + parseDate(results.samplingTime) + "</p>"))
					   .append($("<p><strong>Report made</strong>: " + parseDate(results.resultTime) + "</p>"))
					   .append($("<p style=\"margin-bottom: 25px\"><strong>Status</strong>: " + results.status + " </p>"))
					   .append($("<h3>Attributed to</h3>"))
					   .append($("<p><strong>Name</strong>: " + results.agent.name + "</p>"))
					   .append($("<p><strong>Role</strong>: " + results.agent.group + "</p>"));
				
				close = $("<div/>",{
					class: "closeBox"
				}).text("[Close]")
				  .click(function(){
					  tooltip.fadeOut(250);
				})
				.appendTo(tooltip);
				
				qa = $("<div/>", {
					class: "info"
				})
				.append("<h2>Quality Assessment</h2>")
				.appendTo(tooltip);
				
				$.each(results.results, function(index, result){
					qa.append($("<h3>" + result.dimension + "</h3>"));
					
					width = (result.score * 100) + "%";
					duration = 500;
					
					bar = $("<div/>", {
						class: "qualityBar"
					}).appendTo(qa);
					
					mag = $("<div/>", {
						class: "qualityMagnitude"
					}).appendTo(bar);
					
					if(result.score >= 0.66)
						mag.animate({
							backgroundColor: "#66CD00",
							width: width
						}, duration);
					else if(result.score >= 0.33 && result.score < 0.66)
						mag.animate({
							backgroundColor: "#FF9912",
							width: width
						}, duration);
					else
						mag.animate({
							backgroundColor: "#ff0000",
							width: width
						}, duration);
					
					qa.append("<p>" + result.desc + "</p>", {
						class: "small"
					});
				});
			},
			error: function(xhr, errorThrown, textStatus)
			{
				alert("Error downloading sighting data");
			}
		});	
	}
	
	function parseDate(timestamp)
	{
		months = new Array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
		days = new Array("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");
		
		dObj = new Date(parseInt(timestamp));
		humanDate = "" + days[dObj.getDay()] + " " + dObj.getDate() + " " + months[dObj.getMonth()] + " " + dObj.getFullYear();
		return humanDate;
	}