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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Main {
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		String xmlFile = args[0];
		String storeFile = "converted.hrm";
		String hrmFile = args[1];

		LinkedHashMap<Integer, Double> xmlData = new LinkedHashMap<>();
		StringBuffer writeBuffer = new StringBuffer();

		// Datum von hrm file lesen
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
					.setMonth(Integer.parseInt(hrmDate.substring(4, 6)) - 1);
			hrmStartDate.setDate(Integer.parseInt(hrmDate.substring(6, 8)));

			hrmStartDate
					.setHours(Integer.parseInt(hrmStartTime.substring(0, 2)));
			hrmStartDate.setMinutes(Integer.parseInt(hrmStartTime.substring(3,
					5)));
			hrmStartDate.setSeconds(Integer.parseInt(hrmStartTime.substring(6,
					8)));

			// xml stuff
			File fXmlFile = new File(xmlFile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();

			// Datum von xml einlesen
			Date xmlDate = new Date();
			xmlDate.setYear(Integer.parseInt(doc
					.getElementsByTagName("StartDateTime").item(0)
					.getTextContent().substring(0, 4)));
			xmlDate.setMonth(Integer.parseInt(doc
					.getElementsByTagName("StartDateTime").item(0)
					.getTextContent().substring(5, 7)) - 1);
			xmlDate.setDate(Integer.parseInt(doc
					.getElementsByTagName("StartDateTime").item(0)
					.getTextContent().substring(8, 10)));

			xmlDate.setHours(Integer.parseInt(doc
					.getElementsByTagName("StartDateTime").item(0)
					.getTextContent().substring(11, 13)));
			xmlDate.setMinutes(Integer.parseInt(doc
					.getElementsByTagName("StartDateTime").item(0)
					.getTextContent().substring(14, 16)));
			xmlDate.setSeconds(Integer.parseInt(doc
					.getElementsByTagName("StartDateTime").item(0)
					.getTextContent().substring(17, 19)));

			// Zeitdifferenz herausfinden
			Calendar xmlCalendar = Calendar.getInstance();
			xmlCalendar.setTime(xmlDate);
			int secondCounter = 0;
			while (xmlCalendar.getTime().before(hrmStartDate)) {
				xmlCalendar.add(Calendar.SECOND, 5);
				secondCounter += 5;
			}

			// Punkte speichern
			NodeList nList = doc
					.getElementsByTagName("CompletedWorkoutDataPoint");
			int currentTime = 0;
			Node nNode = null;
			Element eElement = null;
			int seconds = 0;
			int counter = 0;
			while (counter < nList.getLength()) {
				nNode = nList.item(counter);
				eElement = (Element) nNode;

				currentTime = ((Double) Double.parseDouble(eElement
						.getElementsByTagName("TimeFromStart").item(0)
						.getTextContent())).intValue();

				if (currentTime <= seconds) {
					xmlData.put(
							seconds,
							(Double) Double.parseDouble(eElement
									.getElementsByTagName("Pace")
									.item(0).getTextContent()));
					counter++;
				} else {
					xmlData.put(seconds, 0.0);
				}
				seconds += 5;
			}

			for (Entry<Integer, Double> test : xmlData.entrySet()) {
				System.out.println(test.getKey());
				System.out.println(test.getValue());
			}

			// HRM neu bauen
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
				currentPace = xmlData.get(secondCounter);
				if (currentPace != 0) {
					writeBuffer.append((((1 / currentPace) * 60) * 60) * 10);
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
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
}
