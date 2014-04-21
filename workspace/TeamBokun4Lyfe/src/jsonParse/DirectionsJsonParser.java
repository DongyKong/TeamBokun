package jsonParse;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;

public class DirectionsJsonParser {
	int[] timeToDest;		//	timeToDest[0] = minutes		timeToDest[1] = hours
	double[] originCoord;
	double[] destCoord;
	double distance;
	String instructions;
	
	public DirectionsJsonParser() {
		timeToDest = new int[2];
		originCoord = new double[2];
		destCoord = new double[2];
		distance = 0;
		instructions = "";
	}
	
	@Override
	public String toString() {
		String line = "Starting point: (" + originCoord[0] + ", " + originCoord[1] + ")\n";
		line += "Ending point: (" + destCoord[0] + ", " + destCoord[1] + ")\n";
		line += "Distance: " + distance + " miles\n";
		line += "Time to destination: " + timeToDest[1] + " hours and " + timeToDest[0] + " minutes";
		
		return line;
	}

	//	Sends request for directions from start to end
	public void requestDirections(String start, String end) {
		//	Replace spaces or commas with pluses
		start = start.replaceAll(",\\s", "+");
		start = start.replaceAll("\\s|,", "+");
		end = end.replaceAll(",\\s", "+");
		end = end.replaceAll("\\s|,", "+");
		
		BufferedReader br = null;
		try {
			URL                url; 
		    URLConnection      connection; 
		    DataInputStream    dataStreamer;
	
		    url = new URL("http://maps.googleapis.com/maps/api/directions/json?origin=" + start + "&destination=" + end + "&sensor=false");
		    connection = url.openConnection(); 
		    connection.setDoInput(true); 
		    connection.setUseCaches(false);
	
		    dataStreamer = new DataInputStream(connection.getInputStream()); 
		    br = new BufferedReader(new InputStreamReader(dataStreamer));
			
		    String line;
		    String feedback = "";
		    while((line = br.readLine()) != null)
		    	feedback += line;
		    feedback = feedback.replaceAll("\\s+", "");
		    
		    parseManually(feedback);
		}
		catch(MalformedURLException e) { e.printStackTrace(); }
		catch(IOException e) { e.printStackTrace(); }
	}
	
	//	Manually parses JSON file for necessary information
	private void parseManually(String json) {
		boolean distanceFound = false, durationFound = false, endPointFound = false;
		boolean startPointFound = false;
		
		for(int i = 0; i < json.length(); i++) {
			//	Search for distance
			if((i+19) < json.length() && json.substring(i, i+19).equals("distance\":{\"text\":\"") && !distanceFound) {
				distanceFound = true;
				i = i+19;
				int j = i;
				while(json.substring(j, j+1).matches("[0-9]|\\.")) { j++; }
				distance = Double.parseDouble(json.substring(i, j));
			}
			
			//	Search for duration
			if((i+19) < json.length() && json.substring(i, i+19).equals("duration\":{\"text\":\"") && !durationFound) {
				durationFound = true;
				i = i+19;
				int j = i;
				while(!json.substring(j, j+1).equals("\"")) { j++; }
				
				//	Parse hours and minutes
				String time = json.substring(i, j);
				//	Get hours
				if(time.contains("hours")) {
					j = 0;
					while(time.substring(j, j+1).matches("[0-9]")) { j++; }
					timeToDest[1] = Integer.parseInt(time.substring(0, j));
					
					time = time.substring(j + 5);	//	Cut off hours
				}
				else
					timeToDest[1] = 0;
				
				//	Get minutes
				for(j = 0; time.substring(j, j+1).matches("[0-9]"); j++) {}
				timeToDest[0] = Integer.parseInt(time.substring(0, j));
			}
			
			//	Search for end location
			if((i+21) < json.length() && json.substring(i, i+21).equals("end_location\":{\"lat\":") && !endPointFound) {
				endPointFound = true;
				
				i = i+21;
				int j = i;
				
				//	Grab latitude
				while(!json.substring(j, j+1).equals(",")) { j++; }
				destCoord[0] = Double.parseDouble(json.substring(i, j));
				
				//	Grab longitude
				i = j + 7;
				j = i;
				while(!json.substring(j, j+1).equals("}")) { j++; }
				destCoord[1] = Double.parseDouble(json.substring(i, j));
			}
			
			//	Search for start location
			if((i+23) < json.length() && json.substring(i, i+23).equals("start_location\":{\"lat\":") && !startPointFound) {
				startPointFound = true;
				
				i = i+23;
				int j = i;
				
				//	Grab latitude
				while(!json.substring(j, j+1).equals(",")) { j++; }
				originCoord[0] = Double.parseDouble(json.substring(i, j));
				
				//	Grab longitude
				i = j + 7;
				j = i;
				while(!json.substring(j, j+1).equals("}")) { j++; }
				originCoord[1] = Double.parseDouble(json.substring(i, j));
			}
		}
	}
	
	//	GETTERS AND SETTERS
	public int[] getTimeToDest() { return timeToDest; }
	public void setTimeToDest(int[] timeToDest) { this.timeToDest = timeToDest; }

	public double[] getOriginCoord() { return originCoord; }
	public void setOriginCoord(double[] originCoord) { this.originCoord = originCoord; }

	public double[] getDestCoord() { return destCoord; }
	public void setDestCoord(double[] destCoord) { this.destCoord = destCoord; }

	public double getDistance() { return distance; }
	public void setDistance(double distance) { this.distance = distance; }

	public String getInstructions() { return instructions; }
	public void setInstructions(String instructions) { this.instructions = instructions; }

	public static void main(String[] args) {
		DirectionsJsonParser yay = new DirectionsJsonParser();
		yay.requestDirections("15371 karl avenue, monte sereno", "3131 mcclintock street, los angeles");
		System.out.println(yay.toString());
	}
}
