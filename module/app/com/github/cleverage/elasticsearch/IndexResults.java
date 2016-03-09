package com.github.cleverage.elasticsearch;

import org.elasticsearch.search.aggregations.Aggregations;

import java.util.List;

/**
 * The Class IndexResult.
 *
 * @param <T> extends Index
 */
public class IndexResults<T extends Index> {

    /** The total count. */
    public long totalCount;

    /**
     * Nb result by page
     */
    public long pageSize;

    /**
     * Number of current page
     */
    public long pageCurrent;

    /**
     * Number of total pages
     */
    public long pageNb;

    /** The results. */
    public List<T> results;

    /** The aggregations. */
    public Aggregations aggregations;

    /**
     * Create new Index Result
     * @param totalCount
     * @param pageSize
     * @param pageCurrent
     * @param pageNb
     * @param results
     * @param aggregations
     */
    public IndexResults(long totalCount,long pageSize, long pageCurrent, long pageNb, List<T> results, Aggregations aggregations) {
        this.totalCount = totalCount;
        this.pageSize = pageSize;
        this.pageCurrent = pageCurrent;
        this.pageNb = pageNb;
        this.results = results;
        this.aggregations = aggregations;
    }

}

