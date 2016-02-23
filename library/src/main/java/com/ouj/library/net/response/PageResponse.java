package com.ouj.library.net.response;

import java.io.Serializable;

public abstract class PageResponse implements Serializable, ResponseItems {

    public Page page = new Page();

    public int getCount() {
        return 0;
    }

    public boolean hasMore() {
        return page.totalRecords - page.firstResult > page.maxResults;
    }

}
