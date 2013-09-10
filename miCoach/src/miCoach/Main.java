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
import java.util.Calendar;
import java.util.Date;
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
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		String xmlFile = "/home/gugugs/miCoachDev/git/miCoach/data/squash/testing/squash.xml";
		String storeFile = "/home/gugugs/miCoachDev/git/miCoach/data/squash/testing/converted.tcx";
		String tcxFile = "/home/gugugs/miCoachDev/git/miCoach/data/squash/testing/garmin.tcx";

		LinkedHashMap<Date, Double> xmlData = new LinkedHashMap<>();
		LinkedHashMap<Date, Double> xmlDataDistance = new LinkedHashMap<>();
		StringBuffer writeBuffer = new StringBuffer();

		try {
			// xml stuff
			File fXmlFile = new File(xmlFile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document xmlDoc = dBuilder.parse(fXmlFile);
			xmlDoc.getDocumentElement().normalize();

			// Datum von xml einlesen
			Date xmlDate = new Date();
			xmlDate.setYear(Integer.parseInt(xmlDoc
					.getElementsByTagName("StartDateTime").item(0)
					.getTextContent().substring(0, 4)));
			xmlDate.setMonth(Integer.parseInt(xmlDoc
					.getElementsByTagName("StartDateTime").item(0)
					.getTextContent().substring(5, 7)) - 1);
			xmlDate.setDate(Integer.parseInt(xmlDoc
					.getElementsByTagName("StartDateTime").item(0)
					.getTextContent().substring(8, 10)));

			xmlDate.setHours(Integer.parseInt(xmlDoc
					.getElementsByTagName("StartDateTime").item(0)
					.getTextContent().substring(11, 13)));
			xmlDate.setMinutes(Integer.parseInt(xmlDoc
					.getElementsByTagName("StartDateTime").item(0)
					.getTextContent().substring(14, 16)));
			xmlDate.setSeconds(Integer.parseInt(xmlDoc
					.getElementsByTagName("StartDateTime").item(0)
					.getTextContent().substring(17, 19)));

			Calendar xmlDateCalendar = Calendar.getInstance();
			xmlDateCalendar.setTime(xmlDate);

			// // Punkte speichern
			NodeList nList = xmlDoc
					.getElementsByTagName("CompletedWorkoutDataPoint");
			int currentSeconds = 0;
			Node nNode = null;
			Element eElement = null;
			Element speedArrayElement = null;
			int seconds = 0;
			int counter = 0;
			while (counter < nList.getLength()) {
				nNode = nList.item(counter);
				eElement = (Element) nNode;

				currentSeconds = ((Double) Double.parseDouble(eElement
						.getElementsByTagName("TimeFromStart").item(0)
						.getTextContent())).intValue();

				while (seconds != currentSeconds) {
					xmlData.put(xmlDateCalendar.getTime(), 0.0);
					xmlDateCalendar.add(Calendar.SECOND, 1);
					seconds++;
				}

				speedArrayElement = (Element) (eElement
						.getElementsByTagName("SpeedArray").item(0));

				xmlData.put(xmlDateCalendar.getTime(), Double
						.parseDouble((speedArrayElement.getElementsByTagName(
								"decimal").item(0).getTextContent())));
				xmlDateCalendar.add(Calendar.SECOND, 1);
				seconds++;

				xmlData.put(xmlDateCalendar.getTime(), Double
						.parseDouble((speedArrayElement.getElementsByTagName(
								"decimal").item(1).getTextContent())));
				xmlDateCalendar.add(Calendar.SECOND, 1);
				seconds++;

				xmlData.put(xmlDateCalendar.getTime(), Double
						.parseDouble((speedArrayElement.getElementsByTagName(
								"decimal").item(2).getTextContent())));
				xmlDateCalendar.add(Calendar.SECOND, 1);
				seconds++;

				xmlData.put(xmlDateCalendar.getTime(), Double
						.parseDouble((speedArrayElement.getElementsByTagName(
								"decimal").item(3).getTextContent())));
				xmlDateCalendar.add(Calendar.SECOND, 1);
				seconds++;

				xmlData.put(xmlDateCalendar.getTime(), Double
						.parseDouble((speedArrayElement.getElementsByTagName(
								"decimal").item(4).getTextContent())));
				xmlDateCalendar.add(Calendar.SECOND, 1);
				seconds++;

				counter++;
			}

			// tcx Stuff
			File ftcxFile = new File(tcxFile);
			dbFactory = DocumentBuilderFactory.newInstance();
			dBuilder = dbFactory.newDocumentBuilder();
			Document tcxDoc = dBuilder.parse(ftcxFile);

			// Punkte speichern
			tcxDoc.getDocumentElement().normalize();
			nList = tcxDoc.getElementsByTagName("Trackpoint");
			Date tcxDate = new Date();
			counter = 0;
			while (counter < nList.getLength()) {
				nNode = nList.item(counter);
				eElement = (Element) nNode;

				tcxDate.setYear(Integer.parseInt(eElement
						.getElementsByTagName("Time").item(0).getTextContent()
						.substring(0, 4)));
				tcxDate.setMonth(Integer.parseInt(eElement
						.getElementsByTagName("Time").item(0).getTextContent()
						.substring(5, 7)) - 1);
				tcxDate.setDate(Integer.parseInt(eElement
						.getElementsByTagName("Time").item(0).getTextContent()
						.substring(8, 10)));
				tcxDate.setHours(Integer.parseInt(eElement
						.getElementsByTagName("Time").item(0).getTextContent()
						.substring(11, 13)) + 2);
				tcxDate.setMinutes(Integer.parseInt(eElement
						.getElementsByTagName("Time").item(0).getTextContent()
						.substring(14, 16)));
				tcxDate.setSeconds(Integer.parseInt(eElement
						.getElementsByTagName("Time").item(0).getTextContent()
						.substring(17, 19)));
				
				System.out.println(tcxDate);

				
				for (Entry currentEntry : xmlData.entrySet()) {
					if (((Date)currentEntry.getKey()).compareTo(tcxDate) == 1) {
						eElement.getElementsByTagName("Speed").item(0)
						.setTextContent(currentEntry.getValue().toString());
						System.out.println("yes");
						break;
					}
				}
				

				counter++;
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(tcxDoc);
			StreamResult result = new StreamResult(new File(storeFile));
			transformer.transform(source, result);

			System.out.println("File saved!");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
}
