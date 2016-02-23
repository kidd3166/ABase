package com.ouj.library.net.response;

import java.io.Serializable;

public class Page implements Serializable {

    public int currentPage;
    public int firstResult;
    public int maxResults;
    public int totalPage;
    public int totalRecords;

    public int nextPage() {
        if (currentPage > totalPage)
            return totalPage;
        return currentPage + 1;
    }
}
