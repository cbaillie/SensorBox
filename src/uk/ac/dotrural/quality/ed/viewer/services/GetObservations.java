package uk.ac.dotrural.quality.ed.viewer.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

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
 * Servlet implementation class GetTemperatures
 */
@WebServlet("/GetObservations")
public class GetObservations extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetObservations() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String property = request.getParameter("property");
		String endpoint = request.getParameter("endpoint");
		
		ArrayList<String> x = new ArrayList<String>();
		ArrayList<String> y = new ArrayList<String>();
		ArrayList<String> z = new ArrayList<String>();
		ArrayList<String> times = new ArrayList<String>();
		
		if(property.length() == 0)
			return;
		
		StringBuilder query = new StringBuilder();
			query.append("select distinct * where { ");
			query.append("?obs a <http://purl.oclc.org/NET/ssnx/ssn#Observation> . ");
			query.append("?obs <http://purl.oclc.org/NET/ssnx/ssn#observedProperty> <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Property/" + property + "> . ");
			query.append("?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> ?time . ");
			query.append("?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResult> ?sOut . ");
			query.append("?sOut <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?obsVal . ");
			
			if(property.equals("Acceleration"))
			{
				query.append("?obsVal <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?val . ");
				query.append("?obsVal <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/AccelerationDirection> ?direction ");
			}
			else if(property.equals("Location"))
			{
				query.append("?obsVal <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat . ");
				query.append("?obsVal <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?lon . ");
				query.append("?obsVal <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/satellites> ?satellites . ");
				query.append("?obsVal <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/precision> ?precision");
			}
			else
				query.append("?obsVal <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?val . ");
			
			query.append("} ");
		
		PrintWriter out = response.getWriter();
		
		try
		{
			int count = 0;
			QueryExecution qe = QueryExecutionFactory.sparqlService(endpoint, query.toString());
			ResultSet rs = qe.execSelect();
			StringBuilder sb = new StringBuilder();
			
			sb.append("[");		
			
			if(!property.equals("Location") && !property.equals("Acceleration"))
				sb.append("[");
			
			while(rs.hasNext())
			{
				QuerySolution qs = rs.next();
				
				Literal time = qs.getLiteral("time");

				if(property.equals("Acceleration"))
				{	
					Literal val = qs.getLiteral("val");
					Literal dir = qs.getLiteral("direction");
					
					if(dir.getLexicalForm().equals("X"))
						x.add(val.getLexicalForm());
					else if(dir.getLexicalForm().equals("Y"))
						y.add(val.getLexicalForm());
					else if(dir.getLexicalForm().equals("Z"))
						z.add(val.getLexicalForm());
					times.add(time.getLexicalForm());
					
					count++;	
					
				} else if(property.equals("Location")){
					
					Literal lat = qs.getLiteral("lat");
					Literal lon = qs.getLiteral("lon");
					Literal satellites = qs.getLiteral("satellites");
					Literal precision = qs.getLiteral("precision");
					if(count > 0)
						sb.append(",");
					writeLine(sb, time.getLexicalForm(), lat.getLexicalForm(), lon.getLexicalForm(), satellites.getLexicalForm(), precision.getLexicalForm());
					count++;
					
				} else {
					
					Literal value = qs.getLiteral("val");
					
					if(count > 0)
						sb.append(",");
					writeLine(sb, time.getLexicalForm(), value.getLexicalForm());
					
					count++;	
				}
			}
			
			if(property.equals("Acceleration"))
			{
				sb.append("[");
				for(int i=0;i<x.size();i++)
				{
					String val = (String)x.get(i);
					String time = (String)times.get(i);
					if(i > 0)
						sb.append(", ");
						
					writeLine(sb, time, val);
				}
				sb.append("], [");
				for(int i=0;i<y.size();i++)
				{
					String val = (String)y.get(i);
					String time = (String)times.get(i);
					if(i > 0)
						sb.append(", ");
						
					writeLine(sb, time, val);
				}
				sb.append("], [");
				for(int i=0;i<z.size();i++)
				{
					String val = (String)z.get(i);
					String time = (String)times.get(i);
					if(i > 0)
						sb.append(", ");
					writeLine(sb, time, val);
				}
				sb.append("]");
			}
			
			if(!property.equals("Location") && !property.equals("Acceleration"))
			{
				sb.append("]");
				checkForDerivedObservations(sb, endpoint.concat("Provenance"), property);
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
	
	private void writeLine(StringBuilder sb, String time, String lat, String lon, String satellites, String precision)
	{
		sb.append("[");
		sb.append(time);
		sb.append(", ");
		sb.append(lat);
		sb.append(", ");
		sb.append(lon);
		sb.append(", ");
		sb.append(satellites);
		sb.append(", ");
		sb.append(precision);
		sb.append("]");		
	}
	
	private void checkForDerivedObservations(StringBuilder sb, String endpoint, String property)
	{
		int count = 0;
		StringBuilder query = new StringBuilder();
		query.append("select distinct * where { ");
		query.append("?obs a <http://purl.oclc.org/NET/ssnx/ssn#Observation> . ");
		query.append("?obs <http://purl.oclc.org/NET/ssnx/ssn#observedProperty> <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Property/" + property + "> . ");
		query.append("?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> ?time . ");
		query.append("?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResult> ?sOut . ");
		query.append("?sOut <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?obsVal . ");
		query.append("?obsVal <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?val . }");
		
		QueryExecution qe = QueryExecutionFactory.sparqlService(endpoint, query.toString());
		ResultSet rs = qe.execSelect();
		
		sb.append(", [");			
		
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
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		out.write("Not implemented. Use GET instead.");
		out.flush();
		out.close();
	}

}
