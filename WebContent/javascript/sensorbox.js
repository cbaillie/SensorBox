	var map;
	var markers;
	var gg = new OpenLayers.Projection("EPSG:4326");
	var sm = new OpenLayers.Projection("EPSG:900913");
	
	var tPlot, hPlot, sPlot, altPlot, accPlot;
	var selectedTemperature, selectedHumidity, selectedSpeed, selectedAltitude, selectedAccel;
	
	var tempSeries, humSeries, speedSeries, altSeries, accSeries;
	var tempOverview, humOverview, speedOverview, altOverview, accOverview;
	
	var keepTooltip = false;
	
	$(function() {
		$( "#tabs" ).tabs();
	});
	
	function getURLParameter(name) {
	    return decodeURI(
	        (RegExp(name + '=' + '(.+?)(&|$)').exec(location.search)||[,null])[1]
	    );
	}
	
	function validateDataset(ds)
	{
		if(ds == "CarCommuter")
			return 0;
		if(ds == "CityWalk")
			return 1;
		if(ds == "TrainJourney")
			return 2;
		if(ds == "CoastalWalk")
			return 3;
		if(ds == "WeatherStation")
			return 4;
		return -1;
	}
	
	$(document).ready(function(){
		//Load map
		map = new OpenLayers.Map("locationPlaceholder");
	    base = new OpenLayers.Layer.OSM();
	    map.addLayer(base);  
	    map.setCenter(new OpenLayers.LonLat(-2.091608,57.231968).transform(gg,sm), 10);
	    
	    markers = new OpenLayers.Layer.Markers("Markers");
	    map.addLayer(markers);
	    
	    //Choose dataset
	    dataset = getURLParameter(name);
	    if(dataset != null)
	    {
	    	id = validateDataset(dataset);
	    	if(id >= 0)
	    	{
	    		option = "#datasetSelector option[value=" + dataset +"]";
	    		$(option).prop("selected","selected");
	    		loadDatasets();
	    	}
	    }
	});
	
    $(document).mousemove(function(e){
    	curX = e.pageX + 10;
    	curY = e.pageY;

    	if(!keepTooltip)
    	{
    		tooltip = $("#tooltip");
    		tooltip.css("left", curX);
    		tooltip.css("top", curY);
    	}
     }); 
    
    function closeTooltip()
    {
    	tooltip = $("#tooltip");
    	tooltip.hide();
    	tooltip.empty();
    	keepTooltip = false;
    }
	
	function getObservations(dataset, property)
	{
		var series;
		var options = {
				series: { 
					lines: { show: true },
					points: { show: true }
				},
				xaxis: { 
					mode: "time",
					},
				grid: {
				    backgroundColor: { 
				    	colors: ["#fff", "#eee"],
					},
			    	hoverable: true,
			    	clickable: true,
				},
				selection: { mode: "xy" }
			};
		
		$.ajax({
			type: "GET",
			url: "GetObservations",
			data: { 
				"property" : property,
				"endpoint" : "http://dtp-126.sncs.abdn.ac.uk:8080/openrdf-sesame/repositories/" + dataset
			},
			dataType: "JSON",
			beforeSend: function()
			{
				$("#" + property.toLowerCase() + "GraphPlaceholder").empty();
				$("#" + property.toLowerCase() + "GraphPlaceholder").css("background", "#fff url(img/ajax-loader.gif) no-repeat center");
			},
			success: function(results)
			{
				$("#" + property.toLowerCase() + "GraphPlaceholder").css("background", "#fff");
				if(property == "Location")
				{
					//remove all features
					markers.clearMarkers();
					
				    //Add a feature
				    $.each(results, function(index, item){
				    	
					    lon = item[2];
					    lat = item[1];
					    prec = item[4];
					    sz = prec / 10;
					    
					    if(sz > 50)
					    	sz = 50;
					    
					    size = new OpenLayers.Size(sz, sz);
					    offset = 0;
					    icon = new OpenLayers.Icon("img/sun-red-circle.png", size, offset);
					    
				    	marker = new OpenLayers.Marker(new OpenLayers.LonLat(lon, lat).transform(gg,sm), icon);
				    	
				    	marker.time = item[0];
				    	
				    	marker.events.register("click", marker, function(){
				    		assessQuality(item[0], "Location", $("#selectedDataset").val());
				    	});
				    	
					    markers.addMarker(marker);
				    });
				    
				    //Reconfigure map
				    dataset = $("#selectedDataset").val();
				    if(dataset == "CarCommuter")
				    	map.setCenter(new OpenLayers.LonLat(-2.091608,57.231968).transform(gg,sm), 10);
				    else if(dataset == "CityWalk")
				    	map.setCenter(new OpenLayers.LonLat(-3.185863,55.948935).transform(gg,sm), 14);
				    else if(dataset == "TrainJourney")
				    	map.setCenter(new OpenLayers.LonLat(-2.091608,57.231968).transform(gg,sm), 10);
				    else if(dataset == "CoastalWalk")
				    	map.setCenter(new OpenLayers.LonLat(-2.008846,57.290651).transform(gg,sm), 14);
				    else
				    	map.setCenter(new OpenLayers.LonLat(-2.03487, 57.317021).transform(gg,sm), 14);
					
				}
				else
				{		
					series = results;
					if(property == "Acceleration")
					{
						series = [{
							label: "X Acceleration",
							data: results[0]
						},
						{
							label: "Y Acceleration",
							data: results[1]
						},
						{
							label: "Z Acceleration",
							data: results[2]
						}];
					}
					else
					{
						if(results[1].length > 0)
						{
							series = [{
									label: property,
									data: results[0]
								},
								{
									label: "Derived " + property,
									data: results[1],
									color: "#f00",
									lines: false,
									points: {
										symbol: "square"
									}
								}];
						}
						else
						{
							series = [{
								label: property,
								data: results[0]
							}];
						}
					}

							
					plot = $.plot($("#" + property.toLowerCase() + "GraphPlaceholder"), series, options);	
					
				   var overview = $.plot($("#" + property.toLowerCase() + "Overview"), series, {
				        legend: { show: false },
				        series: {
				            lines: { show: true, lineWidth: 1 },
				            shadowSize: 0
				        },
				        xaxis: { 
				        	ticks: 4,
				        	mode: "time"
				        },
				        grid: { color: "#999" },
				        selection: { mode: "xy" }
				    });
					
					if(property == "Temperature"){
						tPlot = plot;
						tempSeries = series;
						tempOverview = overview;
					}else if(property == "Humidity"){
						hPlot = plot;
						humSeries = series;
						humOverview = overview;
					}else if(property == "Speed"){
						sPlot = plot;
						speedSeries = series;
						speedOverview = overview;
					}else if(property == "Altitude"){
						altPlot = plot;
						altSeries = series;
						altOverview = overview;
					}else if(property == "Acceleration"){
						accPlot = plot;
						accSeries = series;
						accOverview = overview;
					}
					
				}
			},
			error: function(xhr, textStatus, errorThrown)
			{
				alert("Error: " + errorThrown);			
			},
			complete: function()
			{
				$("#" + property.toLowerCase() + "GraphPlaceholder").css("background", "none");
			}
		});
		
		$("#" + property.toLowerCase() + "GraphPlaceholder").bind("plotselected", function (event, ranges) {
	        // clamp the zooming to prevent eternal zoom
	        if (ranges.xaxis.to - ranges.xaxis.from < 0.00001)
	            ranges.xaxis.to = ranges.xaxis.from + 0.00001;
	        if (ranges.yaxis.to - ranges.yaxis.from < 0.00001)
	            ranges.yaxis.to = ranges.yaxis.from + 0.00001;
	        
	        // do the zooming
	        plot = $.plot($("#" + property.toLowerCase() + "GraphPlaceholder"), getData(property, ranges.xaxis.from, ranges.xaxis.to),
	                      $.extend(true, {}, options, {
	                          xaxis: { min: ranges.xaxis.from, max: ranges.xaxis.to },
	                          yaxis: { min: ranges.yaxis.from, max: ranges.yaxis.to }
	                      }));
	        
	        // don't fire event on the overview to prevent eternal loop
	        if(property == "Temperature")
	        	overview = tempOverview;
	        else if(property == "Humidity")
	        	overview = humOverview;
	        else if(property == "Speed")
	        	overview = speedOverview;
	        else if(property == "Altitude")
	        	overview = altOverview;
	        else
	        	overview = accOverview;
	        overview.setSelection(ranges, true);
	    });
		
	    $("#" + property.toLowerCase() + "Overview").bind("plotselected", function (event, ranges) {
	        if(property == "Temperature")
	        	plot = tPlot;
	        else if(property == "Humidity")
	        	plot = hPlot;
	        else if(property == "Speed")
	        	plot = sPlot;
	        else if(property == "Altitude")
	        	plot = altPlot;
	        else
	        	plot = accPlot;
	        
	        plot.setSelection(ranges);
	    });
		
		$("#" + property.toLowerCase() + "GraphPlaceholder").bind("plotclick", function(event, pos, item)
		{
    		tooltip = $("#tooltip");
    		tooltip.css("left", curX);
    		tooltip.css("top", curY);
			if(item)
			{
				//handle highlighting
				if(property == "Temperature")
					plot = tPlot;
				else if(property == "Humidity")
					plot = hPlot;
				else if(property == "Speed")
					plot = sPlot;
				else if(property == "Altitude")
					plot = altPlot;
				else if(property == "Acceleration")
					plot = accPlot;
				
				plot.unhighlight();
				plot.highlight(item.series, item.datapoint);
				
				label = item.series.label;
				if(label.indexOf("Derived") >= 0)
				{
					prop = label.substring((label.indexOf(' ')+1));
					ds = $("#selectedDataset").val() + "Provenance";
				}
				else
				{
					if(label.indexOf(' '))
						prop = label.substring(label.indexOf(' ')+1);
					else
						prop = label;
					ds = $("#selectedDataset").val();

					console.log("LABEL: " + label);
					direction = "X";
					if(label.indexOf("Y") >= 0)
						direction = "Y";
					else if(label.indexOf("Z") >= 0)
						direction = "Z";
				}
				assessQuality(item.datapoint[0], prop, ds, direction);
			}
		});
		
		$("#" + property.toLowerCase() + "GraphPlaceholder").bind("plothover", function(event, pos, item){
			if(item && !keepTooltip)
			{
				if(item.series.label.indexOf("Derived") >= 0)
				{
					requestUrl = "GetDerivedObservations";
					endpoint = $("#selectedDataset").val() + "Provenance";
				}
				else
				{
					requestUrl = "GetObservation";
					endpoint = $("#selectedDataset").val();
				}
				$.ajax({
					type: "GET",
					dataType: "JSON",
					url: requestUrl,		
					data: { 
						"time" : item.datapoint[0],
						"property" : property,
						"endpoint" : "http://dtp-126.sncs.abdn.ac.uk:8080/openrdf-sesame/repositories/" + endpoint
					},
					dataType: "JSON",
					beforeSend: function(){
						$("#tooltip").css("background", "#fff url(img/ajax-loader.gif) no-repeat center");
						$("#tooltip").show();
					},
					success: function(data)
					{
						$("#tooltip").empty();
						$("#tooltip").css("background", "#fff");
						content = $("<div/>")
							.append($("<p><strong>URI:</strong> " + data.uri.substring(data.uri.lastIndexOf('/')+1) + "</p>"))
							.append($("<p><strong>Time:</strong> " + data.time + "</p>"))
							.append($("<p><strong>Value:</strong> " + data.value + "</p>"));
									
					    if(item.series.label.indexOf("Derived") >= 0)
					    {
					    	content.append($("<p><strong>Derived from:</strong></p>"));
							list = $("<ul/>");
							$.each(data.derivedFrom, function(index, item)
							{
								list.append($("<li>" + item.substring(item.lastIndexOf('/')+1) + "</li>"));
							});
							content.append(list);
					    }
						$("#tooltip").append(content);
						$("#tooltip").append($("<p>Click to assess this observation's quality.</p>"));
					},
					error: function(xhr, textStatus, errorThrown)
					{
						alert("Error: " + errorThrown);			
					},
				});
			}
			else
			{
				if(!keepTooltip)
					$("#tooltip").empty().hide();
			} 
		});
	}
	
	function getData(property, from, to)
	{
		if(property == "Temperature")
			series = tempSeries;
		else if(property == "Humidity")
			series = humSeries;
		else if(property == "Speed")
			series = speedSeries;
		else if(property == "Altitude")
			series = altSeries;
		else
			series = accSeries;

		if(property == "Speed")
		{
			fromIndex = findIndex(series[0].data, from, false);
			toIndex = findIndex(series[0].data, to, true);
			
			arr = series[0].data.slice(fromIndex,toIndex);
			
	        return [
	                { label: "Speed", data: arr }
	            ];
		}
		else if(property == "Altitude")
		{
			if(series.length > 1)
			{
				obs = series[0].data;
				der = series[1].data;
				
				obsFromInd = findIndex(obs, from, false);
				obsToInd = findIndex(obs, to, true);
				
				derFromInd = findIndex(der, from, false);
				derToInd = findIndex(der, to, true);
				
				obsArr = obs.slice(obsFromInd, obsToInd);
				derArr = der.slice(derFromInd, derToInd);
				
				return [{
					label: series[0].label,
					data: obsArr
				},
				{
					label: "Derived " + series[0].label,
					data: derArr,
					color: "#f00",
					lines: false,
					points: {
						symbol: "square"
					}
				}];
			}
			else
			{	
				temp = series[0].data;
				fromIndex = findIndex(temp, from, false);
				toIndex = findIndex(temp, to, true);
				arr = temp.slice(fromIndex, toIndex);
				return [
				        { label: "Altitude", data: arr}
				        ];
			}
		}
		else if(property == "Acceleration")
		{
			x = series[0].data;
			y = series[1].data;
			z = series[2].data;
			
			xFromInd = findIndex(x, from, false);
			xToInd = findIndex(x, to, true);
			
			yFromInd = findIndex(y, from, false);
			yToInd = findIndex(y, to, true);
			
			zFromInd = findIndex(z, from, false);
			zToInd = findIndex(z, to, true);
			
			xArr = x.slice(xFromInd, xToInd);
			yArr = y.slice(yFromInd, yToInd);
			zArr = z.slice(zFromInd, zToInd);
			
			return [{
				label: "X Acceleration",
				data: xArr
			}, {
				label: "Y Acceleration",
				data: yArr
			}, {
				label: "Z Acceleration",
				data: zArr
			}];
		}
		else
		{
			obs = series[0].data;
			der = series[1].data;
			
			obsFromInd = findIndex(obs, from, false);
			obsToInd = findIndex(obs, to, true);
			
			derFromInd = findIndex(der, from, false);
			derToInd = findIndex(der, to, true);
			
			obsArr = obs.slice(obsFromInd,obsToInd);
			derArr = der.slice(derFromInd,derToInd);
			
			data = [{
				label: series[0].label,
				data: obsArr
			},
			{
				label: "Derived " + series[0].label,
				data: derArr,
				color: "#f00",
				lines: false,
				points: {
					symbol: "square"
				}
			}];
			
			return data;
		}
	}
	
	function getObservationRdf(time, property, dataset, contentDiv, direction)
	{
		$.ajax({
			type: "GET",
			url: "GetObservationRDF",
			data: {
				time: time,
				property: property,
				endpoint: "http://dtp-126.sncs.abdn.ac.uk:8080/openrdf-sesame/repositories/" + dataset,
				direction: direction
			},
			success: function(data){
				contentDiv.append($("<div/>"))
							//.append($("<textarea>" + data + "</textarea>"));
							.append("<pre>" + data + "</pre>");
			},
			error: function(xhr, status, error){
				alert("Error: " + error);
			}
		});
	}
	
	function assessQuality(time, property, dataset, direction)
	{		
		console.log("Direction: " + direction);
		keepTooltip = true;
		
		$.ajax({
			type: "GET",
			url: "QualityAssessment",
			data: {
				time: time,
				property: property,
				dataset: dataset,
			},
			beforeSend: function(){
				$("#tooltip").empty();
				$("#tooltip").css("background", "#fff url(img/ajax-loader.gif) no-repeat center");
				$("#tooltip").show();
			},
			success: function(data)
			{	
				$("#tooltip").empty();
				$("#tooltip").css("background", "#fff");
				
				content = $("<div/>")
					.append("<ul>" +
								"<li><a href=\"#results\">Quality Assessment</a></li>" +
								"<li><a href=\"#rdf\">Observation RDF</a></li>" + 
							"</ul");
				
				$("#tooltip").append(content);
				
				results = $("<div/>", {
						id: "results"
				})
				.append($("<h3>Quality Assessment Results</h3>"));
				
				content.append(results);
				
				if(data.results.length > 0)
				{
					$.each(data.results, function(index, item){
						
						var barColor;
						if(item.score > 0.66)
							barColor = "#0f0";
						else if(item.score > 0.33 && item.score <= 0.66)
							barColor = "#ff8c00";
						else
							barColor = "#f00";
						
						barWidth = (item.score * 100) + "%";
						
						if(item.score == 0)
							barWidth = "1%";
						
						bar = $("<div/>",{
							class: "qualityBar"
							}).appendTo(content);
						
						mag = $("<div/>", {
								class: "qualityMagnitude",
							}).appendTo(bar)
							.animate({
								backgroundColor: barColor,
								width: barWidth
							});
						
						results.append("<h4>" + item.dimension + "</h4>")
							   .append(bar)
							   .append("<p style=\"width: 500px\"><em>" + item.description + "</em></p>")
							   .append("<p>Assessed value: " + item.assessedValue + "</p>");
							   
							   provDesc = $("<div/>",{
								   class: "provDesc"
							   })
							   .append("<p>This <span class=\"tt\">Result</span> <em>wasGeneratedBy</em> an <span class=\"tt\">Activity</span>, which <em>wasControlledBy</em> <span class=\"tt\">Agent</span> '" + item.asmtControlledBy + "'</p>")
							   .append("<p>This <span class=\"tt\">Activity</span> <em>used</em> the following <span class=\"tt\">Entities</span>:");
							   
							   entityList = $("<ul/>");
							   $.each(item.used, function(index, item){
								   entityList.append("<li>" + item.entity + " (<span class=\"tt\">" + item.type + "</span>)</li>");
							   });
							   
							   provDesc.append(entityList);
							   results.append(provDesc);
					});
					results.append($("<hr/>"));
					results.append("<p>Quality assessment took " + data.assessmentTime + "ms.</p>");
				}
				else
				{
					results.append($("<p>There were no quality results generated for this observation.</p>"));
				}
				
				rdf = $("<div/>",{
					id: "rdf"
				})
				.append("<h4>Observation RDF</h4>");
				
				getObservationRdf(time, property, dataset, rdf, direction);
				
				content.append(rdf);
				content.append("<p><a href=\"javascript:closeTooltip()\">Dismiss results</a>.</p>");
				content.tabs();
			},
			error: function(xhr, textstatus, error)
			{
				alert(error);
			}
		});
	}
	
	function findIndex(series, index, to)
	{
		for(var i=0;i<series.length;i++)
		{
			entry = series[i];
			if(entry[0] > index)
			{
				if(to)
					needle = (i+1);
				else
					needle = (i-1);
			}
		}
		if(to)
			needle = (series.length + 1);
		else
			needle = 0;
		
		return needle;
	}
	
	function loadDatasets()
	{
		closeTooltip();
		$("#selectedDataset").val($("#datasetSelector option:selected").val());
		dataset = $("#datasetSelector option:selected").val();	
		getObservations(dataset, "Location");
		getObservations(dataset, "Temperature");
		getObservations(dataset, "Humidity");
		getObservations(dataset, "Speed");
		getObservations(dataset, "Altitude");
		getObservations(dataset, "Acceleration");
	}