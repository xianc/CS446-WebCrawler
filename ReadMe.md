CS446 Assignment 2: Webcrawler
Documentation


Table of Contents
1. Introduction
2. Java web crawler implementation
	a. processLink method
	b. checkRobots method
3. First 100 URLs
	a. wget unique 100 list
	b. java crawler in (1)  unique 100 list
4. Comparison
5. References


1. Introduction
The purpose of this document is to outline the details of my implementation of the Java web as well as compare the the results I receive from my crawler program with that of wget. This document is divided into three parts. The first part is a discussion on my implementation of a crawler. The second shows a list of the top 100 URLs I obtained from using wget and the top 100 URLs I obtained from the java web crawler. The last part will be a comparison of the two crawlers. References will be listed at the end. 


2. Java web crawler implementation
The web crawler keeps track of two lists: unvisited and unique. Both are in the form of a queue-- the unvisited list contains a list of links that the crawler has yet to crawl. The unique list contains a list of unique links that the crawler found when crawling. 

Design Choice:
I used a queue of the unvisited list because it is a simple data structure that can easily add and remove entries. I could have used a set or a map for the unique list (which is probably a better fit), but since I used a queue for the unvisited list, and since a queue works for my needs, I decided to use it. 

Since the crawl will be starting at: http://ciir.cs.umass.edu/, the link is added to both the unvisited and unique queue. 

The crawler then checks to see if the unvisited list is empty, if it is not, a entry is taken from the unvisited list, and its robots.txt file is checked. If the robots.txt file disallow crawling, we go back to check the unvisited list again for a link. Otherwise, we read the webpage line by line to find links that exist, are either web pages or pdfs, and are in cs.umass.edu. Once this is done, the crawler checks to see if the link already exist in the unique queue. If it does not, it is added the to unique and unvisited queue. This process is done until the unique queue has 100 entries. The 100 unique entries are outputted to a text file called url.txt. 

a. processLink method:
This method takes the extracted links and put them in the correct format. For example, links that are in the form of “.../zzz” are changed into something like “http://xxx/yyy/yyy” and “..../.../zzz” to “http://xxx/zzz”. It also deals with other link oddities like “/%7Emccallum” and changes it to “/~mccallum” and removes “www” from links so that the unique list does not add both http://www.ciir.umass.edu/ and http://ciir.umass.edu/. 

b. checkRobots method:
This method checks the robot.txt file of an url. This is done by obtaining the hostname from an url, and reading its robots.txt page. The program checks the “Disallow” tags in robots and checks to see if the url it is currently look at falls in the disallow category. If it does, the url is not crawled. Otherwise, the program will crawl the page. The “Crawl-delay” tag is also observed-- if there is a stated crawl-delay, the program delay the crawl by that time, otherwise it will delay the crawl for 5 seconds (set as default in this program). 


3a. wget
Command: 
wget -r -w 5 -A pdf -H -D cs.umass.edu ciir.cs.umass.edu 2> temp
cat temp | grep "saved"  | more | awk ‘{print $6}’ > url.txt

-r : allows the crawler to recursively crawl pages
-w 5 : wait 5 second after each crawl
-H : makes the crawler search outside of the initial domain that it is given
-D : tells the crawler to only crawl cs.umass.edu and ciir.cs.umass.edu
2> output to a temporary file
grep “saved” | more : looks at the line that contains the word “saved”
awk ‘{print $6}’ : displays only the 6th column in the temp file


4. Comparison
As you can see from the two lists above, they are not identical, though there are  overlaps. The first difference you can probably observe is that all the links that the wget command generated omitted the “http://” in the beginning of links and have either .html, .htm, or .pdf suffixes. For syntactical issues for the “URL” object in java, I had to include the “http://” in front of links. As for the suffix, my crawler does not automatically complete links such as “http://ciir.umass.edu/” with the “index.html” ending. You can see the discrepancy in link #9 on both list: cs.umass.edu/index.html cs.umass.edu/ vs. http://cs.umass.edu/. This is definitely an issue-- though fixing it can possibly lead to other problems… for example “.pdf/index.html”, and then there is the case of checking if “index.htm” works. Lastly, is it possible to guess what a site is referring to when it links to: “http://cs.umass.edu/xx/”? Does it want “http://cs.umass.edu/xx/index.html”? “http://cs.umass.edu/xx/index.htm”? Or just the directory “http://cs.umass.edu/xx/”? For this reason, I simply left the link as is. 

Although the links may be displayed differently, there is a considerable amount of overlap between the two results. The links might not be in the same order, but there are chunks of links that are almost identical. For example, lines 34-48 from the wget results are links to the same page as lines 56-70 from the Java crawler. Other chunks of links that matches like this are: wget 50-53 and crawler 71-74, and 13-25 and crawler 15-27. It is only after the top 50 in the wget link list does the links start to differ greatly. For example, in the wget list, most of the links in the 90s are in the form of  people.cs.umass.edu/~keikham/papers or ciir.cs.umass.edu/~dietz/ while for the crawler, it is mostly cs.umass.edu/talks or publications.

The differences in links is probably due to the different way the wget and the java crawler crawls a website. My program starts off with the first link it was given (ciir.cs.umass.edu), it crawls the site and finds all the unique links and adds it to the unvisited queue. It then checks the next link in the queue and repeats this process. wget, on the other hand, recursively crawls. wget also, by default, uses robots.txt and parses the HTML of web pages. This is done manually on the Java crawler, and many exceptions are not taken care of. This difference in how the crawler crawls a webpage and processes them cause the differences in links. This is the reason why the first few links are identical-- the crawlers start at the same point, but after that point, links are visited in the order of the algorithm the crawler follows, resulting in quite a different set of links near the end of the list.


5. References
wget flags: http://www.gnu.org/software/wget/manual/wget.html
Queue: http://docs.oracle.com/javase/7/docs/api/java/util/Queue.html
BufferedReader: http://docs.oracle.com/javase/7/docs/api/java/io/BufferedReader.html
Check if a page exist: http://stackoverflow.com/questions/1378199/how-to-check-if-a-url-exists-or-returns-404-with-java
String.matches: http://stackoverflow.com/questions/2275004/in-java-how-to-check-if-a-string-contains-a-substring-ignoring-the-case
Pausing a program: http://docs.oracle.com/javase/tutorial/essential/concurrency/sleep.html

