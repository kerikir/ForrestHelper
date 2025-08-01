package com.example.myapplicationvoice;

import android.text.TextUtils;
import android.util.Log;

public class MyLogger
{
    public void verbose(String tag, String msg)
    {
        Log.v(tag, getLocation() + msg);
    }


    public void error(String tag, String msg)
    {
        Log.v(tag, getLocation() + msg);
    }


    private String getLocation()
    {
        final String className = MyLogger.class.getName();
        final StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        boolean found = false;

        for (int i = 0; i < traces.length; i++)
        {
            StackTraceElement trace = traces[i];

            try {
                if (found)
                {
                    if (!trace.getClassName().startsWith(className))
                    {
                        Class<?> tempClass = Class.forName(trace.getClassName());
                        return "[" + getClassName(tempClass) + ":" + trace.getMethodName() + ":" + trace.getLineNumber() + "]: ";
                    }
                }
                else if (trace.getClassName().startsWith(className))
                {
                    found = true;
                    continue;
                }
            }
            catch (ClassNotFoundException e)
            {
            }
        }

        return "[]: ";
    }


    private String getClassName(Class<?> tempClass)
    {
        if (tempClass != null)
        {
            if (!TextUtils.isEmpty(tempClass.getSimpleName()))
            {
                return tempClass.getSimpleName();
            }

            return getClassName(tempClass.getEnclosingClass());
        }

        return "";
    }
}
