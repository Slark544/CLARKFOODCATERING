# Food Catering System

Layered Java console app connected to an SQLite database.

## Structure
- `com.scc.Main` – startup only: initialize DB -> create UserDao and Scanner -> `Authentication.start()`
- `com.scc.dao` – DatabaseConnection (enables `PRAGMA foreign_keys = ON`), DatabaseInitializer, UserDao
- `com.scc.model` – User (record), Role (enum)
- `com.scc.ui` – Authentication, RoleMenu, AdminMenu, CustomerMenu, StaffMenu

## Run
Open the project in IntelliJ IDEA (Maven) and run `com.scc.Main`.
`foodcatering.db` is created in the project folder on first run.

## Admin features
ADMIN accounts can: view all users, view pending registrations, approve a pending account, and update another user's role and status (enter 0 to cancel at any step). An admin cannot modify their own active account.
