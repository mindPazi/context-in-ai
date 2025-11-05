package com.github.mindpazi.contextinaitool.model;

public class ExtractionStats {
    private int methodCount = 0;
    private int anonymousClassCount = 0;
    private int lambdaCount = 0;

    public void incrementMethods() {
        methodCount++;
    }

    public void incrementAnonymousClasses() {
        anonymousClassCount++;
    }

    public void incrementLambdas() {
        lambdaCount++;
    }

    public int getMethodCount() {
        return methodCount;
    }

    public int getAnonymousClassCount() {
        return anonymousClassCount;
    }

    public int getLambdaCount() {
        return lambdaCount;
    }

    public int getTotalCount() {
        return methodCount + anonymousClassCount + lambdaCount;
    }
}
