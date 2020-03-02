import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GtaCarsPrepare {

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {

        // Get car list from html table
        File file = new File("D:\\temp\\gta\\car_table.html");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
        NodeList trList = doc.getElementsByTagName("tr");
        String wikiUrl = "https://gta.fandom.com";
        List<GtaCar> carList = new ArrayList<>();
        for (int trIdx = 0; trIdx < trList.getLength(); trIdx++) {
            Node trNode = trList.item(trIdx);
            for (int tdIdx = 0; tdIdx < trNode.getChildNodes().getLength(); tdIdx++) {
                Node tdNode = trNode.getChildNodes().item(tdIdx);
                if (!tdNode.getNodeName().equals("td"))
                    continue;
                for (int ulIdx = 0; ulIdx < tdNode.getChildNodes().getLength(); ulIdx++) {
                    Node ulNode = tdNode.getChildNodes().item(ulIdx);
                    if(!ulNode.getNodeName().equals("ul"))
                        continue;
                    for (int liIdx = 0; liIdx < ulNode.getChildNodes().getLength(); liIdx++) {
                        Node liNode = ulNode.getChildNodes().item(liIdx);
                        for (int aIdx = 0; aIdx < liNode.getChildNodes().getLength(); aIdx++) {
                            Node aNode = liNode.getChildNodes().item(aIdx);
                            if (!aNode.getNodeName().equals("a"))
                                continue;
                            Element aElement = (Element) aNode;
                            String title = aElement.getAttribute("title");
                            String href = wikiUrl + aElement.getAttribute("href");
                            carList.add(new GtaCar(title, href));
                        }
                    }
                }
            }
        }
        System.out.println("Cars: " + carList.size());

        /* Save wiki pages for each car
        for (GtaCar gtaCar : carList) {
            System.out.println(gtaCar.getWikiUrl() + "...");
            String html = readUrl(gtaCar.getWikiUrl());
            String fileName = gtaCar.getName().replace("/", "");
            try (PrintWriter out = new PrintWriter("D:\\temp\\gta\\wiki\\" + fileName + ".html")) {
                out.println(html);
            }
        }
        */

        // Get stats images
        for (GtaCar gtaCar : carList) {
            String wikiFileName = "D:\\temp\\gta\\wiki\\" +
                    gtaCar.getName().replace("/", "") + ".html";
            Scanner scanner = new Scanner( new File(wikiFileName) );
            String htmltext = scanner.useDelimiter("\\A").next();
            scanner.close();
            int idx = htmltext.indexOf("Stats.png");
            if (idx == -1)
                idx = htmltext.indexOf("stats.png");
            if (idx == -1)
                idx = htmltext.indexOf("Stats.PNG");
            if (idx == -1)
                idx = htmltext.indexOf("RSC.png");
            if (idx == -1)
                idx = htmltext.indexOf("RSCStats.JPG");
            if (idx == -1)
                idx = htmltext.indexOf("Placeholder.png");
            int idxStart = getIdx(htmltext, idx, '"', -1);
            int idxEnd = getIdx(htmltext, idx, '"', 1);
            String urlStr = "";
            if (idxStart > 0)
                urlStr = htmltext.substring(idxStart + 1, idxEnd);
            if (urlStr.startsWith("http"))
                System.out.println("<item>" + gtaCar.getName() + "," + urlStr + "</item>");
        }

    }

    public static int getIdx(String str, int start, char c, int delta) {
        while (start > 0 && start < str.length() && str.charAt(start) != c) {
            start += delta;
        }
        return start;
    }

    // Get url content
    public static String readUrl(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        Authenticator authenticator = new Authenticator() {

            public PasswordAuthentication getPasswordAuthentication() {
                return (new PasswordAuthentication("user",
                        "password".toCharArray()));
            }
        };
        Authenticator.setDefault(authenticator);
        Proxy proxy = new Proxy(Proxy.Type.HTTP,
                new InetSocketAddress("host", 3128));
        HttpURLConnection con = (HttpURLConnection) url.openConnection(proxy);
        con.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();
        return content.toString();
    }

}
