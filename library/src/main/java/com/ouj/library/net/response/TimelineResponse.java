package com.ouj.library.net.response;

import java.io.Serializable;
import java.util.List;

/**
 * Created by liqi on 2016-2-22.
 */
public abstract class TimelineResponse extends PageResponse  {

    public int next;
    public String timeline;

    @Override
    public boolean hasMore() {
        return next == 1;
    }
}
