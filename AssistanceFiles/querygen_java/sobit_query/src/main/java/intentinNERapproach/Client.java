package intentinNERapproach;

import java.util.Date;

class Client {
    public int clientId;
    public String firstName;
    public String lastName;
    public String title;
    public Date dateOfBirth;
    public String ssn;
    public String sex;
    public String citizenship;
    public String profession;
    public String birthName;
    public String birthPlace;
    public String birthCountry;

    public Client(int clientId, String firstName, String lastName, String title, Date dateOfBirth, String ssn, String sex,
                  String citizenship, String profession, String birthName, String birthPlace, String birthCountry) {
        this.clientId = clientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.dateOfBirth = dateOfBirth;
        this.ssn = ssn;
        this.sex = sex;
        this.citizenship = citizenship;
        this.profession = profession;
        this.birthName = birthName;
        this.birthPlace = birthPlace;
        this.birthCountry = birthCountry;
    }
}
