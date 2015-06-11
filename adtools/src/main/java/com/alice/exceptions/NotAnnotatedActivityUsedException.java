package com.alice.exceptions;

import com.alice.annonatations.AutoActivity;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class NotAnnotatedActivityUsedException extends RuntimeException {

    public NotAnnotatedActivityUsedException() {
        super("Target activity should be annotated with %s and specify layout resource id" + AutoActivity.class );
    }
}
