package com.example.trivia.data;

import android.app.DownloadManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.trivia.MainActivity;
import com.example.trivia.controller.NewController;
import com.example.trivia.model.Question;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.example.trivia.controller.NewController.TAG;

public class QuestionBank {
    private String url = "https://raw.githubusercontent.com/curiousily/simple-quiz/master/script/statements-data.json";
    ArrayList<Question> questionArrayList = new ArrayList<Question>();


    public List<Question> getQuestions(final AnswerListAsyncResponse callBack) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                url,
                (JSONObject) null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i = 0; i<response.length();i++){
                            try {
                                Question question = new Question();
                                question.setQuestion(response.getJSONArray(i).get(0).toString());
                                question.setAnswer(response.getJSONArray(i).getBoolean(1));

                                questionArrayList.add(question);

                                //Log.d("json", "onResponse: " +question);
                                //Log.d("json2", "onResponse: " +response.getJSONArray(i).get(1));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        if(callBack != null)callBack.processFinished(questionArrayList);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        NewController.getInstance().addToRequestQueue(jsonArrayRequest);

        return questionArrayList;
    }

}
