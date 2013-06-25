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

/**
 * Servlet implementation class GetLiveObervations
 */
@WebServlet("/GetLiveObservations")
public class GetLiveObservations extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final String ENDPOINT = "http://dtp-126.sncs.abdn.ac.uk:8080/openrdf-sesame/repositories/OfficeArduino";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetLiveObservations() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
{
		String t = "0";
		
		String property = request.getParameter("property");
		if(request.getParameterMap().containsKey("time"))
			t = request.getParameter("time");
		
		if(property.length() == 0)
			return;
		
		StringBuilder query = new StringBuilder();
			query.append("SELECT DISTINCT * WHERE { ");
			query.append("	?obs a <http://purl.oclc.org/NET/ssnx/ssn#Observation> . ");
			query.append("	?obs <http://purl.oclc.org/NET/ssnx/ssn#observedProperty> <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Property/" + property + "> . ");
			query.append("	?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> ?time . ");
			query.append("	?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResult> ?sOut . ");
			query.append("	?sOut <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?obsVal . ");
			query.append("	?obsVal <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?val . ");
			query.append("	FILTER( ?time > " + t + ") . ");
			query.append("} ");
			
		System.out.println(query.toString());
		
		PrintWriter out = response.getWriter();
		
		try
		{
			int count = 0;
			QueryExecution qe = QueryExecutionFactory.sparqlService(ENDPOINT, query.toString());
			ResultSet rs = qe.execSelect();
			StringBuilder sb = new StringBuilder();
			
			sb.append("[");		
			
			while(rs.hasNext())
			{
				QuerySolution qs = rs.next();
				
				Literal time = qs.getLiteral("time");
				Literal value = qs.getLiteral("val");
					
					if(count > 0)
						sb.append(",");
					writeLine(sb, time.getLexicalForm(), value.getLexicalForm());
					
					count++;	
			}
			
			sb.append("]");
			out.write(sb.toString());
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			out.write("Exception: " + ex);
			out.flush();
		}
		finally
		{
			out.flush();
			out.close();
			response.flushBuffer();
		}	
	}
	
	private void writeLine(StringBuilder sb, String time, String value)
	{
		Long t = Long.parseLong(time);
		//t += 3600000;
		
		double valDbl = Double.parseDouble(value);
		
		if(valDbl < -25.0)
			valDbl = 0.0;
		
		sb.append("[");
		sb.append(t);
		sb.append(", ");
		sb.append(valDbl);
		sb.append("]");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
}
