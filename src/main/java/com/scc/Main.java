
package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.dao.CateringDao;
import com.scc.ui.Authentication;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        try {
            // Initialize existing SQLite database.
            DatabaseInitializer.initialize();

            // Create shared DAO objects.
            UserDao userDao = new UserDao();
            CateringDao cateringDao = new CateringDao();

            // Start application.
            try (Scanner scanner = new Scanner(System.in)) {

                Authentication authentication =
                        new Authentication(
                                scanner,
                                userDao,
                                cateringDao
                        );

                authentication.start();
            }

        } catch (Exception e) {
            System.out.println("Application error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
