import java.io.*;
import java.net.*;
import java.util.*;
// Import java util for hashset and linkedlist.

/**
 * 
 * Course: CS4446
 * Author: Harjap Grewal
 * Description: Crawl is a class that crawls through all possible pages from the base URL to find pages with all query words.
 *
 */

public class Crawl {

	/*
	 * Do not change this base URL. All URLs for ths assignmetn are relative to this
	 * address
	 */
	private static String baseURL = "https://www.csd.uwo.ca/~rsolisob/cs9668/test/";

	public static void main(String[] args) {

		

		URL theUrl, u;			// Create variables to hold current Url, u for newly found Urls, and input from Url.
		BufferedReader input;
		String s;  // Holds line from input
		String[] query; // User specified query
		int docCounter = 0; // Counter for the number of documents visited.

		Queue<URL> queue = new LinkedList<>();  // Queue to holds all Urls to go through
		HashSet<String> visited = new HashSet<>();  // Hashset to hold all visited Urls

		boolean qFound = true;  // boolean that stores if full query was found, assume true until checking for query words

		// Starting endpoint of Url.
		String endPoint = "test.html";

		try { // Try catch for errors.

			// Get query
			query = InOut.readQuery();

			input = null; // Initialize input
			theUrl = new URL(baseURL + endPoint); // This is the complete URL of the test page
			
			queue.add(theUrl);					 // Add Url to queue and visited
			visited.add(theUrl.toString());

			while (!queue.isEmpty()) {  // Loop to go through all Urls in queue.

				theUrl = queue.remove(); // Get next Url

				boolean[] seen = new boolean[query.length]; // boolean array to tell if we found all query words in a page.
				// reset each loop for new Url

				input = new BufferedReader(new InputStreamReader(theUrl.openStream())); // Open URL for reading
				qFound = true;  // Assume query found until check later
				docCounter++;   // Increment number of docs program has crawled through.
				u = null;		// Initialize u Url, holds any Urls found in this page.

				while ((s = input.readLine()) != null) { // Read the document specified by theUrl

					u = extractURL(s);		// Get any Urls in this page and add them to queue and visited if u has not been visited.
					if (u != null && !visited.contains(u.toString())) {
						queue.add(u);
						visited.add(u.toString());
					}

					for (int i = 0; i < query.length; ++i) { // Check if any of the query words is in this line of the
						// document
						if ((s.toLowerCase()).indexOf(query[i].toLowerCase()) != -1) {
							seen[i] = true; // If word i found write true to seen boolean array for word i.
						}
					}
				}
				// Check through seen, if a word was not found qFound = false and query considered not found.
				for (int i = 0; i < seen.length; i++) {
					if (!seen[i]) {
						qFound = false;
						break;
					}
				}
				if (qFound) {  // If all query words found then query considered found.
					InOut.printFileName(theUrl);
				}
			}

			input.close();  // Close input and then output endListFiles
			InOut.endListFiles(); // You MUST invoke this method before your program terminates
			docCounter--; // Decrement docCounter to not count starting page.

			System.out.println("Number of documents: " + docCounter); // Print docCounter

		} catch (MalformedURLException mue) {  // Catch Errors.
			System.out.println("Malformed URL");

		} catch (IOException ioe) {
			System.out.println("IOException " + ioe.getMessage());
		}

	}

	/*
	 * If there is an URL embedded in the text passed as parameter, the URL will be
	 * extracted and returned; if there is no URL in the text, the value null will
	 * be returned
	 */
	public static URL extractURL(String text) throws MalformedURLException {
		String textUrl;
		int index = text.lastIndexOf("a href=");
		if (index > -1) {
			textUrl = baseURL + text.substring(index + 8, text.length() - 2); // Form the complete URL
			return new URL(textUrl);
		} else
			return null;
	}
}
