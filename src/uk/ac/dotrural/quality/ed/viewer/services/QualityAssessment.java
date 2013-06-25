package uk.ac.dotrural.quality.ed.viewer.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import uk.ac.dotrural.quality.edsensor.observation.AltitudeObservation;
import uk.ac.dotrural.quality.edsensor.observation.GPSObservation;
import uk.ac.dotrural.quality.edsensor.observation.Observation;
import uk.ac.dotrural.quality.edsensor.observation.ObservationType;
import uk.ac.dotrural.reasoning.reasoner.Reasoner;
import uk.ac.dotrural.reasoning.reasoner.ReasonerResult;

/**
 * Servlet implementation class QualityAssessment
 */
@WebServlet("/QualityAssessment")
public class QualityAssessment extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private final String NS = "http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/";
	
	private String qualityRules = "http://dtp-126.sncs.abdn.ac.uk/ontologies/CarWalkTrain/QualityRules.ttl";
	private String provQualRules = "http://dtp-126.sncs.abdn.ac.uk/ontologies/CarWalkTrain/ProvQualityRules.ttl";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public QualityAssessment() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String time = request.getParameter("time");
		String property = request.getParameter("property");
		String dataset = request.getParameter("dataset");
		
		boolean prov = false;
		if(dataset.contains("Provenance"))
			prov = true;

		OntModel model = getObservation(time, property, dataset, prov);
		//model.write(System.out);
		
		Reasoner reasoner;
		if(prov)
			reasoner = new Reasoner(provQualRules, "TTL", false);
		else
			reasoner = new Reasoner(qualityRules, "TTL", false);
		ReasonerResult results = reasoner.performReasoning(model);
		
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		printAllResults(results.ntriples);
		parseResults(out, results.ntriples, results.getDuration(), prov, dataset);
		out.close();
		
		model.removeAll();
		results.ntriples.removeAll();
	}
	
	private OntModel getObservation(String time, String property, String dataset, boolean prov)
	{	
		OntModel model = ModelFactory.createOntologyModel();
		
		String endpoint = "http://dtp-126.sncs.abdn.ac.uk:8080/openrdf-sesame/repositories/" + dataset;
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT * WHERE { ");
		query.append("?s a <http://purl.oclc.org/NET/ssnx/ssn#Observation> . ");
		query.append("?s <http://purl.oclc.org/NET/ssnx/ssn#observedProperty> <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Property/" + property + "> . ");
		query.append("?s <http://purl.oclc.org/NET/ssnx/ssn#featureOfInterest> ?f . ");
		query.append("?s <http://purl.oclc.org/NET/ssnx/ssn#observedBy> ?sens . ");
		query.append("?s <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> " + time + " . ");
		query.append("?s <http://purl.oclc.org/NET/ssnx/ssn#observationResult> ?sOut . ");
		query.append("?sOut <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?obsVal . ");
		
		if(prov)
			query.append("?s <http://www.w3.org/ns/prov-o/wasDerivedFrom> ?obs . ");
		
		if(property.equals("Altitude"))
		{
			query.append("?obsVal <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?val . ");
			query.append("?obsVal <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/satellites> ?sats . ");
		}
		else if(property.equals("Location"))
		{
			query.append("?obsVal <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat . ");
			query.append("?obsVal <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long . ");
			query.append("?obsVal <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/satellites> ?sat . ");
			query.append("?obsVal <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/precision> ?prec . ");
		}
		else
			query.append("?obsVal <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?val . ");
		
		query.append("}");
		
		QueryExecution qe = QueryExecutionFactory.sparqlService(endpoint, query.toString());
		ResultSet rs = qe.execSelect();
		
		while(rs.hasNext())
		{
			QuerySolution qs = rs.next();
			Resource observation = qs.getResource("s");
			Resource foi = qs.getResource("f");
			Resource sens = qs.getResource("sens");
			Resource result = qs.getResource("sOut");
			Resource obsVal = qs.getResource("obsVal");
			Literal val = qs.getLiteral("val");
			
			ArrayList<String> df = new ArrayList<String>();
			if(prov)
			{
				df = getDerivedFromObservations(observation.getURI(), endpoint);
			}
			
			switch(ObservationType.strToObsType(property))
			{
			case ALTITUDE:
				Literal sats = qs.getLiteral("sats");
				AltitudeObservation aObs = new AltitudeObservation(
						observation.getURI(), 
						ObservationType.strToObsType(property), 
						foi.getURI(), 
						sens.getURI(), 
						result.getURI(), 
						obsVal.getURI(), 
						val.getLexicalForm(), 
						sats.getLexicalForm(), 
						"" + time, 
						"0", 
						"",
						df);
				
				model.add(aObs.getRdfModel(NS));
				
				break;
			case GPS:
				Literal lat = qs.getLiteral("lat");
				Literal lon = qs.getLiteral("long");
				Literal sat = qs.getLiteral("sat");
				Literal prec = qs.getLiteral("prec");
				GPSObservation gObs = new GPSObservation(
						observation.getURI(),
						ObservationType.strToObsType(property), 
						foi.getURI(), 
						sens.getURI(), 
						result.getURI(), 
						obsVal.getURI(), 
						lat.getLexicalForm(), 
						lon.getLexicalForm(), 
						sat.getLexicalForm(), 
						prec.getLexicalForm(), 
						time, 
						"0", 
						"");
				
				model.add(gObs.getRdfModel(NS));
				
				break;
			default:
				Observation obs = new Observation(
						observation.getURI(), 
						ObservationType.strToObsType(property),
						foi.getURI(), 
						sens.getURI(), 
						result.getURI(), 
						obsVal.getURI(), 
						val.getLexicalForm(), 
						"" + time, 
						"0", 
						"",
						df);
				
				model.add(obs.getRdfModel(NS));
				
				break;
			}
			

			if(prov)
			{
				for(int i=0;i<df.size();i++)
				{
					String pObs = (String)df.get(i);
					
					int index = endpoint.indexOf("Provenance");
					if(index > 0)
						endpoint = endpoint.substring(0,endpoint.indexOf("Provenance")); 
					String resultTime = getObservationTime(pObs, endpoint);
					
					model.add(getObservation(resultTime, property, endpoint.substring(endpoint.lastIndexOf('/')), false));
				}
			}
				
		}
		return model;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}
	
	private void printAllResults(Model results)
	{
		String query = "SELECT * WHERE {" +
				   "	?s ?p ?o . " + 
				   "}";
		QueryExecution qe = QueryExecutionFactory.create(query, results);
		ResultSet rs = qe.execSelect();
		
		while(rs.hasNext())
		{
			QuerySolution qs = rs.next();
			System.out.println(qs.toString());
		}
	}
	
	private void parseResults(PrintWriter out, Model results, long duration, boolean prov, String dataset)
	{
		String provNs = "http://www.w3.org/ns/prov-o#";
		if(prov)
			provNs = "http://www.w3.org/ns/prov-o/";
		
		String query = "SELECT * WHERE {" +
					   "	?metric <http://abdn.ac.uk/~r01ccb9/Qual-O/metricDescription> ?desc . " +
					   "	?result <http://abdn.ac.uk/~r01ccb9/Qual-O/hasScore> ?score . " +
					   "	?result <http://abdn.ac.uk/~r01ccb9/Qual-O/assessedValue> ?value . " + 
					   "	?result <http://abdn.ac.uk/~r01ccb9/Qual-O/measures> ?dimension . " + 
					   "	?result <http://abdn.ac.uk/~r01ccb9/Qual-O/basedOn> ?metric . " + 
					   "	?result <" + provNs + "wasGeneratedBy> ?asmt . " +
					   "	?asmt <" + provNs + "wasControlledBy> ?agent . " + 
					   "}";
		QueryExecution qe = QueryExecutionFactory.create(query, results);
		ResultSet rs = qe.execSelect();
		
		System.out.println(query);
		
		out.write("{ \"results\" : [");
		int count = 0;
		while(rs.hasNext())
		{
			QuerySolution qs = rs.next();

			Literal desc = qs.getLiteral("desc");
			Literal score = qs.getLiteral("score");
			Literal value = qs.getLiteral("value");
			Resource dimension = qs.getResource("dimension");
			
			if(count > 0)
				out.write(",");
			out.write("{\"dimension\" : \"" + dimension.getLocalName() + "\",");
			out.write("\"score\" : \"" + score.getLexicalForm() + "\",");
			out.write("\"assessedValue\" : \"" + value.getLexicalForm() + "\",");
			out.write("\"description\" : \"" + desc.getLexicalForm() + "\",");
			out.write("\"wasGeneratedBy\" : \"" + UUID.randomUUID() + "\",");
			out.write("\"used\" : [");
			
			String q2 = "SELECT * WHERE {" +
					 "	?result <http://abdn.ac.uk/~r01ccb9/Qual-O/measures> <" + dimension.getURI() + "> . " +
					 "  ?result <" + provNs + "wasGeneratedBy> ?activity . " + 
					 "	?activity <" + provNs + "used> ?entity . " +
					 "	OPTIONAL {" +
					 "		?entity a ?type . " +
					 "	}" +
					 "}";
			
			System.out.println(q2);
			
			QueryExecution qe2 = QueryExecutionFactory.create(q2, results);
			ResultSet rs2 = qe2.execSelect();
			int c2 = 0;
			while(rs2.hasNext())
			{
				QuerySolution qs2 = rs2.next();
				
				//System.out.println(qs2.toString());
				
				Resource entity = qs2.getResource("entity");
				
				String type = "Observation";
				if(qs2.contains("type"))
				{
					Resource t = qs2.getResource("type");
					type = t.getLocalName();
				}
				
				if(c2 > 0){
					out.write(",");
				}
				
				String location = "Aberdeen";
				if(dataset.equals("CityWalk"))
					location = "Edinburgh";
				
				if(entity.getLocalName().equals("Aberdeen"))
				{
						out.write("{\"entity\" : \"" + location + "\",");
				} else
					out.write("{\"entity\" : \"" + entity.getLocalName() + "\",");
				out.write("\"type\" : \"" + type + "\"}");
				
				c2++;
			}
			out.write("],");
			out.write("\"asmtControlledBy\" : \"SensorBox Assessor\"");
			out.write("}");
			
			count++;
		}
		out.write("],");
		out.write("\"assessmentTime\" : " + duration);
		out.write("}");
		out.flush();
	}
	
	private String getObservationTime(String uri, String endpoint)
	{
		String t = "0";
		String query = "SELECT * WHERE {" +
						"<" + uri + "> <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> ?time . " +
						"}";
		
		QueryExecution qe = QueryExecutionFactory.sparqlService(endpoint, query);
		ResultSet rs = qe.execSelect();
		while(rs.hasNext())
		{
			QuerySolution qs = rs.next();
			Literal time = qs.getLiteral("time");
			t = time.getLexicalForm();
		}
		return t;
	}
	
	private ArrayList<String> getDerivedFromObservations(String uri, String endpoint)
	{
		ArrayList<String> df = new ArrayList<String>();
		String query = "SELECT * WHERE {" +
						"<" + uri + "> <http://www.w3.org/ns/prov-o/wasDerivedFrom> ?obs . " +
						"}";
	
		QueryExecution qe = QueryExecutionFactory.sparqlService(endpoint, query);
		ResultSet rs = qe.execSelect();
		while(rs.hasNext())
		{
			QuerySolution qs = rs.next();
			Resource obs = qs.getResource("obs");
			df.add(obs.getURI());
		}
		return df;
	}
}
