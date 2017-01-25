package com.bitso;

import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class BitsoTestAPI {
    public static void main(String[] args){
        boolean mockEnabled = System.getenv("mock_enabled").equals("enabled") ? true:false;
        JUnitCore core = new JUnitCore();
        Result result =  null;
        if(mockEnabled){
            result = core.run(BitsoMockTest.class);
        }else{
            result = core.run(BitsoServerTest.class);
        }
        getResultInfo(result, mockEnabled);
    }

    private static void getResultInfo(Result result, boolean mockEnabled){
        if(result == null){
            System.out.println("Result is null");
        }

        int totalTests = result.getRunCount();
        int totalFail = result.getFailureCount();
        int totalSucced = (totalTests - totalFail);
        int totalIgnored = result.getIgnoreCount();

        String testType = mockEnabled ? " with mocks" : " calling server";

        System.out.println("========= Bitso Test API result" + testType + " =========");

        System.out.println("Run Test: " + totalTests);
        System.out.println("Suceed Tests: " + totalSucced);
        System.out.println("Failure Tests:" + totalFail);
        System.out.println("Ignored Tests: " + totalIgnored);

        if(totalFail > 0){
            List<Failure> failures = result.getFailures();
            if(totalFail != failures.size()){
                return;
            }
            for (Failure failure : failures) {
                System.out.println("===========================================");
                System.out.println(failure.getMessage());
                System.out.println(failure.getTrace());
            }
        }
    }
}