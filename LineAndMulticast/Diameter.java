/**
 * Author: Harjap Grewal
 * Course Name: CS4446
 * Description: Algorithm to get distance of each processor from their terminal and for the hub to return the diameter.
 */

import java.util.Vector;

/* Algorithm for computing the diameter of a hub-and-spoke network. */

public class Diameter extends Algorithm {

	public int findDiameter(String id) {
		Vector<String> list_neighbours = neighbours(); // Set of neighbours of this node.
		int num_neighbours = list_neighbours.size(); // Number of neighbours of this node

		// Your initialization code goes here
		int distance = 0; // Initialize distance, the number of messages hub has recieved, the 2 largest distances.
		int numRecieved = 0;
		int largest = 0;
		int secondLrg = 0;

		Message m = null; // Initialize message.

		boolean hub = false; // To tell if the node is the hub.
		if (numNeighbours() >= 3) { 
			hub = true;
		}

		if (isRightmost()) { // If a node is a terminal it makes a message to send to its left node.
			m = createMessage(leftNeighbour(), Integer.toString(distance + 1));
		}

		try {
			while (waitForNextRound()) { // Main synchronous loop

				// Your code goes here


				if (m != null && !hub) { // Processors that are not hub send message and terminate.
					send(m);
					return distance;
				}

				if (hub) { // If processor is hub then recieve messages from neighbors to get 2 largest distances.
					m = receive();
					while (m != null) {

						distance = Integer.parseInt(getDataItem(1, m)); // Get spoke distance.

						if (distance > largest) { // Update 2 largest distances.
							secondLrg = largest;
							largest = distance;
						} else if (distance > secondLrg) {
							secondLrg = distance;
						}
						m = receive(); // Continue recieving and update number of messages recieved.
						numRecieved++;

					}

					if (numRecieved == numNeighbours()) { // Once all messages recieved from neighbors hub returns diameter.
						return largest + secondLrg;
					}

				}

				else { // If processor is not hub.

					m = receive();

					if (m != null) { // Recieve a message to get node's distance and make a message to send new distance to left node.

						distance = Integer.parseInt(getDataItem(1, m));
						m = createMessage(leftNeighbour(), Integer.toString(distance + 1));
					}

				}

			}
		} catch (SimulatorException e) {
			System.out.println("ERROR: " + e.toString());
		}

		// If we got here, something went wrong! (Exception, node failed, etc.)
		return 0;
	}

	/*
	 * =============================================================================
	 * == Do not modify any of the methods below
	 * =============================================================================
	 * ==
	 */

	public Object run() {
		int status = findDiameter(getID());
		return status;
	}

	/*
	 * Receives as input a message containing several data items and the position of
	 * a data item, and it returns the corresponding data item. The first data item
	 * is at position 1, the second is ar posiiton 2, and so on.
	 */
	private String getDataItem(int numItem, Message msg) {
		String[] messages = unpack(msg.data());
		return messages[numItem - 1];
	}

	/*
	 * Creates a message <destination,source, dataItem1, dataItem2, ...> with data
	 * containing an arbitray number of data items.
	 */
	private Message createMessage(String destination, String... dataItems) {
		String msg = "";
		for (int i = 0; i < dataItems.length - 1; ++i)
			msg = msg + dataItems[i] + ",";
		msg = msg + dataItems[dataItems.length - 1];
		return makeMessage(destination, msg);
	}

	/* Returns the id of the left neighbour of this processor */
	private String rightNeighbour() {
		Vector v = neighbours();
		return (String) v.elementAt(1);
	}

	/* Returns the id of the right neighbour of this processor */
	private String leftNeighbour() {
		Vector v = neighbours();
		return (String) v.elementAt(0);
	}

	/* Returns true if this processor is the leftmost one in the network */
	private boolean isLeftmost() {
		Vector<String> v = neighbours();
		String leftNeighbour = (String) v.elementAt(0);
		if (equal(leftNeighbour, "0"))
			return true;
		else
			return false;
	}

	/* Returns true if this processor is the rightmost one in the network */
	private boolean isRightmost() {
		Vector<String> v = neighbours();
		String rightNeighbour = (String) v.elementAt(1);
		if (equal(rightNeighbour, "0"))
			return true;
		else
			return false;
	}

}
