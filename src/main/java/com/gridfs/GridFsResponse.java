package com.gridfs;

import java.io.Serializable;

public class GridFsResponse implements Serializable {
    private String imageName;

    public GridFsResponse(String imageName) {
        this.imageName = imageName;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

}