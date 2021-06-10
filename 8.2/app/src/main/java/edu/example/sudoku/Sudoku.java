package edu.example.sudoku;

import android.app.slice.Slice;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class Sudoku
{
	private final Context kContext;
	private final ConstraintLayout kLayout;

	private int n;
	private Cell bLatest;
	private Cell[][] kCells;
	private final ArrayList<Cell> kList;
	private Matrix mMatrix;

	Sudoku(Context context, ConstraintLayout layout)
	{
		kContext = context;
		kLayout = layout;
		kList = new ArrayList<>();
	}

	public void setRandom()
	{
		double pob = 0.4;

		if (n == 9)
			pob = 1 / 81f * 2;
		else if (n == 16)
			pob = 1 / 256f;

		while (true)
		{
			int count = 0;
			for (int row = 0; row < n; row++)
			{
				for (int col = 0; col < n; col++)
				{
					if (Math.random() < pob)
					{
						int value = (int)(Math.random() * n);

						if (value != 0)
						{
							count++;
							kCells[row][col].setInt(value);
						}
					}
				}
			}

			if (count == 0)
				continue;

			Matrix m = new Matrix(n, kCells);
			setSquares(m);

			BackTracking backTracking = new BackTracking(m);

			if (backTracking.trySolve())
				return;
			else
			{
				for (int iRow = 0; iRow < n; iRow++)
				{
					for (int iCol = 0; iCol < n; iCol++)
					{
						kCells[iRow][iCol].setInt(0);
					}
				}
			}
		}
	}

	public void setGame(int n)
	{
		this.n = n;
		bLatest = null;
		kCells = new Cell[n][n];
		mMatrix = null;

		for (Cell button : kList)
		{
			kLayout.removeView(button);
		}
		kList.clear();

		DisplayMetrics DM = SLib.GetMetrics(kContext);

		int iSize = Math.min(DM.widthPixels, DM.heightPixels) / n;

		for (int iRow = 0; iRow < n; iRow++)
		{
			for (int iCol = 0; iCol < n; iCol++)
			{
				Cell cell = new Cell(kContext);
				cell.setOnClickListener(onCell);
				cell.setBackgroundColor((iRow + iCol) % 2 == 0 ? SLib.HighColor : SLib.LightColor);
				cell.setTextSize(iSize / 5.0f);

				kLayout.addView(cell, iSize, iSize);
				kCells[iRow][iCol] = cell;
				kList.add(cell);
			}
		}

		ConstraintSet set = new ConstraintSet();
		set.clone(kLayout);

		for (int iRow = 0; iRow < n; iRow++)
		{
			for (int iCol = 0; iCol < n; iCol++)
			{
				if (iCol == 0)
				{
					set.setHorizontalChainStyle(kCells[iRow][iCol].getId(), ConstraintSet.CHAIN_PACKED);
					set.connect(kCells[iRow][iCol].getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
				}
				else
				{
					set.connect(kCells[iRow][iCol].getId(), ConstraintSet.START, kCells[iRow][iCol - 1].getId(), ConstraintSet.END);
				}

				if (iCol == n - 1)
				{
					set.connect(kCells[iRow][iCol].getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
				}
				else
				{
					set.connect(kCells[iRow][iCol].getId(), ConstraintSet.END, kCells[iRow][iCol + 1].getId(), ConstraintSet.START);
				}

				if (iRow == 0)
				{
					set.setVerticalChainStyle(kCells[iRow][iCol].getId(), ConstraintSet.CHAIN_PACKED);
					set.connect(kCells[iRow][iCol].getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
				}
				else
				{
					set.connect(kCells[iRow][iCol].getId(), ConstraintSet.TOP, kCells[iRow - 1][iCol].getId(), ConstraintSet.BOTTOM);
				}

				if (iRow == n - 1)
				{
					set.connect(kCells[iRow][iCol].getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
				}
				else
				{
					set.connect(kCells[iRow][iCol].getId(), ConstraintSet.BOTTOM, kCells[iRow + 1][iCol].getId(), ConstraintSet.TOP);
				}
			}
		}

		set.applyTo(kLayout);
	}

	View.OnClickListener onCell = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Cell bClicked = (Cell)v;

			if (bLatest != bClicked)
			{
				bLatest = bClicked;
			}

			if (bClicked.getText().length() == 0)
			{
				bClicked.setText("1");
			}
			else
			{
				int value = (bClicked.getInt() + 1) % (n + 1);

				if (value != 0)
				{
					bClicked.setInt(value);
				}
				else
				{
					bClicked.setText("");
				}
			}

			Matrix matrix = new Matrix(n, kCells);

			ArrayList<ArrayList<Integer>> failedList = matrix.getFailed();

			if (failedList.get(0).size() != 0 || failedList.get(1).size() != 0 || failedList.get(2).size() != 0)
			{
				for (int i = 0; i < failedList.get(0).size(); i++)
					setRedForRow(failedList.get(0).get(i));

				for (int i = 0; i < failedList.get(1).size(); i++)
					setRedForCol(failedList.get(1).get(i));

				for (int i = 0; i < failedList.get(2).size(); i++)
					setRedForCage(failedList.get(2).get(i));
			}
			else
			{
				if (matrix.validate())
				{
					setColorForAll(R.color.green);
					Toast.makeText(kContext, "A valid solution.", Toast.LENGTH_SHORT).show();
				}
				else
				{
					setColorForAll(R.color.black);
				}
			}
		}
	};

	private void setRedForRow(int row)
	{
		for (int iCol = 0; iCol < n; iCol++)
		{
			kCells[row][iCol].setTextColor(ContextCompat.getColor(kContext, R.color.red));
		}
	}

	private void setRedForCol(int col)
	{
		for (int iRow = 0; iRow < n; iRow++)
		{
			kCells[iRow][col].setTextColor(ContextCompat.getColor(kContext, R.color.red));
		}
	}

	private void setRedForCage(int cage)
	{
		int sqrt = (int)Math.sqrt(n);
		int cRow = cage / sqrt;
		int cCol = cage % sqrt;

		for (int row = cRow * sqrt; row < cRow * sqrt + sqrt; row++)
		{
			for (int col = cCol * sqrt; col < cCol * sqrt + sqrt; col++)
			{
				kCells[row][col].setTextColor(ContextCompat.getColor(kContext, R.color.red));
			}
		}
	}

	private void setColorForAll(int color)
	{
		for (int iRow = 0; iRow < n; iRow++)
		{
			for (int iCol = 0; iCol < n; iCol++)
			{
				kCells[iRow][iCol].setTextColor(ContextCompat.getColor(kContext, color));
			}
		}
	}

	void solve()
	{
		Matrix matrix;

		if (mMatrix == null)
			mMatrix = new Matrix(n, kCells);

		matrix = new Matrix(mMatrix);

		Solver backTracking = new BackTracking(matrix);

		long lStart = System.currentTimeMillis();
		boolean solved = false;

		if (!matrix.failedAlready(false))
		{
			solved = backTracking.trySolve();
		}

		long lEnd = System.currentTimeMillis();

		setSquares(matrix);

		if (solved)
		{
			setColorForAll(R.color.green);
			Toast.makeText(kContext, String.format(Locale.getDefault(), "Time taken: %.3f seconds.", (lEnd - lStart) / 1000.0), Toast.LENGTH_SHORT).show();
		}
		else
		{
			setColorForAll(R.color.red);
			Toast.makeText(kContext, String.format(Locale.getDefault(), "Failed to Solve. Time taken: %.3f seconds.", (lEnd - lStart) / 1000.0), Toast.LENGTH_SHORT).show();
		}
	}

	void setSquares(Matrix matrix)
	{
		setGame(matrix.iSide);

		for (int iRow = 0; iRow < n; iRow++)
		{
			for (int iCol = 0; iCol < n; iCol++)
			{
				if (matrix.iMatrix[iRow][iCol] != 0)
				{
					kCells[iRow][iCol].setInt(matrix.iMatrix[iRow][iCol]);
				}
				else
				{
					kCells[iRow][iCol].clear();
				}
			}
		}
	}
}