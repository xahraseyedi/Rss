import java.io.*;
import java.net.HttpCookie;
import java.util.Scanner;
import org.jsoup.*;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.net.*;
import javax.xml.parsers.*;

public class Main {
    public static void main(String[] args) {

        RSS rss = new RSS(5);
    }
}

class RSS{

    Scanner input = new Scanner(System.in);
    int MAX_ITEMS;
    int option;

    //The number of blogs in the file
    static int k = 1 ;

    public RSS( int maxItem ){

        this.MAX_ITEMS = maxItem;
        buildFile();

        System.out.println("Welcome to RSS Reader!");

        option = 0 ;

        do { showAction();
            option = input.nextInt();
            chooseAction( option) ; }

        while( option != 4 );
    }

    //Creat data.txt & add information of a blog
    public void buildFile() {

        try
        {
            FileWriter writer = new FileWriter("data.txt" );
            writer.write("Cay Horstmann's Unblog;https://horstmann.com/unblog/index.html;https://horstmann.com/unblog/rss.xml");
            writer.close();

        }
        catch ( IOException e ) { e.printStackTrace(); }
    }

    //show options
    public void showAction() {

        System.out.println("Type a valid number for your desired action:");
        System.out.println("[1] show updates");
        System.out.println("[2] Add URL");
        System.out.println("[3] Remove URL");
        System.out.println("[4] Exit");
    }

    //chosee options
    public void chooseAction( int option ) {

        if ( option == 1) {

            showupdates();
        }

        else if( option == 2 ) {

            System.out.println("Please enter website URL to add ");
            String URL = input.next();
            addURL( URL );
        }

        else if( option == 3 ) {

            System.out.println("Please enter website URL to remove ");
            String URL = input.next();
            removeURL( URL );
        }
    }

    //show list of blogs
    public void showupdates() {

        System.out.println("show updates for:");

        try
        {
            FileReader fileReader = new FileReader( new File("data.txt") );
            BufferedReader reader = new BufferedReader(fileReader);

            String line;
            int i = 1 ;
            while( (line = reader.readLine()) != null ) {

                String[] split = line.split(";");
                System.out.format("[%d] "+split[0]+"\n",i);
                i++;
            }

            reader.close();
        }
        catch (IOException e) { e.printStackTrace(); }

        System.out.println("Enter -1 to return");

        int n = input.nextInt();

        while( n > k ) {

            System.out.format("Please enter a number between 1 and %d\n",k);
            n=input.nextInt();
        }

        if( n !=-1 ) {

            showblog( n );
        }
    }

    //show blog updates
    public void showblog( int n )
    {
        try
        {
            FileReader fileReader = new FileReader( new File("data.txt") );
            BufferedReader reader = new BufferedReader(fileReader);

            String line = " ";

            for( int i = 0 ; i < n ; i++ ) {

                line = reader.readLine();
            }

            reader.close();

            String[] split = line.split(";");
            String[] split2=split[2].split("rss.xml");  //برای برداشتن url از rss از split2 استفاده شده
            retrieveRssContent(split2[0]);
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    // Add blog to list
    public void addURL( String URL ) {

        try
        {
            String headline = extractPageTitle( fetchPageSource( URL ) );
            FileReader fileReader = new FileReader( new File("data.txt") );
            BufferedReader reader = new BufferedReader( fileReader );

            String line;
            int sign = 0;

            while( ( line = reader.readLine() ) !=null ) {

                String[] split = line.split(";");

                if( split[0].startsWith(headline) ) {

                    sign = 1;
                    System.out.println(URL+" already exist!");
                }
            }

            reader.close();

            if( sign == 0 ) {

                FileWriter fileWriter = new FileWriter( new File("data.txt") ,true );
                BufferedWriter writer = new BufferedWriter( fileWriter );
                writer.write("\n"+headline+";"+URL+"index.html;"+URL+"rss.xml");
                writer.close();

                System.out.println( URL + " added successfully!" );

                k = k+1;
            }
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    //remove URL from list
    public void removeURL( String URL ) {

        try
        {
            String headline = extractPageTitle( fetchPageSource( URL ) );
            FileReader fileReader = new FileReader( new File("data.txt") );
            BufferedReader reader = new BufferedReader( fileReader );

            String line;
            int sign = 0;
            int counter = 0;

            while( (line = reader.readLine()) != null ) {

                counter++;
                String[] split = line.split(";");

                if( split[0].startsWith(headline) ) {

                    sign = counter;
                }
            }

            reader.close();

            if( sign == 0 ) {

                System.out.println("Could'nt find "+URL);
            }

            else {

                String[] Filelines = new String[k];
                fileReader = new FileReader( new File("data.txt") );
                reader = new BufferedReader( fileReader );

                int i = 0;

                while( (line = reader.readLine()) != null ) {

                    Filelines[i] = line;
                    i++;
                }

                reader.close();

                FileWriter writer = new FileWriter("data.txt");

                for( int j = 0 ; j < k ; j++ )
                    if ( (j + 1) != sign )
                        writer.write(Filelines[j]+"\n");

                writer.close();

                System.out.println( URL + " removed successfully!" );

                k = k-1;
            }
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    public void retrieveRssContent( String rssUrl ) {

        try
        {
            String rssXml = fetchPageSource(rssUrl);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append( rssXml );
            ByteArrayInputStream input = new ByteArrayInputStream(
                    xmlStringBuilder.toString().getBytes("UTF-8"));
            Document doc = documentBuilder.parse(input);
            NodeList itemNodes = doc.getElementsByTagName("item");

            for (int i = 0 ; i < MAX_ITEMS ; ++i ) {

                Node itemNode = (Node) itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) itemNode;
                    System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent());
                    System.out.println("Link: " + element.getElementsByTagName("link").item(0).getTextContent());
                    System.out.println("Description: " + element.getElementsByTagName("description").item(0).
                            getTextContent());
                }
            }
        }
        catch (Exception e) { System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage()); }
    }

    public String extractPageTitle( String html ) {

        try
        {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            return doc.select("title").first().text();
        }
        catch (Exception e) { return "Error: no title tag found in page source!"; }
    }

    public String fetchPageSource(String urlString) throws Exception {

        URI uri = new URI(urlString);
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML ,like Gecko) Chrome/108.0.0.0 Safari/537.36");
        return toString(( urlConnection.getInputStream() ));
    }

    private String toString( InputStream inputStream ) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader( inputStream , "UTF-8" ));
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ( (inputLine = bufferedReader.readLine()) != null )
            stringBuilder.append( inputLine );

        return stringBuilder.toString();
    }

    public String extractRssUrl( String url ) throws IOException {

        org.jsoup.nodes.Document doc = Jsoup.connect( url ).get();
        return doc.select("[type='application/rss+xml']").attr("abs:href");
    }
}
