package miCoach;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Main {
	@SuppressWarnings({ "deprecation", "rawtypes" })
	public static void main(String[] args) {
		String miCoachFile = "/home/gugugs/miCoach_dev/git/miCoachDev/data/badminton/micoach.tcx";
		String storeFile = "/home/gugugs/miCoach_dev/git/miCoachDev/data/badminton/converted.tcx";
		String garminFile = "/home/gugugs/miCoach_dev/git/miCoachDev/data/badminton/garmin.tcx";

		// String miCoachFile =
		// "/home/gugugs/miCoachDev/git/miCoach/data/badminton/micoach.tcx";
		// String storeFile =
		// "/home/gugugs/miCoachDev/git/miCoach/data/badminton/converted.tcx";
		// String garminFile =
		// "/home/gugugs/miCoachDev/git/miCoach/data/badminton/garmin.tcx";

		LinkedHashMap<Date, Integer> heartRateData = new LinkedHashMap<>();

		try {
			// make builders
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			File xmlGarminFile = new File(garminFile);
			File xmlMiCoachFile = new File(miCoachFile);
			Document garminDoc = dBuilder.parse(xmlGarminFile);
			Document miCoachDoc = dBuilder.parse(xmlMiCoachFile);
			garminDoc.getDocumentElement().normalize();
			miCoachDoc.getDocumentElement().normalize();

			// get heartrate Data
			NodeList trackpointNodeList = garminDoc
					.getElementsByTagName("Trackpoint");
			int counter = 0;
			int year, month, day, hours, minutes, seconds, heartrate;
			Calendar calendar = Calendar.getInstance();
			while (counter < trackpointNodeList.getLength()) {
				Element currentElement = (Element) trackpointNodeList
						.item(counter);
				String dateString = currentElement.getElementsByTagName("Time")
						.item(0).getTextContent();
				year = Integer.parseInt(dateString.substring(0, 4));
				month = Integer.parseInt(dateString.substring(5, 7)) - 1;
				day = Integer.parseInt(dateString.substring(8, 10));
				hours = Integer.parseInt(dateString.substring(11, 13)) + 2;
				minutes = Integer.parseInt(dateString.substring(14, 16));
				seconds = Integer.parseInt(dateString.substring(17, 19));
				calendar.set(year, month, day, hours, minutes, seconds);

				heartrate = Integer
						.parseInt(((Element) currentElement
								.getElementsByTagName("HeartRateBpm").item(0))
								.getElementsByTagName("Value").item(0)
								.getTextContent());

				heartRateData.put(calendar.getTime(), heartrate);
				counter++;
			}

			// heartrate data umwandlen fuer pro sekunde
			Object[] heartRateArray = heartRateData.entrySet().toArray();
			Entry first, second = null;
			counter = 0;
			Double heartRateOne = 0.0;
			Double heartRateTwo = 0.0;
			Double steps = 0.0;
			Double heartRateDiff = 0.0;
			LinkedHashMap<Date, Integer> tempMap = new LinkedHashMap<>();
			while (counter + 1 < heartRateArray.length) {
				first = (Entry)heartRateArray[counter];
				second = (Entry)heartRateArray[++counter];
				heartRateOne = Double.parseDouble((((Integer)first.getValue()).toString()));
				heartRateTwo = Double.parseDouble((((Integer)second.getValue()).toString()));
				
				System.out.println("first sec " + first.getKey() + " " + heartRateOne);
				System.out.println("second sec" + second.getKey() + " " + heartRateTwo);
				
				seconds = 0;
				calendar.setTime((Date) first.getKey());
				while (calendar.getTime().before((Date) second.getKey())) {
					calendar.add(Calendar.SECOND, 1);
					seconds++;
				}
				
				heartRateDiff = 0.0;
				steps = 0.0;
				calendar.setTime((Date) first.getKey());
				if (heartRateOne > heartRateTwo) {
					heartRateDiff = heartRateOne - heartRateTwo;
					steps = (Double) (heartRateDiff / seconds);
					
					while (calendar.getTime().before((Date) second.getKey())) {
						System.out.println("save " + calendar.getTime() + " " + (Math.round(heartRateOne)));
						tempMap.put(calendar.getTime(), (int) Math.round(heartRateOne));
						heartRateOne -= steps;
						calendar.add(Calendar.SECOND, 1);
					}
				} else {
					heartRateDiff = heartRateTwo - heartRateOne;
					steps = (Double) (heartRateDiff / seconds);
					
					while (calendar.getTime().before((Date) second.getKey())) {
						System.out.println("save " + calendar.getTime() + " " + (Math.round(heartRateOne)));
						tempMap.put(calendar.getTime(), (int) Math.round(heartRateOne));
						heartRateOne += steps;
						calendar.add(Calendar.SECOND, 1);
					}
				}
			}
			
			heartRateData = tempMap;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
}
