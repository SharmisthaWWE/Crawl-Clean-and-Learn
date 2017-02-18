
package crawlerforwikicfp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.util.Elements;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class CrawlerForWikiCFP {

    public static int DELAY = 7;
    
    public static void main(String[] args) {
        
	try {		
	
            String[] searchCategory = {"data mining", "databases", "machine learning", "artificial intelligence"};

            int numOfPages = 20;
            
            //create the output file
            File file = new File("wikicfp_crawl.txt");
            file.createNewFile();
            FileWriter writer = new FileWriter(file); 
            
            writer.write("Conference Acronym \tConference Name \t Conference Location\n");      //Labels of the data

            //now start crawling the all 'numOfPages' pages
            
            for(int j = 0; j < searchCategory.length; j++)                  //Crawling the webpages according to the category 
            {                 
                for(int i = 1; i<=numOfPages; i++) {
                    
                    //Create the initial request to read the first page 
                    //and get the number of total results
                    String linkToScrape = "http://www.wikicfp.com/cfp/call?conference="+
                                                  URLEncoder.encode(searchCategory[j], "UTF-8") +"&page=" + i;
                    
                    String content = getPageFromUrl(linkToScrape);	        	
                    //parse or store the content of page 'i' here in 'content'

                    //YOUR CODE GOES HERE
                    
                    if(content != null)
                    {
                        String pageContent = parseTableData(content);       //Conference Information Collection and Parsing the data
                        writer.write(pageContent);                          //Writing the parsed data in file
                    }
                    else{
                        System.out.println("No important information found!");
                   }
                    
                    //IMPORTANT! Do not change the following:
                    Thread.sleep(DELAY*1000); //rate-limit the queries
            }    
        }
        writer.close();
        } catch (IOException e) {
                e.printStackTrace();
        } catch (InterruptedException e) {
                e.printStackTrace();
        }

    }
	
    /**
     * Given a string URL returns a string with the page contents
     * Adapted from example in 
     * http://docs.oracle.com/javase/tutorial/networking/urls/readingWriting.html
     * @param link
     * @return
     * @throws IOException
     */
    
    public static String getPageFromUrl(String link) throws IOException {
    URL thePage = new URL(link);
    URLConnection yc = thePage.openConnection();
    BufferedReader in = new BufferedReader(new InputStreamReader(
                                yc.getInputStream()));
    String inputLine;
    String output = "";
    while ((inputLine = in.readLine()) != null) {
            output += inputLine + "\n";
    }
    in.close();
            return output;   
    }
    
    public static String parseTableData(String content)
    {
        Document tableInfo = Jsoup.parse(content);
        Element conferenceTable = tableInfo.select("table").get(2);                     //Information of the Conference Table
                    
        org.jsoup.select.Elements conferenceRows = conferenceTable.select("tr");        //Rows of the Conference Table 
        
        String conferenceAcronym, conferenceName, conferenceTime, conferencePlace, conferenceDeadline, currentPageContent = "", currentPageContentFile = "";
        
        for(int i = 8; i < conferenceRows.size() - 2; i += 2)
        {
            Element singleUpRow = conferenceRows.get(i);                                //Fetching Upper row for single entry from the Conference Table
            org.jsoup.select.Elements tableColumnsUp = singleUpRow.select("td");        //Fectching the column data for the particular row 
           
            conferenceAcronym = tableColumnsUp.get(0).text();                           //Fetching Conference Acronym    
            
            if(!conferenceAcronym.equals("Expired CFPs"))
            {
                conferenceName = tableColumnsUp.get(1).text();                          //Fetching Conference Name
            
                Element singleDownRow = conferenceRows.get(i+1);                        //Fetching Down row for single entry from the Conference Table
                org.jsoup.select.Elements tableColumnsDown = singleDownRow.select("td");//Fectching the column data for the particular row 

                conferenceTime = tableColumnsDown.get(0).text();                    //Fetching Conference Time
                conferencePlace = tableColumnsDown.get(1).text();                   //Fetching Conference Place
                conferenceDeadline = tableColumnsDown.get(2).text();                //Fetching Conference Deadline

                currentPageContentFile += conferenceAcronym + "\t" + conferenceName + "\t" + conferencePlace + "\n";
            }
            else{
                i--;
            }
        }
        return currentPageContentFile;
    }
}
