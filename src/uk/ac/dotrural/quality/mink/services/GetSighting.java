package uk.ac.dotrural.quality.mink.services;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.dotrural.quality.mink.observation.Sighting;
import uk.ac.dotrural.reasoning.reasoner.Reasoner;
import uk.ac.dotrural.reasoning.reasoner.ReasonerResult;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Servlet implementation class GetSighting
 */
@WebServlet("/GetSighting")
public class GetSighting extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private String endpoint = "http://dtp-126.sncs.abdn.ac.uk:8080/openrdf-sesame/repositories/MinkApp";
	private String rules = "http://dtp-126.sncs.abdn.ac.uk/ontologies/MinkApp/Mink.ttl";
	private String notation = "TTL";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetSighting() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		String uri = request.getParameter("sightingUri");
		
		StringBuilder query = new StringBuilder();
		query.append("PREFIX ssn: <" + Data.SSNNS + "> ");
		query.append("PREFIX mink: <" + Data.MINKNS + "> ");
		query.append("SELECT * WHERE {");
		query.append("	<" + uri + "> ssn:featureOfInterest ?foi . ");
		query.append("  <" + uri + "> mink:x_coord ?x . ");
		query.append("  <" + uri + "> mink:y_coord ?y . ");
		query.append("	<" + uri + "> ssn:observationResultTime ?rTime . ");
		query.append("	<" + uri + "> ssn:observationSamplingTime ?sTime . ");
		query.append("  <" + uri + "> <" + Data.PROVNS.concat("wasAttributedTo") + "> ?agent . ");
		query.append("  <" + uri + "> ssn:observationResult ?result . ");
		query.append("  ?result ssn:hasValue ?value . ");
		query.append("  ?value mink:count ?count . ");
		query.append("  ?value mink:status ?status . ");
		query.append("  ?foi mink:name ?riverName . ");
		query.append("  ?agent <" + Data.FOAFNS.concat("member") + "> ?group . ");
		query.append("	OPTIONAL {");
		query.append("    ?agent <" + Data.FOAFNS.concat("name") + "> ?agentName . ");
		query.append("	}");
		query.append("}");
		
		System.out.println(query.toString());
		
		QueryExecution qe = QueryExecutionFactory.sparqlService(endpoint, query.toString());
		ResultSet rs = qe.execSelect();
		
		while(rs.hasNext())
		{
			String assessmentResults = assessQuality(uri);
			
			QuerySolution qs = rs.next();
			
			Literal x = qs.getLiteral("x");
			Literal y = qs.getLiteral("y");
			Literal resultTime = qs.getLiteral("rTime");
			Literal samplingTime = qs.getLiteral("sTime");
			Literal count = qs.getLiteral("count");
			Literal status = qs.getLiteral("status");
			Literal riverName = qs.getLiteral("riverName");
			Resource group = qs.getResource("group");
			Literal agentName = null;
			
			if(qs.contains("agentName"))
				agentName = qs.getLiteral("agentName");
			
			StringBuilder result = new StringBuilder();
			result.append("{ ");
			result.append("  \"x\" : \"" + x.getLexicalForm().substring(0,6) + "\", ");
			result.append("  \"y\" : \"" + y.getLexicalForm().substring(0,6) + "\", ");
			result.append("  \"resultTime\" : \"" + resultTime.getLexicalForm() + "\", ");
			result.append("  \"samplingTime\" : \"" + samplingTime.getLexicalForm() + "\", ");
			result.append("  \"count\" : \"" + count.getLexicalForm() + "\", ");
			result.append("  \"status\" : \"" + status.getLexicalForm() + "\", ");
			result.append("  \"foi\" : {");
			result.append("    \"name\" : \"" + riverName.getLexicalForm() + "\"");
			result.append("  },");
			result.append("  \"agent\" : {");
			if(agentName != null)
				result.append("    \"name\" : \"" + agentName.getLexicalForm() + "\",");
			else
				result.append("    \"name\" : \"unknown\",");
			result.append("    \"group\" : \"" + group.getLocalName() + "\"");
			result.append("  },");
			result.append(assessmentResults);
			result.append("}");
			
			PrintWriter out = response.getWriter();
			out.write(result.toString());
			out.flush();
			out.close();
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
	private String assessQuality(String uri)
	{
		Sighting s = getSighting(uri);
		
		Reasoner r = new Reasoner(rules, notation, false);
		ReasonerResult rr = r.performReasoning(s.toOntModel());
		
		String results = buildResultString(rr.ntriples, rr.getDuration());
		
		return results;
	}
	
	private Sighting getSighting(String uri)
	{
		String query = "PREFIX ssn: <http://purl.oclc.org/NET/ssnx/ssn#> " +
					   "PREFIX mink: <http://dtp-126.sncs.abdn.ac.uk/mink/> " +
					   "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " + 
					   "PREFIX prov: <http://www.w3.org/ns/prov#> " +
					   "SELECT * WHERE { " + 
							"<" + uri + "> ssn:featureOfInterest ?foi . " +  
							"<" + uri + "> mink:x_coord ?x . " + 
							"<" + uri + "> mink:y_coord ?y . " +
							"<" + uri + "> prov:wasAttributedTo ?agent . " +    
						    "<" + uri + "> ssn:observationResultTime ?rTime . " +  
						    "<" + uri + "> ssn:observationSamplingTime ?sTime . " + 
							"<" + uri + "> ssn:observationResult ?result . " +
							"?result ssn:hasValue ?value . " +
							"?value mink:count ?count . " + 
							"?value mink:status ?status . " + 
							"?foi mink:name ?riverName . " + 
							"?agent foaf:member ?group . " +
							"OPTIONAL { " + 
								"?agent foaf:name ?agentName . " + 	
							"}" + 
						"}";
		
		System.out.println(query);
		
		QueryExecution qe = QueryExecutionFactory.sparqlService(endpoint, query);
		ResultSet rs = qe.execSelect();
		QuerySolution qs = rs.next();
		
		Resource group = qs.getResource("group");
		Resource agent = qs.getResource("agent");
		
		Literal rTime = qs.getLiteral("rTime");
		Literal riverName = qs.getLiteral("riverName");
		Literal x = qs.getLiteral("x");
		Literal y = qs.getLiteral("y");
		Literal count = qs.getLiteral("count");
		Literal status = qs.getLiteral("status");
		Literal sTime = qs.getLiteral("sTime");
		
		String cCode = "unknown";
		String cName = "unknown";
		
		if(qs.contains("agentName"))
			cName = qs.getLiteral("agentName").getLexicalForm();
		else
			cCode = agent.getLocalName();
		
		Sighting s = new Sighting(
						uri.substring(11),
						rTime.getLexicalForm(),
						group.getLocalName(),
						cCode,
						cName,
						"unknown",
						"",
						"",
						riverName.getLexicalForm(),
						x.getLexicalForm(),
						y.getLexicalForm(),
						count.getLexicalForm(),
						status.getLexicalForm(),
						null,
						null,
						sTime.getLexicalForm()
					);
		return s;
	}
	
	private String buildResultString(Model results, long duration)
	{
		String query =  "PREFIX qual: <http://abdn.ac.uk/~r01ccb9/Qual-O/>" + 
						"SELECT * WHERE {" +
							"?result qual:hasScore ?score . " +
							"?result qual:basedOn ?metric . " +
							"?metric qual:metricDescription ?desc . " +
							"?metric qual:measures ?dimension . " +
							"?dimension a ?dimName . " +
						"}";
		
		QueryExecution qe = QueryExecutionFactory.create(query, results);
		ResultSet rs = qe.execSelect();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("\"results\" : [");
		int count = 0;
		while(rs.hasNext())
		{
			QuerySolution qs = rs.next();
			
			System.out.println(qs.toString());
			
			Resource dimension = qs.getResource("dimName");
			Literal score = qs.getLiteral("score");
			Literal desc = qs.getLiteral("desc");
			
			if(count > 0)
				sb.append(", ");
			sb.append("{ \"dimension\" : \"");
			sb.append(dimension.getLocalName());
			sb.append("\", ");
			sb.append("\"score\" : \"");
			sb.append(score.getLexicalForm());
			sb.append("\", ");
			sb.append("\"desc\" : \"");
			sb.append(desc.getLexicalForm());
			sb.append("\"}");
			
			count++;
		}
		sb.append("], \"duration\" : \"");
		sb.append(duration);
		sb.append("\"");
		
		return sb.toString();
	}

}
