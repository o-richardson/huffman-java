import java.nio.ByteBuffer;

import java.util.List;
import java.util.Stack;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;

/* Huffman Encoder
 * An ArrayList of leaf nodes is used to traverse up the tree for encoding.
 * Only ASCII test is supported right now, and it has not been tested with
 * control characters. For educational purposes only, all rights reserved.
 */
public class Encoder
{
	private	char[]      plaintext;
	private	HuffmanTree huffmanTree;
	private	String      encodingTable;
	private	String      bitString;
	private	byte[]      encodedData;

	private static final int BASE_2;
	private static final int INT_SIZE;
	private static final int FIELD_SIZE;
	private static final int RECORD_SIZE;

	private static final int ASCII_ALPHABET_SIZE;
	private static final int ASCII_MIN;
	private static final int ASCII_MAX;

	static
	{
		BASE_2              = 2;   // for bit string parsing with built-in functions
		INT_SIZE            = 4;   // 4 bytes in a standard Java int
		FIELD_SIZE			= 1;   // 1 byte per field in a 3-byte record
		RECORD_SIZE         = 3;   // 3 bytes for char-encodingLength-encoding tuple

		ASCII_ALPHABET_SIZE = 128; // ASCII, 0-127 inclusive
		ASCII_MIN           = 0;   // null character
		ASCII_MAX           = 127; // DEL control character
	}

	public Encoder(byte[] unencodedByteArray)
	{
		this(ByteBuffer.wrap(unencodedByteArray).asCharBuffer().toString());
	}

	public Encoder(String unencodedString)
	{
		this(unencodedString.toCharArray());
	}

