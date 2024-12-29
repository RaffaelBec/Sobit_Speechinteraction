package classes;

import java.util.HashMap;
import java.util.Map;

public class QueryInfo {


    private String action;
    private String person;
    private String attribute;
    private String time;

    public QueryInfo() {
    }

    // Getters and Setters
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "QueryInfo{" +
                "action='" + action + '\'' +
                ", person='" + person + '\'' +
                ", attribute=" + attribute +
                ", time='" + time + '\'' +
                '}';
    }
}
