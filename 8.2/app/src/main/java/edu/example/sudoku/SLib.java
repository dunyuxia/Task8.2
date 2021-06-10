package edu.example.sudoku;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class SLib
{
	final static int LightColor = Color.rgb(254, 206, 158);
	final static int HighColor = Color.rgb(208, 140, 71);

	static DisplayMetrics GetMetrics(Context context)
	{
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		return dm;
	}

	static boolean notOneToOneMapping(ArrayList<Integer> aList, int[] iArr)
	{
		if (aList.size() != iArr.length)
			return true;

		for (int I : iArr)
		{
			if (!aList.contains(I))
				return true;
		}

		return false;
	}

	static boolean hasDuplicated(ArrayList<Integer> iList)
	{
		HashSet<Integer> iSet = new HashSet<>(iList);
		return iSet.size() != iList.size();
	}

	static List<Integer> getComplement(int[] iValues, HashSet<Integer> union)
	{
		List<Integer> complete = Arrays.stream(iValues).boxed().collect(Collectors.toList());
		complete.removeAll(union);

		return complete;
	}

	static ArrayList<Integer> valuesInRow(int[][] matrix, int row)
	{
		ArrayList<Integer> aList = new ArrayList<>();

		for (int anInt : matrix[row])
		{
			if (anInt != 0)
				aList.add(anInt);
		}

		return aList;
	}

	static ArrayList<Integer> valuesInColumn(int[][] matrix, int column)
	{
		ArrayList<Integer> aList = new ArrayList<>();

		for (int[] ints : matrix)
		{
			if (ints[column] != 0)
				aList.add(ints[column]);
		}

		return aList;
	}

	static ArrayList<Integer> valuesInCage(int[][] iMatrix, int row, int col)
	{
		int sqrt = (int)Math.sqrt(iMatrix.length);
		int cRow = row / sqrt;
		int cCol = col / sqrt;
		int cage = cRow * sqrt + cCol;

		return valuesInCage(iMatrix, cage);
	}

	static ArrayList<Integer> valuesInCage(int[][] iMatrix, int cage)
	{
		int sqrt = (int)Math.sqrt(iMatrix.length);
		int cRow = cage / sqrt;
		int cCol = cage % sqrt;

		ArrayList<Integer> aList = new ArrayList<>();

		for (int row = cRow * sqrt; row < cRow * sqrt + sqrt; row++)
		{
			for (int col = cCol * sqrt; col < cCol * sqrt + sqrt; col++)
			{
				if (iMatrix[row][col] != 0)
					aList.add(iMatrix[row][col]);
			}
		}

		return aList;
	}
}