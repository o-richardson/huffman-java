public class HuffmanTree
{
	private HNode root;

	public HuffmanTree()
	{
		this(null);
	}

	public HuffmanTree(HNode treeRoot)
	{
		root = treeRoot;
	}

	public HNode getRoot()
	{
		return root;
	}

	public void setRoot(HNode treeRoot)
	{
		root = treeRoot;
	}
}
