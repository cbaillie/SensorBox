package uk.ac.dotrural.quality.ed.viewer.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import uk.ac.dotrural.quality.edsensor.observation.AccelerometerObservation;
import uk.ac.dotrural.quality.edsensor.observation.AltitudeObservation;
import uk.ac.dotrural.quality.edsensor.observation.GPSObservation;
import uk.ac.dotrural.quality.edsensor.observation.Observation;
import uk.ac.dotrural.quality.edsensor.observation.ObservationType;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Servlet implementation class GetObservationRDF
 */
@WebServlet("/GetObservationRDF")
public class GetObservationRDF extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final String NS = "http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetObservationRDF() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String time = request.getParameter("time");
		String property = request.getParameter("property");
		String endpoint = request.getParameter("endpoint");
		String direction = null;
		
		if(request.getParameterMap().containsKey("direction"))
		{
			direction = request.getParameter("direction");
		}
		System.out.println("Direction is " + direction);
		
		PrintWriter out = response.getWriter();
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT * WHERE { ");
		query.append("?s a <http://purl.oclc.org/NET/ssnx/ssn#Observation> . ");
		query.append("?s <http://purl.oclc.org/NET/ssnx/ssn#observedProperty> <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Property/" + property + "> . ");
		query.append("?s <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> " + time + " . ");
		query.append("?s <http://purl.oclc.org/NET/ssnx/ssn#observationServerTime> ?sTime . " );
		query.append("?s <http://purl.oclc.org/NET/ssnx/ssn#observedBy> ?sens . ");
		query.append("?s <http://purl.oclc.org/NET/ssnx/ssn#featureOfInterest> ?foi . ");
		query.append("?s <http://purl.oclc.org/NET/ssnx/ssn#observationResult> ?sOut . ");
		query.append("?sOut <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?obsVal . ");
		
		switch(ObservationType.strToObsType(property))
		{
		case ACCELERATION:
			query.append("?s <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Event> ?event . ");
			query.append("?obsVal <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?val . ");
			query.append("?obsVal <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/AccelerationDirection> \"" + direction + "\" . }");
			break;
		case ALTITUDE:
			query.append("?s <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Event> ?event . ");
			query.append("?obsVal <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?val . ");
			query.append("?obsVal <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/satellites> ?sats . }");
			break;
		case GPS:
			query.append("	?obsVal <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat . "); 
			query.append("?obsVal <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long . ");
			query.append("?obsVal <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/satellites> ?sats . ");  
			query.append("?obsVal <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/precision> ?prec . ");
			query.append("?s <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Event> ?event . }"); 
			break;
		default:
			if(property.equals("Speed"))
				query.append("?s <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Event> ?event . ");
			else
				query.append("?s <http://dtp-126.sncs.abdn.ac.uk/quality/CarSensor/Event> ?event . ");
			
			query.append("?obsVal <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?val . }");	
			break;
		}
		
		System.out.println(query.toString());
		
		QueryExecution qe = QueryExecutionFactory.sparqlService(endpoint, query.toString());
		ResultSet rs = qe.execSelect();
		
		while(rs.hasNext())
		{
			QuerySolution qs = rs.next();
			Resource uri = qs.getResource("s");
			Resource foi = qs.getResource("foi");
			Resource sens = qs.getResource("sens");
			Resource so = qs.getResource("sOut");
			Resource ov = qs.getResource("obsVal");
			Literal sTime = qs.getLiteral("sTime");
			Literal event = qs.getLiteral("event");
			
			Literal val;
			
			switch(ObservationType.strToObsType(property))
			{
			case ACCELERATION:
				val = qs.getLiteral("val");
				AccelerometerObservation alObs = new AccelerometerObservation(
							uri.getURI(),
							ObservationType.strToObsType(property),
							foi.getURI(),
							sens.getURI(),
							so.getURI(),
							ov.getURI(),
							val.getLexicalForm(),
							direction,
							time,
							sTime.getLexicalForm(),
							event.getLexicalForm()
						);
					writeModel(alObs.getRdfModel(NS),out);
				break;
			case ALTITUDE:
				val = qs.getLiteral("val");
				Literal sats = qs.getLiteral("sats");
				AltitudeObservation aobs = new AltitudeObservation(
							uri.getURI(),
							ObservationType.strToObsType(property),
							foi.getURI(),
							sens.getURI(),
							so.getURI(),
							ov.getURI(),
							val.getLexicalForm(),
							sats.getLexicalForm(),
							time,
							sTime.getLexicalForm(),
							event.getLexicalForm()
						);
				writeModel(aobs.getRdfModel(NS), out);
				break;
			case GPS:
				Literal lat = qs.getLiteral("lat");
				Literal lon = qs.getLiteral("long");
				Literal sat = qs.getLiteral("sats");
				Literal prec = qs.getLiteral("prec");
				
				//String id, ObservationType property, String foi, String obsBy, String result, String obsVal, String lat, String lon, String sats, String prec, String rTime, String sTime, String event)
				GPSObservation gps = new GPSObservation(
							uri.getURI(),
							ObservationType.strToObsType(property),
							foi.getURI(),
							sens.getURI(),
							so.getURI(),
							ov.getURI(),
							lat.getLexicalForm(),
							lon.getLexicalForm(),
							sat.getLexicalForm(),
							prec.getLexicalForm(),
							time,
							sTime.getLexicalForm(),
							event.getLexicalForm()
						);
				writeModel(gps.getRdfModel(NS), out);
				break;
			default:
				System.out.println("Writing Observation object...");
				val = qs.getLiteral("val");
				//String id, ObservationType property, String foi, String obsBy, String result, String obsVal, String value, String rTime, String sTime, String event
				Observation obs = new Observation(
							uri.getURI(),
							ObservationType.strToObsType(property),
							foi.getURI(),
							sens.getURI(),
							so.getURI(),
							ov.getURI(),
							val.getLexicalForm(),
							time,
							sTime.getLexicalForm(),
							event.getLexicalForm()
						);
				writeModel(obs.getRdfModel(NS), out);
				break;
			}
		}
		
		/*StringBuilder sb = new StringBuilder();
		sb.append("{");			
		
		QuerySolution qs = rs.next();
		
		Resource uri = qs.getResource("s");
		Literal value = qs.getLiteral("val");
		Resource df = qs.getResource("dObs");
		
		sb.append("\"uri\" : \"" + uri.getURI() + "\",");
		sb.append("\"time\" : \"" + TimeUtilities.parseTime(time) + "\",");
		sb.append("\"value\" : \"" + value.getLexicalForm() + "\",");
		
		sb.append("\"derivedFrom\" : [");
		sb.append("\"" + df.getURI() + "\"");
		
		while(rs.hasNext())
		{
			qs = rs.next();
			df = qs.getResource("dObs");
			sb.append(",\"" + df.getURI() + "\"");
		}
		
		sb.append("]");
		
		sb.append("}");*/
		
		//out.write(sb.toString());
		out.flush();
		out.close();
	}
	
	private void writeModel(OntModel model, PrintWriter out)
	{
		String syntax = "RDF/XML-ABBREV"; // also try "N-TRIPLE" and "TURTLE"
		StringWriter sw = new StringWriter();
		model.write(sw, syntax);
		
		String r = StringEscapeUtils.escapeHtml(sw.toString());
		out.write(r);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}
}
