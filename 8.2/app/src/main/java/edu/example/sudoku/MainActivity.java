package edu.example.sudoku;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
{
	private Sudoku sudoku;
	private int size;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		SharedPreferences sharedPreferences = getSharedPreferences("Theme", Context.MODE_PRIVATE);
		int theme = sharedPreferences.getInt("Theme", 0);

		switch (theme)
		{
			case 0:
			{
				setTheme(R.style.CStyle);
				break;
			}

			case 1:
			{
				setTheme(R.style.CStyle_Dark);
				break;
			}
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Spinner sizeSP = findViewById(R.id.size);

		sizeSP.setOnItemSelectedListener(onSizeChanged);
		sizeSP.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.size)));

		sudoku = new Sudoku(getBaseContext(), findViewById(R.id.kLayout));
		sudoku.setGame(4);
	}

	AdapterView.OnItemSelectedListener onSizeChanged = new AdapterView.OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
		{
			size = (int) Math.pow(position + 2, 2);
			sudoku.setGame(size);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent)
		{

		}
	};

	public void onNewGame(View view)
	{
		sudoku.setGame(size);
	}

	public void onResolve(View view)
	{
		sudoku.solve();
	}

	public void onRandom(View view)
	{
		sudoku.setGame(size);
		sudoku.setRandom();
	}
}