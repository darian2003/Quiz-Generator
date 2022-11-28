package com.example.project;

import java.util.ArrayList;

public class Question {

    public static int population = 0;

    private int id;
    private String text;
    private String type;

    public ArrayList<Answer> answers = new ArrayList<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() { return this.text; }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // constructor with no id -> used for reference
    public Question(String text, String type) {
        this.id = 0;
        this.text = text;
        this.type = type;
    }

    // used to read  a question from file
    public Question(String text, String type, int id) {
        this.text = text;
        this.type = type;
        this.id = id;
    }

    // used to create a question from createQuestion method
    public Question(String text, String type, boolean doesntMatter) {
        population++;
        this.id = population;
        this.text = text;
        this.type = type;
    }

    public Question(){}

    public boolean checkIfQuestionExists(DataBase dataBase) {
        for (int i = 0; i < dataBase.questions.size(); i++) {
            Question ref = dataBase.questions.get(i);
            if (ref.text.equals(this.text))
                return true;
        }
        return false;
    }

    public int returnQuestionId(DataBase dataBase) {
        for (int i = 0; i < dataBase.questions.size(); i++) {
            Question ref = dataBase.questions.get(i);
            if (ref.text.equals(this.text))
                return ref.id;
        }
        return -1;
    }

    public boolean singleTypeQuestionHasMultipleCorrectAnswers() {
        int count = 0; // if count != 0 => false
        for (int i = 0; i < this.answers.size(); i++) {
            Answer ref = this.answers.get(i);
            if (ref.type.equals(Answer.AnswerType.CORRECT))
                count++;
        }
        if (count != 1)
            return false;
        return true;
    }

    public boolean duplicateAnswers() {
        for(int i = 0; i < this.answers.size() - 1 ; i++) {
            Answer ref1 = this.answers.get(i);
            for(int j = i+1; j < this.answers.size(); j++) {
                Answer ref2 = this.answers.get(j);
                if (ref1.getText().equals(ref2.getText()))
                    return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", type='" + type + '\'' +
                ", answers=" + answers +
                '}';
    }

    public String getQustion() {
        return "{\"question_id\" : \"" + this.id + "\", \"question_name\" : \"" + this.text + "\"}";
    }

    public void setPointsPerAnswer() {
        if (this.type.equals("single")) {
            // => any wrong answer has -1 points and the right on has +1 points
            for (Answer answer : this.answers) {
                if (answer.type.equals(Answer.AnswerType.CORRECT)) {
                    answer.points = 1;
                } else {
                    answer.points = -1;
                }
            }
        } else {
            // first we count the number of right and wrong answers to calculate the weight of each answer
            int right = 0, wrong = 0;
            for (Answer answer : this.answers) {
                if (answer.type.equals(Answer.AnswerType.CORRECT)) {
                    right++;
                } else {
                    wrong++;
                }
            }
            // now start assigning the right number of points per answer
            for (Answer answer : this.answers) {
                if (answer.type.equals(Answer.AnswerType.CORRECT)) {
                    answer.points = (double)(1) / right;
                } else {
                    answer.points = (double)(-1) / wrong;
                }
            }
        }
    }

    // adds all its answers to the answers ArrayList in the database
    public void addAnswersToDatabase(DataBase dataBase) {
        for (Answer answer : this.answers) {
            dataBase.answers.add(answer);
        }
    }
}
