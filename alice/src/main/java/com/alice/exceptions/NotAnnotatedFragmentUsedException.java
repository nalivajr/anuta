package com.alice.exceptions;

import com.alice.annonatations.AutoFragment;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class NotAnnotatedFragmentUsedException extends RuntimeException {

    public NotAnnotatedFragmentUsedException() {
        super("Target fragment should be annotated with %s and specify layout resource id" + AutoFragment.class );
    }
}
