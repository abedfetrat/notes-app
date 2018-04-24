package com.abed.notepad;

import java.io.Serializable;

/**
 * Created by Abed on 04/16/2018.
 */

public class Tag implements Serializable {

    private String id;
    private String name;

    public Tag() {}

    public Tag(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId() {
        this.id = id;
    }

    public void setName() {
        this.name = name;
    }

}
