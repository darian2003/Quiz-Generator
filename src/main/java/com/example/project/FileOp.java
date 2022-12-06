package com.example.project;

import java.io.*;

public class FileOp {

    // path to files
    private static final String pathToData = "src/main/data/";
    private static final File userFile = new File(pathToData + "users.csv"); // users file
    private static final File questionFile = new File(pathToData + "questions.csv"); // questions file

    private static final File quizzFile = new File(pathToData + "quizz.csv"); // quizz file

    private static final File solutionFile = new File(pathToData + "solution.csv"); // user's solutions file
    public static void readUserFromFile(DataBase dataBase) {

        try {
            userFile.createNewFile();
        } catch (Exception e) {
            System.err.println("File cannot be created");
        }
        try (BufferedReader br = new BufferedReader(new FileReader(userFile))) {
            for (String line; (line = br.readLine()) != null; ) {

                String[] str = line.split(",", 0);
                int userID = Integer.parseInt(str[0]);
                String username = str[1];
                String userPassword = str[2];
                User user = new User(username, userPassword, userID);
                int arraySize = Integer.parseInt(str[3]); // indicates how many quizzes this user has answered
                for (int i = 4; i < arraySize; i+=2) {
                    int quizzID = Integer.parseInt(str[i]);
                    Quizz quizz = Quizz.returnQuizzByID(quizzID, dataBase);
                    user.answeredQuizz.add(quizz);
                    int score = Integer.parseInt(str[i+1]);
                    user.quizzResults.add(score);
                }
                dataBase.users.add(user);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void readQuestionFromFile(DataBase dataBase) {

        try {
            questionFile.createNewFile();
        } catch (Exception e) {
            System.err.println("File cannot be created");
        }
        try (BufferedReader br = new BufferedReader(new FileReader(questionFile))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] str = line.split(",", 0);
                int questionID = Integer.parseInt(str[0]);
                String questionText = str[1];
                String questionType = str[2];
                Question question = new Question(questionText, questionType, questionID);
                if (str.length > 4) {
                    int answerNo = Integer.parseInt(str[3]); // indicates how many answers we are going to read
                    for (int i = 0; i < answerNo * 4; i += 4) {
                        String answerText = str[i + 4];
                        int answerID = Integer.parseInt(str[i+5]);
                        double answerPoints = Double.parseDouble(str[i+6]);
                        int answerType = Integer.parseInt(str[i+7]);
                        Answer answer = new Answer(answerID, answerText);
                        answer.points = answerPoints;
                        if (answerType == 1)
                            answer.setType(Answer.AnswerType.CORRECT);
                        else answer.setType(Answer.AnswerType.INCORRECT);
                        question.answers.add(answer);
                        dataBase.answers.add(answer);
                    }
                }
                dataBase.questions.add(question);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void readQuizzFromFile(DataBase dataBase) {

        try {
            quizzFile.createNewFile();
        } catch (Exception e) {
            System.err.println("File cannot be created");
        }
        try (BufferedReader br = new BufferedReader(new FileReader(quizzFile))) {
            for (String line; (line = br.readLine()) != null; ) {

                String[] str = line.split(",", 0);
                int quizzId = Integer.parseInt(str[0]);
                String quizzName = str[1];
                int quizzNumberOfQuestions = Integer.parseInt(str[2]);
                int userOwnerId = Integer.parseInt(str[3]);
                int userAlreadyAnsweredSize = Integer.parseInt(str[4]);
                Quizz quizz = new Quizz(quizzId, quizzName, quizzNumberOfQuestions, userOwnerId);
                for (int i = 0; i < quizzNumberOfQuestions; i++) {
                    int question_id = Integer.parseInt(str[5+i]);
                    quizz.addQuestionToQuizzFromFile(dataBase, question_id);
                }
                for (int i = 0; i < userAlreadyAnsweredSize; i++) {
                    int userAnsweredID = Integer.parseInt(str[5+quizzNumberOfQuestions+i]);
                    quizz.userHasAnsweredThisQuizz(userAnsweredID, dataBase);
                }
                dataBase.quizz.add(quizz);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // remembers what quizzes have been answered by each user and the score
    public static void readSolutionFromFile(DataBase dataBase) {

        try {
            solutionFile.createNewFile();
        } catch (Exception e) {
            System.err.println("File cannot be created");
        }
        try (BufferedReader br = new BufferedReader(new FileReader(solutionFile))) {
            for (String line; (line = br.readLine()) != null; ) {

                String[] str = line.split(",", 0);
                int userId = Integer.parseInt(str[0]);
                int quizzId = Integer.parseInt(str[1]);
                int score = Integer.parseInt(str[2]);
                User user = User.getUserByID(userId, dataBase);
                Quizz quizz = Quizz.returnQuizzByID(quizzId, dataBase);
                user.answeredQuizz.add(quizz); // add this quizz to the user's answered quizzes array
                quizz.userHasAnswered.add(user); // add this user to the quizz's array of users that have answered it
                user.quizzResults.add(score);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // for each user write its id, username, password and number of answered quizzes
    // then for each answered quizz, write its id and score
    public static void writeUsersToFile(DataBase dataBase) {
        try (FileWriter fw = new FileWriter(userFile, false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
             for (int i = 0; i < dataBase.users.size(); i++) {
                 User ref =  dataBase.users.get(i);
                 out.print(ref.getId() + "," + ref.getUsername() + "," + ref.getPassword() + "," + ref.answeredQuizz.size() + ",");
                 int j = 0;
                 for (Quizz quizz : ref.answeredQuizz) {
                     out.print(quizz.getId() + "," + ref.answeredQuizz.get(j) + ",");
                     j++;
                 }
                 out.println("");
             }

         } catch (IOException e) {
            System.err.println("IO error");
        }
    }

    // for each question, write to file its id, text, type and number of answers
    // then, for each answer write its text, id, points and type
    public static void writeQuestionsToFile(DataBase dataBase) {
        try (FileWriter fw = new FileWriter(questionFile, false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            for (int i = 0; i < dataBase.questions.size(); i++) {
                Question ref =  dataBase.questions.get(i);
                out.print(ref.getId() + ","+ ref.getText() + "," + ref.getType() + "," + ref.answers.size() + ",");
                for (Answer answer : ref.answers) {
                    out.print(answer.getText() + "," + answer.getId() + "," + answer.points + ",");
                    if (answer.getType().equals(Answer.AnswerType.CORRECT))
                        out.print("1,");
                    else out.print("0,");
                }
                out.println("");
            }

        } catch (IOException e) {
            System.err.println("IO error");
        }
    }

    // for each quizz, write to file its id, name, number of questions, ownerID and number of users that have answered it
    // then, for each question write its id
    // then write the id of all of the users that have answered it
    public static void writeQuizzToFile(DataBase dataBase) {
        try (FileWriter fw = new FileWriter(quizzFile, false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            for (int i = 0; i < dataBase.quizz.size(); i++) {
                Quizz ref =  dataBase.quizz.get(i);
                out.print(ref.getId() + "," + ref.getName() + "," + ref.getNumberOfQuestions() + "," + ref.getOwnerID() + "," + ref.userHasAnswered.size() + ",");
                for (Question question : ref.questions) {
                    out.print(question.getId() + ",");
                }
                for (User user : ref.userHasAnswered) {
                    out.print(user.getId() + ",");
                }
                out.println("");
            }

        } catch (IOException e) {
            System.err.println("IO error");
        }
    }

    // for each time that a quizz has been submitted, write its id, the user's that submitted it id and the score
    public static void writeSolutionToFile(DataBase dataBase) {
        try (FileWriter fw = new FileWriter(solutionFile, false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            for (int i = 0; i < dataBase.users.size(); i++) {
                User user = dataBase.users.get(i);
                for (int j = 0; j < user.answeredQuizz.size(); j++) {
                    Quizz quizz = user.answeredQuizz.get(j);
                    int score = user.quizzResults.get(j);
                    out.println(user.getId() + "," + quizz.getId() + "," + score);
                }
            }

        } catch (IOException e) {
            System.err.println("IO error");
        }
    }
}