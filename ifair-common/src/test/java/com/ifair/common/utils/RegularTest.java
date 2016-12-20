package com.ifair.common.utils;

import junit.framework.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

/**
 * Created by feiying on 16/10/20.
 */
public class RegularTest {

    @Test
    public void testMatch(){
        String regular = "/ywy/task/(create|\\d+/edit|\\d+/step2).*";
        Assert.assertTrue("/ywy/task/create".matches(regular));
        Assert.assertTrue("/ywy/task/30/edit".matches(regular));
        Assert.assertTrue("/ywy/task/30/step2".matches(regular));

        Assert.assertTrue("/ywy/task/create.html".matches(regular));
        Assert.assertTrue("/ywy/task/30/edit.html".matches(regular));
        Assert.assertTrue("/ywy/task/30/step2.html".matches(regular));

        Assert.assertFalse("/ywy/task/list".matches(regular));
        Assert.assertFalse("/ywy/task/29".matches(regular));
        Assert.assertFalse("/ywy/task/25/assign".matches(regular));
    }

    @Test
    public void testMatch2(){
        String regular = "/ywy/task/(list.*|\\d+|\\d+/assign)";
        Assert.assertFalse("/ywy/task/create".matches(regular));
        Assert.assertFalse("/ywy/task/30/edit".matches(regular));
        Assert.assertFalse("/ywy/task/30/step2".matches(regular));

        Assert.assertFalse("/ywy/task/create.html".matches(regular));
        Assert.assertFalse("/ywy/task/30/edit.html".matches(regular));
        Assert.assertFalse("/ywy/task/30/step2.html".matches(regular));

        Assert.assertTrue("/ywy/task/list".matches(regular));
        Assert.assertTrue("/ywy/task/29".matches(regular));
        Assert.assertTrue("/ywy/task/25/assign".matches(regular));
    }

    @Test
    public void testC(){
        int n = (int) (System.currentTimeMillis()/1000);
        System.out.println(n);
    }

    @Test
    public void testD(){
        Pattern p = Pattern.compile("^[a-zA-Z]{2}\\d{8}+$");
        String a = "OC52831767";
        Assert.assertTrue(p.matcher(a).matches());
    }

}
