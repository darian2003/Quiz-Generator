package com.example.project;

import java.util.ArrayList;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import static java.lang.Math.round;

public class Quizz {

    private int id;
    private int ownerID;
    private String name;
    public int numberOfQuestions;
    public ArrayList<Question> questions = new ArrayList<>();
    public ArrayList<User> userHasAnswered = new ArrayList<>();

    public Quizz(String name) {
        this.name = name;
    }

    public Quizz(int id, String name, int numberOfQuestions, int ownerID) {
        this.id = id;
        this.name = name;
        this.numberOfQuestions = numberOfQuestions;
        this.ownerID = ownerID;
    }

    public Quizz(){}

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public int getOwnerID() {
        return ownerID;
    }

    // search for the lowest free natural number
    public void setId(DataBase dataBase) {
        for (this.id = 1; ; this.id++) {
            int alreadyExists = 0;
            for (Quizz quizz : dataBase.quizz) {
                if (quizz.id == this.id) {
                    alreadyExists = 1;
                    break;
                }
            }
            if (alreadyExists == 0)
                return;
        }

    }

    public void setOwnerID(DataBase dataBase, User user) {
        for (User ref : dataBase.users) {
            if (ref.getUsername().equals(user.getUsername()) && ref.getPassword().equals(user.getPassword())) {
                this.ownerID = ref.getId();
                return;
            }
        }
    }

    // check whether quizz name already exists in the database
    public static boolean checkQuizName(String quiz_name, DataBase dataBase) {
        for (Quizz quizz : dataBase.quizz) {
            if (quizz.name.equals(quiz_name))
                return false;
        }
        return true;
    }

    // used by readQuizzFromFile function
    public void addQuestionToQuizzFromFile(DataBase dataBase, int question_id) {
        for (Question question : dataBase.questions) {
            if (question.getId() == question_id)
                this.questions.add(question);
        }
    }

    // gets the id of a quizz and searchs for it in the quizz ArrayList
    public static Quizz returnQuizzByID(int quizzID, DataBase dataBase) {
        for (Quizz quizz : dataBase.quizz) {
            if (quizz.id == quizzID)
                return quizz;
        }
        return null;
    }

    // search for a specific quizz when given its name
    public void returnQuizzID(DataBase dataBase) {
        for (Quizz quizz : dataBase.quizz) {
            if (this.name.equals(quizz.name)) {
                System.out.println("{ 'status' : 'ok', 'message' : '" + quizz.id +"'}");
                return;
            }
        }
        System.out.println("{ 'status' : 'error', 'message' : 'Quizz does not exist'}");
    }

    // prints all quizzes in the database
    // auxiliary function for getAllQuizzes
    public static void returnAllQuizzes(DataBase dataBase, User user) {
        System.out.print("{ 'status' : 'ok', 'message' : '[");
        for (int i = 0 ; i  < dataBase.quizz.size() ; i++) {
            Quizz quizz = dataBase.quizz.get(i);
            String isCompleted = "False";
            if (quizz.checkIfUserCompletedThisQuizz(user))
                isCompleted = "True";
            System.out.print("{\"quizz_id\" : \"" + quizz.id + "\", \"quizz_name\" : \"" + quizz.name + "\", \"is_completed\" : \"" + isCompleted + "\"}");
            if (i+1 < dataBase.quizz.size()) {
                System.out.print(", ");
            }
        }
        System.out.println("]'}");
    }

    public boolean checkIfUserCompletedThisQuizz(User user) {
        for (User ref : this.userHasAnswered) {
            if (ref.getId() == user.getId())
                return true;
        }
        return false;
    }

    public void userHasAnsweredThisQuizz(int userID, DataBase dataBase) {
        for (User user : dataBase.users) {
            if (user.getId() == userID) {
                this.userHasAnswered.add(user);
                return;
            }
        }
    }

    public void returnQuizzDetails() {
        System.out.print("{'status' : 'ok', 'message' : '[");
        for (int i = 0; i < this.questions.size(); i++) {
            Question question = this.questions.get(i);
            System.out.print("{\"question-name\":\"" + question.getText() + "\", \"question_index\":\""+(i+1)+"\", \"question_type\":\""+question.getType()+ "\", \"answers\":\"[");
            for (int j = 0; j < question.answers.size(); j++) {
                Answer answer = question.answers.get(j);
                System.out.print("{\"answer_name\":\"" + answer.getText() + "\", \"answer_id\":\"" + answer.getId() + "\"}");
                if (j+1 < question.answers.size())
                    System.out.print(", ");
            }
            System.out.print("]\"}");
            if (i+1 < this.questions.size())
                System.out.print(", ");
        }
        System.out.println("]'}");
    }

    public double getQuizzResults(String[] args, DataBase dataBase) {
        double sum = 0;
        for (int i = 4; i < args.length; i++) {
            String[] arrOfStr = args[i].split("'", 0);
            int answerID = Integer.parseInt(arrOfStr[1]);
            // search for the answer reference using its ID by going through the answers ArrayList from the database
            int found = 0; // states whether the answer ID exists or not
            for (Answer answer : dataBase.answers) {
                if (answer.getId() == answerID) {
                    sum += answer.points;
                    found = 1;
                    break;
                }
            }
            if (found == 0) {
                System.out.println("{ 'status' : 'error', 'message' : 'Answer ID for answer " + (i-3) + " does not exist'}");
                return -1;
            }
        }
        if (sum < 0)
            return 0;

        final DecimalFormat df = new DecimalFormat("0.00");
        df.format(sum); // round the score to two decimals
        return sum / this.numberOfQuestions;
    }

    public static void deleteQuizz(int quizzID, DataBase dataBase) {
        for (Quizz quizz : dataBase.quizz) {
            if (quizz.id == quizzID) {
                dataBase.quizz.remove(quizz);
                System.out.println("{ 'status' : 'ok', 'message' : 'Quizz deleted successfully'}");
                return;
            }
        }
        System.out.println("{ 'status' : 'error', 'message' : 'No quiz was found'}");
    }

}
