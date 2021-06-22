import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList; 

public class WebScraper {
    public static final int SCREEN_LENGTH = 124;
    public static final int SUBHEADING_LENGTH = 39;
    private static Scanner s;

    public static void main(String[] args){
        boolean continueProgram = true;
        s = new Scanner(System.in);

        try{  
            do{
                System.out.print("Search a movie OR type \"list\" to select from IMDb's Top 20 Rated movies: ");
                String movieSelected = s.nextLine();
                Document document;

                if(movieSelected.equals("list")){                    
                    document= Jsoup.connect("http://www.imdb.com/chart/top").get();
                    String[][] rowArr = new String[20][2];
                    int i = 0;

                    System.out.println("\n" + formatHeading(SCREEN_LENGTH, " IMDb Charts: Top 20 Rated Movies ", "heading") + "\n");

                    for (Element row : document.select(".lister-list tr")) {
                        if(i == 20) break;
                        int ordering = i + 1;

                        String title = row.select(".titleColumn a").text();
                        rowArr[i][1] = title;
                        System.out.println(ordering + ". " + title);
                        
                        Element link = row.select(".titleColumn a").first();
                        rowArr[i][0] = link.attr("abs:href");

                        i++;
                    }

                    System.out.print("\nSelect an option (1-20) OR go back to search menu (0): ");
                    int selection = validateSelection(i);                

                    if(selection == -1){
                        System.out.println("Going back to search menu ...\n");
                        s.nextLine();
                        continue;
                    }

                    movieSelected = rowArr[selection][1];
                    document = Jsoup.connect(rowArr[selection][0]).get();

                }else{
                    movieSelected = movieSelected.replaceAll(" ", "+");

                    document= Jsoup.connect("https://www.imdb.com/find?q=" + movieSelected + "&ref_=nv_sr_sm").get();
                    String[][] rowArr = new String[3][2];
                    int i = 0;

                    for(Element row : document.select("table.findList tr")){
                        String title = row.select(".result_text").text();
                        int ordering = i + 1;

                        // Special conditions
                        if(i == 3 || title == null) break;
                        else if(i == 0) System.out.println("\nFirst 3 matches: ");

                        System.out.println(ordering + ". " + title);
                        rowArr[i][1] = title;

                        // Find link for each result
                        Element link = row.select(".result_text a").first();
                        String url = link.attr("abs:href"); 
                        rowArr[i][0] = url;
                        i++;
                    }

                    // If there's no matches, send user back to the main menu
                    if(i == 0){
                        System.out.println("No matches. Please try again.");
                        continue;
                    }

                    System.out.print("\nSelect an option (1-3) OR go back to search menu (0): ");
                    int selection = validateSelection(i);                

                    if(selection == -1){
                        System.out.println("Going back to search menu ...\n");
                        s.nextLine();
                        continue;
                    }
                
                    movieSelected = rowArr[selection][1];
                    document = Jsoup.connect(rowArr[selection][0]).get();
                }

                Movie movie = new Movie(movieSelected, document);
                System.out.println(movie.toString());
                
                // User input: receive suggestion
                System.out.print("\nNeed help deciding if you should watch this film? ");
                String decide = validateYesNo();                
                if(decide.equals("yes")) System.out.println(movie.shouldYouWatch());

                // User input: search again or end program
                System.out.print("\nSearch again? ");
                String continueProgramString = validateYesNo();                

                if(continueProgramString.equals("yes")) s.nextLine();
                else continueProgram = false;

            } while(continueProgram);

        }catch (IOException e){
            System.out.println("Can't access IMDb. Maybe check your wifi?");
        }       

        System.out.println("IMDb web scraper terminated.");
        s.close();
    }

    private static String validateYesNo(){
        String string = s.next().toLowerCase();

        while(!(string.equals("yes")) && !(string.equals("no"))){
            System.out.print("Not a valid input. Please answer with yes or no: ");
            string = s.next().toLowerCase();
        }

        return string;
    }

    private static int validateSelection(int max){
        int selection;

        while(true){
            try{
                selection = s.nextInt() - 1;
                if(selection == -1 || 0 <= selection && selection < max) break;
                else throw new java.util.InputMismatchException();
            }catch(java.util.InputMismatchException e){
                System.out.print("Not a valid input. Select an option (1-" + max +") OR go back to search menu (0): "); 
                s.nextLine();
            }
        }

        return selection;
    }

    static String formatHeading(int width, String s, String style){
        s = s.replace(" ", "*");

        if(style.equals("subheading")){
            int count = (SUBHEADING_LENGTH - s.length())/2;
            String add = "";
            for(int i = 0; i < count; i++) add += "-";            
            s = add + s + add;
            if(s.length() % 2 == 0) s += "-";
        }  

        s = String.format("%-" + width  + "s", String.format("%" + (s.length() + (width - s.length()) / 2) + "s", s));
        if(style.equals("heading")) s = s.replace(" ", "-");
        s = s.replace("*", " ");

        return s;
    }

    // TO DO: fix format (for description)
    static String formatBody(int width, String s){
        ArrayList<String> stringArray = new ArrayList<String>();
        String temp = "";
        int count = 0;

        for(int i = 0; i < s.length(); i++){
            if(s.charAt(i) == '\n'){
                count = 0;
                stringArray.add(temp);
                temp = "";
            }else{
                temp += s.charAt(i);
                if(count != 0){
                    if(count % width == 0 && s.charAt(i) == ' '){
                        count = 0;
                        stringArray.add(temp);
                        temp = "";
                    }else if(count % width == 0){
                        count = 0;
                        String temp2 = "";
                        int j = temp.length() - 1;
    
                        while(j >= 0 && temp.charAt(j) != ' '){
                            temp2 = temp.charAt(j) + temp2;
                            count++;
                            j--;
                        }
    
                        stringArray.add(temp.substring(0, j));
                        temp = temp2;
                    }
                }
                count++;
            }
        }

        stringArray.add(temp);
        
        return String.join("\n", stringArray);
    }
}