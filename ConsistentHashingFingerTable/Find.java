
/**
 * Name: Harjap Grewal
 * Description: This class finds key values for processors 
 * 				and handles processors with the same hash value in a peer to peer network.
 */

import java.util.Vector;

public class Find extends Algorithm {
	private int m; // Ring of identifiers has size 2^m
	private int SizeRing; // SizeRing = 2^m

	public Object run() {
		return find(getID());
	}

	// Each message sent by this algorithm has the form: flag, value, ID
	// where:
	// - if flag = "GET" then the message is a request to get the document with the
	// given key
	// - if flag = "LOOKUP" then the message is request to forward the message to
	// the closest
	// processor to the position of the key
	// - if flag = "FOUND" then the message contains the key and processor that
	// stores it
	// - if flag = "NOT_FOUND" then the requested data is not in the system
	// - if flag = "END" the algorithm terminates

	/*
	 * Complete method find, which must implement the Chord search algorithm using
	 * finger tables and assumming that there are two processors in the system that
	 * received the same ring identifier.
	 */

	/*
	 * All Found and Not Found messeges sent also pack the sending processors id.
	 * Sometime I write send lookup or get, and what I mean is make mssg into lookup or get along with the key value and searching pID.
	 * When going through the fingerTable I write in the comments send to smaller element, I mean the lower bound fingerTable element in the segment.
	 */
	/* ----------------------------- */
	public Object find(String id) {
		/* ------------------------------ */
		try {

			/*
			 * The following code will determine the keys to be stored in this processor,
			 * the keys that this processor needs to find (if any), and the addresses of the
			 * finger table
			 */
			Vector<Integer> searchKeys; // Keys that this processor needs to find in the P2P system. Only
										// for one processor this vector will not be empty
			Vector<Integer> localKeys; // Keys stored in this processor

			localKeys = new Vector<Integer>();
			String[] fingerTable; // Addresses of the fingers are stored here
			searchKeys = keysToFind(); // Read keys and fingers from configuration file
			fingerTable = getKeysAndFingers(searchKeys, localKeys, id); // Determine local keys, keys that need to be
																		// found, and fingers
			m = fingerTable.length - 1;
			SizeRing = exp(2, m);

			/* Your initialization code goes here */

			String[] data; // Holds message recieved data.
			int keyValue; // Holds key value.
			boolean keyProcessed = false; // Used to determine if current key has been processed
			String result = ""; // A string to hold the result.
			Message mssg = null, message; // Variables to hold messages.
			int hashID = hp(id); // Ring identifier for this processor

			if (searchKeys.size() > 0) { // If this condition is true, the processor has keys that need to be found

				
				keyValue = searchKeys.elementAt(0); // Get the first key value.
				searchKeys.remove(0); // Do not search for the same key twice

				if (localKeys.contains(keyValue)) { // If the processor has the key.

					result = result + keyValue + ":" + id + " "; // Store location of key in the result
					keyProcessed = true; // Make true so can process next key later.

				} else { // Key was not stored locally
					// Check if key would be between current processor and finger 0
					if (inSegment(hk(keyValue), hashID, hp(fingerTable[0]))) {
						mssg = makeMessage(fingerTable[0], pack("GET", keyValue, id)); // If so send Get to finger 0

					} else { // Key is not between processor and finger 0, check all other possible location
								// in finger table.
						for (int i = 0; i < fingerTable.length - 1; i++) { // Go through all the spaces between the
																			// finger table elements.

							// If key value found between two elements of the finger table then send lookup
							// to smaller element on finger table.
							if (inSegment(hk(keyValue), hp(fingerTable[i]), hp(fingerTable[i + 1]))) {

								mssg = makeMessage(fingerTable[i], pack("LOOKUP", keyValue, id));
								break; // break so mssg doesn't get over written.
							}

						}

					}
				}
			}

			while (waitForNextRound()) { // Synchronous loop
				/* Your code goes here */

				if (mssg != null) { // Send messages.
					send(mssg);

					data = unpack(mssg.data()); // If sending END messege and not searching for key values then return.
					if (data[0].equals("END") && searchKeys.size() == 0) {
						return result;
					}
				}

				mssg = null; // Clear messeges to be sent and recieve messege into message.
				message = receive();

				// Loop for recieving messeges.
				while (message != null) {

					data = unpack(message.data()); // Get messege data.

					if (data[0].equals("GET")) { // If GET messege.

						// If this is the same GET message that this processor originally sent, then the
						// key is not in the system
						if (data[2].equals(id)) {
							result = result + data[1] + ":not found ";
							keyProcessed = true;
						}
						// This processor must contain the key, if it is in the system
						else if (localKeys.contains(stringToInteger(data[1]))) { // If key value found tell searching
																					// processor.
							mssg = makeMessage(data[2], pack("FOUND", data[1], id));

						} else if (hp(id) == hp(fingerTable[0])) { // If key value not in this processor but more
																	// processors
																	// available in this ring identier send GET messege
																	// to next processor in current ring identifier.

							keyValue = stringToInteger(data[1]); // Get Key value.
							mssg = makeMessage(fingerTable[0], pack("GET", keyValue, data[2]));

						} else { // If not found and no other processors in ring Identifier then send searching
									// processor not found.
							mssg = makeMessage(data[2], pack("NOT_FOUND", data[1]));
						}

					} else if (data[0].equals("LOOKUP")) { // If recieved Lookup messege

						keyValue = stringToInteger(data[1]); // Get key value.

						if (inSegment(hk(keyValue), hashID, hp(fingerTable[0]))) // If key value between current
																					// processor and fingerTable[0]
							mssg = makeMessage(fingerTable[0], pack("GET", keyValue, data[2])); // Send Get messege to
																								// FingerTable[0].

						else { // If not go through all ranges in between fingertable elements
							for (int i = 0; i < fingerTable.length - 1; i++) {

								// If key value in segment send lookup to lower element in fingertable.
								if (inSegment(hk(keyValue), hp(fingerTable[i]), hp(fingerTable[i + 1]))) {

									mssg = makeMessage(fingerTable[i], pack("LOOKUP", keyValue, data[2]));
									break; // break so mssg doesn't get over written.
								}

							}
						}
						// For messege recieved
					} else if (data[0].equals("FOUND")) { // If messege recieved is Found, update result and get ready
															// to process next key.
						result = result + data[1] + ":" + data[2] + " ";
						keyProcessed = true;

					} else if (data[0].equals("NOT_FOUND")) { // If not found, update result and get ready to process
																// next key.
						result = result + data[1] + ":not found ";
						keyProcessed = true;

					} else if (data[0].equals("END")) { // If END messege recieved then searching processor returns,
														// other processors get ready to forward END.
						if (searchKeys.size() > 0) {
							return result;
						} else {
							mssg = makeMessage(successor(), "END");
						}
					}
					message = receive(); // Recieve next messege.
				}

				// Outside of recieve messege loop.
				if (keyProcessed) { // Search for the next key

					if (searchKeys.size() == 0) { // There are no more keys to find
						mssg = makeMessage(successor(), "END"); // Get ready for forwarding END.

					} else { // Otherwise get next key value.

						keyValue = searchKeys.elementAt(0);
						searchKeys.remove(0); // Do not search for same key twice

						if (localKeys.contains(keyValue)) { // Check if searching procesor holds key.
							result = result + keyValue + ":" + id + " "; // Store location of key in the result
							keyProcessed = true; // Get ready to process next key.

							// Check if key must be between current processor and fingerTable[0]
						} else if (inSegment(hk(keyValue), hashID, hp(fingerTable[0]))) {
							// Send Get messege to fingerTable[0].
							mssg = makeMessage(fingerTable[0], pack("GET", keyValue, id));

						} else { // Otherwise go through all spaces in fingertable
							for (int i = 0; i < fingerTable.length - 1; i++) {

								// If key value in segment send lookup messege to lower element in finger table
								// from the segment.
								if (inSegment(hk(keyValue), hp(fingerTable[i]), hp(fingerTable[i + 1]))) {

									mssg = makeMessage(fingerTable[i], pack("LOOKUP", keyValue, id));
									break; // Break so messege is not overwritten.
								}

							}
						}
					}
					if (mssg != null) // If sending a messege next round turn keyProcessed false.
						keyProcessed = false;
				}
			}

		} catch (SimulatorException e) {
			System.out.println("ERROR: " + e.toString());
		}

		/*
		 * At this point something likely went wrong. If you do not have a result you
		 * can return null
		 */
		return null;
	}

