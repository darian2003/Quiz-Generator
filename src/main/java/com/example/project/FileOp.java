package com.example.project;

import java.io.*;

public class FileOp {
    private static String pathToData = "src/main/data/";
    private static File userFile = new File(pathToData + "users.csv");
    private static File questionFile = new File(pathToData + "questions.csv");

    private static File quizzFile = new File(pathToData + "quizz.csv");

    private static File solutionFile = new File(pathToData + "solution.csv");
    public static void readUserFromFile(DataBase dataBase) {

        try {
            userFile.createNewFile();
        } catch (Exception e) {
            System.err.println("File cannot be created");
        }
        try (BufferedReader br = new BufferedReader(new FileReader(userFile))) {
            for (String line; (line = br.readLine()) != null; ) {

                String[] str = line.split(",", 0);
                int user_id = Integer.parseInt(str[0]);
                String user_name = str[1];
                String user_password = str[2];
                User user = new User(user_name, user_password, user_id);
                int arraySize = Integer.parseInt(str[3]);
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
                int question_id = Integer.parseInt(str[0]);
                String question_text = str[1];
                String question_type = str[2];
                Question question = new Question(question_text, question_type, question_id);
                if (str.length > 4) {
                    int answer_no = Integer.parseInt(str[3]);
                    for (int i = 0; i < answer_no * 4; i += 4) {
                        String answer_text = str[i + 4];
                        int answer_id = Integer.parseInt(str[i+5]);
                        double answer_points = Double.parseDouble(str[i+6]);
                        int answer_type = Integer.parseInt(str[i+7]);
                        Answer answer = new Answer(answer_id, answer_text);
                        answer.points = answer_points;
                        if (answer_type == 1)
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
                int quizz_id = Integer.parseInt(str[0]);
                String quizz_name = str[1];
                int quizz_numberOfQuestions = Integer.parseInt(str[2]);
                int user_owner_id = Integer.parseInt(str[3]);
                int user_already_answered_size = Integer.parseInt(str[4]);
                Quizz quizz = new Quizz(quizz_id, quizz_name, quizz_numberOfQuestions, user_owner_id);
                for (int i = 0; i < quizz_numberOfQuestions; i++) {
                    int question_id = Integer.parseInt(str[5+i]);
                    quizz.addQuestionToQuizzFromFile(dataBase, question_id);
                }
                for (int i = 0; i < user_already_answered_size; i++) {
                    int userAnsweredID = Integer.parseInt(str[5+quizz_numberOfQuestions+i]);
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

    public static void readSolutionFromFile(DataBase dataBase) {

        try {
            solutionFile.createNewFile();
        } catch (Exception e) {
            System.err.println("File cannot be created");
        }
        try (BufferedReader br = new BufferedReader(new FileReader(solutionFile))) {
            for (String line; (line = br.readLine()) != null; ) {

                String[] str = line.split(",", 0);
                int user_id = Integer.parseInt(str[0]);
                int quizz_id = Integer.parseInt(str[1]);
                int score = Integer.parseInt(str[2]);
                User user = User.getUserByID(user_id, dataBase);
                Quizz quizz = Quizz.returnQuizzByID(quizz_id, dataBase);
                user.answeredQuizz.add(quizz);
                quizz.userHasAnswered.add(user);
                user.quizzResults.add(score);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
                 }
                 out.println("");
             }

         } catch (IOException e) {
            System.err.println("IO error");
        }
    }

    public static void writeQuestionsToFile(DataBase dataBase) {
        try (FileWriter fw = new FileWriter(questionFile, false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            for (int i = 0; i < dataBase.questions.size(); i++) {
                Question ref =  dataBase.questions.get(i);
                // TODO
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