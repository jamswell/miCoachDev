package miCoach;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("unchecked")
public class Main {
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		String jsonFile = "/home/gugugs/miCoach_dev/bonsweiher/json";
		// String tcxFile =
		// "/home/gugugs/miCoach_dev/04.09.2013 18_05_30_history.tcx";
		String storeFile = "/home/gugugs/miCoach_dev/bonsweiher/converted.hrm";
		String hrmFile = "/home/gugugs/miCoach_dev/bonsweiher/bonsweiher.hrm";

		LinkedHashMap<Integer, Double> jsonData = new LinkedHashMap<>();
		LinkedHashMap<Integer, Integer> jsonDataDistance = new LinkedHashMap<>();
		JSONParser parser = new JSONParser();
		StringBuffer writeBuffer = new StringBuffer();

		try {
			FileInputStream fis = new FileInputStream(hrmFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis,
					Charset.forName("UTF-8")));
			String line = null;
			String hrmStartTime = null;
			String hrmDate = null;
			while ((line = br.readLine()) != null) {
				if (line.length() > 12) {
					if (line.substring(0, 4).equals("Date")) {
						hrmDate = line.substring(5, 13);
					}
					if (line.substring(0, 9).equals("StartTime")) {
						hrmStartTime = line.substring(10, 20);
					}
				}
			}
			br.close();
			fis.close();

			Date hrmStartDate = new Date();
			hrmStartDate.setYear(Integer.parseInt(hrmDate.substring(0, 4)));
			hrmStartDate
					.setMonth(Integer.parseInt(hrmDate.substring(4, 6)) - 2);
			hrmStartDate.setDate(Integer.parseInt(hrmDate.substring(6, 8)));

			hrmStartDate
					.setHours(Integer.parseInt(hrmStartTime.substring(0, 2)));
			hrmStartDate.setMinutes(Integer.parseInt(hrmStartTime.substring(3,
					5)));
			hrmStartDate.setSeconds(Integer.parseInt(hrmStartTime.substring(6,
					8)));

			// JSON STUFF!!!!!!!!!!!!!
			Object obj;
			obj = parser.parse(new FileReader(jsonFile));
			JSONObject jsonObject = (JSONObject) obj;

			JSONObject workoutInfo = (JSONObject) jsonObject.get("WorkoutInfo");
			String jsonStartDateTimeString = (String) workoutInfo
					.get("StartDateTime");
			Date jsonDate = new Date();
			jsonDate.setYear(Integer.parseInt(jsonStartDateTimeString
					.substring(0, 4)));
			jsonDate.setMonth(Integer.parseInt(jsonStartDateTimeString
					.substring(5, 7)) - 2);
			jsonDate.setDate(Integer.parseInt(jsonStartDateTimeString
					.substring(8, 10)));
			jsonDate.setHours(Integer.parseInt(jsonStartDateTimeString
					.substring(11, 13)));
			jsonDate.setMinutes(Integer.parseInt(jsonStartDateTimeString
					.substring(14, 16)));
			jsonDate.setSeconds(Integer.parseInt(jsonStartDateTimeString
					.substring(17, 19)));
			
			System.out.println(jsonDate);
			System.out.println(hrmStartDate);

			JSONArray msg = (JSONArray) jsonObject
					.get("CompletedWorkoutDataPoints");

			Iterator<JSONObject> iterator = msg.iterator();
			JSONObject current = iterator.next();
			Calendar jsonCalendar = Calendar.getInstance();
			jsonCalendar.setTime(jsonDate);

			int secondCounter = 0;
			while (jsonCalendar.getTime().before(hrmStartDate)) {
				secondCounter++;
				jsonCalendar.add(Calendar.SECOND, 5);
			}
			
			System.out.println("secondCounter " + secondCounter);

			int seconds = ((Double) current.get("TimeFromStart")).intValue();
			System.out.println("iterator " + msg.size());
			while (iterator.hasNext()) {
				System.out.println("seconds " + seconds);
				System.out.println("timefromstart " + (((Double) current.get("TimeFromStart")).intValue()));
				System.out.println("value " + (Double) current.get("Pace"));
				if (((Double) current.get("TimeFromStart")).intValue() <= seconds) {
					if (current.get("Pace") == null) {
						jsonData.put(seconds, 0.0);
					} else {
						jsonData.put(seconds, (Double) current.get("Pace"));
					}
					seconds += 5;
					current = iterator.next();
				} else {
					jsonData.put(seconds, 0.0);
					seconds += 5;
				}
			}
			
