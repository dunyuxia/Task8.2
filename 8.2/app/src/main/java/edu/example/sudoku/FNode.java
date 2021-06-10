package edu.example.sudoku;

public class FNode
{
	//	Reference to its column header, which is used to facilitate "traverse" from top to bottom of a column
	protected FNode pHeader;
	//	Reference to its column header's bottom-most node. This is used to facilitate jumping from top to bottom of a column
	protected FNode pBottom;

	//	iRow and iCol represent the X and Y coordinates of the node in the sparse matrix
	protected int iRow;
	protected int iCol;

	//	The following four references represent left, right, up, and down child nodes respectively
	protected FNode pLeft;
	protected FNode pRight;

	protected FNode pUp;
	protected FNode pDown;

	//	Indicates whether the current node is removed from the link
	protected boolean bRemoved;

	//	For column headers use ONLY. Indicates how many child nodes are there in the column
	protected int iCount;

	//	To which DancingLink instance the node belongs.
	//	DancingLink requires STRICT REVERSED ORDER when recovering removed nodes
	//	This reference is used to notify the DancingLink instance when the current node is removed or recovered.
	//	Hence the DancingLink instance is able to hold the order of nodes being removed
	private final DancingLink pLink;

	FNode(DancingLink iLink)
	{
		this.pLink = iLink;
		this.pBottom = this;
	}

	FNode(int iRow, int iCol, DancingLink iLink)
	{
		this.iRow = iRow;
		this.iCol = iCol;
		this.pLink = iLink;
	}

	void remove()
	{
		//	If the node is removed already, it should not be removed again
		if (!bRemoved)
		{
			if (pLeft != null)
				pLeft.pRight = pRight;

			if (pRight != null)
				pRight.pLeft = pLeft;

			if (pUp != null)
				pUp.pDown = pDown;

			if (pDown != null)
				pDown.pUp = pUp;

			bRemoved = true;

			//	Notify the column header one of its child nodes are removed.
			//	Such that we can easily keep track of how many child nodes are there in the column by just evaluating iCount rather than traverse all child nodes in the column
			//	This feature is quite useful to select the column with least existing nodes
			//	Which could be a powerful optimization of algorithm performance
			if (pHeader != null)
				pHeader.iCount--;

			//	Push the node into a stack
			//	Stack - LIFO (Last In First Out)
			//	Consequently, the order of recovering nodes will be STRICT OPPOSITE TO that the nodes being removed
			pLink.push(this);
		}
	}

	void recover()
	{
		if (bRemoved)
		{
			if (pLeft != null)
				pLeft.pRight = this;

			if (pRight != null)
				pRight.pLeft = this;

			if (pUp != null)
				pUp.pDown = this;

			if (pDown != null)
				pDown.pUp = this;

			bRemoved = false;

			if (pHeader != null)
				pHeader.iCount++;
		}
	}
}