	/*
	 * Determine the keys that need to be stored locally and the keys that the
	 * processor needs to find. Negative keys returned by the simulator's method
	 * keysToFind() are to be stored locally in this processor as positive numbers.
	 */
	/*
	 * -----------------------------------------------------------------------------
	 * -----------------------
	 */
	private String[] getKeysAndFingers(Vector<Integer> searchKeys, Vector<Integer> localKeys, String id)
			throws SimulatorException {
		/*
		 * -----------------------------------------------------------------------------
		 * -----------------------
		 */
		Vector<Integer> fingers = new Vector<Integer>();
		String[] fingerTable;
		String local = "";
		int m;

		if (searchKeys.size() > 0) {
			for (int i = 0; i < searchKeys.size();) {
				if (searchKeys.elementAt(i) < 0) { // Negative keys are the keys that must be stored locally
					localKeys.add(-searchKeys.elementAt(i));
					searchKeys.remove(i);
				} else if (searchKeys.elementAt(i) > 1000) {
					fingers.add(searchKeys.elementAt(i) - 1000);
					searchKeys.remove(i);
				} else
					++i; // Key that needs to be searched for
			}
		}

		m = fingers.size();
		// Store the finger table in an array of Strings
		fingerTable = new String[m + 1];
		for (int i = 0; i < m; ++i)
			fingerTable[i] = integerToString(fingers.elementAt(i));
		fingerTable[m] = id;

		for (int i = 0; i < localKeys.size(); ++i)
			local = local + localKeys.elementAt(i) + " ";
		showMessage(local); // Show in the simulator the keys stored in this processor
		return fingerTable;
	}

