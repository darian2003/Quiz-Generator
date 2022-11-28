package com.example.project;

import java.util.ArrayList;

public class User {

    public static int population = 0;
    private int id;
    private String username;
    private String password;

    public ArrayList<Quizz> answeredQuizz = new ArrayList<>();
    public ArrayList<Integer> quizzResults =  new ArrayList<>(); // corresponding results to the Quiz ArrayList above

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean loginAttempt(DataBase dataBase) {
        for (int i = 0; i < dataBase.users.size(); i++) {
            User ref = dataBase.users.get(i);
            if (ref.username.equals(this.username) && ref.password.equals(this.password))
                return true;
        }
        return false;
    }

    public User() {
    }

    // constructor for no id user -> used for reference
    public User(String username, String password) {
        this.id = 0;
        this.username = username;
        this.password = password;
    }

    // constructor for id user -> used to add a user to dataBase from file
    public User(String username, String password, int id) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    // used to add a user to dataBase from createUser method
    public User(String username, String password, boolean doesntMatter) {
        population++;
        this.id = population;
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    // get the ID of an user when username and password are known
    public static int getUserID(String username,String password, DataBase dataBase) {
        for (User ref : dataBase.users) {
            if (ref.username.equals(username) && ref.password.equals(password))
                return ref.getId();
        }
        return -1;
    }

    // get a reference to a user when knowing its ID
    public static User getUserByID(int id, DataBase dataBase) {
        for (User user : dataBase.users) {
            if (user.getId() == id)
                return user;
        }
        return null;
    }

    public void getSolutions() {
        System.out.print("{ 'status' : 'ok', 'message' : '[");
        for(int i = 0; i < this.answeredQuizz.size(); i++) {
            Quizz quizz = this.answeredQuizz.get(i);
            int score = this.quizzResults.get(i);
            System.out.print("{\"quiz-id\" : \"" + quizz.getId() + "\", \"quiz-name\" : \"" + quizz.getName() + "\", \"score\" : \"" + score + "\", \"index_in_list\" : \"" + (i+1) +"\"}");
            if(i+1 < this.answeredQuizz.size()) {
                System.out.print(", ");
            }
            System.out.println("]'}");
        }
    }
}
