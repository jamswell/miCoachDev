package miCoach;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



@SuppressWarnings("unchecked")
public class Main {
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		String jsonFile = "/home/gugugs/miCoach_dev/json/miCoach20130904_185418.json";
		String tcxFile = "/home/gugugs/miCoach_dev/04.09.2013 18_05_30_history.tcx";
		String storeFile = "/home/gugugs/miCoach_dev/hrmConverted.tcx";

		LinkedHashMap<Date, Double> jsonData = new LinkedHashMap<>();
		LinkedHashMap<Date, Long> jsonDataDistance = new LinkedHashMap<>();
		JSONParser parser = new JSONParser();

		try {
//			FileInputStream fis = new FileInputStream(hrmFile);
//			BufferedReader br = new BufferedReader(new InputStreamReader(fis,
//					Charset.forName("UTF-8")));
//			String line = null;
//			String hrmStartTime = null;
//			String hrmDate = null;
//			while ((line = br.readLine()) != null) {
//				if (line.length() > 12) {
//					if (line.substring(0, 4).equals("Date")) {
//						hrmDate = line.substring(5, 13);
//					}
//					if (line.substring(0, 9).equals("StartTime")) {
//						hrmStartTime = line.substring(10, 20);
//					}
//				}
//			}
//			br.close();
//			fis.close();
//
//			Date hrmStartDate = new Date();
//			hrmStartDate.setYear(Integer.parseInt(hrmDate.substring(0, 4)));
//			hrmStartDate
//					.setMonth(Integer.parseInt(hrmDate.substring(4, 6)) - 2);
//			hrmStartDate.setDate(Integer.parseInt(hrmDate.substring(6, 8)));
//
//			hrmStartDate
//					.setHours(Integer.parseInt(hrmStartTime.substring(0, 2)));
//			hrmStartDate.setMinutes(Integer.parseInt(hrmStartTime.substring(3,
//					5)));
//			hrmStartDate.setSeconds(Integer.parseInt(hrmStartTime.substring(6,
//					8)));

			//JSON STUFF!!!!!!!!!!!!!
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
					.substring(5, 7)) - 1);
			jsonDate.setDate(Integer.parseInt(jsonStartDateTimeString
					.substring(8, 10)));
			jsonDate.setHours(Integer.parseInt(jsonStartDateTimeString
					.substring(11, 13)));
			jsonDate.setMinutes(Integer.parseInt(jsonStartDateTimeString
					.substring(14, 16)));
			jsonDate.setSeconds(Integer.parseInt(jsonStartDateTimeString
					.substring(17, 19)));

			JSONArray msg = (JSONArray) jsonObject
					.get("CompletedWorkoutDataPoints");

			Iterator<JSONObject> iterator = msg.iterator();
			JSONObject current = iterator.next();
			Calendar jsonCalendar = Calendar.getInstance();
			jsonCalendar.setTime(jsonDate);
			
			int seconds = ((Double) current.get("TimeFromStart")).intValue();
			while (iterator.hasNext()) {
				if (((Double) current.get("TimeFromStart")).intValue() == seconds) {
					if (current.get("Pace") == null) {
						jsonData.put(jsonCalendar.getTime(), 0.0);
					} else {
						jsonData.put(jsonCalendar.getTime(), (Double) current.get("Pace"));
					}
					
					if (current.get("Distance") == null) {
						jsonDataDistance.put(jsonCalendar.getTime(), new Long(0));
					} else {
						jsonDataDistance.put(jsonCalendar.getTime(), (Long) current.get("Distance"));
					}
					
					jsonCalendar.add(Calendar.SECOND, 5);
					seconds += 5;
					current = iterator.next();
				} else {
					jsonData.put(jsonCalendar.getTime(), 0.0);
					jsonDataDistance.put(jsonCalendar.getTime(), new Long(0));
					jsonCalendar.add(Calendar.SECOND, 5);
					seconds += 5;
				}
			}

			// TCX STUFF!!!!!!!!!!!!
			try {
				File fXmlFile = new File(tcxFile);
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder;
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(fXmlFile);
				
				doc.getDocumentElement().normalize();
			 
				NodeList nList = doc.getElementsByTagName("Trackpoint");
				int jsonCounter = 0;
				Date tcxTime = null;
				String timeString = null;
				Node nNode = null;
				Element eElement = null;
				for (int temp = 0; temp < nList.getLength(); temp++) {
					nNode = nList.item(temp);
					eElement = (Element) nNode;
					timeString = eElement.getElementsByTagName("Time").item(0).getTextContent();
					tcxTime = new Date();
					tcxTime.setYear(Integer.parseInt(timeString.substring(0, 4)));
					tcxTime.setMonth(Integer.parseInt(timeString.substring(5, 7)) - 1);
					tcxTime.setDate(Integer.parseInt(timeString.substring(8, 10)));
					tcxTime.setHours(Integer.parseInt(timeString.substring(11, 13)) + 2);
					tcxTime.setMinutes(Integer.parseInt(timeString.substring(14, 16)));
					tcxTime.setSeconds(Integer.parseInt(timeString.substring(17, 19)));
		
					while (((Entry<Date, Double>) jsonData.entrySet().toArray()[jsonCounter]).getKey().before(tcxTime)) {
						jsonCounter++;
					}
					
					System.out.println("counter " + jsonCounter);
					System.out.println("tcx date " + tcxTime);
					System.out.println("anderes " + ((Entry<Date, Double>) jsonData.entrySet().toArray()[jsonCounter]).getKey());
				
					eElement.getElementsByTagName("Speed").item(0).setTextContent(String.valueOf(((Entry<Date, Double>) jsonData.entrySet().toArray()[jsonCounter]).getValue()));
					eElement.getElementsByTagName("DistanceMeters").item(0).setTextContent(String.valueOf(((Entry<Date, Long>) jsonDataDistance.entrySet().toArray()[jsonCounter]).getValue() / 10));
				
				}				
				
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File(storeFile));
				transformer.transform(source, result);
				System.out.println("File saved!");
				
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			}
			
			// HRM STUFF!!!!!!!!!!!
			// fis = new FileInputStream(hrmFile);
			// br = new BufferedReader(new InputStreamReader(fis,
			// Charset.forName("UTF-8")));
			// line = null;
			// while ((line = br.readLine()) != null) {
			// writeBuffer.append(line + "\n");
			// if (line.length() > 7) {
			// if (line.substring(0, 8).equals("[HRData]")) {
			// break;
			// }
			// }
			// }
			//
			// int digitCounter = 0;
			// Double currentPace = null;
			// while ((line = br.readLine()) != null) {
			// digitCounter = 0;
			// while (Character.isDigit(line.toCharArray()[digitCounter])) {
			// writeBuffer.append(line.toCharArray()[digitCounter]);
			// digitCounter++;
			// }
			//
			// writeBuffer.append("\t");
			// currentPace = jsonData.get(secondCounter);
			// if (currentPace != 0) {
			// writeBuffer.append((60 / jsonData.get(secondCounter)) * 1000);
			// } else {
			// writeBuffer.append("0.0");
			// }
			// writeBuffer.append("\n");
			// secondCounter += 5;
			//
			// }
			// br.close();
			// fis.close();
			//
			// System.out.println(writeBuffer.toString());
			//
			// FileWriter fstream = new FileWriter(storeFile);
			// BufferedWriter out = new BufferedWriter(fstream);
			// out.write(writeBuffer.toString());
			// out.close();
			// fstream.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
