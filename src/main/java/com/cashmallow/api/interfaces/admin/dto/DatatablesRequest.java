package com.cashmallow.api.interfaces.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DatatablesRequest {
    private int draw;
    private int start;
    private int length;
    private Search search;
    private List<Column> columns;
    private List<Order> order;


    // 내부 클래스 정의
    @Getter
    @Setter
    public static class Search {
        private String value;
        private boolean regex;
    }

    @Getter
    @Setter
    public static class Column {
        private String data;
        private String name;
        private boolean searchable;
        private boolean orderable;
        private Search search;
    }

    @Getter
    @Setter
    public static class Order {
        private int column;
        private String dir;
    }
}
