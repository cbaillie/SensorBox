package uk.ac.dotrural.quality.ed.viewer.services;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Servlet implementation class GetDerivedObservations
 */
@WebServlet("/GetDerivedObservations")
public class GetDerivedObservations extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetDerivedObservations() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String time = request.getParameter("time");
		String property = request.getParameter("property");
		String endpoint = request.getParameter("endpoint");
		
		StringBuilder query = new StringBuilder();
		query.append("select * where { ");
		query.append("?s a <http://purl.oclc.org/NET/ssnx/ssn#Observation> . ");
		query.append("?s <http://purl.oclc.org/NET/ssnx/ssn#observedProperty> <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Property/" + property + "> . ");
		query.append("?s <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> " + time + " . ");
		query.append("?s <http://purl.oclc.org/NET/ssnx/ssn#observationResult> ?sOut . ");
		query.append("?s <http://www.w3.org/ns/prov-o/wasDerivedFrom> ?dObs . ");
		query.append("?sOut <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?obsVal . ");
		query.append("?obsVal <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?val . }");
		
		QueryExecution qe = QueryExecutionFactory.sparqlService(endpoint, query.toString());
		ResultSet rs = qe.execSelect();
		
		StringBuilder sb = new StringBuilder();
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
		
		sb.append("}");
		
		PrintWriter out = response.getWriter();
		out.write(sb.toString());
		out.flush();
		out.close();
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