			System.out.println("werte geschrieben");

//			int first = 0;
//			int second = 0;
//			int key = 0;
//			for (int i = 1; i < jsonData.size(); i++) {
//				key = ((Entry<Integer, Long>) jsonData.entrySet().toArray()[i - 1]).getKey();
//				first = ((Entry<Integer, Long>) jsonData.entrySet().toArray()[i - 1]).getValue().intValue();
//				second = ((Entry<Integer, Long>) jsonData.entrySet().toArray()[i]).getValue().intValue();
//				System.out.println("first" + first);
//				System.out.println("second" + second);
//				System.out.println("result" + (second - first));
//				System.out.println(second);
//				if (first == 0 || second == 0) {
//					jsonDataDistance.put(key, 0);
//				} else {
//					jsonDataDistance.put(key, (second - first));
//				}
//			}

			// TCX STUFF!!!!!!!!!!!!
			// try {
			// File fXmlFile = new File(tcxFile);
			// DocumentBuilderFactory dbFactory =
			// DocumentBuilderFactory.newInstance();
			// DocumentBuilder dBuilder;
			// dBuilder = dbFactory.newDocumentBuilder();
			// Document doc = dBuilder.parse(fXmlFile);
			//
			// doc.getDocumentElement().normalize();
			//
			// NodeList nList = doc.getElementsByTagName("Trackpoint");
			//

			// for (int temp = 0; temp < nList.getLength(); temp++) {
			// Node nNode = nList.item(temp);
			//
			// Element eElement = (Element) nNode;
			// System.out.println("Time : " +
			// eElement.getElementsByTagName("Time").item(0).getTextContent());
			// }

			// Node nNode = nList.item(0);
			// Element eElement = (Element) nNode;
			// String timeString =
			// eElement.getElementsByTagName("Time").item(0).getTextContent();
			// Date tcxTime = new Date();
			// tcxTime.setYear(Integer.parseInt(timeString.substring(0, 4)));
			// tcxTime.setMonth(Integer.parseInt(timeString.substring(5, 7)));
			// tcxTime.setDate(Integer.parseInt(timeString.substring(8, 10)));
			// tcxTime.setHours(Integer.parseInt(timeString.substring(11, 13)));
			// tcxTime.setMinutes(Integer.parseInt(timeString.substring(14,
			// 16)));
			// tcxTime.setSeconds(Integer.parseInt(timeString.substring(17,
			// 19)));
			//
			//
			//
			// TransformerFactory transformerFactory =
			// TransformerFactory.newInstance();
			// Transformer transformer = transformerFactory.newTransformer();
			// DOMSource source = new DOMSource(doc);
			// StreamResult result = new StreamResult(new File(storeFile));

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);

			// transformer.transform(source, result);
			//
			// System.out.println("File saved!");
			// } catch (ParserConfigurationException e) {
			// e.printStackTrace();
			// } catch (SAXException e) {
			// e.printStackTrace();
			// } catch (TransformerConfigurationException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (TransformerException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			// HRM STUFF!!!!!!!!!!!
			fis = new FileInputStream(hrmFile);
			br = new BufferedReader(new InputStreamReader(fis,
					Charset.forName("UTF-8")));
			line = null;
			while ((line = br.readLine()) != null) {
				writeBuffer.append(line + "\n");
				if (line.length() > 7) {
					if (line.substring(0, 8).equals("[HRData]")) {
						break;
					}
				}
			}

			int digitCounter = 0;
			Double currentPace = 0.0;
			while ((line = br.readLine()) != null) {
				digitCounter = 0;
				while (Character.isDigit(line.toCharArray()[digitCounter])) {
					writeBuffer.append(line.toCharArray()[digitCounter]);
					digitCounter++;
				}

				writeBuffer.append("\t");
				currentPace = jsonData.get(secondCounter);
				if (currentPace != 0) {
					writeBuffer.append((60 / currentPace) * 1000);
				} else {
					writeBuffer.append("0.0");
				}
				writeBuffer.append("\n");
				secondCounter += 5;

			}
			br.close();
			fis.close();

			System.out.println(writeBuffer.toString());

			FileWriter fstream = new FileWriter(storeFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(writeBuffer.toString());
			out.close();
			fstream.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
