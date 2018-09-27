package com.github.upatovav.mlmGroupValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

public class Main {

    static BigDecimal GO_LIMIT = new BigDecimal(1000);

    public static void main(String[] args) {
        if (args.length < 1){
            System.out.println(
                    "usage: java -jar mlm-group-value-0.0.1-SNAPSHOT.jar <input file name> [ouput file name]\n" +
                    "examples: java -jar mlm-group-value-0.0.1-SNAPSHOT.jar ../test_task_data.txt\n" +
                    "   java -jar mlm-group-value-0.0.1-SNAPSHOT.jar ../another_task_data.txt ../another_out.txt");
            return;
        }
        String inputFileName = args[0];
        File inputFile = new File(inputFileName);
        try {
            HashSet<User> roots = new HashSet<>();
            HashMap<Long, User> userMap = new HashMap<>();

            parseInput(inputFile, roots, userMap);

            roots.forEach(Main::computeGo);

            String outFileName = args.length >1 ? args[1] : "out.txt";
            outToFileAndConsole(outFileName, userMap);

        } catch (FileNotFoundException e) {
            System.out.println("Input file not found");
        } catch (ParentAbsentException | IOException e){
            System.out.println(e.getMessage());
        }
    }



    private static void parseInput(File inputFile, HashSet<User> roots, HashMap<Long, User> userMap) throws FileNotFoundException {
        System.out.println("Input file contents:");

        Scanner scanner = new Scanner(inputFile);
        scanner.useDelimiter(System.getProperty("line.separator"));
        while (scanner.hasNext()){
            String line = scanner.next();
            System.out.println(line);
            String[] split = line.split(",\\s?");
            long userId = Long.valueOf(split[0]);
            Long parentId = "null".equals(split[1])? null : Long.valueOf(split[1]);
            BigDecimal value = "null".equals(split[2])? null : new BigDecimal(split[2]);
            User user = new User(
                    userId,
                    value);
            if (parentId == null){
                roots.add(user);
            } else {
                User parent = userMap.computeIfAbsent(parentId,
                        (k) -> {
                            throw new ParentAbsentException("Parent with id " + parentId + " not found for user " + userId);
                        });
                parent.getChildren().add(user);
            }
            userMap.put(user.getId(), user);
        }

        System.out.println();
    }

    private static BigDecimal computeGo(User user){
        if (user.getChildren().isEmpty()){
            user.setGo(user.getValue());
            return user.getGo();
        }
        BigDecimal go =  user.getChildren().stream()
                .map(Main::computeGo) //recursive call
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        user.setGo(user.getValue().add(go));
        if (user.getGo().compareTo(GO_LIMIT) > 0) {
            return BigDecimal.ZERO;
        } else {
            return user.getGo();
        }
    }

    private static void outToFileAndConsole(String outFileName, HashMap<Long, User> userMap) throws IOException {
        System.out.println("Output file contents:");

        File out = new File(outFileName);
        boolean deleted = out.delete();
        if (!out.exists()){
            FileOutputStream outStream = new FileOutputStream(out);
            for (User user : userMap.values()){
                String userString = String.join(", ", Long.toString(user.getId()), user.getGo().toString()) + "\n";
                outStream.write(userString.getBytes());
                System.out.print(userString);
            }
        }
    }

    static class ParentAbsentException extends RuntimeException{
        public ParentAbsentException(String s){
            super(s);
        }
    }
}
