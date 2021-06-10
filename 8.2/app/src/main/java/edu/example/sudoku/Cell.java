package edu.example.sudoku;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatButton;

public class Cell extends AppCompatButton
{
	Cell(Context context)
	{
		super(context);

		setId(generateViewId());
		setGravity(Gravity.CENTER);
		setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
	}

	public int getInt()
	{
		return Integer.parseInt(getText().toString());
	}

	public void setInt(int n)
	{
		if (n > 0)
		{
			setText(String.valueOf(n));
		}
		else
		{
			clear();
		}
	}

	public void clear()
	{
		setText("");
	}
}