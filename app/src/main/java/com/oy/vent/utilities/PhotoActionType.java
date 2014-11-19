package com.oy.vent.utilities;

/**
 * Created by mehmet on 11/15/2014.
 */
public enum PhotoActionType {
    CAPTURE("CAPTURE"),
    PICK("PICK");

    private String type;

    private PhotoActionType(String type){
        this.type = type;
    }

    public String getType(){
        return this.type;
    }

    public static PhotoActionType fromString(String type) {
        if (type != null) {
            for (PhotoActionType a : PhotoActionType.values()) {
                if (type.equalsIgnoreCase(a.type)) {
                    return a;
                }
            }
        }
        return null;
    }
}



