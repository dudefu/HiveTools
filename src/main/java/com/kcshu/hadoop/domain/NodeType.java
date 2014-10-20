package com.kcshu.hadoop.domain;

public enum NodeType {
	ROOT, SERVER, DATABASE, TABLE, FIELD,FIELD_PARTITION,FIELD_IDX;
	
    public static final String NODE_TYPE = "nodeType";
    public static final String NODE_ITEM_OPENED = "nodeOpen";
    public static final String NODE_LOADING = "nodeLoading";
}
