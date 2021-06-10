package edu.example.sudoku;

import java.util.ArrayList;

public class BackTracking extends Solver
{
	private final Matrix mMatrix;

	BackTracking(Matrix matrix)
	{
		this.mMatrix = matrix;
	}

	boolean trySolve()
	{
		boolean solved = false;

		ArrayList<Matrix> matrixList = new ArrayList<>();
		matrixList.add(new Matrix(mMatrix));

		while (matrixList.size() != 0)
		{
			Matrix next = matrixList.get(matrixList.size() - 1).tryFirstBlankCell();

			if (next != null)
			{
				if (next.validate())
				{
					mMatrix.setTo(next);
					solved = true;
					break;
				}
				else
				{
					//it to the back of the list
					matrixList.add(next);
				}
			}
			else
			{
				matrixList.remove(matrixList.size() - 1);
			}
		}

		return solved;
	}
}