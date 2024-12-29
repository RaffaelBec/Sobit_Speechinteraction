package database;

import models.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeRepository {
    public Employee findEmployeeByName(String firstname, String lastname) throws SQLException {
        String query = "SELECT * FROM Employee WHERE Firstname = ? AND Lastname = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, firstname);
            stmt.setString(2, lastname);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Employee employee = new Employee();
                employee.setEmployeeId(rs.getInt("EmployeeId"));
                employee.setFirstName(rs.getString("Firstname"));
                employee.setLastName((rs.getString("Lastname")));
                return employee;
            }
            return null;
        }
    }
}
