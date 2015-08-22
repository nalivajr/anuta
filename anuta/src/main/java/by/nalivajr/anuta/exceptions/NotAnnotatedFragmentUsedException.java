package by.nalivajr.anuta.exceptions;

import by.nalivajr.anuta.annonatations.ui.AutoFragment;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class NotAnnotatedFragmentUsedException extends RuntimeException {

    public NotAnnotatedFragmentUsedException() {
        super("Target fragment should be annotated with %s and specify layout resource id " + AutoFragment.class );
    }
}
