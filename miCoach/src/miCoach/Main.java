package miCoach;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
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
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		// String miCoachFile = args[0];
		// String garminFile = args[1];
		// String storeFile = "converted.tcx";

		String miCoachFile = "/home/gugugs/miCoach_dev/git/miCoachDev/data/squash2/micoach.tcx";
		String garminFile = "/home/gugugs/miCoach_dev/git/miCoachDev/data/squash2/garmin.tcx";
		String storeFile = "/home/gugugs/miCoach_dev/git/miCoachDev/data/squash2/converted.tcx";

		LinkedHashMap<Date, Integer> heartRateData = new LinkedHashMap<>();
		LinkedHashMap<Date, Element> lapData = new LinkedHashMap<>();

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
			Element currentElement = null;
			String dateString = null;
			while (counter < trackpointNodeList.getLength()) {
				currentElement = (Element) trackpointNodeList.item(counter);
				dateString = currentElement.getElementsByTagName("Time")
						.item(0).getTextContent();
				year = Integer.parseInt(dateString.substring(0, 4));
				month = Integer.parseInt(dateString.substring(5, 7)) - 1;
				day = Integer.parseInt(dateString.substring(8, 10));
				hours = Integer.parseInt(dateString.substring(11, 13)) + 2;
				minutes = Integer.parseInt(dateString.substring(14, 16));
				seconds = Integer.parseInt(dateString.substring(17, 19));
				calendar.set(year, month, day, hours, minutes, seconds);

				if (((Element) currentElement.getElementsByTagName(
						"HeartRateBpm").item(0)) != null) {
					heartrate = Integer.parseInt(((Element) currentElement
							.getElementsByTagName("HeartRateBpm").item(0))
							.getElementsByTagName("Value").item(0)
							.getTextContent());

					heartRateData.put(calendar.getTime(), heartrate);
				}

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
				first = (Entry) heartRateArray[counter];
				second = (Entry) heartRateArray[++counter];
				heartRateOne = Double.parseDouble((((Integer) first.getValue())
						.toString()));
				heartRateTwo = Double
						.parseDouble((((Integer) second.getValue()).toString()));

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
						tempMap.put(calendar.getTime(),
								(int) Math.round(heartRateOne));
						heartRateOne -= steps;
						calendar.add(Calendar.SECOND, 1);
					}
				} else {
					heartRateDiff = heartRateTwo - heartRateOne;
					steps = (Double) (heartRateDiff / seconds);

					while (calendar.getTime().before((Date) second.getKey())) {
						tempMap.put(calendar.getTime(),
								(int) Math.round(heartRateOne));
						heartRateOne += steps;
						calendar.add(Calendar.SECOND, 1);
					}
				}
			}
			heartRateData = tempMap;

			// remove old tracks and get laps
			NodeList nList = garminDoc.getElementsByTagName("Lap");
			Element garminTrackElement = null;
			for (int i = 0; i < nList.getLength(); i++) {
				currentElement = (Element) nList.item(i);
				dateString = currentElement.getAttribute("StartTime");

				year = Integer.parseInt(dateString.substring(0, 4));
				month = Integer.parseInt(dateString.substring(5, 7)) - 1;
				day = Integer.parseInt(dateString.substring(8, 10));
				hours = Integer.parseInt(dateString.substring(11, 13)) + 2;
				minutes = Integer.parseInt(dateString.substring(14, 16));
				seconds = Integer.parseInt(dateString.substring(17, 19));
				calendar.set(year, month, day, hours, minutes, seconds);

				currentElement.removeChild(currentElement.getElementsByTagName(
						"Track").item(0));
				garminTrackElement = garminDoc.createElement("Track");
				currentElement.appendChild(garminTrackElement);
				lapData.put(calendar.getTime(), garminTrackElement);
			}
			int lapLength = nList.getLength();

			// put trackpoints from miCoach to garmin
			nList = miCoachDoc.getElementsByTagName("Trackpoint");
			counter = 0;
			Integer heartRateValue = 0;
			Node importNode = null;
			Integer lastMaxDistance = 0;
			Integer maxDistance = 0;
			int lapCounter = 0;
			Date nextLapDate = null;
			Integer maxSpeed = 0;
			Integer currentSpeed = null;
			Element currentLap = (Element) ((Entry) lapData.entrySet()
					.toArray()[lapCounter]).getValue();
			if (lapLength > 1) {
				nextLapDate = (Date) ((Entry) lapData.entrySet().toArray()[lapCounter + 1])
						.getKey();
			}
			Date tempDate = null;
			while (counter < nList.getLength()) {
				currentElement = (Element) nList.item(counter);
				dateString = currentElement.getElementsByTagName("Time")
						.item(0).getTextContent();

				year = Integer.parseInt(dateString.substring(0, 4));
				month = Integer.parseInt(dateString.substring(5, 7)) - 1;
				day = Integer.parseInt(dateString.substring(8, 10));
				hours = Integer.parseInt(dateString.substring(11, 13));
				minutes = Integer.parseInt(dateString.substring(14, 16));
				seconds = Integer.parseInt(dateString.substring(17, 19));
				calendar.set(year, month, day, hours, minutes, seconds);

				heartRateValue = heartRateData.get(calendar.getTime());

				currentSpeed = Integer.parseInt(((Element) currentElement
						.getElementsByTagName("RunCadence").item(0))
						.getTextContent());

				if (currentSpeed > maxSpeed) {
					maxSpeed = currentSpeed;
				}

				// lap zeigen aendern
				if (nextLapDate != null
						&& calendar.getTime().after(nextLapDate)) {
					(((Element) currentLap.getParentNode())
							.getElementsByTagName("DistanceMeters").item(0))
							.setTextContent((new Integer(maxDistance
									- lastMaxDistance)).toString());
					(((Element) currentLap.getParentNode())
							.getElementsByTagName("MaximumSpeed").item(0))
							.setTextContent(maxSpeed.toString());

					maxSpeed = 0;
					lastMaxDistance = maxDistance;
					lapCounter++;
					currentLap = (Element) ((Entry) lapData.entrySet()
							.toArray()[lapCounter]).getValue();
					if (lapCounter == lapLength - 1) {
						nextLapDate = null;
					} else {
						nextLapDate = (Date) ((Entry) lapData.entrySet()
								.toArray()[lapCounter + 1]).getKey();
					}
				}

				// herzdaten setzen
				if (heartRateValue != null) {

					tempDate = ((Date) ((Entry) heartRateData.entrySet()
							.toArray()[0]).getKey());

					// trackpoint von garmin uebernehmen
					if (tempDate.before(calendar.getTime())) {
						System.out.println(tempDate);
						// add data
						Element newTrackpoint = garminDoc
								.createElement("Trackpoint");

						DateFormat dateFormater = new SimpleDateFormat(
								"YYYY-MM-dd'T'HH:mm:ss");
						Element newTime = garminDoc.createElement("Time");
						newTime.setTextContent(dateFormater.format(tempDate));
						newTrackpoint.appendChild(newTime);

						// Element newDistanceMeters = garminDoc
						// .createElement("DistanceMeters");
						// newDistanceMeters
						// .setTextContent(maxDistance.toString());
						// newTrackpoint.appendChild(newDistanceMeters);

						Element newHeartRateBpm = garminDoc
								.createElement("HeartRateBpm");
						Element newHeartRateValue = garminDoc
								.createElement("Value");
						newHeartRateValue.setTextContent(heartRateData.get(
								tempDate).toString());
						newHeartRateBpm.setAttribute("xsi:type",
								"HeartRateInBeatsPerMinute_t");
						newHeartRateBpm.appendChild(newHeartRateValue);
						newTrackpoint.appendChild(newHeartRateBpm);

						// Element newExtensions = garminDoc
						// .createElement("Extensions");
						// Element newFatCalories = garminDoc
						// .createElement("FatCalories");
						// Element newValue = garminDoc.createElement("Value");
						// Element newActivityTrackpointExtension = garminDoc
						// .createElement("ActivityTrackpointExtension");
						// Element newRunCadence = garminDoc
						// .createElement("RunCadence");
						// newFatCalories
						// .setAttribute("xmlns",
						// "http://www.garmin.com/xmlschemas/FatCalories/v1");
						// newActivityTrackpointExtension
						// .setAttribute("xmlns",
						// "http://www.garmin.com/xmlschemas/ActivityExtension/v1");
						// newActivityTrackpointExtension.setAttribute(
						// "SourceSensor", "Footpod");
						// newValue.setTextContent("0");
						// newRunCadence.setTextContent("0");
						// newFatCalories.appendChild(newValue);
						// newActivityTrackpointExtension
						// .appendChild(newRunCadence);
						// newExtensions.appendChild(newFatCalories);
						// newExtensions
						// .appendChild(newActivityTrackpointExtension);
						// newTrackpoint.appendChild(newExtensions);

						// Element newPosition = garminDoc
						// .createElement("Position");
						// Element newLatitudeDegrees = garminDoc
						// .createElement("LatitudeDegrees");
						// Element newLongitudeDegrees = garminDoc
						// .createElement("LongitudeDegrees");
						// newLatitudeDegrees.setTextContent("0.0");
						// newLongitudeDegrees.setTextContent("0.0");
						// newPosition.appendChild(newLatitudeDegrees);
						// newPosition.appendChild(newLongitudeDegrees);
						// newTrackpoint.appendChild(newPosition);

						currentLap.appendChild(newTrackpoint);

						heartRateData.remove(tempDate);

					} else {
						System.out.println(calendar.getTime());
						((Element) ((Element) currentElement
								.getElementsByTagName("HeartRateBpm").item(0))
								.getElementsByTagName("Value").item(0))
								.setTextContent(heartRateValue.toString());

						importNode = garminDoc.importNode(nList.item(counter),
								true);
						currentLap.appendChild(importNode);

						maxDistance = Integer
								.parseInt(((Element) currentElement
										.getElementsByTagName("DistanceMeters")
										.item(0)).getTextContent());

						heartRateData.remove(calendar.getTime());

						counter++;

					}
				} else {
					counter++;
				}
			}

			(((Element) currentLap.getParentNode())
					.getElementsByTagName("DistanceMeters").item(0))
					.setTextContent((new Integer(maxDistance - lastMaxDistance))
							.toString());

			// saving file
			garminDoc.normalizeDocument();
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(garminDoc);
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
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
}
