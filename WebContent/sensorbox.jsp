<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>SensorBox Data Viewer</title>
<link rel="stylesheet" type="text/css" href="style/style.css" />
<link rel="stylesheet" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />
<script src="http://www.openlayers.org/api/OpenLayers.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.3/jquery-ui.min.js"></script>
<script src="javascript/jquery.flot.js"></script>
<script src="javascript/jquery.flot.navigate.js"></script>
<script src="javascript/jquery.flot.symbol.js"></script>
<script src="javascript/jquery.flot.selection.js"></script>
<script type="text/javascript" src="javascript/sensorbox.js"></script>
</head>
<body>
	<div id="datasetSelector">
		<label for="dataset">Select a dataset to display: </label>
		<select id="dataset">
			<option id="CarCommuter" value="CarCommuter">CarCommuter</option>
			<option id="CityWalk" value="CityWalk">CityWalk</option>
			<option id="TrainJourney" value="TrainJourney">TrainJourney</option>
			<option id="CoastalWalk" value="CoastalWalk">CoastalWalk</option>
			<option id="WeatherStation" value="WeatherStation">WeatherStation</option>
		</select>
		<input type="button" value="Display" onclick="loadDatasets()" />
	</div>
	<div>
		<div id="locationPlaceholder" class="placeholder"></div>
	</div>
	<div>
		<div id="temperatureGraphPlaceholder" class="placeholder"></div>
		<div id="temperatureMiniature"  class="overview">
      		<div id="temperatureOverview" style="width:166px;height:100px"></div>
    	</div>
	</div>
	<div>
		<div id="humidityGraphPlaceholder" class="placeholder"></div>
		<div id="humidityMiniature"  class="overview">
      		<div id="humidityOverview" style="width:166px;height:100px"></div>
    	</div>
	</div>
	<div>
		<div id="speedGraphPlaceholder" class="placeholder"></div>
		<div id="speedMiniature"  class="overview">
      		<div id="speedOverview" style="width:166px;height:100px"></div>
    	</div>
	</div>
	<div>
		<div id="altitudeGraphPlaceholder" class="placeholder"></div>
		<div id="altitudeMiniature"  class="overview">
      		<div id="altitudeOverview" style="width:166px;height:100px"></div>
    	</div>
	</div>
	<div>
		<div id="accelerationGraphPlaceholder" class="placeholder"></div>
		<div id="accelerationMiniature"  class="overview">
      		<div id="accelerationOverview" style="width:166px;height:100px"></div>
    	</div>
	</div>
	<div id="tooltip"></div>
	<input type="hidden" id="selectedDataset" />
</body>
</html>