	// This builds the huffman tree and a table of the codes
	public Encoder(char[] unencodedSymbolArray)
	{
		plaintext = unencodedSymbolArray;

		// ASCII table of symbol counts
		// A symbol's count is stored at its numeric index
		// e.x. symbolCounts[ (int) 'a' ] would contain the count for ASCII
		//      character a
		int symbolCounts[] = new int[ASCII_ALPHABET_SIZE];

		// LinkedHashSet does not allow duplicates
		LinkedHashSet<Character> symbolSet = new LinkedHashSet<>();
		int numUniqueSymbols = 0;
		for(char symbol : plaintext)
		{
			if( (int) symbol >= ASCII_MIN  &&  (int) symbol <= ASCII_MAX )
			{
				symbolCounts[ (int) symbol ]++;

				// If this is a unique symbol
				if(symbolSet.add(symbol))
				{
					numUniqueSymbols++;
				}
			}
		}

		char uniqueSymbols[] = new char[numUniqueSymbols];

		Iterator<Character> symbolSetIterator = symbolSet.iterator();
		for(int i = 0 ; symbolSetIterator.hasNext() ; i++)
		{
			uniqueSymbols[i] = symbolSetIterator.next();
			System.out.print(uniqueSymbols[i] + "\t"); // ************TEST
		}
		System.out.println();// ************TEST

		// eligibleNodes holds HNodes for building the tree.
		// Calling eligibleNodes.poll() will remove and return the lowest
		// count HNode
		PriorityQueue<HNode> eligibleNodes = new PriorityQueue<HNode>();
		int tempCount = 0;
		HNode eligibleNode;
		for( int i = 0 ; i < uniqueSymbols.length ; i++ )
		{
			tempCount    = symbolCounts[ (int) uniqueSymbols[i] ];
			eligibleNode = new HNode( uniqueSymbols[i] , tempCount );
			eligibleNodes.add(eligibleNode);
		}

		// leafNodes holds HNodes for traversing up the tree later
		HNode leaves[] = new HNode[eligibleNodes.size()];
		leaves = eligibleNodes.toArray(leaves);
		List<HNode> leafList = Arrays.asList(leaves);
		ArrayList<HNode> leafNodes = new ArrayList<HNode>(leafList);

		// Build the HuffmanTree.
		HNode tempLeftNode, tempParent, tempRightNode;
		int parentCount;
		while(eligibleNodes.size() > 1)
		{
			// lowest count HNode will be the left child
			tempLeftNode = eligibleNodes.poll();

			// second lowest count HNode will be the right child
			tempRightNode = eligibleNodes.poll();

			// Parent has sum of child counts but its symbol is marked -1 to
			// signal that it is not a child with a valid symbol
			parentCount = tempLeftNode.getCount() + tempRightNode.getCount();
			tempParent = new HNode( (char) -1 , parentCount );

			// Boring node logic zzzzzzz
			tempParent.setLeftChild(tempLeftNode);
			tempParent.setRightChild(tempRightNode);

			tempLeftNode.setParent(tempParent);
			tempRightNode.setParent(tempParent);

			// We still want to be able to merge with the parent
			eligibleNodes.add(tempParent);

			// HNodes with -1 for their symbol are not leaf nodes
			if(tempLeftNode.getSymbol() != (char) -1)
			{
				leafNodes.add(tempLeftNode);
			}
			if(tempRightNode.getSymbol() != (char) -1)
			{
				leafNodes.add(tempRightNode);
			}
		}

		// final node in the queue will be the root
		huffmanTree = new HuffmanTree(eligibleNodes.poll());

		// Generate an encoding table by traversing up the tree
		// If current node is a left child we push 0 onto the stack,
		// and if current node is a right child we push 1 onto the stack.
		StringBuilder encodingTableBuilder = new StringBuilder("");
		Stack<Character> bitStack;
		HNode tempNode;
		int length;
		for(HNode leaf : leafNodes)
		{
			tempNode = leaf;
			length = 0;
			bitStack = new Stack<Character>();
			
			while(tempNode.getParent() != null)
			{
				if(tempNode == tempNode.getParent().getLeftChild())
				{
					bitStack.add('0');
				}
				else // right child
				{
					bitStack.add('1');
				}
				tempNode = tempNode.getParent();
				length++;
			}

			encodingTableBuilder.append(leaf.getSymbol());
			encodingTableBuilder.append("::");
			encodingTableBuilder.append(length);
			encodingTableBuilder.append("::");

			while(!bitStack.empty())
			{
				encodingTableBuilder.append(bitStack.pop());
			}

			encodingTableBuilder.append(":!");
		}

		encodingTable = encodingTableBuilder.toString();

		// Build the bit string by traversing up the tree
		StringBuilder bitStringBuilder = new StringBuilder();
		for(char symbol : plaintext)
		{
			for(HNode leaf : leafNodes)
			{
				if(leaf.getSymbol() == symbol)
				{
					tempNode = leaf;
					bitStack = new Stack<Character>();
					while(tempNode.getParent() != null)
					{
						// if tempNode is a left child
						if(tempNode == tempNode.getParent().getLeftChild())
						{
							bitStack.add('0');
						}
						else // else tempNode is a right child
						{
							bitStack.add('1');
						}

						tempNode = tempNode.getParent();
					}
					while(!bitStack.empty())
					{
						bitStringBuilder.append(bitStack.pop());
					}

					// move to next symbol now
					break;
				}
			}
		}

		bitString = bitStringBuilder.toString();

		/*
		 *	Pack the encoding table and bitString into an array of bytes.
		 *
		 *	The first byte will represent the number of unique chars.
		 *	After that, a numeric character value is stored as its byte
		 *	representation followed by the length of its binary encoding
		 *	followed by the binary encoding itself, all 1 byte each. It is
		 *	done for each unique character. Then, the integer length of the
		 *	bit string representation of the encoded data will be
		 *	stored in INT_SIZE (8) bytes (big-endian). Finally, the
		 *	encoded bit string itself is stored as the remaining bytes in the
		 *	array. If their are less than 8 bits for the last byte, it will be
		 *	padded with 0s on the right.
		 *
		 */
		int bytesNeeded = (int) Math.ceil(bitString.length() / 8.0);
		int tableSize = (numUniqueSymbols * RECORD_SIZE) + 1 + INT_SIZE;
		bytesNeeded += tableSize;
		byte tempEncodedData[] = new byte[bytesNeeded];
		byte tempByte;
		byte numPaddingBits;
		int  limit;
		String byteString;
		// First we go through bitString 8 bits (1 byte) at a time and add
		// the encoded byte from tempEncodedData[tableSize] to
		// tempEncodedData[tempEncodedData.length - 1]
		for(int bitIndex = 0 ; bitIndex < bitString.length() ; bitIndex += 8)
		{
			try
			{
				// Check if there are 8 bits left in the bitString
				limit      = bitIndex + 8;
				byteString = bitString.substring(bitIndex, limit);
				tempByte   = (byte) Integer.parseInt(byteString, BASE_2 );
			}
			catch(IndexOutOfBoundsException lastByteNotifierException)
			{
				// Pad the last byte with bitIndex + 8 - bitString.length() 0s
				limit          = bitString.length();
				byteString     = bitString.substring(bitIndex, limit);
				tempByte       = (byte) Integer.parseInt(byteString, BASE_2);
				numPaddingBits = (byte) (bitIndex + 8 - limit);
				tempByte       = (byte) (tempByte << numPaddingBits);
			}
			tempEncodedData[ tableSize + (bitIndex / 8) ] = tempByte;
		}

		tempEncodedData[0] = (byte) numUniqueSymbols;

		// Split table into records
		String encodings[] = encodingTable.split(":!");
		String charAndEncoding[] = new String[RECORD_SIZE];
		char character;
		String encLength;
		String encString;
		for(int i = 1 ; i < tableSize - INT_SIZE ; i += RECORD_SIZE)
		{
			// Split record into fields
			charAndEncoding = encodings[i / RECORD_SIZE].split("::");

			// 3 fields per record, takes up 3 bytes
			character = charAndEncoding[0].toCharArray()[0];
			encLength = charAndEncoding[FIELD_SIZE];
			encString = charAndEncoding[2 * FIELD_SIZE];

			tempEncodedData[i] = (byte) character;

			tempEncodedData[ i + FIELD_SIZE ] = Byte.parseByte(encLength);

			tempByte = Byte.parseByte(encString, BASE_2);

			tempEncodedData[ i + (2 * FIELD_SIZE) ] = tempByte;
		}

		// Store the integer length of the bitString in 4 bytes
		ByteBuffer lengthBuffer = ByteBuffer.allocate(INT_SIZE);
		lengthBuffer.putInt(bitString.length());
		byte lengthBytes[] = lengthBuffer.array();
		for(int i = tableSize - INT_SIZE ; i < tableSize ; i++)
		{
			tempEncodedData[i] = lengthBytes[ i - tableSize + INT_SIZE ];
		}

		encodedData = tempEncodedData;

		// print numeric value of bytes for testing
		StringBuilder encodedBitStringBuilder = new StringBuilder();
		String binaryString;
		for(byte b : encodedData)
		{
			// Java forces signed variables. & a byte with 0xFF for unsigned.
			// Bitwise AND will promote the value to an int with 32 bits,
			// then we will take only the final 8 bits when formatting the
			// String representation.
			// e.x.
			// New sign bit            10101010
			// |                       11111111 &
			// v                       --------
			// 00000000000000000000000010101010
			binaryString = Integer.toBinaryString(b & 0xFF);

			// Take only the lowest 8 bits
			binaryString = String.format("%8s", binaryString);

			// Pad with 0's
			binaryString = binaryString.replace(' ', '0');

			encodedBitStringBuilder.append(binaryString);
			System.out.print("\n" + binaryString);
		}
		System.out.println();
		System.out.println(encodedBitStringBuilder.toString());
	}

	public HuffmanTree getHuffmanTree()
	{
		return huffmanTree;
	}

	public String getEncodingTable()
	{
		return encodingTable;
	}

	public String getBitString()
	{
		return bitString;
	}

	public byte[] getEncodedData()
	{
		return Arrays.copyOf(encodedData, encodedData.length);
	}
}
