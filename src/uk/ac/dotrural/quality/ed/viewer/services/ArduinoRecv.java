package uk.ac.dotrural.quality.ed.viewer.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.dotrural.quality.ed.viewer.properties.Property;

import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

@WebServlet("/ArduinoRecv")
public class ArduinoRecv extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final String endpoint = "http://dtp-126.sncs.abdn.ac.uk:8080/openrdf-sesame/repositories/OfficeArduino";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ArduinoRecv() {
        super();
        // TODO Auto-generated constructor stub
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
    	System.out.println("Received a GET request...");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String observationResultTime = "" + System.currentTimeMillis();
		
		String temp = request.getParameter("temp");
		String light = request.getParameter("light");
		String vibe = request.getParameter("vibe");
		String mot = request.getParameter("mot");
		
		cleanup();
		
		String temperatureSensorUri = "http://dtp-126.sncs.abdn.ac.uk:8080/Arduino/Office/1/TemperatureSensingDevice";
		String lightSensorUri = "http://dtp-126.sncs.abdn.ac.uk:8080/Arduino/Office/1/LightSensingDevice";
		String vibrationSensorUri = "http://dtp-126.sncs.abdn.ac.uk:8080/Arduino/Office/1/VibrationSensingDevice";
		String motionSensorUri = "http://dtp-126.sncs.abdn.ac.uk:8080/Arduino/Office/1/MotionSensingDevice";
		
		String temperatureObservationUri = "http://dtp-126.sncs.abdn.ac.uk:8080/Arduino/Office/1/TemperatureSensingDevice/Observation/" + UUID.randomUUID();
		String temperatureSensorOutputUri = "http://dtp-126.sncs.abdn.ac.uk:8080/Arduino/Office/1/TemperatureSensingDevice/SensorOutput/" + UUID.randomUUID();
		String temperatureObservationValueUri = "http://dtp-126.sncs.abdn.ac.uk:8080/Arduino/Office/1/TemperatureSensingDevice/ObservationValue/" + UUID.randomUUID();
		
		String lightObservationUri = "http://dtp-126.sncs.abdn.ac.uk:8080/Arduino/Office/1/LightSensingDevice/Observation/" + UUID.randomUUID();
		String lightSensorOutputUri = "http://dtp-126.sncs.abdn.ac.uk:8080/Arduino/Office/1/LightSensingDevice/SensorOutput/" + UUID.randomUUID();
		String lightObservationValueUri = "http://dtp-126.sncs.abdn.ac.uk:8080/Arduino/Office/1/LightSensingDevice/ObservationValue/" + UUID.randomUUID();
		
		String vibeObservationUri = "http://dtp-126.sncs.abdn.ac.uk:8080/Arduino/Office/1/VibrationSensingDevice/Observation/" + UUID.randomUUID();
		String vibeSensorOutputUri = "http://dtp-126.sncs.abdn.ac.uk:8080/Arduino/Office/1/VibrationSensingDevice/SensorOutput/" + UUID.randomUUID();
		String vibeObservationValueUri = "http://dtp-126.sncs.abdn.ac.uk:8080/Arduino/Office/1/VibrationSensingDevice/ObservationValue/" + UUID.randomUUID();
		
		String motObservationUri = "http://dtp-126.sncs.abdn.ac.uk:8080/Arduino/Office/1/MotionSensingDevice/Observation/" + UUID.randomUUID();
		String motSensorOutputUri = "http://dtp-126.sncs.abdn.ac.uk:8080/Arduino/Office/1/MotionSensingDevice/SensorOutput/" + UUID.randomUUID();
		String motObservationValueUri = "http://dtp-126.sncs.abdn.ac.uk:8080/Arduino/Office/1/MotionSensingDevice/ObservationValue/" + UUID.randomUUID();
		
		String tempObsStr = createObservationString(Property.TEMPERATURE, temperatureObservationUri, temperatureSensorOutputUri, temperatureSensorUri, observationResultTime);
		String tempSenOutStr = createSensorOutputString(temperatureSensorOutputUri, temperatureObservationValueUri);
		String tempObsValStr = createObservationValueString(temperatureObservationValueUri, temp);
		
		String lightObsStr = createObservationString(Property.LIGHT, lightObservationUri, lightSensorOutputUri, lightSensorUri, observationResultTime);
		String lightSenOutStr = createSensorOutputString(lightSensorOutputUri, lightObservationValueUri);
		String lightObsValStr = createObservationValueString(lightObservationValueUri, light);
		
		String vibeObsStr = createObservationString(Property.VIBRATION, vibeObservationUri, vibeSensorOutputUri, vibrationSensorUri, observationResultTime);
		String vibeSenOutStr = createSensorOutputString(vibeSensorOutputUri, vibeObservationValueUri);
		String vibeObsValStr = createObservationValueString(vibeObservationValueUri, vibe);
		
