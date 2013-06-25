	var tPlot, lPlot, vPlot, mPlot;
	var selectedTemperature, selectedLight, selectedVibration, selectedMotion;
	
	var tempSeries, lightSeries, vibeSeries, motSeries;
	var tempOverview, lightOverview, vibeOverview, motOverview;
	
	var version = 12;
	alert("Version " + version);
	
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
	
	var overviewOptions = {
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
	    };
	
	var keepTooltip = false;
	
	var lastUpdate = 0;
	
	$(document).ready(function(){
	   loadDatasets();
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
    
    function reloadDatasets()
    {
    	updateObservations("Temperature");
    	updateObservations("Light");
    	updateObservations("Vibration");
    	updateObservations("Motion");
    	setTimeout(function(){reloadDatasets();}, 5000);
    }
    
    function updateObservations(property)
    {
    	$.ajax({
    		typr: "GET",
    		url: "GetLiveObservations",
    		data: {
    			"property" : property,
    			"time" : lastUpdate,
    		},
    		dataType: 'JSON',
    		beforeSend: function()
    		{
    			
    		},
    		success: function(results)
    		{
    			if(results.length > 0)
    			{
    				lastUpdate = (new Date()).getTime();
    				
    				if(property == "Temperature")
    				{
    					series = tempSeries;
    				}
    				else if(property == "Light")
    				{
    					series = lightSeries;
    				}
    				else if(property == "Vibration")
    				{
    					series = vibeSeries;
    				}
    				else
    				{
    					series = motSeries;
    				}
    				
    				newData = series[0].data.slice(results.length);
    				d = newData.concat(results);
    				
    				newSeries = [{
    						label : property,
    						data: d
    					}];
    				
    				$.plot("#" + property.toLowerCase() + "GraphPlaceholder", newSeries, options);   
    				$.plot($("#" + property.toLowerCase() + "Overview"), newSeries, overviewOptions);
    				
    				if(property == "Temperature")
    				{
    					tempSeries = newSeries;
    				}
    				else if(property == "Light")
    				{
    					lightSeries = newSeries;
    				}
    				else if(property == "Vibration")
    				{
    					vibeSeries = newSeries;
    				}
    				else
    				{
    					motSeries = newSeries;
    				}
    			}
    		},
    		error: function(xhr, status, error)
    		{
    			
    		},
    		complete: function()
    		{
    			
    		}
    	});
    }
	
	function getObservations(property)
	{
		var series;
		$.ajax({
			type: "GET",
			url: "GetLiveObservations",
			data: { 
				"property" : property,
				"time" : 0,
				"endpoint" : "http://dtp-126.sncs.abdn.ac.uk:8080/openrdf-sesame/repositories/OfficeArduino"
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

				series = [{
					label: property,
					data: results
				}];

				plot = $.plot($("#" + property.toLowerCase() + "GraphPlaceholder"), series, options);	
				var overview = $.plot($("#" + property.toLowerCase() + "Overview"), series, overviewOptions);
					
				if(property == "Temperature"){
					tPlot = plot;
					tempSeries = series;
					tempOverview = overview;
				}else if(property == "Light"){
					lPlot = plot;
					lightSeries = series;
					lightOverview = overview;
				}else if(property == "Vibration"){
					vPlot = plot;
					vibeSeries = series;
					vibeOverview = overview;
				}else if(property == "Motion"){
					mPlot = plot;
					motSeries = series;
					motOverview = overview;
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
	        else if(property == "Light")
	        	overview = lightOverview;
	        else if(property == "Vibration")
	        	overview = vibeOverview;
	        else if(property == "Motion")
	        	overview = motOverview;
	        overview.setSelection(ranges, true);
	    });
		
	    $("#" + property.toLowerCase() + "Overview").bind("plotselected", function (event, ranges) {
	        if(property == "Temperature")
	        	plot = tPlot;
	        else if(property == "Light")
	        	plot = lPlot;
	        else if(property == "Vibration")
	        	plot = vPlot;
	        else if(property == "Motion")
	        	plot = motPlot;
	        
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
				else if(property == "Light")
					plot = lPlot;
				else if(property == "Vibration")
					plot = vPlot;
				else if(property == "Motion")
					plot = motPlot;
				
				plot.unhighlight();
				plot.highlight(item.series, item.datapoint);
				
				label = item.series.label;

				if(label.indexOf(' '))
					prop = label.substring(label.indexOf(' ')+1);
				else
					prop = label;
				}
				//assessQuality(item.datapoint[0], prop, ds);
		});
		
		$("#" + property.toLowerCase() + "GraphPlaceholder").bind("plothover", function(event, pos, item){
			if(item && !keepTooltip)
			{
				requestUrl = "GetObservation";
				endpoint = "OfficeArduino";
				
				$.ajax({
					type: "GET",
					dataType: "JSON",
					url: requestUrl,		
					data: { 
						"time" : item.datapoint[0],
						"property" : property,
						"endpoint" : "http://dtp-126.sncs.abdn.ac.uk:8080/openrdf-sesame/repositories/OfficeArduino"
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
									
						$("#tooltip").append(content);
						//$("#tooltip").append($("<p>Click to assess this observation's quality.</p>"));
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
		else if(property == "Light")
			series = lightSeries;
		else if(property == "Vibration")
			series = vibeSeries;
		else if(property == "Motion")
			series = motSeries;
		
		obs = series[0].data;
		
		obsFromInd = findIndex(obs, from, false);
		obsToInd = findIndex(obs, to, true);
		
		obsArr = obs.slice(obsFromInd,obsToInd);
		
		data = [{
			label: series[0].label,
			data: obsArr
		}];
		
		return data;
	}
	
	function assessQuality(time, property, dataset)
	{		
		keepTooltip = true;
		
		$.ajax({
			type: "GET",
			url: "QualityAssessment",
			data: {
				time: time,
				property: property,
				dataset: dataset
			},
			beforeSend: function(){
				$("#tooltip").empty();
				$("#tooltip").css("background", "#fff url(img/ajax-loader.gif) no-repeat center");
				$("#tooltip").show();
			},
			success: function(data)
			{	
				console.log(data);
				$("#tooltip").empty();
				$("#tooltip").css("background", "#fff");
				
				content = $("<div/>")
					.append($("<h3>Quality Assessment Results</h3>"));
				
				if(data.results.length > 0)
				{
					$.each(data.results, function(index, item){
						
						if(item.score > 0.66)
							color = "#0f0";
						else if(item.score > 0.33 && item.score <= 0.66)
							color = "#ff8c00";
						else
							color = "#f00";
						
						barWidth = (item.score * 100) + "%";
						
						if(item.score == 0)
							barWidth = "1%";
						
						bar = $("<div/>",{
							class: "qualityBar"
							})
							.append($("<div/>", {
								class: "qualityMagnitude",
							})
							.css("background-color",color)
							.css("width",barWidth)
							);
						
						content.append("<h4>" + item.dimension + "</h4>")
							   .append(bar)
							   .append("<p><em>" + item.description + "</em></p>")
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
							   content.append(provDesc);
					});
					content.append($("<hr/>"));
					content.append("<p>Quality assessment took " + data.assessmentTime + "ms.</p>");
				}
				else
				{
					content.append($("<p>There were no quality results generated for this observation.</p>"));
				}
				content.append("<p><a href=\"javascript:closeTooltip()\">Dismiss results</a>.</p>");
				$("#tooltip").append(content);
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
		getObservations("Temperature");
		getObservations("Light");
		getObservations("Vibration");
		getObservations("Motion");
		lastUpdate = (new Date()).getTime();
		setTimeout(function(){reloadDatasets();}, 5000);
	}