package com.targaryen.marketintel.model;

public class DiffResult {
    private String newOrModifiedText;

    public DiffResult() {}

    public DiffResult(String newOrModifiedText) {
        this.newOrModifiedText = newOrModifiedText;
    }

    public String getNewOrModifiedText() {
        return newOrModifiedText;
    }

    public void setNewOrModifiedText(String newOrModifiedText) {
        this.newOrModifiedText = newOrModifiedText;
    }
}
