/**
 * Author: Harjap Grewal
 * Course Name: CS4446
 * Description: A class to create a multicast tree.
 * I sometimes call processors nodes.
 */

import java.util.Vector;

public class Multicast extends Algorithm {

	/*
	 * Build a multicasting tree connecting the root processor to all processors
	 * with ID's > 10
	 */
	public String multicasting(String id) {
		try {
			Vector<String> adjacent = neighbours(); // Set of neighbors of this node

			// Your initialization code goes here
			Message mssg, message, ack; // Buffer for message to send, message received, and acknowledgement

			// Parent, potential Parent, and children of this node in the BFS tree.
			String parent, pParent;
			Vector<String> children = new Vector<String>();

			String[] data; // Data to place in each message

			// Node's number of neighbours, if node is a leaf, if node is allowed to be in
			// multicast.
			int neighbourCount = numNeighbours();
			boolean leaf = (neighbourCount == 1);
			boolean valid = (leaf || (Integer.parseInt(id) > 10));

			int retMsg = 0; // This counts the number of messages a processor has recieved.

			if (isRoot()) { // The root node will send messages in the first round.
				mssg = makeMessage(adjacent, pack(id, "?")); // Request for adoption to all neighbours.
				parent = "";
				pParent = "";// No parent
				
			} else { // If processor is not the root node.
				mssg = null;
				parent = "null";
				pParent = "unknown"; // Make parent null, and pParent unknown.
			}

			// Initialize acknoledgment and rounds_left
			ack = null;
			int rounds_left = -1;

			while (waitForNextRound()) { // Synchronous loop

				// Your code goes here

				if (mssg != null) {
					send(mssg); // If we have a msg send requests for adoption to all neighbours.
				}

				// Send acknolegment if this node has recieved a message from all neighbours.
				// This means its valid status cannot change any more as all shortest paths
				// related to this node have been found.
				if ((ack != null) && (retMsg == neighbourCount)) {

					if (valid) { // If valid node then set parent, send ack, and terminate.
						send(ack);
						parent = pParent;
						rounds_left = 0;
						
					} else { // Send special ack so this node is not accepted as a child, and terminate.
						ack = makeMessage(pParent, pack(id, "N"));
						send(ack);
						rounds_left = 0;
					}
				}

				mssg = null; // Message returns to null so repeat messages aren't sent.

				message = receive(); // Get a message.

				// Loop through all messages recieved this round.
				while (message != null) {

					data = unpack(message.data()); // Get the data from the message
					if (data[1].equals("?")) { // Request for adoption

						retMsg++; // Increment messages recieved.

						if (equal(pParent, "unknown")) { // Parent not set yet
							pParent = data[0];
							adjacent.remove(pParent); // Requests for adoption will not be sent to parent

							mssg = makeMessage(adjacent, pack(id, "?")); // Sent own adoption requests to neighbours
							ack = makeMessage(pParent, pack(id, "Y")); // Agree to be child of parent
						}

					} else if (data[1].equals("Y")) { // Neighbour agreed to be child of this processor
						retMsg++;
						children.add(data[0]);
						valid = true; // Increment messages returned and make this node valid as it is
										// part of the path to another valid node.

					} else if (data[1].equals("N")) { // Message from neighbor not part of multicast.
						retMsg++; // Increment returned messages.
					}

					message = receive(); // Get another message for loop.
				}

				// If root has recieved all return messages then terminate this round.
				if (isRoot() && (neighbourCount == retMsg))
					rounds_left = 0;

				if (rounds_left == 0) { // Print partent, children, and then terminate
					printParentChildren(parent, children);
					return "";
				}
			}

		} catch (SimulatorException e) { // Error message.
			System.out.println("ERROR: " + e.getMessage());
		}
		return ""; // This shouldn't happen.
	}

	/*
	 * =============================================================================
	 * == Do not modify any of the methods below
	 * =============================================================================
	 * ==
	 */

	public Object run() {
		String result = multicasting(getID());
		return result;
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

	/* Print information about parent and children of this node */
	private void printParentChildren(String parent, Vector<String> children) {
		String outMssg = "p: " + parent + ", c: ";
		for (int i = 0; i < children.size(); ++i)
			outMssg = outMssg + children.elementAt(i) + " ";
		showMessage(outMssg);
		printMessage(outMssg);
		try {
			Check.verify(getID(), isRoot(), parent, children);
		} catch (SimulatorException e) {
			System.out.println("Error, invalid netowrk node");
		}
	}

}
