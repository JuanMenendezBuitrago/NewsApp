package com.example.android.newsapp;

import java.util.ArrayList;

/**
 * AsyncTaskResult holds both the result of the task as well as the exceptions so the main thread
 * can deal with them if necessary.
 */

public class AsyncTaskResult<T> {
    /** the actual result **/
    private final ArrayList<T> result;

    /** the exception **/
    private final Exception exception;

    /**
     * Construct
     * @param result result obtained during the task
     * @param exception exception thrown during the task
     */
    public AsyncTaskResult(ArrayList<T> result, Exception exception) {
        this.result = result;
        this.exception = exception;
    }

    ///////////////
    /// getters ///
    ///////////////

    /**
     * @return the list of elements
     */
    public ArrayList<T> getResult() {
        return result;
    }

    /**
     * @return the exception thrown
     */
    public Exception getException() {
        return exception;
    }
}
