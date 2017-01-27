package com.bitso;

import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class BitsoTestAPI {
    public static void main(String[] args){
        String serverRequest = System.getenv("SERVER_REQUEST");
        JUnitCore core = new JUnitCore();
        boolean mocksEnabled = Boolean.TRUE;
        Result result =  null;
        if(serverRequest != null){
            System.out.println("No mocks enabled, real petitions done to server");
            mocksEnabled = Boolean.FALSE;
            result = core.run(BitsoServerTest.class);
        }else{
            System.out.println("Mocks enabled for tests");
            result = core.run(BitsoMockTest.class);
        }
        getResultInfo(result, mocksEnabled);
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