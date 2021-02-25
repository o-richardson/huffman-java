public class HNode implements Comparable<HNode>
{
	private char symbol;
	private int count;
	private HNode leftChild, parent, rightChild;

	public HNode()
	{
		this((char) -1);
	}

	public HNode(char asciiChar)
	{
		this(asciiChar, -1);
	}

	public HNode(char asciiChar, int numAppearances)
	{
		// Only ASCII characters are supported, other characters are rendered as periods
		symbol = asciiChar;
		count = numAppearances;
	}

	public int compareTo(HNode other)
	{
		// if this object is less than other return negative, if this is greater than other return positive
		return this.getCount() == other.getCount() ? 0 : this.getCount() - other.getCount();
	}

	public HNode getParent()
	{
		return parent;
	}

	public char getSymbol()
	{
		return symbol;
	}

	public int getCount()
	{
		return count;
	}

	public HNode getLeftChild()
	{
		return leftChild;
	}

	public HNode getRightChild()
	{
		return rightChild;
	}

	public void setParent(HNode newParent)
	{
		parent = newParent;
	}

	public void setSymbol(char asciiChar)
	{
		symbol = asciiChar;
	}

	public void setCount(int numAppearances)
	{
		count = numAppearances;
	}

	public void setLeftChild(HNode left)
	{
		leftChild = left;
	}

	public void setRightChild(HNode right)
	{
		rightChild = right;
	}
}
