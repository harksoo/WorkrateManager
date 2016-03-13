package com.sovate.workratemanager.bean;

/**
 * Created by harks on 2016-03-05.
 */
public class ActivityDeviceStudentInfo extends ActivityDevice {


    String userId = "";
    String userName = "";

    String schoolYear = ""; //학기년도
    String schoolId = "";
    String schoolGradeId = "";

    String classNumber = "";

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSchoolYear() {
        return schoolYear;
    }

    public void setSchoolYear(String schoolYear) {
        this.schoolYear = schoolYear;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getSchoolGradeId() {
        return schoolGradeId;
    }

    public void setSchoolGradeId(String schoolGradeId) {
        this.schoolGradeId = schoolGradeId;
    }

    public String getClassNumber() {
        return classNumber;
    }

    public void setClassNumber(String classNumber) {
        this.classNumber = classNumber;
    }

    @Override
    public String toString() {
        return "ActivityDeviceStudentInfo [userId=" + userId + ", userName=" + userName + ", schoolYear=" + schoolYear
                + ", schoolId=" + schoolId + ", schoolGradeId=" + schoolGradeId + ", classNumber=" + classNumber
                + ", name=" + name + ", mac=" + mac + ", alias=" + alias + ", description=" + description + "]";
    }

}