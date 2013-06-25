<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Live Observations from MacRobert 918</title>
<link rel="stylesheet" type="text/css" href="style/style.css" />
<script src="http://www.openlayers.org/api/OpenLayers.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script src="javascript/jquery.flot.js"></script>
<script src="javascript/jquery.flot.navigate.js"></script>
<script src="javascript/jquery.flot.symbol.js"></script>
<script src="javascript/jquery.flot.selection.js"></script>
<script type="text/javascript" src="javascript/officearduino.js"></script>
</head>
<body>
	<div>
		<div id="temperatureGraphPlaceholder" class="placeholder"></div>
		<div id="temperatureMiniature"  class="overview">
      		<div id="temperatureOverview" style="width:166px;height:100px"></div>
    	</div>
	</div>
	<div>
		<div id="lightGraphPlaceholder" class="placeholder"></div>
		<div id="lightMiniature"  class="overview">
      		<div id="lightOverview" style="width:166px;height:100px"></div>
    	</div>
	</div>
	<div>
		<div id="vibrationGraphPlaceholder" class="placeholder"></div>
		<div id="vibrationMiniature"  class="overview">
      		<div id="vibrationOverview" style="width:166px;height:100px"></div>
    	</div>
	</div>
	<div>
		<div id="motionGraphPlaceholder" class="placeholder"></div>
		<div id="motionMiniature"  class="overview">
      		<div id="motionOverview" style="width:166px;height:100px"></div>
    	</div>
	</div>
	<div id="tooltip"></div>
</body>
</html>