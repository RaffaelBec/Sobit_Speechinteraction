package org.example;

import java.util.List;
import java.util.StringJoiner;

public class SQLQueryBuilder {

    public static String generateSQL(NERPreprocessor.ProcessedNER nerData, int empId) {
        String sql = "";
        String firstName = nerData.firstName;
        String lastName = nerData.lastName;
        String personType= nerData.personType;

        String relationType=nerData.relationType;
        String person = nerData.person;
        String action = nerData.action.toLowerCase();
        String time = nerData.time;
        String task = nerData.task;
        String entity = nerData.entity;
        String distance = nerData.distance;

        // Handle special cases for employee and patient
        if ("CURRENT_EMPLOYEE".equalsIgnoreCase(person)) {
            person = SessionManager.getCurrentEmployeeId();
        } else if ("CURRENT_PATIENT".equalsIgnoreCase(person)) {
            person = SessionManager.getCurrentPatientId();
        }

        System.out.println("\n=== DEBUG: NER Data Before Query Generation ===");
        System.out.println("Action  : " + action);
        System.out.println("Person  : " + person);
        System.out.println("First Name: " + firstName);
        System.out.println("Last Name : " + lastName);
        System.out.println("Time    : " + time);
        System.out.println("Task    : " + task);
        System.out.println("Entity  : " + entity);
        System.out.println("Distance: " + distance);
        System.out.println("====================================");

        // Build WHERE clause for names
        StringBuilder whereClause = new StringBuilder();

        if (firstName != null && lastName != null) {
            whereClause.append("FirstName = '").append(firstName).append("' AND LastName = '").append(lastName).append("' OR ").append("FirstName = '").append(lastName).append("' AND LastName = '").append(firstName).append("'");
        } else if (firstName != null) {
            whereClause.append("FirstName = '").append(firstName).append("'").append(" OR LastName = '").append(firstName).append("'");
        } else if (lastName != null) {
            whereClause.append("LastName = '").append(lastName).append("'").append(" OR FirstName = '").append(lastName).append("'");
        } else {
            whereClause.append("EmployeeId='").append(empId).append("'");  // Default fallback if no name is detected
        }

        // Generate SQL query based on detected action
        switch (action) {
            case "select":
                // Ensure the time is correctly resolved
                String resolvedTime = nerData.time;

                if ("next".equals(resolvedTime)) {
                    // Get the next upcoming assignment
                    sql = "SELECT c.*,a.* FROM Client c " +
                            "JOIN Assignment a ON c.ClientId = a.ClientId " +
                            "WHERE a.EmployeeId = (SELECT EmployeeId FROM Employee WHERE " + whereClause + ") " +
                            "AND a.TargetTimeStart > datetime('now', 'localtime') " +
                            "ORDER BY a.TargetTimeStart ASC " +
                            "LIMIT 1";
                }
                else if ("heute".equals(resolvedTime)) {
                    // Get all assignments for today
                    sql = "SELECT c.*,a.*  FROM Client c " +
                            "JOIN Assignment a ON c.ClientId = a.ClientId " +
                            "WHERE a.EmployeeId = (SELECT EmployeeId FROM Employee WHERE " + whereClause + ") " +
                            "AND date(a.TargetTimeStart) = date('now', 'localtime')";  // ✅ Only this condition is needed

                    // If we also have "CURRENT_PATIENT", filter by current patient
                    if ("CURRENT_PATIENT".equals(nerData.personType)) {
                        sql += " AND c.ClientId IN (SELECT ClientId FROM Assignment " +
                                "WHERE date(TargetTimeStart) = date('now', 'localtime') " +
                                "AND EmployeeId = (SELECT EmployeeId FROM Employee WHERE " + whereClause + "))";
                    }
                }
                else if ("gestern".equals(resolvedTime)) {
                    // Get all assignments for yesterday
                    sql = "SELECT c.*,a.*  FROM Client c " +
                            "JOIN Assignment a ON c.ClientId = a.ClientId " +
                            "WHERE a.EmployeeId = (SELECT EmployeeId FROM Employee WHERE " + whereClause + ") " +
                            "AND date(a.TargetTimeStart) = date('now', '-1 day', 'localtime')";
                }
                else if ("morgen".equals(resolvedTime)) {
                    // Get all assignments for tomorrow
                    sql = "SELECT c.*,a.*  FROM Client c " +
                            "JOIN Assignment a ON c.ClientId = a.ClientId " +
                            "WHERE a.EmployeeId = (SELECT EmployeeId FROM Employee WHERE " + whereClause + ") " +
                            "AND date(a.TargetTimeStart) = date('now', '+1 day', 'localtime')";
                }
                else if ("CURRENT_CLIENT".equals(nerData.personType)) {
                    // Get the currently assigned patient at this moment
                    sql = "SELECT c.*,a.* FROM Client c " +
                            "JOIN Assignment a ON c.ClientId = a.ClientId " +
                            "WHERE a.EmployeeId = (SELECT EmployeeId FROM Employee WHERE " + whereClause + ") " +
                            "AND a.TargetTimeStart <= datetime('now', 'localtime') " +
                            "AND a.TargetTimeEnd >= datetime('now', 'localtime')";
                }
                else if ("now".equals(resolvedTime)) {
                    // Get all assignments for tomorrow
                    sql = "SELECT c.*,a.*  FROM Client c " +
                            "JOIN Assignment a ON c.ClientId = a.ClientId " +
                            "WHERE a.EmployeeId = (SELECT EmployeeId FROM Employee WHERE " + whereClause + ") " +
                            "AND a.TargetTimeStart <= datetime('now', 'localtime') " +
                            "AND a.TargetTimeEnd >= datetime('now', 'localtime')";
                }
                else {
                    sql = "SELECT * FROM Assignment " +
                            "WHERE EmployeeId = (SELECT EmployeeId FROM Employee WHERE " + whereClause + ")";
                }
                break;




            case "start":
                sql = "UPDATE Assignment SET ActualTimeStart = datetime('now','localtime') " +
                        "WHERE AssignmentId = (SELECT AssignmentId FROM Assignment WHERE " +
                        "EmployeeId = (SELECT EmployeeId FROM Employee WHERE " + whereClause + ") " +
                        "AND TargetTimeStart <= datetime('now') " +
                        "AND TargetTimeEnd >= datetime('now')\n" +
                        "ORDER BY TargetTimeStart ASC " +
                        "LIMIT 1" +
                        ");";
                break;
            case "end":
                sql = "UPDATE Assignment SET ActualTimeEnd = datetime('now','localtime') " +
                        "WHERE AssignmentId = (SELECT AssignmentId FROM Assignment WHERE " +
                        "EmployeeId = (SELECT EmployeeId FROM Employee WHERE " + whereClause + ") " +
                        "AND ActualTimeStart IS NOT NULL AND ActualTimeEnd IS NULL " +  // Ensures assignment has started but not ended
                        "ORDER BY TargetTimeStart DESC " +  // Prioritizes the most recent active assignment
                        "LIMIT 1);";
                break;


            case "call":
                if (nerData.person != null) {
                    System.out.println("DEBUG: relationType -> " + nerData.relationType);
                    if (nerData.relationType != null) {
                        System.out.println("DEBUG: relationType content -> '" + nerData.relationType + "'");
                    } else {
                        System.out.println("ERROR: relationType is NULL!");
                    }

                    // Check if personType is not null before trying to use startsWith
                    if (nerData.relationType != null && nerData.relationType.startsWith("relative") && !nerData.person.equals("CURRENT_CLIENT")) {
                        // Extract the relationship type (if it's specified in the personType)
                        String relationshipType = nerData.relationType.split("\\|")[1];  // Get the part after "relative|", e.g., "mother", "sibling"

                        // Build the SQL query with dynamic relationship type handling
                        sql = "SELECT p.PhoneNr,p.firstName,p.LastName, rt.Value AS RelationshipType " +
                                "FROM Person p " +
                                "JOIN PersonRelationship pr ON p.PersonId = pr.PersonId " +
                                "JOIN RelationshipType rt ON pr.RelationshipTypeId = rt.RelationshipTypeId " +
                                "WHERE pr.ClientId IN (SELECT ClientId FROM Client WHERE " + whereClause + ") " +
                                "AND (pr.DateEnd IS NULL OR pr.DateEnd >= DATE('now')) " +  // Ensuring active relationships
                                "AND pr.DateStart <= DATE('now') " +
                                "AND rt.ShortValue = '" + relationshipType.toUpperCase() + "' " +  // Directly insert the relationshipType into the query
                                "LIMIT 1";
                    }
                    else if(nerData.person.equals("CURRENT_CLIENT")){
                        String relationshipType = nerData.relationType.split("\\|")[1];  // Get the part after "relative|", e.g., "mother", "sibling"
                        sql = "SELECT p.PhoneNr,p.firstName,p.Lastname,rt.Value AS RelationshipType " +
                                "FROM Person p " +
                                "JOIN PersonRelationship pr ON p.PersonId = pr.PersonId " +
                                "JOIN RelationshipType rt ON pr.RelationshipTypeId = rt.RelationshipTypeId " +
                                "WHERE pr.ClientId = " + nerData.clientId + " " +
                                "AND (pr.DateEnd IS NULL OR pr.DateEnd >= DATE('now')) " +  // Ensuring active relationships
                                "AND pr.DateStart <= DATE('now') " +
                                "AND rt.ShortValue = '" + relationshipType.toUpperCase() + "' " +  // Directly insert the relationshipType into the query
                                "LIMIT 1";
                    }else {
                        // For non-relative person types, just fetch the phone number
                        sql = "SELECT PhoneNr " +
                                "FROM Person " +
                                "WHERE " + whereClause + ";";
                    }
                }
                break;




            case "insert":
                sql = "INSERT INTO Assignment (EmployeeId, ClientId, Task, TargetTimeStart) " +
                        "VALUES ((SELECT EmployeeId FROM Employee WHERE " + whereClause + "), " +
                        "(SELECT ClientId FROM Client WHERE " + whereClause + "), '" + task + "', datetime('now'));";
                break;

            case "update":
                sql = "UPDATE Assignment SET Task = '" + task + "', Note = '" + entity + "' " +
                        "WHERE EmployeeId = (SELECT EmployeeId FROM Employee WHERE " + whereClause + ");";
                break;

            case "distance":
                sql = "SELECT * FROM DriversLog WHERE EmployeeId = " +
                        "(SELECT EmployeeId FROM Employee WHERE " + whereClause + ") " +
                        "AND (MileageEnd - MileageStart) <= " + distance + ";";
                break;

            case "schedule":
                sql = "SELECT * FROM Assignment WHERE EmployeeId = " +
                        "(SELECT EmployeeId FROM Employee WHERE " + whereClause + ") " +
                        "AND TargetTimeStart = '" + time + "';";
                break;

            case "assigned":
                sql = "SELECT * FROM Assignment WHERE EmployeeId IN " +
                        "(SELECT EmployeeId FROM Employee WHERE " + whereClause + ") " +
                        "AND TargetTimeStart > datetime('now');";
                break;

            case "verify":
                sql = "SELECT EXISTS (SELECT 1 FROM Assignment WHERE EmployeeId = " +
                        "(SELECT EmployeeId FROM Employee WHERE " + whereClause + ") " +
                        "AND Task = '" + task + "');";
                break;

            case "log":
                sql = "INSERT INTO DriversLog (EmployeeId, AssignmentId, Note, DepartureTime) " +
                        "VALUES ((SELECT EmployeeId FROM Employee WHERE " + whereClause + "), " +
                        "(SELECT AssignmentId FROM Assignment WHERE EmployeeId = (SELECT EmployeeId FROM Employee WHERE " + whereClause + ")), " +
                        "'" + task + "', datetime('now'));";
                break;

            case "getlog":
                sql = "SELECT * FROM DriversLog WHERE EmployeeId = " +
                        "(SELECT EmployeeId FROM Employee WHERE " + whereClause + ") " +
                        "ORDER BY DepartureTime DESC;";
                break;

            case "allassignments":
                sql = "SELECT * FROM Assignment WHERE EmployeeId = " +
                        "(SELECT EmployeeId FROM Employee WHERE " + whereClause + ");";
                break;

            case "status":
                sql = "SELECT * FROM Assignment WHERE EmployeeId = " +
                        "(SELECT EmployeeId FROM Employee WHERE " + whereClause + ") " +
                        "AND ActualTimeEnd IS NULL;";
                break;

            default:
                sql = "SELECT * FROM Assignment WHERE EmployeeId = " +
                        "(SELECT EmployeeId FROM Employee WHERE " + whereClause + ") " +
                        "AND TargetTimeStart > datetime('now');";
        }

        System.out.println("\n=== DEBUG: Generated SQL Query ===\n" + sql);
        return sql;
    }
}