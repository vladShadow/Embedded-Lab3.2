package com.example.embedded32;

        import androidx.appcompat.app.AppCompatActivity;

        import android.os.Bundle;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.CompoundButton;
        import android.widget.Spinner;
        import android.widget.Switch;
        import android.widget.TextView;
        import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{

    private Switch deadlineSwitch;
    private Spinner speedSpinner;
    private Spinner deadlineSpinner;
    private Button calcButton;
    private TextView resultText;

    private Double[] setupSpeed = {0.001, 0.01, 0.05, 0.1, 0.2, 0.3};
    private Double[] setupTimeDL = {0.5, 1.0, 2.0, 5.0};
    private Double[] setupIterationsDL = {100.0, 200.0, 500.0, 1000.0};
    private Double[][] setupPoints = {{0.0, 6.0}, {1.0, 5.0}, {3.0, 3.0}, {2.0, 4.0}};
    private Double setupThreshold = 4.0;

    private boolean useDeadline;
    private Double deadline;
    private Double speed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        deadlineSwitch = findViewById(R.id.switch_deadline);
        speedSpinner = findViewById(R.id.spinner_st);
        deadlineSpinner = findViewById(R.id.spinner_d);
        calcButton = findViewById(R.id.calculate_button);
        resultText = findViewById(R.id.result_text);
        deadlineSwitch.setOnCheckedChangeListener(this);

        ArrayAdapter<Double> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, setupSpeed);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speedSpinner.setAdapter(adapter);
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, setupTimeDL);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deadlineSpinner.setAdapter(adapter);

        deadlineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (useDeadline) {
                    deadline = setupIterationsDL[position];
                } else {
                    deadline = setupTimeDL[position];
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast toast = Toast.makeText(getApplicationContext(), "Нічого не вибрано!",
                        Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        speedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                speed = setupSpeed[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast toast = Toast.makeText(getApplicationContext(), "Нічого не вибрано!",
                        Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        calcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultText.setText(process());
            }
        });

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean bool) {
        if (bool) {
            ArrayAdapter<Double> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, setupIterationsDL);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            deadlineSpinner.setAdapter(adapter);
        } else {
            ArrayAdapter<Double> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, setupTimeDL);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            deadlineSpinner.setAdapter(adapter);
        }
        useDeadline = bool;
    }

    private String process() {
        double W1 = 0;
        double W2 = 0;
        Double[] point;
        int j;
        double y;
        double delta;
        int i = 0;
        long start;
        long end = 0;
        if (useDeadline) {
            start = System.nanoTime();
            for (; i < deadline * 2; i = i + 2) {
                j = i % 4;
                point = setupPoints[j];
                y = point[0] * W1 + point[1] * W2;
                if (pointsArray(W1, W2)) {
                    break;
                }
                delta = setupThreshold - y;
                W1 += delta * point[0] * speed;
                W2 += delta * point[1] * speed;
            }
            if (i >= deadline * 2) {
                end = System.nanoTime();
                return "Було досягнуто дедлайну ітерацій.\nW1 = " + W1 + ", W2 = " + W2 + ";" +
                        "\nчас: " + (end - start)/1000 + " мкс;\nітерацї: " + (i / 2);
            }
            end = System.nanoTime();
        } else {
            start = System.nanoTime();
            while (true) {
                j = i % 4;
                point = setupPoints[j];
                y = point[0] * W1 + point[1] * W2;
                delta = setupThreshold - y;
                if (pointsArray(W1, W2)) {
                    break;
                }
                W1 += delta * point[0] * speed;
                W2 += delta * point[1] * speed;
                i = i + 2;
                end = System.nanoTime();
                if ((end - start) >= deadline * 1000000000) {
                    return "Було досягнуто дедлайну по часу.\nW1 = " + W1 + ", W2 = " + W2 + ";" +
                            "\nчас: " + (end - start)/1000 + " мкс;\nітерацї: " + (i / 2 + 1);
                }
            }
        }
        return "Було знайдено рішення: W1 = " + W1 + ", W2 = " + W2 + ";" +
                "\nчас: " + (end - start)/1000 + " мкс;\nітерацї: " + (i / 2 + 1);
    }

    private boolean pointsArray(double W1, double W2) {
        double y1;
        for (int k = 0; k < setupPoints.length / 2; k++) {
            y1 = setupPoints[k][0] * W1 + setupPoints[k][1] * W2;
            if (y1 < setupThreshold) {
                return false;
            }
        }
        for (int k = setupPoints.length / 2; k < setupPoints.length; k++) {
            y1 = setupPoints[k][0] * W1 + setupPoints[k][1] * W2;
            if (y1 > setupThreshold) {
                return false;
            }
        }
        return true;
    }

}

