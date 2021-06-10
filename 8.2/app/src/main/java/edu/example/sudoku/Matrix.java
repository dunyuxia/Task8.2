package edu.example.sudoku;

import android.util.Log;

import androidx.appcompat.widget.AppCompatButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Matrix
{
	protected int iSide;
	protected int[] iValues;
	protected int[][] iMatrix;
	protected int iCursor;
	protected int iAttempt;

	Matrix(int iSize, AppCompatButton[][] mCells)
	{
		this.iSide = iSize;

		this.iValues = new int[iSide];
		for (int i = 0; i < iSide; i++)
		{
			iValues[i] = i + 1;
		}

		iMatrix = new int[iSide][iSide];

		for (int iRow = 0; iRow < iSize; iRow++)
		{
			for (int iCol = 0; iCol < iSize; iCol++)
			{
				String text = mCells[iRow][iCol].getText().toString();

				if (!text.isEmpty())
				{
					iMatrix[iRow][iCol] = Integer.parseInt(text);
				}
			}
		}
	}

	Matrix(Matrix matrix)
	{
		iSide = matrix.iSide;
		iValues = matrix.iValues;

		iMatrix = new int[matrix.iSide][matrix.iSide];

		for (int i = 0; i < matrix.iMatrix.length; i++)
		{
			System.arraycopy(matrix.iMatrix[i], 0, iMatrix[i], 0, matrix.iSide);
		}

		iCursor = -1;
		iAttempt = -1;
	}

	public boolean validate()
	{
		//  Because all LGCs are taken into consideration in algorithms
		//  To validate whether a state is a valid solution, we just need to evaluate all row and columns are in ONE-TO-ONE MAPPING state to the array of allowed values
		for (int i = 0; i < iSide; i++)
		{
			if (SLib.notOneToOneMapping(SLib.valuesInRow(iMatrix, i), iValues))
				return false;

			if (SLib.notOneToOneMapping(SLib.valuesInColumn(iMatrix, i), iValues))
				return false;

			if (SLib.notOneToOneMapping(SLib.valuesInCage(iMatrix, i), iValues))
				return false;
		}

		return true;
	}

	public void setTo(Matrix matrix)
	{
		iSide = matrix.iSide;
		iValues = matrix.iValues;

		iMatrix = new int[matrix.iSide][matrix.iSide];

		for (int i = 0; i < matrix.iMatrix.length; i++)
		{
			System.arraycopy(matrix.iMatrix[i], 0, iMatrix[i], 0, matrix.iSide);
		}

		iCursor = matrix.iCursor;
		iAttempt = matrix.iAttempt;
	}

	//	Set the Matrix as the state of DancingLink instance
	//  Used when a valid DancingLink solution is found
	public void setTo(DancingLink link)
	{
		//  mInitialSolution holds all the rows added to the SOLUTION because of the existence of initial non-zero values
		//  Because the initial non-zero values MUST be preserved in final solution
		//  Consequently, these rows CANNOT be backtracked
		for (int anInt : link.mInitialSolution)
		{
			int iRow = anInt / (iSide * iSide);
			int iCol = anInt % (iSide * iSide) / iSide;

			iMatrix[iRow][iCol] = iValues[anInt % iSide];
		}

		//  mSolution holds all the rows added as the algorithm proceeds
		//  These rows may be backtracked
		for (int anInt : link.mSolution)
		{
			int iRow = anInt / (iSide * iSide);
			int iCol = anInt % (iSide * iSide) / iSide;
			iMatrix[iRow][iCol] = iValues[anInt % iSide];
		}
	}

	public int get(int iRow, int iCol)
	{
		return iMatrix[iRow][iCol];
	}

	public Matrix tryFirstBlankCell()
	{
		for (int iRow = 0; iRow < iSide; iRow++)
		{
			for (int iCol = 0; iCol < iSide; iCol++)
			{
				if (iMatrix[iRow][iCol] == 0)
				{
					int index = iRow * iSide + iCol;

					if (iCursor != index)
					{
						iCursor = index;
						iAttempt = -1;
					}

					List<Integer> iList = candidatesAt(iRow, iCol);

					if (iList.size() == 0 || ++iAttempt >= iList.size())
						return null;

					Matrix nMatrix = new Matrix(this);
					nMatrix.set(iRow, iCol, iList.get(iAttempt));

					return nMatrix;
				}
			}
		}

		return null;
	}

	public List<Integer> candidatesAt(int row, int col)
	{
		HashSet<Integer> iSet = new HashSet<>();
		iSet.addAll(SLib.valuesInRow(iMatrix, row));
		iSet.addAll(SLib.valuesInColumn(iMatrix, col));
		iSet.addAll(SLib.valuesInCage(iMatrix, row, col));
		iSet.remove(0);

		return SLib.getComplement(iValues, iSet);
	}
	//  =================================================

	public void set(int iRow, int iCol, int value)
	{
		iMatrix[iRow][iCol] = value;
	}

	public ArrayList<ArrayList<Integer>> getFailed()
	{
		ArrayList<ArrayList<Integer>> failedList = new ArrayList<>();
		failedList.add(new ArrayList<>());
		failedList.add(new ArrayList<>());
		failedList.add(new ArrayList<>());

		for (int i = 0; i < iSide; i++)
		{
			//  If there is any duplicated value in any row
			if (SLib.hasDuplicated(SLib.valuesInRow(iMatrix, i)))
			{
				failedList.get(0).add(i);
			}

			//  If there is any duplicated value in any column
			if (SLib.hasDuplicated(SLib.valuesInColumn(iMatrix, i)))
			{
				failedList.get(1).add(i);
			}

			if (SLib.hasDuplicated(SLib.valuesInCage(iMatrix, i)))
			{
				failedList.get(2).add(i);
			}
		}

		return failedList;
	}

	public boolean failedAlready(boolean bPrompt)
	{
		for (int i = 0; i < iSide; i++)
		{
			//  If there is any duplicated value in any row
			if (SLib.hasDuplicated(SLib.valuesInRow(iMatrix, i)))
			{
				if (bPrompt)
				{
					Log.d("Debug", String.format("Duplicated value in row %d", i));
				}

				return true;
			}

			//  If there is any duplicated value in any column
			if (SLib.hasDuplicated(SLib.valuesInColumn(iMatrix, i)))
			{
				if (bPrompt)
				{
					Log.d("Debug", String.format("Duplicated value in column %d", i));
				}
				return true;
			}

			if (SLib.hasDuplicated(SLib.valuesInCage(iMatrix, i)))
			{
				if (bPrompt)
				{
					Log.d("Debug", String.format("Duplicated value in column %d", i));
				}
				return true;
			}
		}

		return false;
	}
}