import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

public class Movie {
    private Document document;

    private double ratingInt = -1;
    private String ratingString;
    private String description;
    private String name;
    private String quotes;
    private String awards; 

    public Movie(String name, Document document){
        this.name = name;
        setDocument(document);
        setRating();

        description = document.select(".summary_text").text();
        if(description.isEmpty() || description.equals("Add a Plot »")) description = "Description not available. Pretty niche.";

        quotes = clean("#quotes");
        if(quotes.isEmpty()) quotes = "Quotes not available. Pretty niche.\n";

        awards = document.select(".awards-blurb").text(); 
        if(awards.isEmpty()) awards = "Awards not available. Pretty niche."; 
    }

    private void setDocument(Document document){
        document.select(".nobr").remove();
        document.select("h1, h2, h3, h4, h5, h6").remove();
        this.document = document;
    }

    private void setRating(){
        ratingString = document.select(".ratingValue").text();

        if(!ratingString.isEmpty()){     
            String rating = this.ratingString.split("/")[0];
            ratingInt = Double.parseDouble(rating);
            ratingString = "User rating: " + ratingString + ". Take this with a grain of salt.";
        }else{
            ratingString = "Rating not available. Pretty niche.";
        }
    }

    public String getDescription(){
        return description;
    }

    public String getRating(){
        return ratingString;
    }

    public String getName(){
        return name;
    }

    public String getQuotes(){
        return quotes;
    }

    private String clean(String element){
        // Extracts texts from the HTML of an element, preserving the break lines
        String html = document.select(element).html();
        if(html.contains("»")) html = html.replace("»", "");
        return Jsoup.clean(html, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
    }

    public String shouldYouWatch(){     
        String suggestion = "";

        suggestion += "\n" + WebScraper.formatHeading(WebScraper.SCREEN_LENGTH, "Analyzing ...", "none");        
        suggestion += "\n" + WebScraper.formatHeading(WebScraper.SCREEN_LENGTH, "Calculating ...", "none");        
        suggestion += "\n" + WebScraper.formatHeading(WebScraper.SCREEN_LENGTH, "Accounting for internet trolls and snubbed awards ... ", "none");        
        suggestion +=  "\n\n" + WebScraper.formatHeading(WebScraper.SCREEN_LENGTH, " RESULTS ", "subheading") + "\n";
        
        Element awardsBank = document.select("#titleAwardsRanks a").first();

        // Check if movie is on IMDb's Top Rated List
        if(awardsBank != null && awardsBank.attr("abs:href").equals("https://www.imdb.com/chart/top?ref_=tt_awd") || 
                awardsBank != null && awardsBank.attr("abs:href").equals("https://www.imdb.com/chart/toptv?ref_=tt_awd")){
            String[] ranking = awardsBank.text().split("#");
            suggestion += "Wait a second. " + name + " is #" + ranking[1] + " on IMDb's " + ranking[0] + "List.";
            suggestion += "\nAnd you haven't watched it yet? What are you doing with your life ...";
        
        // Checks if movie won an oscar
        }else if(awards.contains("Oscar")){
            suggestion += name + " won " + awards.charAt(4) + " Oscars. And you haven't watched it yet?";
            suggestion += "\nWhat are you doing with your life ...";

        // Suggest based on user ratings
        }else if(8 <= ratingInt && ratingInt <= 10) suggestion += "Based on ratings, DEFINITELY! Watch " + name + ". Today.";
        else if(5 <= ratingInt && ratingInt < 8) suggestion += "Based on ratings, maybe watch " + name + " if you have time to kill.";
        else if(0 <= ratingInt && ratingInt < 5) suggestion += "Based on ratings ... stay away from " + name + ".";
        else suggestion += "Did not gather enough information to decide whether you should watch " + name + ".";

        return WebScraper.formatBody(WebScraper.SCREEN_LENGTH, suggestion);
    }

    public String toString(){
        String string = "";

        String titlePrinted = " MOVIE SELECTED: " + name + " ";
        string += "\n" + WebScraper.formatHeading(WebScraper.SCREEN_LENGTH, titlePrinted, "heading") + "\n";        
        string += "\n" + WebScraper.formatHeading(WebScraper.SCREEN_LENGTH, " DESCRIPTION ", "subheading") + "\n" + description;
        string += "\n\n" + WebScraper.formatHeading(WebScraper.SCREEN_LENGTH, " QUOTES ", "subheading") + "\n" + quotes;
        string += "\n" + WebScraper.formatHeading(WebScraper.SCREEN_LENGTH, " AWARDS & RATINGS ", "subheading") + "\n" + awards + "\n" + ratingString;        

        return WebScraper.formatBody(WebScraper.SCREEN_LENGTH, string);
    }    
}
