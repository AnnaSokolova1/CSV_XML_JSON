package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";

        // CSV-Jcon
        List<Employee> list = parseCSV(columnMapping, fileName);
        String json = listToJson(list);
        writeString(json, "data.json");

        // XML-Jcon
        List<Employee> list1 = parseXML("data.xml");
        String json1 = listToJson(list1);
        writeString(json1, "data2.json");

        // JSON парсер
        String json2 = readString("new_data.json");
        List<Employee> list3 = jsonToList(json2);
        for (Employee employee : list3) {
            System.out.println(employee);
        }
    }

    public static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping("id", "firstName", "lastName", "country", "age");
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();
            List<Employee> list = csv.parse();
            return list;
        } catch (IOException e) {
            return null;
        }
    }

    public static List<Employee> parseXML(String fileName) {
        List<Employee> list2 = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(fileName);
            Node root = doc.getDocumentElement();
            NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (Node.ELEMENT_NODE == node.getNodeType()) {
                    Element element = (Element) node;
                    long id = Long.parseLong(getValue(element, "id"));
                    String firstName = getValue(element, "firstName");
                    String lastName = getValue(element, "lastName");
                    String country = getValue(element, "country");
                    int age = Integer.parseInt(getValue(element, "age"));
                    Employee employee = new Employee(id, firstName, lastName, country, age);
                    list2.add(employee);
                }
            }
        } catch (Exception e) {
            return null;
        }
        return list2;
    }

    public static String getValue(Element element, String nodeName) {
        return element.getElementsByTagName(nodeName).item(0).getTextContent();
    }

    static String listToJson(List<Employee> list) {

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<Employee>>() {
        }
                .getType();
        return (gson.toJson(list, listType));
    }

    static void writeString(String json, String fileName) {

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(json);
            writer.flush();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static String readString(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            StringBuilder stringBuilder = new StringBuilder();
            String s;
            while ((s = reader.readLine()) != null) {
                stringBuilder.append(s);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static List<Employee> jsonToList(String json) {
        JSONParser parser = new JSONParser();
        Gson gson = new GsonBuilder().create();
        List<Employee> list = new ArrayList<>();
        try {
            JSONArray array = (JSONArray) parser.parse(json);
            for (int i = 0; i < array.size(); i++) {
                list.add(gson.fromJson(String.valueOf(array.get(i)), Employee.class));
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
