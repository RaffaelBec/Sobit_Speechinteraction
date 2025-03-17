package intentinNERapproach;

import java.util.Date;

class Assignment {
    public int assignmentId;
    public int assignmentTypeId;
    public int employeeId;
    public int clientId;
    public Date date;
    public Date targetTimeStart;
    public Date targetTimeEnd;
    public Date actualTimeStart;
    public Date actualTimeEnd;
    public String cancelationInfo;
    public String note;

    public Assignment(int assignmentId, int assignmentTypeId, int employeeId, int clientId, Date date,
                      Date targetTimeStart, Date targetTimeEnd, Date actualTimeStart, Date actualTimeEnd,
                      String cancelationInfo, String note) {
        this.assignmentId = assignmentId;
        this.assignmentTypeId = assignmentTypeId;
        this.employeeId = employeeId;
        this.clientId = clientId;
        this.date = date;
        this.targetTimeStart = targetTimeStart;
        this.targetTimeEnd = targetTimeEnd;
        this.actualTimeStart = actualTimeStart;
        this.actualTimeEnd = actualTimeEnd;
        this.cancelationInfo = cancelationInfo;
        this.note = note;
    }
}