		String motObsStr = createObservationString(Property.MOTION, motObservationUri, motSensorOutputUri, motionSensorUri, observationResultTime);
		String motSenOutStr = createSensorOutputString(motSensorOutputUri, motObservationValueUri);
		String motObsValStr = createObservationValueString(motObservationValueUri, mot);
		
		sendUpdate(temperatureObservationUri, tempObsStr);
		sendUpdate(temperatureSensorOutputUri, tempSenOutStr);
		sendUpdate(temperatureObservationValueUri, tempObsValStr);
		
		sendUpdate(lightObservationUri, lightObsStr);
		sendUpdate(lightSensorOutputUri, lightSenOutStr);
		sendUpdate(lightObservationValueUri, lightObsValStr);
		
		sendUpdate(vibeObservationUri, vibeObsStr);
		sendUpdate(vibeSensorOutputUri, vibeSenOutStr);
		sendUpdate(vibeObservationValueUri, vibeObsValStr);
		
		sendUpdate(motObservationUri, motObsStr);
		sendUpdate(motSensorOutputUri, motSenOutStr);
		sendUpdate(motObservationValueUri, motObsValStr);
		
		PrintWriter out = response.getWriter();
		out.write("Thanks!");
		out.flush();
		out.close();
		response.flushBuffer();
	}
	
	private String createObservationString(Property prop, String observationUri, String sensorOutputUri, String sensorUri, String timestamp)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("<" + observationUri + "> a <http://purl.oclc.org/NET/ssnx/ssn#Observation> . \n");
		sb.append("<" + observationUri + "> <http://purl.oclc.org/NET/ssnx/ssn#featureOfInterest> <http://dtp-126.sncs.abdn.ac.uk:8080/Arduino/Office/1/Features/UoA> . \n");
		sb.append("<" + observationUri + "> <http://purl.oclc.org/NET/ssnx/ssn#observationResult> <" + sensorOutputUri + "> . \n");
		sb.append("<" + observationUri + "> <http://purl.oclc.org/NET/ssnx/ssn#observedBy> <" + sensorUri + "> . \n");
		sb.append("<" + observationUri + "> <http://purl.oclc.org/NET/ssnx/ssn#observedProperty> " + getProperty(prop) + " . \n");
		sb.append("<" + observationUri + "> <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> " + timestamp + " . \n");
		sb.append("<" + observationUri + "> <http://purl.oclc.org/NET/ssnx/ssn#observationServerTime> " + System.currentTimeMillis() + " . \n");

		return sb.toString();
	}
	
	private String createSensorOutputString(String uri, String value)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("<" + uri + "> a <http://purl.oclc.org/NET/ssnx/ssn#SensorOutput> . \n");
		sb.append("<" + uri + "> <http://purl.oclc.org/NET/ssnx/ssn#hasValue> <" + value + "> . \n");
		
		return sb.toString();
	}
	
	private String createObservationValueString(String uri, String value)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("<" + uri + "> a <http://purl.oclc.org/NET/ssnx/ssn#ObservationValue> . \n");
		sb.append("<" + uri + "> <http://purl.oclc.org/NET/ssnx/ssn#hasValue> " + value + " . \n");
		
		return sb.toString();
	}
	
	private String getProperty(Property prop)
	{
		switch(prop)
		{
		case TEMPERATURE:
			return "<http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Property/Temperature>";
		case LIGHT:
			return "<http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Property/Light>";
		case VIBRATION:
			return "<http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Property/Vibration>";
		case MOTION:
			return "<http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Property/Motion>";
		case NULL:
			return null;
		}
		return "<http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Property/UnknownProperty>";
	}
	
	private void sendUpdate(String uri, String sparql)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT DATA { ");
		sb.append(sparql);
		sb.append("}");
		
		UpdateRequest request = UpdateFactory.create();
		request.add(sb.toString());
		
		UpdateProcessor update = UpdateExecutionFactory.createRemoteForm(request, endpoint + "/statements");
		update.execute();
		
		System.out.println(uri + " added to repository");
	}
	
	private void cleanup()
	{
		long threshold = (System.currentTimeMillis() - 3600000);
		String query = "DELETE {" + 
					   "	?obs ?p1 ?o1 . " +  
					   "	?so ?p2 ?o2 . " +  
					   "	?ov ?p3 ?o3 . " + 
					   "} WHERE {" +
					   "	?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> ?time . " +
					   "	FILTER (?time < " + threshold + ") . " +
					   "	?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResult> ?so . " +
					   "	?so <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?ov . " +
					   "	?obs ?p1 ?o1 . " + 
					   " 	?so ?p2 ?o2 . " +
					   "	?ov ?p3 ?o3 ." +
					   "}";
		
		System.out.println(query);
		UpdateRequest request = UpdateFactory.create();
		request.add(query);
		UpdateProcessor update = UpdateExecutionFactory.createRemoteForm(request, endpoint + "/statements");
		update.execute();
		
		System.out.println("\nTriple store has been purged.\n");
	}
}
