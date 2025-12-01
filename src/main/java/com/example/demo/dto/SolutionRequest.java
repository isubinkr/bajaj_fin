package com.example.demo.dto;

public class SolutionRequest {

    private String finalQuery;

    public SolutionRequest() {}

    public SolutionRequest(String finalQuery) {
        this.finalQuery = finalQuery;
    }

    public String getFinalQuery() {
        return finalQuery;
    }

    public void setFinalQuery(String finalQuery) {
        this.finalQuery = finalQuery;
    }
}
