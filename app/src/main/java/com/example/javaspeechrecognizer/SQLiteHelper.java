package com.example.javaspeechrecognizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "medical.db";
    private static final int DATABASE_VERSION = 1;

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL("CREATE TABLE Person (PersonId INTEGER PRIMARY KEY, FirstName TEXT, LastName TEXT, Title TEXT, DateOfBirth TEXT, SSN TEXT, Sex TEXT, Citizenship TEXT, Profession TEXT, BirthName TEXT, BirthPlace TEXT, BirthCountry TEXT, PhoneNr TEXT)");
        db.execSQL("CREATE TABLE Client (ClientId INTEGER PRIMARY KEY, FirstName TEXT, LastName TEXT, Title TEXT, DateOfBirth TEXT, SSN TEXT, Sex TEXT, Citizenship TEXT, Profession TEXT, BirthName TEXT, BirthPlace TEXT, BirthCountry TEXT)");
        db.execSQL("CREATE TABLE Employee (\n" +
                "    EmployeeId INTEGER PRIMARY KEY,\n" +
                "    FirstName TEXT NOT NULL,\n" +
                "    LastName TEXT NOT NULL,\n" +
                "    Title TEXT,\n" +
                "    DateOfBirth DATE,\n" +
                "    SSN TEXT,\n" +
                "    Sex TEXT,\n" +
                "    Citizenship TEXT,\n" +
                "    Profession TEXT,\n" +
                "    BirthName TEXT,\n" +
                "    BirthPlace TEXT,\n" +
                "    BirthCountry TEXT\n" +
                ");");
        db.execSQL("CREATE TABLE Assignment (\n" +
                "    AssignmentId INTEGER PRIMARY KEY,\n" +
                "    AssignmentTypeId INTEGER NOT NULL,\n" +
                "    EmployeeId INTEGER NOT NULL,\n" +
                "    ClientId INTEGER NOT NULL,\n" +
                "    Date DATE,\n" +
                "    TargetTimeStart DATETIME,\n" +
                "    TargetTimeEnd DATETIME,\n" +
                "    ActualTimeStart DATETIME,\n" +
                "    ActualTimeEnd DATETIME,\n" +
                "    CancelationInfo TEXT,\n" +
                "    Note TEXT,\n" +
                "    FOREIGN KEY (EmployeeId) REFERENCES Employee(EmployeeId),\n" +
                "    FOREIGN KEY (ClientId) REFERENCES Client(ClientId)\n" +
                ");\n");
        db.execSQL("CREATE TABLE Car (\n" +
                "    CarId INTEGER PRIMARY KEY,\n" +
                "    LicensePlate TEXT NOT NULL,\n" +
                "    LastCheck DATE,\n" +
                "    DateStart DATE,\n" +
                "    DateEnd DATE\n" +
                ");\n");
        db.execSQL("CREATE TABLE DriversLog (\n" +
                "    DriversLogId INTEGER PRIMARY KEY,\n" +
                "    CarId INTEGER NOT NULL,\n" +
                "    AssignmentId INTEGER NOT NULL,\n" +
                "    EmployeeId INTEGER NOT NULL,\n" +
                "    AddressFrom TEXT,\n" +
                "    AddressTo TEXT,\n" +
                "    DepartureTime DATETIME,\n" +
                "    ArrivalTime DATETIME,\n" +
                "    MileageStart INTEGER,\n" +
                "    MileageEnd INTEGER,\n" +
                "    AddedFuel REAL,\n" +
                "    Note TEXT,\n" +
                "    IsPrivate BOOLEAN,\n" +
                "    FOREIGN KEY (CarId) REFERENCES Car(CarId),\n" +
                "    FOREIGN KEY (AssignmentId) REFERENCES Assignment(AssignmentId),\n" +
                "    FOREIGN KEY (EmployeeId) REFERENCES Employee(EmployeeId)\n" +
                ");\n");
        db.execSQL("CREATE TABLE RelationshipType (\n" +
                "    RelationshipTypeId INTEGER PRIMARY KEY,\n" +
                "    Value TEXT NOT NULL,\n" +
                "    ShortValue TEXT\n" +
                ");\n");
        db.execSQL("CREATE TABLE PersonRelationship (\n" +
                "    RelationshipId INTEGER PRIMARY KEY,\n" +
                "    RelationshipTypeId INTEGER NOT NULL,\n" +
                "    ClientId INTEGER,\n" +
                "    PersonId INTEGER NOT NULL,\n" +
                "    DateStart DATE,\n" +
                "    DateEnd DATE,\n" +
                "    IsEmergencyContact BOOLEAN,\n" +
                "    Comment TEXT,\n" +
                "    FOREIGN KEY (RelationshipTypeId) REFERENCES RelationshipType(RelationshipTypeId),\n" +
                "    FOREIGN KEY (ClientId) REFERENCES Client(ClientId),\n" +
                "    FOREIGN KEY (PersonId) REFERENCES Person(PersonId)\n" +
                ");\n");
        // Add more CREATE TABLE statements for other tables
        ContentValues values = new ContentValues();
        values.put("FirstName", "John");
        values.put("LastName", "Doe");
        values.put("DateOfBirth", "1985-06-15");
        db.execSQL("INSERT INTO Person (PersonId, FirstName, LastName, Title, DateOfBirth, SSN, Sex, Citizenship, Profession, BirthName, BirthPlace, BirthCountry, PhoneNr) VALUES\n" +
                "(1, 'Alice', 'Johnson', 'Ms.', '1985-03-12', '123-45-6789', 'F', 'USA', 'Engineer', 'Alice', 'New York', 'USA', '555-1234'),\n" +
                "(2, 'Bob', 'Smith', 'Mr.', '1979-07-19', '987-65-4321', 'M', 'USA', 'Consultant', 'Bob', 'Los Angeles', 'USA', '555-5678');\n");
        db.execSQL("INSERT INTO Client (ClientId, FirstName, LastName, Title, DateOfBirth, SSN, Sex, Citizenship, Profession, BirthName, BirthPlace, BirthCountry) VALUES\n" +
                "(1, 'John', 'Doe', 'Mr.', '1990-01-15', '555-66-7777', 'M', 'USA', 'Businessman', 'John', 'Chicago', 'USA'),\n" +
                "(2, 'Jane', 'Roe', 'Mrs.', '1988-05-25', '444-22-3333', 'F', 'Canada', 'Teacher', 'Jane', 'Toronto', 'Canada');\n");
        db.execSQL("INSERT INTO Employee (EmployeeId, FirstName, LastName, Title, DateOfBirth, SSN, Sex, Citizenship, Profession, BirthName, BirthPlace, BirthCountry) VALUES\n" +
                "(1, 'Alice', 'Johnson', 'Ms.', '1985-03-12', '123-45-6789', 'F', 'USA', 'Engineer', 'Alice', 'New York', 'USA'),\n" +
                "(2, 'Bob', 'Smith', 'Mr.', '1979-07-19', '987-65-4321', 'M', 'USA', 'Consultant', 'Bob', 'Los Angeles', 'USA');\n");
        db.execSQL("INSERT INTO RelationshipType (RelationshipTypeId, Value, ShortValue) VALUES\n" +
                "(1, 'Emergency Contact', 'EC'),\n" +
                "(2, 'Family Member', 'FM'),\n" +
                "(3, 'Friend', 'FR');\n");
        db.execSQL("INSERT INTO PersonRelationship (RelationshipId, RelationshipTypeId, ClientId, PersonId, DateStart, DateEnd, IsEmergencyContact, Comment) VALUES\n" +
                "(1, 1, 1, 1, '2023-01-01', NULL, 1, 'Primary emergency contact'),\n" +
                "(2, 2, 2, 2, '2023-02-01', NULL, 0, 'Family member contact');\n");
        db.execSQL("INSERT INTO Assignment (AssignmentId, AssignmentTypeId, EmployeeId, ClientId, Date, TargetTimeStart, TargetTimeEnd, ActualTimeStart, ActualTimeEnd, CancelationInfo, Note) VALUES\n" +
                "(1, 1, 1, 1, '2023-11-01', '2023-11-01 09:00', '2023-11-01 17:00', '2023-11-01 09:15', '2023-11-01 17:10', NULL, 'Consulting session'),\n" +
                "(2, 1, 2, 2, '2023-11-02', '2023-11-02 10:00', '2023-11-02 16:00', '2023-11-02 10:05', '2023-11-02 15:50', NULL, 'Project review');\n");
        db.execSQL("INSERT INTO Car (CarId, LicensePlate, LastCheck, DateStart, DateEnd) VALUES\n" +
                "(1, 'ABC123', '2023-10-15', '2023-01-01', NULL),\n" +
                "(2, 'XYZ789', '2023-09-20', '2022-12-01', NULL);\n");
        db.execSQL("INSERT INTO DriversLog (DriversLogId, CarId, AssignmentId, EmployeeId, AddressFrom, AddressTo, DepartureTime, ArrivalTime, MileageStart, MileageEnd, AddedFuel, Note, IsPrivate) VALUES\n" +
                "(1, 1, 1, 1, '100 Main St', '200 Oak Ave', '2023-11-01 08:30', '2023-11-01 09:15', 12000, 12050, 10.5, 'Morning drive', 0),\n" +
                "(2, 2, 2, 2, '500 Pine St', '800 Maple Dr', '2023-11-02 09:00', '2023-11-02 10:00', 50000, 50025, 8.0, 'Private drive', 1);\n");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrade
        db.execSQL("DROP TABLE IF EXISTS Person");
        db.execSQL("DROP TABLE IF EXISTS Client");
        db.execSQL("DROP TABLE IF EXISTS Employee");
        db.execSQL("DROP TABLE IF EXISTS RelationshipType");
        db.execSQL("DROP TABLE IF EXISTS PersonRelationship");
        db.execSQL("DROP TABLE IF EXISTS Assignment");
        db.execSQL("DROP TABLE IF EXISTS Car");
        db.execSQL("DROP TABLE IF EXISTS DriversLog");

        // Drop other tables
        onCreate(db);
    }
}

