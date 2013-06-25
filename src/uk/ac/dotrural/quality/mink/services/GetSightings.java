package uk.ac.dotrural.quality.mink.services;

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
 * Servlet implementation class GetSightings
 */
@WebServlet("/GetSightings")
public class GetSightings extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private String endpoint = "http://dtp-126.sncs.abdn.ac.uk:8080/openrdf-sesame/repositories/MinkApp";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetSightings() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT * WHERE {");
		query.append("?s a <http://dtp-126.sncs.abdn.ac.uk/mink/SightingObservation> . ");
		query.append("?s <http://dtp-126.sncs.abdn.ac.uk/mink/x_coord> ?x . ");
		query.append("?s <http://dtp-126.sncs.abdn.ac.uk/mink/y_coord> ?y . ");
		query.append("}");
		
		QueryExecution qe = QueryExecutionFactory.sparqlService(endpoint, query.toString());
		ResultSet rs = qe.execSelect();
		
		int count = 0;
		StringBuilder result = new StringBuilder();
		result.append("{ \"sightings\" : [");
		while(rs.hasNext())
		{
			if(count > 0)
				result.append(",");
			QuerySolution qs = rs.next();
			
			Resource uri = qs.getResource("s");
			Literal xLit = qs.getLiteral("x");
			Literal yLit = qs.getLiteral("y");
			
			result.append("{");
			result.append(" \"uri\" : \"" + uri.getURI() + "\",");
			result.append(" \"x\" : \"" + xLit.getLexicalForm() + "\",");
			result.append(" \"y\" : \"" + yLit.getLexicalForm() + "\"");
			result.append("}");
			
			count++;
		}
		result.append("]}");
		
		PrintWriter out = response.getWriter();
		out.write(result.toString());
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