	/* Determine whether hk(value) is in (hp(ID),hp(succ)] */
	/* ---------------------------------------------------------------- */
	private boolean inSegment(int hashValue, int hashID, int hashSucc) {
		/* ----------------------------------------------------------------- */
		if (hashID == hashSucc)
			if (hashValue == hashID)
				return true;
			else
				return false;
		else if (hashID < hashSucc)
			if ((hashValue > hashID) && (hashValue <= hashSucc))
				return true;
			else
				return false;
		else if (((hashValue > hashID) && (hashValue < SizeRing)) || ((0 <= hashValue) && (hashValue <= hashSucc)))
			return true;
		else
			return false;
	}

	/* Hash function to map processor ids to ring identifiers. */
	/* ------------------------------- */
	private int hp(String ID) throws SimulatorException {
		/* ------------------------------- */
		return stringToInteger(ID) % SizeRing;
	}

	/* Hash function to map keys to ring identifiers */
	/* ------------------------------- */
	private int hk(int key) {
		/* ------------------------------- */
		return key % SizeRing;
	}

	/* Compute base^exponent ("base" to the power "exponent") */
	/* --------------------------------------- */
	private int exp(int base, int exponent) {
		/* --------------------------------------- */
		int i = 0;
		int result = 1;

		while (i < exponent) {
			result = result * base;
			++i;
		}
		return result;
	}
}

