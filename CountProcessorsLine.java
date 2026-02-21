/**
 * Author: Harjap Grewal
 * Course Name: CS4446
 * Description: A class that finds the distance fromt the righmost processor in a line.
 */

import java.util.Vector;

/* Algorithm for computing the distance to the rightmost processor in a line network. */

public class CountProcessorsLine extends Algorithm {

	public int countProcessors(String id) {

		// Your initialization code goes here
		
		// Initialize distance, message, and if we have reached the end of the line.
		int distance = 0;
		Message m = null;
		boolean end = false;
		
		// If node is rightmost then start by sending a message as it already knows its distance to be 0.
		if (isRightmost()) {
			m = createMessage(leftNeighbour(), Integer.toString(distance + 1));
		}
		
		// The leftmost node is the end.
		if (isLeftmost()) {
			end = true;
		}

		try {
			while (waitForNextRound()) {
				// Main synchronous loop. All processors wait here for
				// the beginning of the next round.

				// Your code goes here
				
				// If processor has a message then send it and return distance/terminate.
				// Ony send message when processor knows its own distance.
				if (m != null) {
					send(m);
					return distance;
				}
				
				// Processor checks for a message
				m = receive();

				if (m != null) { // If recieved a message then get distance from right node.

					distance = Integer.parseInt(getDataItem(1, m));

					if (end == true) { // If last node just return distance.
						return distance;
					}
					// Create a message to tell the left node its distance.
					m = createMessage(leftNeighbour(), Integer.toString(distance + 1));
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
		int larger = countProcessors(getID());
		return "" + larger;
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
