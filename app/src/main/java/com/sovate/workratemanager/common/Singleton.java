package com.sovate.workratemanager.common;

import com.sovate.workratemanager.bean.ActivityBaseInfo;
import com.sovate.workratemanager.bean.ActivityGrade;
import com.sovate.workratemanager.bean.ActivitySchool;
import com.sovate.workratemanager.bean.ActivitySport;

import java.util.ArrayList;

/**
 * Created by harksoo on 2016-03-11.
 */
public class Singleton {
    private static Singleton uniqueInstance = new Singleton();

    private Singleton() {
    }

    public static Singleton getInstance() {
        return uniqueInstance;
    }


    ActivityBaseInfo activityBaseInfo;

    public ActivityBaseInfo getActivityBaseInfo() {
        return activityBaseInfo;
    }

    public void setActivityBaseInfo(ActivityBaseInfo activityBaseInfo) {
        this.activityBaseInfo = activityBaseInfo;
    }

    public String[] getSchoolNames(){

        if(activityBaseInfo != null
                && activityBaseInfo.getMapSchool() != null
                && activityBaseInfo.getMapSchool().size() > 0){

            String[] schoolNames = new String[activityBaseInfo.getMapSchool().size()];
            int i = 0;
            for(String key : activityBaseInfo.getMapSchool().keySet()){
                schoolNames[i++] = key;
            }

            return  schoolNames;
        }

        return  null;
    }

    public String[] getGradeNames(String schoolName){

        if(activityBaseInfo != null
                && activityBaseInfo.getMapSchool() != null
                && activityBaseInfo.getMapSchool().size() > 0){

            ActivitySchool activitySchool = activityBaseInfo.getMapSchool().get(schoolName);

            if(activitySchool != null) {
                ArrayList<ActivityGrade> list = activitySchool.getListGrade();
                if(list != null && list.size() > 0) {
                    String[] gradedNames = new String[list.size()];

                    int i = 0;
                    for(ActivityGrade item : list){
                        gradedNames[i++] = item.getName();
                    }

                    return gradedNames;
                }
            }
        }

        return  null;
    }

    public String[] getClassNames(String schoolName, String gradeName){

        if(activityBaseInfo != null
                && activityBaseInfo.getMapSchool() != null
                && activityBaseInfo.getMapSchool().size() > 0){

            ActivitySchool activitySchool = activityBaseInfo.getMapSchool().get(schoolName);

            if(activitySchool != null) {
                ArrayList<ActivityGrade> list = activitySchool.getListGrade();
                if(list != null && list.size() > 0) {
                    for(ActivityGrade item : list){
                        if(gradeName.equals(item.getName())) {
                            int count = Integer.parseInt(item.getClassCount());
                            String[] classNames = new String[count];

                            for(int i = 0; i < count; i++){
                                classNames[i] = Integer.toString(i + 1);
                            }

                            return classNames;
                        }
                    }
                }
            }
        }

        return  null;
    }

    // String [0] : School ID, [1] : Grade ID
    public String[] getSchoolGradeId(String schoolName, String gradeName){

        if(activityBaseInfo != null
                && activityBaseInfo.getMapSchool() != null
                && activityBaseInfo.getMapSchool().size() > 0){

            ActivitySchool activitySchool = activityBaseInfo.getMapSchool().get(schoolName);

            if(activitySchool != null) {
                ArrayList<ActivityGrade> list = activitySchool.getListGrade();
                if(list != null && list.size() > 0) {
                    for(ActivityGrade item : list){
                        if(gradeName.equals(item.getName())) {

                            String[] result = new String[2];

                            result[0] = activitySchool.getId();
                            result[1] = item.getId();

                            return result;
                        }
                    }
                }
            }
        }

        return  null;
    }

    public String[] getSportNames(){

        if(activityBaseInfo != null
                && activityBaseInfo.getListSport() != null
                && activityBaseInfo.getListSport().size() > 0){

            String[] sportNames = new String[activityBaseInfo.getListSport().size()];
            int i = 0;
            for(ActivitySport item : activityBaseInfo.getListSport()){
                sportNames[i++] = item.getName();
            }

            return  sportNames;
        }

        return  null;
    }
}