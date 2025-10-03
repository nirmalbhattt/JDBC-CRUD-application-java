import java.sql.*;
import java.util.Scanner;

public class EmployeeApp {
    private static final String URL = "jdbc:mysql://localhost:3306/employee_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "oel#123";

    public static void main(String[] args) {
        // Optional: load driver (modern JVMs load automatically)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC driver not found. Make sure the connector JAR is on the classpath.");
        }

        try (Scanner scanner = new Scanner(System.in)) {
            // Ensure DB/table exist (in case user didn't run SQL). This creates DB/table if missing.
            ensureDatabaseAndTable();

            while (true) {
                printMenu();
                System.out.print("Enter choice: ");
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        addEmployee(scanner);
                        break;
                    case "2":
                        viewEmployees();
                        break;
                    case "3":
                        updateEmployee(scanner);
                        break;
                    case "4":
                        deleteEmployee(scanner);
                        break;
                    case "5":
                        System.out.println("Exiting. Goodbye.");
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n=== Employee Management ===");
        System.out.println("1. Add Employee");
        System.out.println("2. View All Employees");
        System.out.println("3. Update Employee");
        System.out.println("4. Delete Employee");
        System.out.println("5. Exit");
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private static void ensureDatabaseAndTable() {
        // This tries to create database and table if they do not exist.
        String createDb = "CREATE DATABASE IF NOT EXISTS employee_db DEFAULT CHARACTER SET utf8mb4";
        String createTable = "CREATE TABLE IF NOT EXISTS employees ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "name VARCHAR(100) NOT NULL,"
                + "salary DOUBLE NOT NULL,"
                + "department VARCHAR(100) NOT NULL"
                + ")";

        // First connect to default mysql server (without a database) to create DB if needed
        String urlNoDb = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try (Connection conn = DriverManager.getConnection(urlNoDb, USER, PASS);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createDb);
        } catch (SQLException e) {
            System.out.println("Warning: Could not create database automatically. If database does not exist, run employee_db.sql yourself.");
        }

        // Then create table in employee_db
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTable);
        } catch (SQLException e) {
            System.out.println("Warning: Could not create table automatically. If table does not exist, run employee_db.sql yourself.");
        }
    }

    private static void addEmployee(Scanner scanner) {
        System.out.print("Enter name: ");
        String name = scanner.nextLine().trim();

        double salary;
        while (true) {
            System.out.print("Enter salary (numeric): ");
            String s = scanner.nextLine().trim();
            try {
                salary = Double.parseDouble(s);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid salary. Try again.");
            }
        }

        System.out.print("Enter department: ");
        String dept = scanner.nextLine().trim();

        String sql = "INSERT INTO employees (name, salary, department) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setDouble(2, salary);
            ps.setString(3, dept);
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("Employee added successfully.");
            else System.out.println("Insert failed.");
        } catch (SQLException e) {
            System.out.println("Error while adding employee: " + e.getMessage());
        }
    }

    private static void viewEmployees() {
        String sql = "SELECT id, name, salary, department FROM employees ORDER BY id";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\nID | Name | Salary | Department");
            System.out.println("-----------------------------------------------");
            boolean any = false;
            while (rs.next()) {
                any = true;
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double salary = rs.getDouble("salary");
                String dept = rs.getString("department");
                System.out.printf("%d | %s | %.2f | %s%n", id, name, salary, dept);
            }
            if (!any) {
                System.out.println("No employees found.");
            }
        } catch (SQLException e) {
            System.out.println("Error while fetching employees: " + e.getMessage());
        }
    }

    private static void updateEmployee(Scanner scanner) {
        int id;
        while (true) {
            System.out.print("Enter employee id to update: ");
            String s = scanner.nextLine().trim();
            try {
                id = Integer.parseInt(s);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid id. Try again.");
            }
        }

        // Check if employee exists
        String checkSql = "SELECT id, name, salary, department FROM employees WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, id);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Employee with id " + id + " not found.");
                    return;
                } else {
                    System.out.println("Current details:");
                    System.out.printf("Name: %s, Salary: %.2f, Department: %s%n",
                            rs.getString("name"), rs.getDouble("salary"), rs.getString("department"));
                }
            }

            // Get new values
            System.out.print("Enter new name (leave blank to keep unchanged): ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter new salary (leave blank to keep unchanged): ");
            String salaryInput = scanner.nextLine().trim();
            System.out.print("Enter new department (leave blank to keep unchanged): ");
            String dept = scanner.nextLine().trim();

            // Build dynamic update statement
            StringBuilder sb = new StringBuilder("UPDATE employees SET ");
            boolean first = true;
            if (!name.isEmpty()) {
                sb.append("name = ?");
                first = false;
            }
            Double salary = null;
            if (!salaryInput.isEmpty()) {
                try {
                    salary = Double.parseDouble(salaryInput);
                    if (!first) sb.append(", ");
                    sb.append("salary = ?");
                    first = false;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid salary input. Update aborted.");
                    return;
                }
            }
            if (!dept.isEmpty()) {
                if (!first) sb.append(", ");
                sb.append("department = ?");
                first = false;
            }
            if (first) {
                System.out.println("No changes provided. Update aborted.");
                return;
            }
            sb.append(" WHERE id = ?");

            try (PreparedStatement updatePs = conn.prepareStatement(sb.toString())) {
                int idx = 1;
                if (!name.isEmpty()) {
                    updatePs.setString(idx++, name);
                }
                if (salary != null) {
                    updatePs.setDouble(idx++, salary);
                }
                if (!dept.isEmpty()) {
                    updatePs.setString(idx++, dept);
                }
                updatePs.setInt(idx, id);

                int updated = updatePs.executeUpdate();
                if (updated > 0) System.out.println("Employee updated successfully.");
                else System.out.println("Update failed.");
            }

        } catch (SQLException e) {
            System.out.println("Error during update: " + e.getMessage());
        }
    }

    private static void deleteEmployee(Scanner scanner) {
        int id;
        while (true) {
            System.out.print("Enter employee id to delete: ");
            String s = scanner.nextLine().trim();
            try {
                id = Integer.parseInt(s);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid id. Try again.");
            }
        }

        String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("Employee deleted.");
            else System.out.println("No employee found with id " + id);
        } catch (SQLException e) {
            System.out.println("Error while deleting: " + e.getMessage());
        }
    }
}
