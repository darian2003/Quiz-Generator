package com.example.project;

import java.util.ArrayList;

public class User {

    protected static int population = 0;
    private final int id;
    private final String username;
    private final String password;

    protected ArrayList<Quizz> answeredQuizz = new ArrayList<>();
    protected ArrayList<Integer> quizzResults =  new ArrayList<>(); // corresponding results to the Quiz ArrayList above

    protected int getId() {
        return id;
    }

    protected String getUsername() {
        return username;
    }

    protected String getPassword() {
        return password;
    }

    // constructor for "-create-user" method
    protected User(String username, String password) {
        population++;
        this.id = population;
        this.username = username;
        this.password = password;
    }

    // constructor for id user -> used to add a user to dataBase from file
    protected User(String username, String password, int id) {
        this.id = id;
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

    protected static boolean usernameAlreadyExists(DataBase dataBase, String username) {
        for (User ref : dataBase.users) {
            if (ref.username.equals(username))
                return false;
        }
        return true;
    }

    // checks whether username and password provided are in the database
    protected boolean loginAttempt(DataBase dataBase) {
        for (int i = 0; i < dataBase.users.size(); i++) {
            User ref = dataBase.users.get(i);
            if (ref.username.equals(this.username) && ref.password.equals(this.password))
                return true;
        }
        return false;
    }

    // returns a reference to the user that is currently logged-in
    protected static User currentUser(String[] args, DataBase dataBase) {
        // take username
        String str = args[1];
        String[] arrOfStrings = str.split("'", 0);
        String username = arrOfStrings[1];

        //take password
        str = args[2];
        arrOfStrings = str.split("'", 0);
        String userPassword = arrOfStrings[1];

        int userID = getUserID(username, userPassword, dataBase);
        return getUserByID(userID, dataBase);
    }

    // checks whether the log-in conditions are satisfied
    protected static boolean loginFailed(String[] args, DataBase dataBase) {
        // first check if -u and -p are given
        if (args.length < 3) {
            System.out.println("{'status':'error','message':'You need to be authenticated'}");
            return false;
        }
        // take username
        String str = args[1];
        String[] arrOfStrings = str.split("'", 0);
        String username = arrOfStrings[1];

        //take password
        str = args[2];
        arrOfStrings = str.split("'", 0);
        String userpassword = arrOfStrings[1];

        User checkIfUserExists = new User(username, userpassword, 0);
        boolean check = checkIfUserExists.loginAttempt(dataBase);
        if(!check) {
            System.out.println("{ 'status' : 'error', 'message' : 'Login failed'}");
            return false;
        }
        return true;
    }

    // checks whether the format of the answers provided from the command is acceptable
    protected static boolean answersProvidedError(String[] args) {
        if(args.length < 6) {
            System.out.println("{ 'status' : 'error', 'message' : 'No answer provided'}");
            return false;
        }
        if(args.length < 8) {
            System.out.println("{ 'status' : 'error', 'message' : 'Only one answer provided'}");
            return false;
        }
        if(args.length > 15) {
            System.out.println("{ 'status' : 'error', 'message' : 'More than 5 answers were submitted'}");
            return false;
        }
        return true;
    }

    // get the ID of a user when username and password are known
    protected static int getUserID(String username,String password, DataBase dataBase) {
        for (User ref : dataBase.users) {
            if (ref.username.equals(username) && ref.password.equals(password))
                return ref.getId();
        }
        return -1;
    }

    // get a reference to a user when knowing its ID
    protected static User getUserByID(int id, DataBase dataBase) {
        for (User user : dataBase.users) {
            if (user.getId() == id)
                return user;
        }
        return null;
    }

    // print all the solutions of the answered quizzes of the current user
    protected void getSolutions() {
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
