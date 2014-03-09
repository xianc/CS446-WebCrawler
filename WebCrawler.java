import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;


public class crawl{

	static String processLink (URL host, String link) throws Exception{
		URL root = host; 
		link=link.replaceAll("www.", "");//remove www.
		link=link.replaceAll("%7E","~"); //%07E == ~
		root= new URL(root.toString().substring(0, root.toString().lastIndexOf("/")));

		if(link.startsWith("http"))
			return link; //is ok
		if(link.startsWith("../")){ //need host
			//System.out.println("*****"+link);
			link = link.substring(3,link.length());
			String a= processLink(root,link); //is it okay now?
			//System.out.println("****XX** "+a);
			return a;
		}
		if(link.startsWith("/")){ //need host
			link = link.substring(1,link.length());
			return root+"/"+link;
		}
		else{
			return root+"/"+link;
		}

	}

	static boolean checkRobots(URL url) throws InterruptedException{
		//getting the hostname
		String link=url.toString();
		int slashes = link.indexOf("//") + 2;
		String root = "https://"+link.substring(slashes,link.indexOf('/', slashes));

		System.out.println("Host "+root);
		System.out.println("Link "+link);

		int delay = 0;
		boolean crawlDelay=false;

		URL robot;
		try{
			robot = new URL(root+"/robots.txt");
		} catch (MalformedURLException e){
			return false;
		}

		System.out.println("Robots File: "+robot.toString());
		System.out.println("-------------------------------------");

		//looking at the robots.txt
		try{
			BufferedReader robotstxt = new BufferedReader(new InputStreamReader(robot.openStream())); 

			String line="";
			while(null != (line = robotstxt.readLine())){  //what if it is moved?

				//System.out.println("ROBOTS: "+line);

				if(line.startsWith("Disallow")){
					//System.out.println("ROBOTS: "+line);
					line = line.substring(10, line.length()).trim();
					//System.out.println("---- "+line);

					if(line.equals("/") | link.matches(".*"+line+".*")){
						System.out.println("ROBOTS: "+line);
						System.out.println("*STATUS: CANNOT CRAWL!");
						return false;
					}
				}

				if(line.startsWith("Crawl-delay")){
					System.out.println("ROBOTS: "+line);
					crawlDelay=true;
					delay = Integer.parseInt(line.substring(13, line.length()).trim());
					System.out.println("*STATUS: CRAWL DELAY FOUND: "+delay +" SEC");
				}
			}
			robotstxt.close();
		} catch (IOException e) { // no robots.txt
			System.out.println("*STATUS: NO ROBOTS. SAFE TO CRAWL");
			return true;
		}

		if(crawlDelay==true){
			System.out.println("*STATUS: DEALYING CRAWL BY: " + delay +" SEC");
			Thread.sleep(delay * 1000);
		}
		else{
			System.out.println("*STATUS: DEALYING CRAWL BY: 5 SEC");
			Thread.sleep(5 * 1000);
		}

		System.out.println("*STATUS: SAFE TO CRAWL");
		return true;

	}

	public static void main(String args[]) throws Exception{

		//Queue<URL> visited = new LinkedList<URL>();
		Queue<URL> unvisited = new LinkedList<URL>();
		Queue<URL> unique = new LinkedList<URL>();

		BufferedWriter writer = new BufferedWriter(new FileWriter("url.txt"));

		//start with crawling ciir.cs.umass.edu
		URL start = new URL("http://ciir.cs.umass.edu/");
		unvisited.add(start);
		unique.add(start);

		while(!unvisited.isEmpty()){ //there are unvisited links

			URL look = unvisited.poll();

			System.out.println("*STATUS: WANT TO CRAWL..." +look);
			System.out.println("*STATUS: CHECKING ROBOTS...\n");

			if(checkRobots(look)){ //crawling is allowed
				System.out.println("*STATUS: CRAWLING...");
				System.out.println("-------------------------------------");

				//visited.add(look); //add to visited links list
				BufferedReader reader = new BufferedReader(new InputStreamReader(look.openStream()));
				String line = "";

				while(null != (line = reader.readLine())){

					//System.out.println(line); //this will print out the html code for the url
					Pattern p = Pattern.compile("<a\\s+href\\s*=\\s*\"?(.*?)[\"|>]");
					Matcher m = p.matcher(line);


					while(m.find()){

						String link = m.group(1).trim(); //remove white space

						if(link.matches(".*php.*")){
							//System.out.println("REJECT: "+link);
							continue;}
						if (link.length()<1 | link.charAt(0)=='#' | link.startsWith("mailto") | link.endsWith("jpg") | link.endsWith("png") | link.endsWith("gif") | link.endsWith("png") | link.endsWith("text")  | link.endsWith("png") | link.endsWith("txt") | link.endsWith("ps")){
							continue; //ignore
						}
						else{
							//
							//if(link.matches(".*pdf") | link.matches(".*htm.*")){ //find only pdf, htm, html
							//System.out.println(link);
							link = processLink(look, link); //take care of ../ 
							//System.out.println(link);

							if(link.matches(".*cs.umass.edu.*")){

								URL found = new URL(link);

								//does the link exist?
								try{
									HttpURLConnection huc =  ( HttpURLConnection )  found.openConnection (); 
									huc.setRequestMethod ("GET");  //OR  huc.setRequestMethod ("HEAD"); 
									huc.connect () ; 
									int code = huc.getResponseCode() ;
									//System.out.println(code);
									if (code==404){
										System.out.println("**BROKEN LINK: "+code + " " +found);
										break;
									}
								}catch (IOException e) {
									System.out.println("**BROKEN LINK: " +found);
									break;
								}

								//if the link has not been seen before. equivalent to (!unvisited.contains(link) && !visited.contains(link))
								//also take care of things like ciir.umass.edu/~hi and cirr.umass.edu/~hi/
								if(!unique.contains(found) && !unique.contains(new URL(found+"/"))){
									System.out.println("*STATUS: UNIQUE LINK FOUND: " +found);
									unvisited.add(found);
									if(unique.size()>=100){ //if we have 100 links, break out (for the purpose of this assignment)
										System.out.println("*STATUS: 100 UNIQUE LINKS FOUND; EXITING PROGRAM.");
										writer.close();
										System.exit(0);
									}
									unique.add(found);
									//System.out.println("*STATUS: WRITING " +found + " TO url.txt");
									writer.write(found.toString());
									writer.newLine();
									//System.out.println(found);
								}
							}//if in cs.umass.edu
						}//if a page
					}//while there is a link
				}//while a line exist
				System.out.println("-------------------------------------");
				reader.close();
			}//if robot allows
			//Thread.sleep(5 * 1000);
		} //while unvisited is not empty

	}

}
