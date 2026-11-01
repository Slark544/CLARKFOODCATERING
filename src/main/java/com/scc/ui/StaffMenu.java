
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.dao.CateringDao;
import com.scc.model.User;

import java.util.Scanner;

public class StaffMenu extends RoleMenu {

    public StaffMenu(
            Scanner scanner,
            UserDao userDao,
            CateringDao cateringDao,
            User user) {

        super(scanner, userDao, cateringDao, user);
    }

    @Override
    protected String title() {
        return "STAFF DASHBOARD";
    }
}
