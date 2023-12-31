package com.example.frontend;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {

    /**
     * ChatGPT Usage: Partial
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        Button courseRecommenderButton = findViewById(R.id.RecommenderButton);

        BottomNavMenu.createBottomNavMenu(this, findViewById(R.id.bottom_navigation), R.id.action_home);

        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");

        courseRecommenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, CourseRecommenderActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);

            }
        });
    }


    /**
     * ChatGPT Usage: No
     */
    public void searchCourses(View view) {
        Intent intent = new Intent(this, CourseSearchActivity.class);
        startActivity(intent);
    }

    /**
     * ChatGPT Usage: No
     */
    public void compareCourse(View view) {
        Intent intent = new Intent(this, CompareCoursesActivity.class);
        startActivity(intent);
    }
}