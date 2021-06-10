package edu.example.sudoku;

import java.util.ArrayList;

public class DancingLink extends Solver
{
	int iSide;
	int iCageSide;
	int[] iValues;

	FNode pRoot;
	FNode[] pRows;
	FNode[] pCols;

	ArrayList<Integer> mInitialSolution;

	ArrayList<Integer> mSolution;
	ArrayList<ArrayList<FNode>> mStack;

	Matrix mMatrix;

	DancingLink(Matrix matrix)
	{
		mMatrix = matrix;
		iSide = matrix.iSide;
		iCageSide = (int)Math.sqrt(iSide);
		iValues = matrix.iValues;

		//	Initialize all rows of the exact cover model. Each row represents fill a specific value into a specific square of the game
		pRows = new FNode[(int)Math.pow(iSide, 3)];
		for (int i = 0; i < pRows.length; i++)
		{
			pRows[i] = new FNode(this);
		}

		//	The root node of the game. If the right child node of the root node is null or equals to it self, the state would be a valid solution.
		pRoot = new FNode(this);

		//	Initialize all columns. All the columns are categorized into three kinds of constraints: row-value constraints, column-value constraints, and row-column constraints
		pCols = new FNode[(int)Math.pow(iSide, 2) * 4];
		for (int i = 0; i < pCols.length; i++)
		{
			FNode pNode = new FNode(this);

			//	The following 12 lines are aimed to initialize relationship of all column headers and the root node
			if (i == 0)
			{
				pNode.pLeft = pRoot;
				pRoot.pRight = pNode;
			}
			else
			{
				pNode.pLeft = pCols[i - 1];
				pCols[i - 1].pRight = pNode;
			}

			pCols[i] = pNode;
		}

		for (int r = 0; r < iSide; r++)
		{
			for (int c = 0; c < iSide; c++)
			{
				for (int v = 0; v < iSide; v++)
				{
					int nRow = r * iSide * iSide + c * iSide + v;
					int nCage = r / iCageSide * iCageSide + c / iCageSide;

					FNode pRowColumn = new FNode(nRow, r * iSide + c, this);
					FNode pRowValue = new FNode(nRow, (iSide * iSide) + r * iSide + v, this);
					FNode pColValue = new FNode(nRow, (iSide * iSide) * 2 + c * iSide + v, this);
					FNode pCageValue = new FNode(nRow, (iSide * iSide) * 3 + nCage * iSide + v, this);

					pRows[nRow].pRight = pRowColumn;
					pRowColumn.pLeft = pRows[nRow];

					pRowColumn.pRight = pRowValue;
					pRowValue.pLeft = pRowColumn;

					pRowValue.pRight = pColValue;
					pColValue.pLeft = pRowValue;

					//	Now all horizontal relationship is initialized
					//	We still need to initialize vertical relationship as the matrix is based on CROSS-LINKED LIST

					//	============For row-column constraint part=======================
					//	Each time a node is created, firstly we find its column header
					pRowColumn.pHeader = pCols[pRowColumn.iCol];
					pRowColumn.pUp = pRowColumn.pHeader.pBottom;
					pRowColumn.pHeader.pBottom.pDown = pRowColumn;
					pRowColumn.pHeader.pBottom = pRowColumn;
					pRowColumn.pHeader.iCount++;

					//	============For row-value constraint part=======================
					pRowValue.pHeader = pCols[pRowValue.iCol];
					pRowValue.pUp = pRowValue.pHeader.pBottom;
					pRowValue.pHeader.pBottom.pDown = pRowValue;
					pRowValue.pHeader.pBottom = pRowValue;
					pRowValue.pHeader.iCount++;

					//	============For column-value constraint part=======================
					pColValue.pHeader = pCols[pColValue.iCol];
					pColValue.pUp = pColValue.pHeader.pBottom;
					pColValue.pHeader.pBottom.pDown = pColValue;
					pColValue.pHeader.pBottom = pColValue;
					pColValue.pHeader.iCount++;

					//	============For cage-value constraint part=======================
					pCageValue.pHeader = pCols[pCageValue.iCol];
					pCageValue.pUp = pCageValue.pHeader.pBottom;
					pCageValue.pHeader.pBottom.pDown = pCageValue;
					pCageValue.pHeader.pBottom = pCageValue;
					pCageValue.pHeader.iCount++;
				}
			}
		}

		mInitialSolution = new ArrayList<>();
		mSolution = new ArrayList<>();
		mStack = new ArrayList<>();

		newState();

		//	Preserve initial values
		for (int iRow = 0; iRow < iSide; iRow++)
		{
			for (int iCol = 0; iCol < iSide; iCol++)
			{
				int iValue = matrix.get(iRow, iCol);

				if (iValue != 0)
				{
					int nRow = iRow * iSide * iSide + iCol * iSide + iValue - 1;
					addToSolution(nRow);
				}
			}
		}

		mInitialSolution.addAll(mSolution);
		mSolution.clear();
		mStack.clear();
	}

