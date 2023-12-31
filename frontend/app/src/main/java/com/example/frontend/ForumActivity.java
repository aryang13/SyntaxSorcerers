package com.example.frontend;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.example.frontend.apiwrappers.ServerRequest;
import com.example.frontend.apiwrappers.UBCGradesRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Objects;

public class ForumActivity extends AppCompatActivity {

    ArrayList<View> joinedForumsViews;
    ArrayList<View> allForumsViews;
    JsonArray joinedForums = new JsonArray();
    private SwipeRefreshLayout swipeRefreshLayout;

    /* ChatGPT usage: Partial */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);

        BottomNavMenu.createBottomNavMenu(this, findViewById(R.id.bottom_navigation), R.id.action_forums);

        TabHost tabHost = findViewById(R.id.forumTabHost);
        tabHost.setup();

        TabHost.TabSpec tabSpec1 = tabHost.newTabSpec("Joined");
        tabSpec1.setIndicator("Joined");
        tabSpec1.setContent(R.id.joinedForums);

        TabHost.TabSpec tabSpec2 = tabHost.newTabSpec("All");
        tabSpec2.setIndicator("All");
        tabSpec2.setContent(R.id.allForums);
        tabHost.addTab(tabSpec1);
        tabHost.addTab(tabSpec2);

        joinedForumsViews = new ArrayList<>();
        allForumsViews = new ArrayList<>();
        generateJoinedForums();

        tabHost.setOnTabChangedListener(tabId -> {
            if (tabId.equals("Joined")) {
                resetAndAddView(findViewById(R.id.forum_layout_joined), joinedForumsViews);
            } else {
                resetAndAddView(findViewById(R.id.forum_layout_all), allForumsViews);
            }
        });

        findViewById(R.id.addForumButton).setOnClickListener(v -> {
            final Dialog dialog = new Dialog(ForumActivity.this);
            dialog.setContentView(R.layout.add_forum_dialog); // Use your custom layout

            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }

            Spinner courseName = dialog.findViewById(R.id.name_of_course);
            ArrayAdapter<String> courseNameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
            courseNameAdapter.add("Select a Subject");
            courseNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            courseName.setAdapter(courseNameAdapter);

            Spinner courseCode = dialog.findViewById(R.id.course_code);
            ArrayAdapter<String> courseCodeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
            courseCodeAdapter.add("Select a Course");
            courseCodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            courseCode.setAdapter(courseCodeAdapter);

            courseName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View selectedItemView, int position, long id) {
                    String course = (String) parent.getItemAtPosition(position);
                    if (!course.equals("Select a Subject")) {
                        populateSpinner("api/v3/courses/UBCV/" + course, courseCodeAdapter);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // Do nothing here
                }
            });

            dialog.findViewById(R.id.create_forum_button).setOnClickListener(view -> {
                String forumName = ((TextView) dialog.findViewById(R.id.new_forum_name)).getText().toString();
                String subject = courseName.getSelectedItem().toString();
                String course = courseCode.getSelectedItem().toString();
                createNewForum(view, dialog, forumName, subject, course);
            });

            // Show the dialog
            dialog.show();
            populateSpinner("api/v3/subjects/UBCV", courseNameAdapter);
        });


        swipeRefreshLayout = findViewById(R.id.forums_refresh);

        ScrollView joinedForumsScrollView = findViewById(R.id.joinedForums);
        joinedForumsScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> swipeRefreshLayout.setEnabled(scrollY == 0));

        ScrollView allForumsScrollView = findViewById(R.id.allForums);
        allForumsScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> swipeRefreshLayout.setEnabled(scrollY == 0));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Objects.equals(tabHost.getCurrentTabTag(), "Joined")) {
                    joinedForumsViews.clear();
                    generateJoinedForums();
                } else {
                    allForumsViews.clear();
                    generateAllForums(true);
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /* ChatGPT usage: No */
    /* https://stackoverflow.com/questions/5545217/back-button-and-refreshing-previous-activity */
    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    /* ChatGPT usage: Partial */
    private void createNewForum(View view, Dialog dialog, String forumName, String subject, String course) {
        if (forumName.length() > 30) {
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(),0);
            Toast.makeText(getApplicationContext(), "Forum name must be less than 30 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("GoogleAccountInfo", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");
        ServerRequest serverRequest = new ServerRequest(userId);

        ServerRequest.ApiRequestListener apiRequestListener = new ServerRequest.ApiRequestListener() {
            @Override
            public void onApiRequestComplete(JsonElement response) {
                dialog.dismiss();
                Toast toast = Toast.makeText(getApplicationContext(), "Forum created", Toast.LENGTH_SHORT);
                toast.show();
                generateJoinedForums();
            }

            @Override
            public void onApiRequestError(String error) {
                Log.d(ServerRequest.RequestTag, "Failure");
                Log.d(ServerRequest.RequestTag, error);
                Toast toast = Toast.makeText(getApplicationContext(), "Failed to create forum", Toast.LENGTH_SHORT);
                toast.show();
            }
        };

        JsonObject body = new JsonObject();
        body.addProperty("name", forumName);
        body.addProperty("course", subject + " " + course);
        try {
            serverRequest.makePostRequest("/forums", body, apiRequestListener);
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(e);
        }
    }

    /* ChatGPT usage: Partial */
    private void populateSpinner(String endpoint, ArrayAdapter<String> adapter) {
        UBCGradesRequest ubcGradesRequest = new UBCGradesRequest();
        UBCGradesRequest.ApiRequestListener apiRequestListener = new UBCGradesRequest.ApiRequestListener() {
            @Override
            public void onApiRequestComplete(JsonElement response) {
                JsonArray response_array = response.getAsJsonArray();
                for (int i = 0; i < response_array.size(); i++) {
                    if (endpoint.equals("api/v3/subjects/UBCV")) {
                        adapter.add(response_array.get(i).getAsJsonObject().get("subject").getAsString());
                    } else {
                        adapter.add(response_array.get(i).getAsJsonObject().get("course").getAsString());
                    }
                }
            }

            @Override
            public void onApiRequestError(String error) {
                Log.d(UBCGradesRequest.RequestTag, "Failure");
                Log.d(UBCGradesRequest.RequestTag, error);
            }
        };

        ubcGradesRequest.makeUBCGradesGetRequest(endpoint, apiRequestListener);
    }

    /* ChatGPT usage: Partial */
    private void generateJoinedForums() {
        SharedPreferences sharedPreferences = getSharedPreferences("GoogleAccountInfo", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);
        ServerRequest serverRequest = new ServerRequest(userId);
        ServerRequest.ApiRequestListener apiRequestListener = new ServerRequest.ApiRequestListener() {
            @Override
            public void onApiRequestComplete(JsonElement response) {
                joinedForums = response.getAsJsonArray();
                for(int i = 0;  i < joinedForums.size(); i++) {
                    JsonObject forum = joinedForums.get(i).getAsJsonObject();
                    Log.d("ForumActivity", forum.toString());
                    View joinedForumsView = getLayoutInflater().inflate(R.layout.forum_card, null);
                    joinedForumsView.setTag(forum.get("forumId").getAsString());
                    ((TextView) joinedForumsView.findViewById(R.id.forum_name)).setText(forum.get("name").getAsString());
                    ((TextView) joinedForumsView.findViewById(R.id.course_name)).setText(forum.get("course").getAsString());

                    ((Button) joinedForumsView.findViewById(R.id.join_button)).setText(R.string.joined_button);
                    joinedForumsView.findViewById(R.id.join_button).setEnabled(false);

                    joinedForumsView.setOnClickListener(v -> {
                        Log.d("TEST", "Clicked");
                        Intent intent = new Intent(ForumActivity.this, ForumViewActivity.class);
                        intent.putExtra("forumId", (String) joinedForumsView.getTag());
                        intent.putExtra("isJoined", true);
                        intent.putExtra("forumName", forum.get("name").getAsString());
                        startActivity(intent);
                    });

                    joinedForumsViews.add(joinedForumsView);
                }

                generateAllForums(false);
                resetAndAddView(findViewById(R.id.forum_layout_joined), joinedForumsViews);
            }

            @Override
            public void onApiRequestError(String error) {
                Log.d(ServerRequest.RequestTag, "Failure");
                Log.d(ServerRequest.RequestTag, error);
            }
        };

        try {
            serverRequest.makeGetRequest("/forums/user", apiRequestListener);
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(e);
        }
    }

    /* ChatGPT usage: Partial */
    private void generateAllForums(Boolean resetView) {
        SharedPreferences sharedPreferences = getSharedPreferences("GoogleAccountInfo", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);
        ServerRequest serverRequest = new ServerRequest(userId);
        ServerRequest.ApiRequestListener apiRequestListener = new ServerRequest.ApiRequestListener() {
            @Override
            public void onApiRequestComplete(JsonElement response) {
                for(int i = 0;  i < response.getAsJsonArray().size(); i++) {
                    JsonObject forum = response.getAsJsonArray().get(i).getAsJsonObject();
                    Log.d("ForumActivity", forum.toString());
                    View addForumsView = getLayoutInflater().inflate(R.layout.forum_card, null);
                    addForumsView.setTag(forum.get("forumId").getAsString());
                    ((TextView) addForumsView.findViewById(R.id.forum_name)).setText(forum.get("name").getAsString());
                    ((TextView) addForumsView.findViewById(R.id.course_name)).setText(forum.get("course").getAsString());

                    boolean isJoined = joinedForums.contains(forum);
                    if (isJoined) {
                        ((Button) addForumsView.findViewById(R.id.join_button)).setText(R.string.joined_button);
                        addForumsView.findViewById(R.id.join_button).setEnabled(false);
                    } else {
                        addForumsView.findViewById(R.id.join_button).setOnClickListener(v -> {
                            joinNewForum((String) addForumsView.getTag(), addForumsView);
                        });
                    }


                    addForumsView.setOnClickListener(v -> {
                        boolean hasJoined = ((Button) addForumsView.findViewById(R.id.join_button)).getText().equals("Joined");
                        Log.d("ForumActivity", "hasJoined: " + hasJoined);
                        Intent intent = new Intent(ForumActivity.this, ForumViewActivity.class);
                        intent.putExtra("forumId", (String) v.getTag());
                        intent.putExtra("isJoined", hasJoined);
                        intent.putExtra("forumName", forum.get("name").getAsString());
                        startActivity(intent);
                    });

                    allForumsViews.add(addForumsView);
                }

                Log.d("ForumActivity", String.valueOf(resetView));
                Log.d("ForumActivity", String.valueOf(allForumsViews));
                if (resetView) {
                    resetAndAddView(findViewById(R.id.forum_layout_all), allForumsViews);
                }
            }

            @Override
            public void onApiRequestError(String error) {
                Log.d(ServerRequest.RequestTag, "Failure");
                Log.d(ServerRequest.RequestTag, error);
            }
        };

        try {
            serverRequest.makeGetRequest("/forums", apiRequestListener);
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(e);
        }
    }

    /* ChatGPT usage: No */
    private void resetAndAddView(LinearLayout layout, ArrayList<View> views) {
        layout.removeAllViews();
        for (View view : views)
            layout.addView(view);
    }

    /* ChatGPT usage: No */
    private void joinNewForum(String forumId, View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("GoogleAccountInfo", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);
        ServerRequest serverRequest = new ServerRequest(userId);

        ServerRequest.ApiRequestListener apiRequestListener = new ServerRequest.ApiRequestListener() {
            @Override
            public void onApiRequestComplete(JsonElement response) {
                ((Button) view.findViewById(R.id.join_button)).setText(R.string.joined_button);
                view.findViewById(R.id.join_button).setEnabled(false);
                View joinedForumsView = getLayoutInflater().inflate(R.layout.forum_card, null);
                joinedForumsView.setTag(forumId);
                ((TextView) joinedForumsView.findViewById(R.id.forum_name)).setText(((TextView) view.findViewById(R.id.forum_name)).getText().toString());
                ((TextView) joinedForumsView.findViewById(R.id.course_name)).setText(((TextView) view.findViewById(R.id.course_name)).getText().toString());
                ((Button) joinedForumsView.findViewById(R.id.join_button)).setText(R.string.joined_button);
                joinedForumsView.findViewById(R.id.join_button).setEnabled(false);

                joinedForumsView.setOnClickListener(v -> {
                    Intent intent = new Intent(ForumActivity.this, ForumViewActivity.class);
                    intent.putExtra("forumId", (String) joinedForumsView.getTag());
                    intent.putExtra("isJoined", true);
                    intent.putExtra("forumName", ((TextView) joinedForumsView.findViewById(R.id.forum_name)).getText());
                    startActivity(intent);
                });

                joinedForumsViews.add(joinedForumsView);
            }

            @Override
            public void onApiRequestError(String error) {
                Log.d(ServerRequest.RequestTag, "Failure");
                Log.d(ServerRequest.RequestTag, error);
            }
        };

        try {
            JsonObject body = new JsonObject();
            body.addProperty("forumId", forumId);
            serverRequest.makePostRequest("/forums/user", body, apiRequestListener);
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(e);
        }
    }
}