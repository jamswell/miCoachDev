package miCoach;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main {
	public static void main(String[] args) {
		JSONParser parser = new JSONParser();
		
		Object obj;
		try {
			obj = parser.parse(new FileReader("/home/gugugs/miCoach_dev/json/miCoach20130904_185418.json"));
			JSONObject jsonObject = (JSONObject) obj;
			

//			// loop array
			JSONArray msg = (JSONArray) jsonObject.get("CompletedWorkoutDataPoints");
			Iterator<JSONObject> iterator = msg.iterator();
			JSONObject test = null;
			int counter = 0;
			while (iterator.hasNext()) {
				if (counter == 20)
					break;
				test = iterator.next();
				System.out.println(test.get("TimeFromStart"));
				counter++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