	private void newState()
	{
		mStack.add(0, new ArrayList<>());
	}

	private void recoverState()
	{
		if (mStack.size() != 0 && mSolution.size() != 0)
		{
			while (mStack.size() != 0 && mStack.get(0).size() != 0)
			{
				mStack.get(0).get(0).recover();
				mStack.get(0).remove(0);
			}

			mStack.remove(0);

			mSolution.remove(0);
		}
	}

	private void addToSolution(int iRow)
	{
		ArrayList<FNode> cList = new ArrayList<>();
		ArrayList<FNode> rList = new ArrayList<>();

		FNode pRow = pRows[iRow].pRight;

		while (pRow != null)
		{
			//	1.c
			cList.add(pRow.pHeader);

			FNode pNode = pRow.pHeader.pDown;

			//	1
			while (pNode != null)
			{
				if (!rList.contains(pNode))
				{
					//	1.b
					rList.add(pNode);
				}
				pNode = pNode.pDown;
			}

			pRow = pRow.pRight;
		}

		for (FNode pC : cList)
		{
			//	1.a
			removeCol(pC);
		}

		for (FNode pR : rList)
		{
			//	1
			removeRow(pR.iRow);
		}

		if (!mSolution.contains(iRow))
			mSolution.add(0, iRow);
	}

	private FNode minHeader()
	{
		FNode pFound = null;

		FNode pHeader = pRoot.pRight;
		int min = pRows.length + 1;

		while (pHeader != null)
		{
			if (min > pHeader.iCount)
			{
				pFound = pHeader;
				min = pHeader.iCount;
			}

			pHeader = pHeader.pRight;
		}

		return pFound;
	}

	@Override
	public boolean trySolve()
	{
		if (recursive())
		{
			mMatrix.setTo(this);
			return true;
		}

		return false;
	}

	private boolean recursive()
	{
		if (pRoot.pRight == null)
			return true;

		FNode pColumn = minHeader();
		FNode pRow = pColumn.pDown;

		if (pRow == null)
			return false;

		newState();
		addToSolution(pRow.iRow);

		if (pRoot.pRight == null)
		{
			return true;
		}
		else
		{
			boolean bSolved = trySolve();

			if (!bSolved)
			{
				recoverState();
				removeRow(pRow.iRow);
				return trySolve();
			}
			else
			{
				return true;
			}
		}
	}

	private void removeCol(FNode pHeader)
	{
		while (pHeader.pDown != null)
		{
			pHeader.pDown.remove();
		}

		pHeader.remove();
	}

	private void removeRow(int iRow)
	{
		FNode pCol = pRows[iRow];

		while (pCol.pRight != null)
		{
			pCol.pRight.remove();
		}
	}

	public void push(FNode pNode)
	{
		if (mStack.size() != 0)
		{
			mStack.get(0).add(0, pNode);
		}
	}
}